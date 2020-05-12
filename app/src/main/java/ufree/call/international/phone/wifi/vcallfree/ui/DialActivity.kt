package ufree.call.international.phone.wifi.vcallfree.ui

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.core.content.ContextCompat
import com.newmotor.x5.db.DBHelper
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.api.Contact
import ufree.call.international.phone.wifi.vcallfree.databinding.ActivityDialBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseDataBindingActivity
import ufree.call.international.phone.wifi.vcallfree.service.CallService
import ufree.call.international.phone.wifi.vcallfree.utils.Dispatcher
import ufree.call.international.phone.wifi.vcallfree.utils.UserManager

/**
 * Created by lyf on 2020/5/6.
 */
class DialActivity :BaseDataBindingActivity<ActivityDialBinding>(){
    var contact:Contact? = null
    private lateinit var conn: ServiceConnection
    private var callBinder: CallService.CallBinder? = null
    override fun getLayoutRes(): Int = R.layout.activity_dial
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        conn = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {}
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                callBinder = service as CallService.CallBinder
            }
        }
        bindService(Intent(this, CallService::class.java), conn, Context.BIND_AUTO_CREATE)
        dataBinding.activity = this

        with(intent.getStringExtra("iso")) {
            if (this == null) {
                if (UserManager.get().country == null) {
                    UserManager.get().country = DBHelper.get().getCountry("US")
                }
            } else {
                UserManager.get().country = DBHelper.get().getCountry(this)
            }
        }

        dataBinding.country = UserManager.get().country
        intent.getParcelableExtra<Contact>("contact")?.also {
            contact = it
            dataBinding.phoneTv.text = it.phone
        }
    }

    override fun onResume() {
        super.onResume()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED){
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),1)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(::conn.isInitialized){
            unbindService(conn)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == 2){
            call(null)
        }
    }

    fun onClick(view: View){
        val number = view.tag.toString()
        println("onClick number=$number")
        dataBinding.phoneTv.text = dataBinding.phoneTv.text.toString() + number
    }

    fun goBack(v:View){
        finish()
    }

    fun del(v:View){
        val phone = dataBinding.phoneTv.text.toString()
        if(phone.isNotEmpty()) {
            dataBinding.phoneTv.text = phone.dropLast(1)
        }
    }

    fun call(v: View?){
        println("call ${dataBinding.phoneTv.text}")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this,
                    Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_DENIED ){
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO),2)
                return
            }
        }
        val phone = dataBinding.phoneTv.text.toString()
        if(phone.isNotEmpty()) {
            val dialog = CallDialog(this) {
                val flag =
                    callBinder?.makeCall("${UserManager.get().country!!.prefix}${UserManager.get().country!!.code}$phone")
                if (flag == true) {
                    Dispatcher.dispatch(this) {
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
        }
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
}