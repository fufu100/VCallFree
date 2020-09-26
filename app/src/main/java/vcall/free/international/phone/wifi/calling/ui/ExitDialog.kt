package vcall.free.international.phone.wifi.calling.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.databinding.DialogExitBinding
import vcall.free.international.phone.wifi.calling.lib.prefs
import vcall.free.international.phone.wifi.calling.utils.AdManager
import vcall.free.international.phone.wifi.calling.utils.Dispatcher
import vcall.free.international.phone.wifi.calling.utils.LogUtils
import vcall.free.international.phone.wifi.calling.utils.UserManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by lyf on 2020/8/19.
 */
class ExitDialog(context: Context,val callback:(flag:Int) -> Unit):Dialog(context, R.style.CustomDialog) {
    val dataBinding:DialogExitBinding = DataBindingUtil.inflate(layoutInflater,R.layout.dialog_exit,null,false)
    init {
        setContentView(dataBinding.root)
        dataBinding.dialog = this
        loadAd()


        setOnDismissListener {
            loadAd()
        }

        setOnShowListener {
            LogUtils.println("ExitDialog setOnShowListener")
            if(UserManager.get().user != null && UserManager.get().user?.phone?.isEmpty() == true){
                dataBinding.setNowTv.text = context.resources.getString(R.string.set_now)
                dataBinding.titleTv.text = context.resources.getString(R.string.don_t_forget_to_set_phone_number)
            } else {
                val format = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
                val today = format.format(Date())
                var lastCheckDay = prefs.getStringValue("last_check_day","")
                if(lastCheckDay != today){
                    dataBinding.setNowTv.text = context.resources.getString(R.string.check_in)
                    dataBinding.titleTv.text = context.resources.getString(R.string.don_t_forget_to_check_in)
                }else{
                    dataBinding.setNowTv.text = context.resources.getString(R.string.more_credits)
                    dataBinding.titleTv.text = context.resources.getString(R.string.don_t_forget_to_get_more_credits)
                }
            }

        }
    }

    private fun loadAd(){
        val adID = getNativeAdID()
        if(adID.isNotEmpty()) {
            val adLoader = AdLoader.Builder(context, adID)
                .forUnifiedNativeAd { unifiedNativeAd ->
                    val styles = NativeTemplateStyle.Builder().withMainBackgroundColor(
                        ColorDrawable(
                            Color.WHITE
                        )
                    ).build()
//                val template:TemplateView = findViewById(R.id.my_template)
                    dataBinding.templateView.setStyles(styles)
                    dataBinding.templateView.setNativeAd(unifiedNativeAd)
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(p0: LoadAdError?) {
                        LogUtils.println("原生广告加载失败 $p0")
                    }

                    override fun onAdLoaded() {
                        dataBinding.templateView.visibility = View.VISIBLE
                        dataBinding.progressBar.visibility = View.GONE
                    }
                })
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        }
    }

    fun getNativeAdID():String{
        var adID = ""
        if(AdManager.get().adData != null){
            for(i in 0 until AdManager.get().adData!!.ads.size){
                if(AdManager.get().adData!!.ads[i].adPlaceID == "voip_ystc"){
                    adID = AdManager.get().adData!!.ads[i].adSources[0].adPlaceID
                    break
                }
            }
        }
        return adID
    }

    fun exit(){
        callback(0)
        dismiss()
    }

    fun setNow(){
        if(dataBinding.setNowTv.text.toString() == context.resources.getString(R.string.set_now)){
            Dispatcher.dispatch(context){
                navigate(SetPhoneNumberActivity::class.java)
                defaultAnimate()
            }.go()
        }else if(dataBinding.setNowTv.text.toString() == context.resources.getString(R.string.check_in)){
            callback(2)
        }else if (dataBinding.setNowTv.text.toString() == context.resources.getString(R.string.more_credits)){
            callback(3)
        }
        dismiss()
    }
}