package vcall.free.international.phone.wifi.calling.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import vcall.free.international.phone.wifi.calling.db.DBHelper
import kotlinx.coroutines.*
import vcall.free.international.phone.wifi.calling.api.AdResp
import vcall.free.international.phone.wifi.calling.lib.App

/**
 * Created by lyf on 2020/8/14.
 */
class AdManager {
    companion object {
        val tag = "AdManager"
        val ad_splash = "AdMob_startpage"
        val ad_preclick = "AdMob_preclick"
        val ad_point = "AdMob_jifen"
        val ad_close = "AdMob_close"
        val ad_rewarded = "AdMob_rewardvideo"
        val ad_quite = "AdMob_ystc"
        val ad_call_result = "AdMob_ysgd"
        private var instance: AdManager? = null
            get() {
                if (field == null) {
                    field = AdManager()
                }

                return field
            }

        @JvmStatic
        fun get(): AdManager {
            return instance!!
        }
    }
    var appOpenManager:AppOpenManager? = null
    var adData: AdResp? = null
    var interstitialAdMap: MutableMap<String, InterstitialAd?> = mutableMapOf()
    var nativeAdMap: MutableMap<String, NativeAd?> = mutableMapOf()
    var rewardedAd: RewardedAd? = null
//    var nativeAd:NativeAd? = null
    var referrer: String = ""
    var showLuckyCredits = false
    var interstitialAdLoadStatus :MutableMap<String,Int> = mutableMapOf()

    var interstitialAdListener: MutableMap<String,VCallAdListener> = mutableMapOf()
    var splashAdListener:VCallAdListener? = null
//    var rewardedAdListener: MutableList<VCallAdListener> = mutableListOf()
    private var referrerClient: InstallReferrerClient =
        InstallReferrerClient.newBuilder(App.context).build()

