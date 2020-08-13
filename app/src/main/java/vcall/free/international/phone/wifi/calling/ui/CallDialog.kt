package vcall.free.international.phone.wifi.calling.ui

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import io.reactivex.disposables.Disposable
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.api.Country
import vcall.free.international.phone.wifi.calling.databinding.DialogCallBinding
import vcall.free.international.phone.wifi.calling.utils.RxUtils
import vcall.free.international.phone.wifi.calling.utils.UserManager
import vcall.free.international.phone.wifi.calling.utils.toast
import java.util.*

/**
 * Created by lyf on 2020/5/7.
 */
class CallDialog(context: Context,val makeCall:(callRate:Int) -> Unit):Dialog(context) {
    var phone :String? = null
    set(value) {
        field = value
        dataBinding.phone = value
        println("dialog phone=$value")
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

        val date = Date()
        dataBinding.date = String.format(Locale.getDefault(),"%tA, %tB %td,%tl:%tM %tp",date,date,date,date,date,date)
    }

    fun getCallRate(){
        disposable = Api.getApiService().getPrice(country!!.iso,country?.code + phone)
            .compose(RxUtils.applySchedulers())
            .subscribe({
                if(it.errcode == 0){
                    rate = it.points
                    dataBinding.callRate.text = it.points.toString()
                    dataBinding.timeRemaining.text = ((UserManager.get().user?.points ?: 0) / it.points).toString()
                }else{
                    context.toast(it.errmsg)
                    dismiss()
                }
            },{
                it.printStackTrace()
            })
    }

    fun call(v:View){
        println("call-$rate")
        if(rate != 0) {
            makeCall(rate)
            dismiss()
        }
    }

}