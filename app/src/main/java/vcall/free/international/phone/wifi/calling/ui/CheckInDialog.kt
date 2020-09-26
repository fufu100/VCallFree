package vcall.free.international.phone.wifi.calling.ui

import android.app.Dialog
import android.content.Context
import android.view.View
import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.dialog_agreement.*
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.databinding.DialogDailySignInBinding
import vcall.free.international.phone.wifi.calling.databinding.DialogSignInBinding
import vcall.free.international.phone.wifi.calling.lib.prefs
import vcall.free.international.phone.wifi.calling.utils.LogUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by lyf on 2020/8/16.
 */
class CheckInDialog(context: Context,val callback:(point:Int) -> Unit): Dialog(context, R.style.CustomDialog) {
    companion object{
        val points = intArrayOf(20,50,100,200,400,600,1000)
    }
    var lastCheckDay = prefs.getStringValue("last_check_day","")
    var consecutive_check:Int = prefs.getIntValue("check_max_day",0)
    val dataBinding: DialogDailySignInBinding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_daily_sign_in,
        null,false)
    var today:String
    var yesterday:String
    init {
        dataBinding.dialog = this
        setContentView(dataBinding.root)

        val format = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
        yesterday = format.format(Date(System.currentTimeMillis() - 24 * 3600 * 1000))
        today = format.format(Date())
        LogUtils.println("CheckInDialog $today $yesterday $lastCheckDay $consecutive_check")
        if(today == lastCheckDay){
            dataBinding.hasCheckedInTv.visibility = View.VISIBLE
            dataBinding.checkInTv.text = context.resources.getString(R.string.close)
        }else {
            dataBinding.hasCheckedInTv.visibility = View.INVISIBLE
            dataBinding.checkInTv.text = context.resources.getString(R.string.check_in)
            if(lastCheckDay != yesterday){
                consecutive_check = 0
                prefs.save("check_max_day",0)
            }
        }
        if(lastCheckDay == yesterday || lastCheckDay == today){
            dataBinding.consecutiveCheck.text = "Consecutive ${consecutive_check} Days"
            if(consecutive_check > 0){
                for(i in 0 until dataBinding.daysLayout.childCount){
                    if(i < consecutive_check){
                        dataBinding.daysLayout.getChildAt(i).isSelected = true
                    }
                }
            }
        }
    }
    fun checkIn(){
        if(lastCheckDay == today) {
            dismiss()
        }else{
            callback(points[consecutive_check])
            dismiss()
        }
    }
}