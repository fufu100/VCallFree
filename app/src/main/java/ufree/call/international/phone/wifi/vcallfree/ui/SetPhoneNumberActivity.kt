package ufree.call.international.phone.wifi.vcallfree.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.newmotor.x5.db.DBHelper
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.databinding.ActivitySetPhoneNumberBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseBackActivity
import ufree.call.international.phone.wifi.vcallfree.utils.Dispatcher
import ufree.call.international.phone.wifi.vcallfree.utils.UserManager

/**
 * Created by lyf on 2020/5/8.
 */
class SetPhoneNumberActivity:BaseBackActivity<ActivitySetPhoneNumberBinding>() {

    override fun getLayoutRes(): Int = R.layout.activity_set_phone_number

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        dataBinding.activity = this
        dataBinding.country = UserManager.get().country

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