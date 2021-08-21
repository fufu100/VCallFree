package vcall.free.international.phone.wifi.calling.ui

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.IBinder
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.newmotor.x5.db.DBHelper
import kotlinx.android.synthetic.main.side_header.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import vcall.free.international.phone.wifi.calling.MainActivity
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.databinding.FragmentDialBinding
import vcall.free.international.phone.wifi.calling.lib.BaseDataBindingFragment
import vcall.free.international.phone.wifi.calling.lib.prefs
import vcall.free.international.phone.wifi.calling.service.CallService
import vcall.free.international.phone.wifi.calling.utils.*

/**
 * Created by lyf on 2020/5/22.
 */
class DialFragment:BaseDataBindingFragment<FragmentDialBinding>(),CallService.RegStateChange {
    private lateinit var conn: ServiceConnection
    private var callBinder: CallService.CallBinder? = null
    private var dialEffect: DialEffectHelper? = null
//    var canInput = false
    override fun getLayoutResId(): Int = R.layout.fragment_dial

    override fun initView(v: View) {
        if (UserManager.get().country == null) {
            LogUtils.println("getCountry US")
            UserManager.get().country = DBHelper.get().getCountry("US")
        }

        dataBinding.fragment = this

        conn = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {}
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                callBinder = service as CallService.CallBinder
                callBinder?.setRegStateChangeListener(this@DialFragment)
                println("$tag onServiceConnected ${callBinder==null} ${callBinder?.getRegStatus()}")
                if(callBinder?.getRegStatus() != 1){
                    dataBinding.call.setImageResource(R.drawable.ic_call2)
                }
            }
        }
        context?.bindService(Intent(context, CallService::class.java), conn, Context.BIND_AUTO_CREATE)
        dialEffect = DialEffectHelper(context!!)

        with(arguments) {
//            canInput = this?.getBoolean("can_input",false)?:false
            var iso = this?.getString("iso","")
            if(TextUtils.isEmpty(iso)) {
                LogUtils.println("getCountry $iso")
                iso = prefs.getStringValue("iso","US")
            }
            LogUtils.println("DialFragment iso=$iso")
            UserManager.get().country = DBHelper.get().getCountry(iso!!)
            dataBinding.country = UserManager.get().country
            val phone = this?.getString("phone","")
            dataBinding.phoneTv.text = phone
        }
    }

    override fun onResume() {
        super.onResume()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(context!!,
                    Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED){
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),1)
            }
        }
        println("$tag onResume ${callBinder==null} ${callBinder?.getRegStatus()}")

    }

    override fun onDestroy() {
        super.onDestroy()
        callBinder?.setRegStateChangeListener(null)
        if (::conn.isInitialized) {
            context?.unbindService(conn)
        }
    }

    fun bgClick(v:View){}

    fun onClick(v:View){
//        if(canInput) {
            val number = v.tag.toString()
            LogUtils.println("onClick number=$number")
            dataBinding.phoneTv.insert(number)
            dialEffect?.dialNumber(number)
//        }
    }

    fun goBack(v:View){
        (activity as MainActivity).goBack()
    }

    fun del(v:View){
//        if(canInput) {
            val phone = dataBinding.phoneTv.text.toString()
            if (phone.isNotEmpty()) {
                dataBinding.phoneTv.delete()
            }
//        }
    }

    fun call(v:View?){
        LogUtils.println("call ${dataBinding.phoneTv.text}")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(context!!,
                    Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED ){
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),2)
                return
            }
        }
        if(callBinder?.getRegStatus() != 1){
            context?.toast("Registration failed,please try later")
            if(UserManager.get().user != null){
                callBinder?.reRegistration()
            }
            return
        }
        if(UserManager.get().user == null){
            LogUtils.println("用户注册失败")
            return
        }
        try {
            val phoneParams = arguments?.getString("phone","")
            var username = arguments?.getString("username","")?:""

            val phone = dataBinding.phoneTv.text.toString()
            val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
            val phoneNumber = phoneNumberUtil.parseAndKeepRawInput(
                "+${UserManager.get().country!!.code}$phone",
                null
            )

//            if(phone != phoneParams){
//                username = ""
//            }
            if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && ActivityCompat.checkSelfPermission(context!!,
                        Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)){
                    var where:String = "${ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER} like ?"
                    var values:Array<String> = arrayOf("%$phone%")
                    val phoneCursor = context?.contentResolver?.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        arrayOf(
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
                            ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                            ContactsContract.CommonDataKinds.Phone._ID
                        ),
                        where, values, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED"
                    )
                    while (phoneCursor?.moveToNext() == true){
                        val name = phoneCursor.getString(1)
                        if(name?.isNotEmpty() == true){
                            username = name
                            break
                        }
                    }
                }
                val dialog = CallDialog(context!!) {
                    val e164 =
                        phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
                            .replace("+", UserManager.get().country!!.prefix)
                    val flag =
                        callBinder?.makeCall(e164)
                    if (flag == true) {
                        Dispatcher.dispatch(context) {
                            navigate(CallActivity::class.java)
                            extra("phone", phone)
                            extra("username",username)
                            extra("rate", it)
                            defaultAnimate()
                        }.go()
                    }
                }.apply {
                    this.phone = phone
                    this.country = UserManager.get().country
                    getCallRate()
                }
                dialog.show()
            } else {
                context?.toast(R.string.tip_invalid_number)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    fun selectCountry(v: View){
        Dispatcher.dispatch(activity){
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
                prefs.save("iso",str)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        LogUtils.println("DialFragment onRequestPermissionsResult $requestCode $permissions $grantResults")
        if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == 2){
            call(null)
        }
    }

    override fun onRegStateChange(state: Int) {
        println("$tag onRegStateChange $state")
        GlobalScope.launch(Dispatchers.Main){
            if(state == 1) {
                dataBinding.call.setImageResource(R.drawable.ic_call)
            }else{
                dataBinding.call.setImageResource(R.drawable.ic_call2)
            }
        }

    }
}