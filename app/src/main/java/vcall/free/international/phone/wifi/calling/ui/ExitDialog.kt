package vcall.free.international.phone.wifi.calling.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.newmotor.x5.db.DBHelper
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.databinding.DialogExitBinding
import vcall.free.international.phone.wifi.calling.lib.App
import vcall.free.international.phone.wifi.calling.lib.prefs
import vcall.free.international.phone.wifi.calling.utils.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by lyf on 2020/8/19.
 */
class ExitDialog(context: Context, val callback: (flag: Int) -> Unit) :
    Dialog(context, R.style.CustomDialog) {
    val TAG = "ExitDialog"
    val dataBinding: DialogExitBinding =
        DataBindingUtil.inflate(layoutInflater, R.layout.dialog_exit, null, false)
    var currentNativeAd: NativeAd? = null
//    private var anyThinkNativeAdView: ATNativeAdView? = null

    init {
        setContentView(dataBinding.root)
        dataBinding.dialog = this
        println("$TAG init ${AdManager.get().adData}")



//        setOnDismissListener {
//            loadAd()
//        }

        setOnShowListener {
            LogUtils.println("ExitDialog setOnShowListener ${AdManager.get().adData}")
            if (AdManager.get().adData?.ads?.count {
                    it.adPlaceID == AdManager.ad_quite
                } == 1) {
                dataBinding.adContainer.visibility = View.VISIBLE
                loadAd()
            } else {
                dataBinding.adContainer.visibility = View.GONE
            }

            if (UserManager.get().user != null && UserManager.get().user?.phone?.isEmpty() == true) {
                dataBinding.setNowTv.text = context.resources.getString(R.string.set_now)
                dataBinding.titleTv.text =
                    context.resources.getString(R.string.don_t_forget_to_set_phone_number)
            } else {
                val format = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
                val today = format.format(Date())
                var lastCheckDay = prefs.getStringValue("last_check_day", "")
                if (lastCheckDay != today) {
                    dataBinding.setNowTv.text = context.resources.getString(R.string.check_in)
                    dataBinding.titleTv.text =
                        context.resources.getString(R.string.don_t_forget_to_check_in)
                } else {
                    dataBinding.setNowTv.text = context.resources.getString(R.string.more_credits)
                    dataBinding.titleTv.text =
                        context.resources.getString(R.string.don_t_forget_to_get_more_credits)
                }
            }

        }
    }

    override fun onDetachedFromWindow() {
//        currentNativeAd?.destroy()
        AdManager.get().nativeAdMap[AdManager.ad_quite]?.destroy()
        AdManager.get().nativeAdMap[AdManager.ad_quite] = null
        super.onDetachedFromWindow()
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

    private fun loadAd() {
        Log.d(TAG, "loadAd: ${AdManager.get().nativeAdMap[AdManager.ad_quite] != null}")
        if(AdManager.get().nativeAdMap[AdManager.ad_quite] != null){
            var activityDestroyed = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                activityDestroyed = !isShowing
            }
            Log.d(TAG, "loadAd activityDestroyed=$activityDestroyed ")
            if (activityDestroyed) {
                AdManager.get().nativeAdMap[AdManager.ad_quite]?.destroy()
                return
            }
            // You must call destroy on old ads when you are done with them,
            // otherwise you will have a memory leak.
//            currentNativeAd?.destroy()
//            currentNativeAd = nativeAd
            val adView = layoutInflater
                .inflate(R.layout.ad_unified, null) as NativeAdView
            populateNativeAdView(AdManager.get().nativeAdMap[AdManager.ad_quite]!!, adView)
            dataBinding.adContainer.removeAllViews()
            dataBinding.adContainer.addView(adView)
        }else{
            AdManager.get().loadNativeAd(context,AdManager.ad_quite)
        }
//        val adID = getNativeAdID()
//        if(adID.isNotEmpty()) {
//            val builder = AdLoader.Builder(context, adID)
//            builder.forNativeAd { nativeAd ->
//                // OnUnifiedNativeAdLoadedListener implementation.
//                // If this callback occurs after the activity is destroyed, you must call
//                // destroy and return or you may get a memory leak.
//                var activityDestroyed = false
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                    activityDestroyed = !isShowing
//                }
//                if (activityDestroyed) {
//                    nativeAd.destroy()
//                    return@forNativeAd
//                }
//                // You must call destroy on old ads when you are done with them,
//                // otherwise you will have a memory leak.
//                currentNativeAd?.destroy()
//                currentNativeAd = nativeAd
//                val adView = layoutInflater
//                    .inflate(R.layout.ad_unified, null) as NativeAdView
//                populateNativeAdView(nativeAd, adView)
//                dataBinding.adContainer.removeAllViews()
//                dataBinding.adContainer.addView(adView)
//            }
//            val videoOptions = VideoOptions.Builder()
//                .setStartMuted(true)
//                .build()
//
//            val adOptions = NativeAdOptions.Builder()
//                .setVideoOptions(videoOptions)
//                .build()
//
//            builder.withNativeAdOptions(adOptions)
//
//            val adLoader = builder.withAdListener(object : AdListener() {
//                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
//                    val error =
//                        """
//           domain: ${loadAdError.domain}, code: ${loadAdError.code}, message: ${loadAdError.message}
//          """"
//                    Log.d(TAG, "onAdFailedToLoad: $error ")
//                }
//            }).build()
//
//            adLoader.loadAd(AdRequest.Builder().build())
//        }
    }

//    fun getNativeAdID(): String {
//        var adID = ""
//        if (AdManager.get().adData != null) {
//            for (i in 0 until AdManager.get().adData!!.ads.size) {
//                if (AdManager.get().adData!!.ads[i].adPlaceID == AdManager.ad_call_result) {
//                    println("getNativeAdID i=$i ${AdManager.get().adData!!.ads[i]}")
//                    adID = AdManager.get().adData!!.ads[i].adSources[0].adPlaceID
//                    break
//                }
//            }
//        }
//        return adID
//    }

    fun exit() {
        callback(0)
        dismiss()
    }

    fun setNow() {
        if (dataBinding.setNowTv.text.toString() == context.resources.getString(R.string.set_now)) {
            Dispatcher.dispatch(context) {
                navigate(SetPhoneNumberActivity::class.java)
                defaultAnimate()
            }.go()
        } else if (dataBinding.setNowTv.text.toString() == context.resources.getString(R.string.check_in)) {
            callback(2)
        } else if (dataBinding.setNowTv.text.toString() == context.resources.getString(R.string.more_credits)) {
            callback(3)
        }
        dismiss()
    }
}