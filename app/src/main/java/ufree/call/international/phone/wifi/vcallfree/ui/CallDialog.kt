package ufree.call.international.phone.wifi.vcallfree.ui

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import io.reactivex.disposables.Disposable
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.api.Api
import ufree.call.international.phone.wifi.vcallfree.api.Country
import ufree.call.international.phone.wifi.vcallfree.databinding.DialogCallBinding
import ufree.call.international.phone.wifi.vcallfree.utils.RxUtils
import ufree.call.international.phone.wifi.vcallfree.utils.UserManager
import ufree.call.international.phone.wifi.vcallfree.utils.toast
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
        dataBinding.call.isClickable = false
        disposable = Api.getApiService().getPrice(country!!.iso,country?.code + phone)
            .compose(RxUtils.applySchedulers())
            .subscribe({
                if(it.errcode == 0){
                    dataBinding.call.isClickable = true
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
        makeCall(rate)
        dismiss()
    }

}