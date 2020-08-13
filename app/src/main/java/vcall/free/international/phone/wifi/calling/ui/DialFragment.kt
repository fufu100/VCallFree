package vcall.free.international.phone.wifi.calling.ui

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import android.view.View
import androidx.core.content.ContextCompat
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.newmotor.x5.db.DBHelper
import vcall.free.international.phone.wifi.calling.MainActivity
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.databinding.FragmentDialBinding
import vcall.free.international.phone.wifi.calling.lib.BaseDataBindingFragment
import vcall.free.international.phone.wifi.calling.service.CallService
import vcall.free.international.phone.wifi.calling.utils.DialEffectHelper
import vcall.free.international.phone.wifi.calling.utils.Dispatcher
import vcall.free.international.phone.wifi.calling.utils.UserManager
import vcall.free.international.phone.wifi.calling.utils.toast

/**
 * Created by lyf on 2020/5/22.
 */
class DialFragment:BaseDataBindingFragment<FragmentDialBinding>() {
    private lateinit var conn: ServiceConnection
    private var callBinder: CallService.CallBinder? = null
    private var dialEffect: DialEffectHelper? = null
    override fun getLayoutResId(): Int = R.layout.fragment_dial

    override fun initView(v: View) {
        if (UserManager.get().country == null) {
            println("getCountry US")
            UserManager.get().country = DBHelper.get().getCountry("US")
        }

        dataBinding.fragment = this

        conn = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {}
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                callBinder = service as CallService.CallBinder
            }
        }
        context?.bindService(Intent(context, CallService::class.java), conn, Context.BIND_AUTO_CREATE)
        dialEffect = DialEffectHelper(context!!)

        with(arguments) {
            var iso = this?.getString("iso","US")
            println("DialFragment iso=$iso")
            if(TextUtils.isEmpty(iso)) {
                println("getCountry $iso")
                iso = "US"
            }
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
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.unbindService(conn)
    }

    fun bgClick(v:View){}

    fun onClick(v:View){
        val number = v.tag.toString()
        println("onClick number=$number")
        dataBinding.phoneTv.insert(number)
        dialEffect?.dialNumber(number)
    }

    fun goBack(v:View){
        (activity as MainActivity).goBack()
    }

    fun del(v:View){
        val phone = dataBinding.phoneTv.text.toString()
        if(phone.isNotEmpty()) {
            dataBinding.phoneTv.delete()
        }
    }

    fun call(v:View?){
        println("call ${dataBinding.phoneTv.text}")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(context!!,
                    Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED ){
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),2)
                return
            }
        }
        try {
            val phone = dataBinding.phoneTv.text.toString()
            val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
            val phoneNumber = phoneNumberUtil.parseAndKeepRawInput(
                "+${UserManager.get().country!!.code}$phone",
                null
            )
            if (phoneNumberUtil.isValidNumber(phoneNumber)) {
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
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        println("DialFragment onRequestPermissionsResult $requestCode $permissions $grantResults")
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == 2){
            call(null)
        }
    }
}