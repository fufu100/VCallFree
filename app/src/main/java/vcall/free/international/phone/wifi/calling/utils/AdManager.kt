package vcall.free.international.phone.wifi.calling.utils

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.android.installreferrer.api.ReferrerDetails
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
class AdManager{
    companion object {
        val tag = "AdManager"
        val ad_splash = "voip_startpage"
        val ad_preclick = "voip_preclick"
        val ad_point = "voip_jifen"
        val ad_close = "voip_close"
        val ad_rewarded = "voip_rewardvideo"
        val ad_origin = "voip_ystc"
        private var instance: AdManager? = null
            get() {
                if (field == null) {
                    field = AdManager()
                }

                return field
            }
        @JvmStatic
        fun get(): AdManager{
            return instance!!
        }
    }
    var adData:AdResp? = null
    var interstitialAdMap:MutableMap<String,InterstitialAd> = mutableMapOf()
    var rewardedAd:RewardedAd? = null
    var referrer:String = ""

    var interstitialAdListener:MutableList<VCallAdListener> = mutableListOf()
    var rewardedAdListener:MutableList<VCallAdListener> = mutableListOf()
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
                        }catch (e:Exception){
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


    fun showSplashInterstitialAd(){
        LogUtils.println("$tag showSplashInterstitialAd-- ${interstitialAdMap[ad_splash]?.isLoaded}")
        if(interstitialAdMap[ad_splash]?.isLoaded == true){
            interstitialAdMap[ad_splash]?.show()
        }
    }

    fun showPreclickInterstitialAd(){
        LogUtils.println("$tag showPreclickInterstitialAd-- ${interstitialAdMap[ad_preclick]?.isLoaded}")
        if(interstitialAdMap[ad_preclick]?.isLoaded == true){
            interstitialAdMap[ad_preclick]?.show()
        }
    }

    fun showCloseInterstitialAd(){
        LogUtils.println("$tag showPreclickInterstitialAd-- ${interstitialAdMap[ad_close]?.isLoaded}")
        if(interstitialAdMap[ad_close]?.isLoaded == true){
            interstitialAdMap[ad_close]?.show()
        }
    }

    fun showPointInterstitialAd(){
        LogUtils.println("$tag showPointInterstitialAd-- ${interstitialAdMap[ad_point]?.isLoaded}")
        if(interstitialAdMap[ad_point]?.isLoaded == true){
            interstitialAdMap[ad_point]?.show()
        }
    }

    var rewardedAdCallback = object :RewardedAdCallback(){
        override fun onUserEarnedReward(p0: RewardItem) {
            Log.d(tag, "onUserEarnedReward $p0")
        }

        override fun onRewardedAdClosed() {
            Log.d(tag, "onRewardedAdClosed- ")
            rewardedAdListener.forEach {
                it.onAdClose()
            }
        }

        override fun onRewardedAdOpened() {
            Log.d(tag, "onRewardedAdOpened- ")
            rewardedAdListener.forEach {
                it.onAdShow()
            }
        }

        override fun onRewardedAdFailedToShow(p0: AdError?) {
            Log.d(tag, "onRewardedAdFailedToShow: $p0")
            rewardedAdListener.forEach {
                it.onAdClose()
            }
        }

    }

    fun loadRewardedAd(position: Int = 0){
        val count = DBHelper.get().getAdClickCount()
        println("$tag loadRewardedAd count=$count")
        if(count >= 10){
            return
        }
        val rewardedListener = MyRewardedListener(position)
        if(adData != null) {
            for (i in adData!!.ads.indices) {
                if(adData!!.ads[i].adPlaceID == ad_rewarded && position < adData!!.ads[i].adSources.size){
//                    if(rewardedAd == null){
                    rewardedAd = RewardedAd(App.context,adData!!.ads[i].adSources[position].adPlaceID)
                    rewardedAd?.loadAd(AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build(),rewardedListener)
//                    }else{
//                        LogUtils.println("AdManager loadRewardedAd ${rewardedAd?.isLoaded}")
//                       if (rewardedAd?.isLoaded == false) {
//                           rewardedAd?.loadAd(AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build(),rewardedListener)
//                        }
//                    }
                    break
                }
            }
        }
    }

    fun loadInterstitialAd(category: String,position: Int = 0){
        val count = DBHelper.get().getAdClickCount()
        LogUtils.println("loadInterstitialAd count=$count")
        if(count >= 10){
            return
        }
        val adListener = MyAdListener(category,position)
        if(adData != null){
            for(i in adData!!.ads.indices){
                LogUtils.println("$tag loadInterstitialAd $category $position $i")
                if(adData!!.ads[i].adPlaceID == category && position < adData!!.ads[i].adSources.size){
//                    if(interstitialAdMap[category] == null){
                    LogUtils.println("$tag loadInterstitialAd 加载广告")
                    interstitialAdMap[category] = InterstitialAd(App.context)
                    interstitialAdMap[category]?.adUnitId =  adData!!.ads[i].adSources[position].adPlaceID
                    interstitialAdMap[category]?.adListener = adListener
                    interstitialAdMap[category]?.loadAd(AdRequest.Builder().build())
//                    }else{
//                        if(interstitialAdMap[category]?.adUnitId != adData!!.ads[i].adSources[position].adPlaceID){
//                            LogUtils.println("$tag loadInterstitialAd adPlaceID不相等 重新load")
//                            interstitialAdMap[category] = InterstitialAd(App.context)
//                            interstitialAdMap[category]?.adUnitId =  adData!!.ads[i].adSources[position].adPlaceID
//                            interstitialAdMap[category]?.adListener = adListener
//                            interstitialAdMap[category]?.loadAd(AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build())
//                        }else if (interstitialAdMap[category]?.isLoaded == false) {
//                            LogUtils.println("$tag loadInterstitialAd 重新load")
//                            interstitialAdMap[category]?.loadAd(AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build())
//                        }
//                    }
                    break
                }
            }
        }
    }

    inner class MyRewardedListener(var position: Int = 0):RewardedAdLoadCallback(){
        override fun onRewardedAdLoaded() {
            Log.d(tag, "onRewardedAdLoaded: ")
            rewardedAdListener.forEach {
                it.onAdLoaded()
            }
        }

        override fun onRewardedAdFailedToLoad(p0: LoadAdError?) {
            Log.d(tag, "onRewardedAdFailedToLoad: $p0")
            rewardedAd = null
            GlobalScope.launch {
                delay(1000)
                withContext(Dispatchers.Main){
                    loadRewardedAd(position + 1)
                }
            }
        }
    }

    inner class MyAdListener(var category:String,var position:Int):AdListener(){
        override fun onAdLoaded() {
            Log.d(tag, "onAdLoaded-- $category")
            interstitialAdListener.forEach {
                it.onAdLoaded()
            }
        }

        override fun onAdOpened() {
            Log.d(tag, "onAdOpened-- $category")
            interstitialAdListener.forEach {
                it.onAdShow()
            }
        }

        override fun onAdClosed() {
            Log.d(tag, "onAdClosed-- $category ${interstitialAdListener.size}")
            for(i in interstitialAdListener.indices){
                try {
                    interstitialAdListener[i].onAdClose()
                }catch (e:Exception){
                    e.printStackTrace()
                }

            }
            if(category == ad_splash){
                loadInterstitialAd(ad_splash)
            }
        }

        @SuppressLint("CheckResult")
        override fun onAdClicked() {
            val map = mutableMapOf<String,String>()
            map["ver"] = App.context?.getVersionName()?:""
            map["sip"] = UserManager.get().user?.sip?:""
            map["type"] = category
            map["update_time"] = System.currentTimeMillis().toString()
            map["ts"] = System.currentTimeMillis().toString()
            Api.getApiService().addClick(map)
                .compose(RxUtils.applySchedulers())
                .subscribe({
                    if(it.isSuccessful){
                        DBHelper.get().addAdClickCount()
                    }
                },{
                    it.printStackTrace()
                })
        }

        override fun onAdFailedToLoad(p0: LoadAdError?) {
            Log.d(tag, "onAdFailedToLoad-- $p0 $category")
            GlobalScope.launch {
                delay(1000)
                withContext(Dispatchers.Main){
                    loadInterstitialAd(category,position + 1)
                }
            }
        }

    }

    interface VCallAdListener{
        fun onAdClose()
        fun onAdShow()
        fun onAdLoaded()
    }
}