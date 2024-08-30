package vcall.free.international.phone.wifi.calling.ui

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.google.i18n.phonenumbers.PhoneNumberToTimeZonesMapper
import com.google.i18n.phonenumbers.PhoneNumberUtil
import io.reactivex.disposables.Disposable
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.api.Country
import vcall.free.international.phone.wifi.calling.databinding.DialogCallBinding
import vcall.free.international.phone.wifi.calling.utils.LogUtils
import vcall.free.international.phone.wifi.calling.utils.RxUtils
import vcall.free.international.phone.wifi.calling.utils.UserManager
import vcall.free.international.phone.wifi.calling.utils.toast
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by lyf on 2020/5/7.
 */
class CallDialog(context: Context,val makeCall:(callRate:Int) -> Unit):Dialog(context) {
    var phone :String? = null
    set(value) {
        field = value
        dataBinding.phone = value
        LogUtils.println("dialog phone=$value")
    }
    var country: Country? = null
    set(value) {
        field = value
        dataBinding.country = value
    }
    var rate:Int = 0
    var disposable:Disposable? = null
    val dataBinding:DialogCallBinding = DataBindingUtil.inflate(layoutInflater,
        R.layout.dialog_call,null,false)

    init {
        dataBinding.dialog = this
        dataBinding.points = UserManager.get().user?.points
        setContentView(dataBinding.root)
        with(window) {
            this?.setGravity(Gravity.BOTTOM)
            this?.setBackgroundDrawable(null)
            this?.attributes?.width = WindowManager.LayoutParams.MATCH_PARENT
        }


    }

    fun getCallRate(){
        val date = Date()
        val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
        val phoneNumber = phoneNumberUtil.parseAndKeepRawInput("+${country?.code}${dataBinding.phone}", null)
        val timeZone = PhoneNumberToTimeZonesMapper.getInstance().getTimeZonesForNumber(phoneNumber).toString()
        val tz = TimeZone.getTimeZone(timeZone.substring(1,timeZone.length - 1))
        val format = SimpleDateFormat("E,dd/MM hh:mm a",Locale.getDefault())
        format.timeZone = tz
        dataBinding.date = format.format(date)
        disposable = Api.getApiService().getPrice(country!!.iso,country?.code + phone)
            .compose(RxUtils.applySchedulers())
            .subscribe({
                if(it.code == 20000){
                    rate = it.data.points
                    dataBinding.callRate.text = it.data.points.toString()
                    dataBinding.timeRemaining.text = ((UserManager.get().user?.points ?: 0) / it.data.points).toString()
                    dataBinding.progressBar1.visibility = View.GONE
                    dataBinding.progressBar2.visibility = View.GONE
                }else{
                    context.toast(it.message)
                    dismiss()
                }
            },{
                it.printStackTrace()
            })
    }

    fun call(v:View){
        val s = dataBinding.timeRemaining.text
        LogUtils.println("CallDialog call-$rate,s=$s")
        if(rate != 0 && s != "0") {
            makeCall(rate)
            dismiss()
        }
    }

}