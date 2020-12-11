package vcall.free.international.phone.wifi.calling.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.newmotor.x5.db.DBHelper
import kotlinx.coroutines.*
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.databinding.ActivitySetPhoneNumberBinding
import vcall.free.international.phone.wifi.calling.lib.BaseBackActivity
import vcall.free.international.phone.wifi.calling.utils.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by lyf on 2020/5/8.
 */
class SetPhoneNumberActivity:BaseBackActivity<ActivitySetPhoneNumberBinding>(),
    AdManager.VCallAdListener {
    private lateinit var auth:FirebaseAuth
    private var verificationInProgress = false
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    override fun getLayoutRes(): Int = R.layout.activity_set_phone_number

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        auth = Firebase.auth
        auth.setLanguageCode(Locale.getDefault().language)
        LogUtils.println("$tag initView ${Locale.getDefault().language}")
        dataBinding.activity = this
        if(UserManager.get().country == null) {
            UserManager.get().country = DBHelper.get().getCountry("US")
        }
        dataBinding.country = UserManager.get().country
        callbacks = object :PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                LogUtils.println("$tag onVerificationCompleted-")
                verificationInProgress = false
                signInWithPhoneAuthCredential(credential)

                dataBinding.phoneTv.isEnabled = false
                dataBinding.countryCode.isEnabled = false
                dataBinding.btn.isEnabled = false
                if(credential.smsCode != null){
                    dataBinding.codeEt.setText(credential.smsCode)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                verificationInProgress = false
                e.printStackTrace()
                if (e is FirebaseAuthInvalidCredentialsException) {
                    snackBar("Invalid phone number.")
                } else if (e is FirebaseTooManyRequestsException) {
                    snackBar("Quota exceeded.")
                }
                dataBinding.phoneTv.isEnabled = true
                dataBinding.countryCode.isEnabled = true
                dataBinding.btn.isEnabled = true
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(tag, "onCodeSent:$verificationId")
                storedVerificationId = verificationId
                resendToken = token

                job?.cancel()
                job = GlobalScope.launch {
                    for (i in 60 downTo 0) {
                        withContext(Dispatchers.Main) {
                            dataBinding.btn.text = String.format(Locale.getDefault(), "%ds", i)
                        }
                        delay(1000)
                    }
                    dataBinding.btn.text = "Send Code"
                }
            }
        }

        AdManager.get().interstitialAdListener.add(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, verificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        verificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }

    override fun onStart() {
        super.onStart()
        if(verificationInProgress){
            getVerCode()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AdManager.get().interstitialAdListener.remove(this)
    }

    fun selectCountry(v: View){
        Dispatcher.dispatch(this){
            navigate(CountriesActivity::class.java)
            requestCode(1)
            defaultAnimate()
        }.go()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                val str = data?.getStringExtra("iso") ?: ""
                UserManager.get().country = DBHelper.get().getCountry(str)
                dataBinding.country = UserManager.get().country
            }
        }
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential){
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    val user = it.result?.user
                    LogUtils.println("$tag FirebaseUser $user")
                    compositeDisposable.add(
                        Api.getApiService()
                            .bindPhone(UserManager.get().user!!.sip, dataBinding.phoneTv.text.toString())
                            .compose(RxUtils.applySchedulers())
                            .subscribe({it2 ->
                                if(it2.isSuccessful){
                                    UserManager.get().user?.phone = dataBinding.phoneTv.text.toString()
                                    GameResultDialog(this,{
                                        UserManager.get().user!!.points += 1000
                                        if(AdManager.get().interstitialAdMap[AdManager.ad_point]?.isLoaded == true){
                                            AdManager.get().loadInterstitialAd(AdManager.ad_point)
                                        }else{
                                            AdManager.get().loadInterstitialAd(AdManager.ad_point)
                                            finish()
                                        }
                                    }).apply {
                                        setResult("+1000")
                                    }.show()
                                }
                            }, {
                                it.printStackTrace()
                                snackBar(R.string.net_error)
                            })
                    )
                }else{
                    if (it.exception is FirebaseAuthInvalidCredentialsException) {
                        snackBar("Invalid code.")
                    }
                }
            }
    }

    var job:Job? = null
    fun getVerCode(){
        val phone = dataBinding.phoneTv.text.toString()
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(dataBinding.phoneTv.windowToken,0)
        try {
            val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
            val phoneNumber = phoneNumberUtil.parseAndKeepRawInput(
                "+${UserManager.get().country!!.code}$phone",
                null
            )
            LogUtils.println("$tag getVerCode ${phoneNumber.rawInput}")
            if(phoneNumberUtil.isValidNumber(phoneNumber)) {
                PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber.rawInput,60000,TimeUnit.MILLISECONDS,this,callbacks)
                verificationInProgress = true
            }else{
                snackBar("Invalid phone number.")
            }
        }catch (e:Exception){
            e.printStackTrace()
            snackBar("Invalid phone number.")
        }

    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    fun bindPhone(){
        var code = dataBinding.codeEt.text.toString()
        if(code.isEmpty()){
            toast(R.string.tip_code_is_empty)
        }else if(TextUtils.isEmpty(storedVerificationId)) {
            toast(R.string.tip_auth_fail)
        }else{
            val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        }
    }

    companion object{
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
    }

    override fun onAdClose() {
        AdManager.get().loadInterstitialAd(AdManager.ad_point)
        finish()
    }

    override fun onAdShow() {

    }

    override fun onAdLoaded() {

    }

//    override fun onAdLoadFail() {
//
//    }

}