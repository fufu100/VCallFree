package vcall.free.international.phone.wifi.calling.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
import com.anythink.core.api.ATAdInfo
import com.anythink.interstitial.api.ATInterstitial
import com.anythink.interstitial.api.ATInterstitialListener
import com.anythink.rewardvideo.api.ATRewardVideoAd
import com.anythink.rewardvideo.api.ATRewardVideoListener
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.newmotor.x5.db.DBHelper
import kotlinx.coroutines.*
import vcall.free.international.phone.wifi.calling.api.AdResp
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.lib.App
import vcall.free.international.phone.wifi.calling.lib.prefs

/**
 * Created by lyf on 2020/8/14.
 */
class AdManager {
    companion object {
        val tag = "anythink AdManager"
        val ad_splash = "topon_startpage"
        val ad_preclick = "topon_preclick"
        val ad_point = "topon_jifen"
        val ad_close = "topon_close"
        val ad_rewarded = "topon_rewardvideo"
        val ad_quite = "topon_ystc"
        val ad_call_result = "topon_ysclose"
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

    var adData: AdResp? = null
    var interstitialAdMap: MutableMap<String, ATInterstitial> = mutableMapOf()
    var rewardedAd: ATRewardVideoAd? = null
    var referrer: String = ""
    var showLuckyCredits = false
    var interstitialAdLoadStatus :MutableMap<String,Int> = mutableMapOf()

    var interstitialAdListener: MutableList<VCallAdListener> = mutableListOf()
    var rewardedAdListener: MutableList<VCallAdListener> = mutableListOf()
    private var referrerClient: InstallReferrerClient =
        InstallReferrerClient.newBuilder(App.context).build()

    init {
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


    fun showSplashInterstitialAd(activity: Activity?) {
        LogUtils.println("$tag showSplashInterstitialAd-- ${interstitialAdMap[ad_splash]?.isAdReady}")
        if (interstitialAdMap[ad_splash]?.isAdReady == true) {
            interstitialAdMap[ad_splash]?.show(activity)
        }
    }

    fun showPreclickInterstitialAd(activity: Activity?) {
        LogUtils.println("$tag showPreclickInterstitialAd-- ${interstitialAdMap[ad_preclick]?.isAdReady}")
        if (interstitialAdMap[ad_preclick]?.isAdReady == true) {
            interstitialAdMap[ad_preclick]?.show(activity)
        }
    }

    fun showCloseInterstitialAd(activity: Activity?) {
        LogUtils.println("$tag showPreclickInterstitialAd-- ${interstitialAdMap[ad_close]?.isAdReady}")
        if (interstitialAdMap[ad_close]?.isAdReady == true) {
            interstitialAdMap[ad_close]?.show(activity)
        }
    }

    fun showPointInterstitialAd(activity: Activity?) {
        LogUtils.println("$tag showPointInterstitialAd-- ${interstitialAdMap[ad_point]?.isAdReady}")
        if (interstitialAdMap[ad_point]?.isAdReady == true) {
            interstitialAdMap[ad_point]?.show(activity)
        }
    }

    fun loadRewardedAd(position: Int = 0) {
        val count = DBHelper.get().getAdClickCount()
        println("$tag loadRewardedAd count=$count")
        if (count >= 10) {
            return
        }
        val rewardedListener = MyRewardedListener(position)
        if (adData != null) {
            for (i in adData!!.ads.indices) {
                if (adData!!.ads[i].adPlaceID == ad_rewarded && position < adData!!.ads[i].adSources.size) {
                    rewardedAd =
                        ATRewardVideoAd(App.context, adData!!.ads[i].adSources[position].adPlaceID)
                    rewardedAd?.setAdListener(rewardedListener)
                    rewardedAd?.load()
                    interstitialAdLoadStatus[ad_rewarded] = 0
                    break
                }
            }
        }
    }

    fun loadInterstitialAd(category: String, position: Int = 0) {
        val count = DBHelper.get().getAdClickCount()
        LogUtils.println("loadInterstitialAd count=$count $category $position")
        if (count >= 10) {
            return
        }
        val adListener = MyAdListener(category, position)
        if (adData != null) {
            for (i in adData!!.ads.indices) {
                LogUtils.println("$tag loadInterstitialAd ${adData!!.ads[i].adPlaceID} $category $position $i")
                if (adData!!.ads[i].adPlaceID == category  ) {
                    if(position < adData!!.ads[i].adSources.size) {
                        LogUtils.println("$tag loadInterstitialAd 加载广告 $category $position $i")
                        interstitialAdMap[category] =
                            ATInterstitial(
                                App.context,
                                adData!!.ads[i].adSources[position].adPlaceID
                            )
                        interstitialAdMap[category]?.setAdListener(adListener)
                        interstitialAdMap[category]?.load()
                        interstitialAdLoadStatus[category] = 0
                    }else{
                        interstitialAdListener.forEach {
                            it.onAdLoadFail()
                        }
                    }
                    break
                }
            }
        }
    }

    inner class MyRewardedListener(var position: Int = 0) : ATRewardVideoListener {
        override fun onRewardedVideoAdClosed(p0: ATAdInfo?) {

        }

        override fun onReward(p0: ATAdInfo?) {

        }

        override fun onRewardedVideoAdPlayFailed(
            p0: com.anythink.core.api.AdError?,
            p1: ATAdInfo?
        ) {
            println("$tag onRewardedVideoAdPlayFailed----")
            p0?.printStackTrace()
        }

        override fun onRewardedVideoAdLoaded() {
            Log.d(tag, "onRewardedAdLoaded adsourceIndex: ${rewardedAd?.checkAdStatus()?.atTopAdInfo?.adsourceIndex}")
            interstitialAdLoadStatus[ad_rewarded] = 1
            if(rewardedAd?.checkAdStatus()?.atTopAdInfo?.adsourceIndex == 0 || rewardedAd?.checkAdStatus()?.atTopAdInfo?.adsourceIndex == 1){
                showLuckyCredits = true
            }
            rewardedAdListener.forEach {
                it.onAdLoaded()
            }
        }

        override fun onRewardedVideoAdPlayStart(p0: ATAdInfo?) {

        }

        override fun onRewardedVideoAdFailed(p0: com.anythink.core.api.AdError?) {
            Log.d(tag, "onRewardedAdFailedToLoad: $p0")
            interstitialAdLoadStatus[ad_rewarded] = 2
            rewardedAd = null
            GlobalScope.launch {
                delay(10000)
                withContext(Dispatchers.Main) {
                    loadRewardedAd(position + 1)
                }
            }
        }

        override fun onRewardedVideoAdPlayEnd(p0: ATAdInfo?) {

        }

        override fun onRewardedVideoAdPlayClicked(p0: ATAdInfo?) {

        }
    }

    inner class MyAdListener(var category: String, var position: Int) : ATInterstitialListener {
        override fun onInterstitialAdLoadFail(p0: com.anythink.core.api.AdError?) {
            Log.d(tag, "onAdFailedToLoad-- $p0 $category")
            interstitialAdLoadStatus[category] = 2
            GlobalScope.launch {
                delay(10000)
                withContext(Dispatchers.Main) {
                    loadInterstitialAd(category, position + 1)
                }
            }
        }

        override fun onInterstitialAdLoaded() {
            Log.d(tag, "onAdLoaded-- $category ${interstitialAdListener.size}")
            interstitialAdLoadStatus[category] = 1
//            if(category == ad_point){
//                if(interstitialAdMap[category]?.checkAdStatus()?.atTopAdInfo?.adsourceIndex == 0 || interstitialAdMap[category]?.checkAdStatus()?.atTopAdInfo?.adsourceIndex == 1){
//                    showLuckyCredits = true
//                }
//            }
            interstitialAdListener.forEach {
                it.onAdLoaded()
            }

            println("加载的广告位置是 ${interstitialAdMap[category]?.checkAdStatus()?.atTopAdInfo?.adsourceIndex} $category $showLuckyCredits")
//            if ( == 0) {
//
//            }
        }

        override fun onInterstitialAdVideoEnd(p0: ATAdInfo?) {
            TODO("Not yet implemented")
        }

        override fun onInterstitialAdShow(p0: ATAdInfo?) {
            Log.d(tag, "onAdOpened-- $category")
            if(category == ad_point && showLuckyCredits){
                if(p0?.adsourceIndex == 0 || p0?.adsourceIndex == 1) {
                    showLuckyCredits = false
                }
            }
            interstitialAdListener.forEach {
                it.onAdShow()
            }
        }

        override fun onInterstitialAdVideoError(p0: com.anythink.core.api.AdError?) {
            TODO("Not yet implemented")
        }

        @SuppressLint("CheckResult")
        override fun onInterstitialAdClicked(p0: ATAdInfo?) {
            val type = category.replace("topon_","").replace("page","")
            val map = mutableMapOf<String, String>()
            map["ver"] = App.context?.getVersionName() ?: ""
            map["sip"] = UserManager.get().user?.sip ?: ""
            map["type"] = type
            map["update_time"] = System.currentTimeMillis().toString()
            map["ts"] = System.currentTimeMillis().toString()
            Api.getApiService().addClick(map)
                .compose(RxUtils.applySchedulers())
                .subscribe({
                    if (it.isSuccessful) {
                        DBHelper.get().addAdClickCount()
                    }
                }, {
                    it.printStackTrace()
                })
        }

        override fun onInterstitialAdVideoStart(p0: ATAdInfo?) {
            TODO("Not yet implemented")
        }

        override fun onInterstitialAdClose(p0: ATAdInfo?) {
            Log.d(tag, "onAdClosed-- $category ${interstitialAdListener.size}")
            for (i in interstitialAdListener.indices) {
                try {
                    interstitialAdListener[i].onAdClose()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            if (category == ad_splash) {
                loadInterstitialAd(ad_splash)
            }
        }

    }

    interface VCallAdListener {
        fun onAdClose()
        fun onAdShow()
        fun onAdLoaded()
        fun onAdLoadFail()
    }
}