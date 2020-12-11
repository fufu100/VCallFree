package vcall.free.international.phone.wifi.calling.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.app.ShareCompat
import com.anythink.core.api.ATAdConst
import com.anythink.core.api.ATAdInfo
import com.anythink.core.api.AdError
import com.anythink.nativead.api.*
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.i18n.phonenumbers.PhoneNumberToTimeZonesMapper
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.newmotor.x5.db.DBHelper
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.api.Record
import vcall.free.international.phone.wifi.calling.databinding.ActivityCallResultBinding
import vcall.free.international.phone.wifi.calling.lib.App
import vcall.free.international.phone.wifi.calling.lib.BaseBackActivity
import vcall.free.international.phone.wifi.calling.nativead.NativeDemoRender
import vcall.free.international.phone.wifi.calling.utils.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by lyf on 2020/5/11.
 */
class CallResultActivity:BaseBackActivity<ActivityCallResultBinding>() {
    override fun getLayoutRes(): Int = R.layout.activity_call_result
    lateinit var record: Record
    private lateinit var atNatives: ATNative
    private var anyThinkNativeAdView: ATNativeAdView? = null
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        record = intent.getParcelableExtra("record")
        dataBinding.activity = this
        if(record.username?.isNotEmpty() == true){
            dataBinding.phone = record.username
        }else {
            dataBinding.phone = "+" + record.code + record.phone
        }
        dataBinding.country = DBHelper.get().getCountry(record.iso)
        val date = Date(record.addTime)
        println("$tag initView +${dataBinding.country?.code}${dataBinding.phone}")
        try {
            val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
            val phoneNumber = phoneNumberUtil.parseAndKeepRawInput("+${record.code}${record.phone}", null)
            val timeZone = PhoneNumberToTimeZonesMapper.getInstance().getTimeZonesForNumber(phoneNumber).toString()
            val tz = TimeZone.getTimeZone(timeZone.substring(1,timeZone.length - 1))
            val format = SimpleDateFormat("E,dd/MM hh:mm a")
            format.timeZone = tz
            dataBinding.date = format.format(date)
        }catch (e:Exception){
            e.printStackTrace()
        }

        dataBinding.coinCost.text = record!!.coinCost.toString()
        dataBinding.duration.text = String.format(Locale.getDefault(),"%d:%02d min",record.duration / 60,record.duration % 60)

        println("CallResultActivity duration=${record.duration}")
        if(record.duration > 30){
            dataBinding.group.visibility = View.VISIBLE
        }else{
            if(AdManager.get().adData?.ads?.count {
                    it.adPlaceID == "voip_ysgd"
                } == 1) {
                dataBinding.templateView.visibility = View.VISIBLE
                dataBinding.progressBar.visibility = View.VISIBLE
                loadAd()
            }
        }
    }

    fun hate(){
        val str = String.format(
            "Please give us your suggestions and questions. If you help us to improve successfully, you will be rewarded 50000 points.<br>" +
                    "VersionName:%s<br>VersionCode:%d<br>sip:%s<br>Device Manufacture:%s<br>Device Brand/Model:%s/%s<br>System Version:%d",
            getVersionName(),
            getVersionCode(),
            UserManager.get().user?.sip ?: "",
            Build.MANUFACTURER,
            Build.BRAND,
            Build.MODEL,
            Build.VERSION.SDK_INT
        )

        ShareCompat.IntentBuilder.from(this).setType("message/rfc822")
            .addEmailTo("VCallFree_Feedback@hotmail.com")
            .setSubject("unlike")
            .setHtmlText(str)
            .setChooserTitle("Choose email")
            .startChooser();
    }

    fun like(){
        Dispatcher.dispatch(this){
            action(Intent.ACTION_VIEW)
            data(Uri.parse("market://details?id=" + packageName))
            defaultAnimate()
        }.go()
    }

    private fun loadAd(){
        val adID = getNativeAdID()
        if(adID.isNotEmpty()) {
            val adLoader = AdLoader.Builder(this, adID)
                .forUnifiedNativeAd { unifiedNativeAd ->
                    val styles = NativeTemplateStyle.Builder().withMainBackgroundColor(
                        ColorDrawable(
                            Color.WHITE
                        )
                    ).build()
                    dataBinding.templateView.setStyles(styles)
                    dataBinding.templateView.setNativeAd(unifiedNativeAd)
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(p0: LoadAdError?) {
                        println("原生广告加载失败 ${p0?.message} ${p0?.cause} ${p0?.code}")
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
                if(AdManager.get().adData!!.ads[i].adPlaceID == "voip_ysgd"){
                    adID = AdManager.get().adData!!.ads[i].adSources[0].adPlaceID
                    break
                }
            }
        }
        return adID
    }
}