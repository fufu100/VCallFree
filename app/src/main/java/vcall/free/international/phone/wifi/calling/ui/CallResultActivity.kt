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
import android.widget.*
import androidx.core.app.ShareCompat
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.i18n.phonenumbers.PhoneNumberToTimeZonesMapper
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.newmotor.x5.db.DBHelper
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.api.Record
import vcall.free.international.phone.wifi.calling.databinding.ActivityCallResultBinding
import vcall.free.international.phone.wifi.calling.lib.App
import vcall.free.international.phone.wifi.calling.lib.BaseBackActivity
import vcall.free.international.phone.wifi.calling.lib.prefs
import vcall.free.international.phone.wifi.calling.utils.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by lyf on 2020/5/11.
 */
class CallResultActivity:BaseBackActivity<ActivityCallResultBinding>() {
    override fun getLayoutRes(): Int = R.layout.activity_call_result
    lateinit var record: Record
    var currentNativeAd: NativeAd? = null
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        record = intent.getParcelableExtra("record")!!
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
        val likeCount = prefs.getIntValue("like_count",0)
        if(record.duration > 60 && likeCount < 6){
            dataBinding.group.visibility = View.VISIBLE
        }else{
            if(AdManager.get().adData?.ads?.count {
                    it.adPlaceID == "voip_ysgd"
                } == 1) {
                dataBinding.adContainer.visibility = View.VISIBLE
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
            .setSubject("VCallFree")
            .setHtmlText(str)
            .setChooserTitle("Choose email")
            .startChooser();
    }

    fun like(){
        val likeCount = prefs.getIntValue("like_count",0)
        prefs.save("like_count",likeCount + 1)
        Dispatcher.dispatch(this){
            action(Intent.ACTION_VIEW)
            data(Uri.parse("market://details?id=" + packageName))
            defaultAnimate()
        }.go()
    }

    private fun populateNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        // Set the media view.
        adView.mediaView = adView.findViewById<MediaView>(R.id.ad_media)

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline
        adView.mediaView.setMediaContent(nativeAd.mediaContent)

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView.visibility = View.INVISIBLE
        } else {
            adView.bodyView.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView.visibility = View.INVISIBLE
        } else {
            adView.callToActionView.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView.visibility = View.GONE
        } else {
            (adView.iconView as ImageView).setImageDrawable(
                nativeAd.icon.drawable
            )
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.price == null) {
            adView.priceView.visibility = View.INVISIBLE
        } else {
            adView.priceView.visibility = View.VISIBLE
            (adView.priceView as TextView).text = nativeAd.price
        }

        if (nativeAd.store == null) {
            adView.storeView.visibility = View.INVISIBLE
        } else {
            adView.storeView.visibility = View.VISIBLE
            (adView.storeView as TextView).text = nativeAd.store
        }

        if (nativeAd.starRating == null) {
            adView.starRatingView.visibility = View.INVISIBLE
        } else {
            (adView.starRatingView as RatingBar).rating = nativeAd.starRating!!.toFloat()
            adView.starRatingView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.INVISIBLE
        } else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        val vc = nativeAd.mediaContent.videoController

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {
            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
                override fun onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
//                    refresh_button.isEnabled = true
//                    videostatus_text.text = "Video status: Video playback has ended."
                    super.onVideoEnd()
                }
            }
        } else {
//            videostatus_text.text = "Video status: Ad does not contain a video asset."
//            refresh_button.isEnabled = true
        }
    }

    private fun loadAd(){
        if(AdManager.get().nativeAdMap[AdManager.ad_call_result] != null){
            var activityDestroyed = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                activityDestroyed = isDestroyed
            }
            Log.d(tag, "loadAd activityDestroyed=$activityDestroyed ")
            if (activityDestroyed  || isFinishing || isChangingConfigurations) {
                AdManager.get().nativeAdMap[AdManager.ad_call_result]?.destroy()
                return
            }
            // You must call destroy on old ads when you are done with them,
            // otherwise you will have a memory leak.
//            currentNativeAd?.destroy()
//            currentNativeAd = nativeAd
            val adView = layoutInflater
                .inflate(R.layout.ad_unified, null) as NativeAdView
            populateNativeAdView(AdManager.get().nativeAdMap[AdManager.ad_call_result]!!, adView)
            dataBinding.adContainer.removeAllViews()
            dataBinding.adContainer.addView(adView)
        }else{
            AdManager.get().loadNativeAd(this,AdManager.ad_call_result)
        }
    }

}