package vcall.free.international.phone.wifi.calling.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import vcall.free.international.phone.wifi.calling.lib.App
import java.util.*

class AppOpenManager {
//    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/3419835294"
    private val AD_UNIT_ID = "ca-app-pub-2764389811554448/5221221339"
    private val LOG_TAG = "AppOpenAdManager"
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false
    var googleMobileAdsConsentManager:GoogleMobileAdsConsentManager

    /** Keep track of the time an app open ad is loaded to ensure you don't show an expired ad. */
    private var loadTime: Long = 0

    init {
        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(App.context!!)
    }
    /**
     * Load an ad.
     *
     * @param context the context of the activity that loads the ad
     */
    fun loadAd(context: Context,onShowAdCompleteListener: OnShowAdCompleteListener? = null) {
        if(!googleMobileAdsConsentManager.canRequestAds){
            return
        }
        // Do not load ad if there is an unused ad or one is already loading.
        if (isLoadingAd || isAdAvailable()) {
            return
        }
        println("$LOG_TAG loadAd")
        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            AD_UNIT_ID,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                /**
                 * Called when an app open ad has loaded.
                 *
                 * @param ad the loaded app open ad.
                 */
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                    loadTime = Date().time
                    Log.d(LOG_TAG, "onAdLoaded.")
//                    App.context?.toast("开屏广告加载成功")
                    if( onShowAdCompleteListener != null && !(context as Activity).isFinishing){
//                        showAdIfAvailable(context,showAfterLoad,onShowAdCompleteListener)
                        onShowAdCompleteListener.onAdLoad()
                    }
                }

                /**
                 * Called when an app open ad has failed to load.
                 *
                 * @param loadAdError the error.
                 */
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
//                    App.context?.toast("开屏广告加载失败")
                    Log.d(LOG_TAG, "onAdFailedToLoad: " + loadAdError.message)
                    onShowAdCompleteListener?.onAdFailedToLoad()
                }
            })
    }

    /** Check if ad was loaded more than n hours ago. */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /** Check if ad exists and can be shown. */
    public fun isAdAvailable(): Boolean {
        // Ad references in the app open beta will time out after four hours, but this time limit
        // may change in future beta versions. For details, see:
        // https://support.google.com/admob/answer/9341964?hl=en
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    /**
     * Show the ad if one isn't already showing.
     *
     * @param activity the activity that shows the app open ad
     */
    fun showAdIfAvailable(activity: Activity) {
        showAdIfAvailable(
            activity,
            object : OnShowAdCompleteListener {
                override fun onShowAdComplete() {}
                override fun onShowAd(){}
                override fun onAdFailedToLoad() {}
                override fun onAdLoad() {}
            })
    }

    /**
     * Show the ad if one isn't already showing.
     *
     * @param activity the activity that shows the app open ad
     * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
     */
    fun showAdIfAvailable(
        activity: Activity,
        onShowAdCompleteListener: OnShowAdCompleteListener
    ) {
        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) {
            Log.d(LOG_TAG, "The app open ad is already showing.")
            return
        }

        // If the app open ad is not available yet, invoke the callback then load the ad.
        if (!isAdAvailable()) {
            Log.d(LOG_TAG, "The app open ad is not ready yet.")
            loadAd(activity,onShowAdCompleteListener)
//            if(!showAfterLoad){
//                onShowAdCompleteListener.onShowAdComplete()
//                return
//            }
        }

        Log.d(LOG_TAG, "Will show ad.")

        if(isAdAvailable()) {

            appOpenAd!!.setFullScreenContentCallback(
                object : FullScreenContentCallback() {
                    /** Called when full screen content is dismissed. */
                    override fun onAdDismissedFullScreenContent() {
                        // Set the reference to null so isAdAvailable() returns false.
                        appOpenAd = null
                        isShowingAd = false
                        Log.d(LOG_TAG, "onAdDismissedFullScreenContent.")

                        onShowAdCompleteListener.onShowAdComplete()
                        loadAd(activity, onShowAdCompleteListener)
                    }

                    /** Called when fullscreen content failed to show. */
                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        appOpenAd = null
                        isShowingAd = false
                        Log.d(LOG_TAG, "onAdFailedToShowFullScreenContent: " + adError.message)

                        onShowAdCompleteListener.onShowAdComplete()
                        loadAd(activity,  onShowAdCompleteListener)
                    }

                    /** Called when fullscreen content is shown. */
                    override fun onAdShowedFullScreenContent() {
                        Log.d(LOG_TAG, "onAdShowedFullScreenContent.")
                        onShowAdCompleteListener.onShowAd()
                    }
                })
            isShowingAd = true
            appOpenAd!!.show(activity)
        }
    }
}

interface OnShowAdCompleteListener {
    fun onShowAdComplete()
    fun onShowAd()
    fun onAdFailedToLoad()
    fun onAdLoad()
}