    init {
        appOpenManager = AppOpenManager()
        referrerClient.startConnection(object : InstallReferrerStateListener {

            override fun onInstallReferrerSetupFinished(responseCode: Int) {
                when (responseCode) {
                    InstallReferrerClient.InstallReferrerResponse.OK -> {
                        // Connection established.
                        try {
                            val response: ReferrerDetails = referrerClient.installReferrer
                            referrer = response.installReferrer
                            println("InstallReferrerStateListener Connection established $referrer")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                    InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                        // API not available on the current Play Store app.
                        println("InstallReferrerStateListener API not available on the current Play Store app")
                    }
                    InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                        // Connection couldn't be established.
                        println("InstallReferrerStateListener Connection couldn't be established")
                    }
                }
            }

            override fun onInstallReferrerServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                println("InstallReferrerStateListener onInstallReferrerServiceDisconnected")
            }
        })
    }


    fun showSplashInterstitialAd(activity: Activity) {
//        LogUtils.println("$tag showSplashInterstitialAd-- ${interstitialAdMap[ad_splash] == null}")
//        if (interstitialAdMap[ad_splash] != null) {
//            interstitialAdMap[ad_splash]?.show(activity)
//        }
        showInterstitialAd(activity, ad_splash)
    }

    fun showPreclickInterstitialAd(activity: Activity?) {
//        LogUtils.println("$tag showPreclickInterstitialAd-- ${interstitialAdMap[ad_preclick] == null} ${interstitialAdLoadStatus[ad_preclick]}")
//        if (interstitialAdLoadStatus[ad_preclick] == 1 && interstitialAdMap[ad_preclick] != null && activity != null) {
//            interstitialAdMap[ad_preclick]?.show(activity)
//        }
        showInterstitialAd(activity, ad_preclick)
    }

    fun showCloseInterstitialAd(activity: Activity?) {
//        LogUtils.println("$tag showPreclickInterstitialAd-- ${interstitialAdMap[ad_close] == null} ${interstitialAdLoadStatus[ad_close]}")
//        if (interstitialAdLoadStatus[ad_close] == 1 && interstitialAdMap[ad_close] != null && activity != null) {
//            interstitialAdMap[ad_close]?.show(activity)
//        }
        showInterstitialAd(activity, ad_close)
    }

    fun showPointInterstitialAd(activity: Activity?) {
//        LogUtils.println("$tag showPointInterstitialAd-- ${interstitialAdMap[ad_point] == null} ${interstitialAdLoadStatus[ad_point]}")
//        if (interstitialAdLoadStatus[ad_point] == 1 && interstitialAdMap[ad_point] != null && activity != null) {
//            interstitialAdMap[ad_point]?.show(activity)
//        }
        showInterstitialAd(activity, ad_point)
    }

    fun showInterstitialAd(activity: Activity?,category: String){
        LogUtils.d(tag, "showInterstitialAd category=$category status=${interstitialAdLoadStatus[category]} ${interstitialAdMap[category] != null} ${activity != null}")
        if (interstitialAdLoadStatus[category] == 1 && interstitialAdMap[category] != null && activity != null) {
            interstitialAdMap[category]!!.fullScreenContentCallback = MyFullScreenContentCallback(activity,category)
            interstitialAdMap[category]!!.show(activity)
        }else{
            LogUtils.d(tag, "showInterstitialAd: 广告展示失败-- $category")
        }
    }

    fun loadNativeAd(context: Context?,category: String){
        if(interstitialAdLoadStatus[category] == 3){
            LogUtils.d(tag, "loadNativeAd  $category 广告正在加载中...")
            return
        }
        if(context == null){
            return
        }

        val adID = getNativeAdID(category)
        LogUtils.d(tag, "loadNativeAd---adID=$adID,category=$category")
        if(adID.isNotEmpty()) {
            val builder = AdLoader.Builder(context, adID)
            builder.forNativeAd { nativeAd ->
                // OnUnifiedNativeAdLoadedListener implementation.
                // If this callback occurs after the activity is destroyed, you must call
                // destroy and return or you may get a memory leak.
//                this@AdManager.nativeAd = nativeAd
                LogUtils.d(tag,"NativeAd forNativeAd--")
                nativeAdMap[category] = nativeAd
            }
            val videoOptions = VideoOptions.Builder()
                .setStartMuted(true)
                .build()

            val adOptions = NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build()

            builder.withNativeAdOptions(adOptions)

            val adLoader = builder.withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    val error =
                        """
           domain: ${loadAdError.domain}, code: ${loadAdError.code}, message: ${loadAdError.message}
          """"
                    LogUtils.d(tag, "onAdFailedToLoad: $error ")
                    interstitialAdLoadStatus[category] = 2
                }

                override fun onAdLoaded() {
                    LogUtils.d(tag, "NativeAd onAdLoaded-- category=$category ")
                    interstitialAdLoadStatus[category] = 1
                }
            }).build()

            adLoader.loadAd(AdRequest.Builder().build())
            interstitialAdLoadStatus[category] = 3
        }
    }

    fun loadRewardedAd(context: Context?,position: Int = 0) {
        val count = DBHelper.get().getAdClickCount()
        println("$tag loadRewardedAd count=$count")
        if (count >= 10) {
            return
        }
        if(context == null){
            return
        }
        if(interstitialAdLoadStatus[ad_rewarded] == 3){
            LogUtils.d(tag, "loadRewardedAd  $ad_rewarded 广告正在加载中...")
            return
        }
        val rewardedListener = MyRewardedListener(context,position)
        if (adData != null) {
            for (i in adData!!.ads.indices) {
                if (adData!!.ads[i].adPlaceID == ad_rewarded && position < adData!!.ads[i].adSources.size) {
                    interstitialAdLoadStatus[ad_rewarded] = 3
                    val adRequest = AdRequest.Builder().build()
                    RewardedAd.load(context,adData!!.ads[i].adSources[position].adPlaceID,adRequest,rewardedListener)
                    break
                }else{
                    interstitialAdListener[ad_rewarded]?.onAdLoadFail()
                    interstitialAdLoadStatus[ad_rewarded] = 2
                }
            }
        }
    }

    fun loadInterstitialAd(context: Context?,category: String, position: Int = 0) {
        val count = DBHelper.get().getAdClickCount()
        LogUtils.println("loadInterstitialAd count=$count $category $position")
        if (count >= 10 || context == null) {
            return
        }
        if(interstitialAdLoadStatus[category] == 3){
            LogUtils.d(tag, "loadInterstitialAd  $category 广告正在加载中...")
            return
        }
        if(interstitialAdLoadStatus[category] == 1 && interstitialAdMap[category] != null){
            LogUtils.d(tag, "loadInterstitialAd  $category 广告已经加载成功")
            return
        }
        val adListener = MyAdListener(context,category, position)
        if (adData != null) {
            for (i in adData!!.ads.indices) {
//                LogUtils.println("$tag loadInterstitialAd ${adData!!.ads[i].adPlaceID} $category $position $i")
                if (adData!!.ads[i].adPlaceID == category  ) {
                    if(position < adData!!.ads[i].adSources.size) {
                        LogUtils.println("$tag loadInterstitialAd 加载广告 $category $position $i")
                        interstitialAdLoadStatus[category] = 3
                        val adRequest = AdRequest.Builder().build()
                        InterstitialAd.load(context,adData!!.ads[i].adSources[position].adPlaceID,adRequest,adListener)
                    }else{
                        interstitialAdListener[category]?.onAdLoadFail()
                        interstitialAdLoadStatus[category] = 2
                        LogUtils.d(tag,"广告加载失败 $category $position ${adData!!.ads[i].adSources.size}")
                        if(category == ad_preclick){
                            GlobalScope.launch {
                                delay(5000)
                                interstitialAdLoadStatus[category] = 2
                                withContext(Dispatchers.Main) {
                                    loadInterstitialAd(context,category,0)
                                }
                            }
                        }
                    }
                    break
                }
            }
        }
    }

    inner class MyRewardedListener(val context: Context?,var position: Int = 0) : RewardedAdLoadCallback() {
        override fun onAdLoaded(rewardedAd: RewardedAd) {
            Log.d(tag, "onRewardedAdLoaded ")
            interstitialAdLoadStatus[ad_rewarded] = 1
            this@AdManager.rewardedAd = rewardedAd
            showLuckyCredits = true
            interstitialAdListener[ad_rewarded]?.onAdLoaded()
//            if(rewardedAd?.checkAdStatus()?.atTopAdInfo?.adsourceIndex == 0 || rewardedAd?.checkAdStatus()?.atTopAdInfo?.adsourceIndex == 1){
//                showLuckyCredits = true
//            }
//            rewardedAdListener.forEach {
//                it.onAdLoaded()
//            }
        }

        override fun onAdFailedToLoad(p0: LoadAdError) {
            super.onAdFailedToLoad(p0)
            Log.d(tag, "onRewardedAdFailedToLoad position=$position, ${p0.toString()}")
//            interstitialAdLoadStatus[ad_rewarded] = 2
            rewardedAd = null
            showLuckyCredits = false
            GlobalScope.launch {
                delay(5000)
                interstitialAdLoadStatus[ad_rewarded] = 2
                withContext(Dispatchers.Main) {
                    loadRewardedAd(context,position + 1)
                }
            }
        }
    }

    inner class MyAdListener(val context: Context,var category: String, var position: Int) : InterstitialAdLoadCallback() {
        override fun onAdFailedToLoad(p0: LoadAdError) {
            Log.d(tag, "onAdFailedToLoad-- $p0 $category")
            GlobalScope.launch {
                delay(200)
                interstitialAdLoadStatus[category] = 2
                withContext(Dispatchers.Main) {
                    loadInterstitialAd(context,category, position + 1)
                }
            }
        }

        override fun onAdLoaded(interstitialAd: InterstitialAd) {
            Log.d(tag, "onAdLoaded-- $category ${interstitialAdListener.size}")
            interstitialAdLoadStatus[category] = 1
            interstitialAdMap[category] = interstitialAd
            interstitialAdListener[category]?.onAdLoaded()

//            println("加载的广告位置是 ${interstitialAdMap[category]?.checkAdStatus()?.atTopAdInfo?.adsourceIndex} $category $showLuckyCredits")
//            if ( == 0) {
//
//            }
        }

//        override fun onInterstitialAdVideoEnd(p0: ATAdInfo?) {
//            TODO("Not yet implemented")
//        }

//        override fun onInterstitialAdShow(p0: ATAdInfo?) {
//            Log.d(tag, "onAdOpened-- $category")
//            if(category == ad_point && showLuckyCredits){
//                if(p0?.adsourceIndex == 0 || p0?.adsourceIndex == 1) {
//                    showLuckyCredits = false
//                }
//            }
//            interstitialAdListener.forEach {
//                it.onAdShow()
//            }
//        }

//        override fun onInterstitialAdVideoError(p0: com.anythink.core.api.AdError?) {
//            TODO("Not yet implemented")
//        }

//        @SuppressLint("CheckResult")
//        override fun onInterstitialAdClicked(p0: ATAdInfo?) {
//            val type = category.replace("topon_","").replace("page","")
//            val map = mutableMapOf<String, String>()
//            map["ver"] = App.context?.getVersionName() ?: ""
//            map["sip"] = UserManager.get().user?.sip ?: ""
//            map["type"] = type
//            map["update_time"] = System.currentTimeMillis().toString()
//            map["ts"] = System.currentTimeMillis().toString()
//            Api.getApiService().addClick(map)
//                .compose(RxUtils.applySchedulers())
//                .subscribe({
//                    if (it.isSuccessful) {
//                        DBHelper.get().addAdClickCount()
//                    }
//                }, {
//                    it.printStackTrace()
//                })
//        }

//        override fun onInterstitialAdVideoStart(p0: ATAdInfo?) {
//            TODO("Not yet implemented")
//        }

//        override fun onInterstitialAdClose(p0: ATAdInfo?) {
//            Log.d(tag, "onAdClosed-- $category ${interstitialAdListener.size}")
//            for (i in interstitialAdListener.indices) {
//                try {
//                    interstitialAdListener[i].onAdClose()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//
//            }
//            if (category == ad_splash) {
//                loadInterstitialAd(ad_splash)
//            }
//        }

    }

    private fun getNativeAdID(category: String):String{
        var adID = ""
        if(adData != null){
            for(i in adData!!.ads.indices){
                if(adData!!.ads[i].adPlaceID == category && adData!!.ads[i].adSources.isNotEmpty()){
                    adID = adData!!.ads[i].adSources[0].adPlaceID
                    break
                }
            }
        }
        return adID
    }

    inner class MyFullScreenContentCallback(val context: Context?, val category: String) :
        FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
//            for (i in interstitialAdListener.indices) {
//                try {
//                    interstitialAdListener[i].onAdClose()
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//
//            }
            interstitialAdLoadStatus[category] = 0
            interstitialAdMap[category] = null
            interstitialAdListener[category]?.onAdClose()
            if (category == ad_splash || category == ad_close) {
                loadInterstitialAd(context,category)
            }
        }

        override fun onAdShowedFullScreenContent() {
            LogUtils.println("onAdShowedFullScreenContent $category")
            interstitialAdListener[category]?.onAdShow()
        }
    }

    interface VCallAdListener {
        fun onAdClose()
        fun onAdShow()
        fun onAdLoaded()
        fun onAdLoadFail()
    }
}