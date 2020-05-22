package ufree.call.international.phone.wifi.vcallfree.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.newmotor.x5.db.DBHelper
import kotlinx.coroutines.*
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.api.Api
import ufree.call.international.phone.wifi.vcallfree.databinding.ActivitySetPhoneNumberBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseBackActivity
import ufree.call.international.phone.wifi.vcallfree.utils.Dispatcher
import ufree.call.international.phone.wifi.vcallfree.utils.RxUtils
import ufree.call.international.phone.wifi.vcallfree.utils.UserManager
import ufree.call.international.phone.wifi.vcallfree.utils.toast
import java.util.*

/**
 * Created by lyf on 2020/5/8.
 */
class SetPhoneNumberActivity:BaseBackActivity<ActivitySetPhoneNumberBinding>() {

    override fun getLayoutRes(): Int = R.layout.activity_set_phone_number

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        dataBinding.activity = this
        if(UserManager.get().country == null) {
            UserManager.get().country = DBHelper.get().getCountry("US")
        }
        dataBinding.country = UserManager.get().country

    }

    fun selectCountry(v: View){
        Dispatcher.dispatch(this){
            navigate(CallRatesActivity::class.java)
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

    var job:Job? = null
    fun getVerCode(){
        if(job == null || job!!.isCompleted) {
            job = GlobalScope.launch {
                for(i in 60 downTo  0){
                    withContext(Dispatchers.Main){
                        dataBinding.btn.text = String.format(Locale.getDefault(),"%ds",i)
                    }
                    delay(1000)
                }
                dataBinding.btn.text = "Send Code"
            }
        }
    }

    fun bindPhone(){
        var phone = dataBinding.phoneTv.text.toString()
        if(phone.isNotEmpty()) {
            compositeDisposable.add(
                Api.getApiService().bindPhone(UserManager.get().user!!.sip, phone)
                    .compose(RxUtils.applySchedulers())
                    .subscribe({

                    },{
                        it.printStackTrace()
                    })
            )
        }else{
            toast(R.string.tip_phone_is_empty)
        }
    }
}