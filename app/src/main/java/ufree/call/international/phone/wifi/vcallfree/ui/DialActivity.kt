package ufree.call.international.phone.wifi.vcallfree.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.newmotor.x5.db.DBHelper
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.api.Contact
import ufree.call.international.phone.wifi.vcallfree.databinding.ActivityDialBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseDataBindingActivity
import ufree.call.international.phone.wifi.vcallfree.utils.Dispatcher
import ufree.call.international.phone.wifi.vcallfree.utils.UserManager

/**
 * Created by lyf on 2020/5/6.
 */
class DialActivity :BaseDataBindingActivity<ActivityDialBinding>(){
    var contact:Contact? = null
    override fun getLayoutRes(): Int = R.layout.activity_dial
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        dataBinding.activity = this
        if(UserManager.get().country == null){
            UserManager.get().country = DBHelper.get().getCountry("US")
        }
        dataBinding.country = UserManager.get().country
        intent.getParcelableExtra<Contact>("contact")?.also {
            contact = it
            dataBinding.phoneTv.text = it.phone
        }
    }
    public fun onClick(view: View){
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

    fun call(v: View){
        println("call ${dataBinding.phoneTv.text}")
        val dialog = CallDialog(this).apply {
            phone = this@DialActivity.dataBinding.phoneTv.text.toString()
            country = UserManager.get().country
            getCallRate()
        }
        dialog.show()
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