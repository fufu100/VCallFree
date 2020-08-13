package vcall.free.international.phone.wifi.calling.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.newmotor.x5.db.DBHelper
import kotlinx.coroutines.*
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.databinding.ActivitySetPhoneNumberBinding
import vcall.free.international.phone.wifi.calling.lib.BaseBackActivity
import vcall.free.international.phone.wifi.calling.utils.Dispatcher
import vcall.free.international.phone.wifi.calling.utils.RxUtils
import vcall.free.international.phone.wifi.calling.utils.UserManager
import vcall.free.international.phone.wifi.calling.utils.toast
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