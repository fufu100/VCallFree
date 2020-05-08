package com.translate.english.voice.lib

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import ufree.call.international.phone.wifi.vcallfree.utils.Prefs
import ufree.call.international.phone.wifi.vcallfree.utils.firstInstallTime
import ufree.call.international.phone.wifi.vcallfree.utils.getVersionName
import ufree.call.international.phone.wifi.vcallfree.utils.readManifestKey
import ufree.call.international.phone.wifi.vcallfree.MainActivity
import ufree.call.international.phone.wifi.vcallfree.utils.CrashHandler
import java.io.File
import java.util.*

val prefs: Prefs by lazy { App.prefs!! }
class App : Application(),Application.ActivityLifecycleCallbacks{
    var hasResume = false
    var hasPause = false
    override fun onActivityPaused(activity: Activity?) {
        println("App onActivityPaused")
        hasPause = activity is MainActivity
    }

    override fun onActivityResumed(activity: Activity?) {
        println("App onActivityResumed")
        hasResume = activity is MainActivity
        if(hasResume and hasPause){
//            toast("应用从后台回来了")
//            ActivityUtils.from(applicationContext)
//                .action(TranslateService.ACTION_SHOW_AD)
//                .send()
        }

    }

    override fun onActivityStarted(activity: Activity?) {
        println("App onActivityStarted")

    }

    override fun onActivityDestroyed(activity: Activity?) {
        println("App onActivityDestroyed")
        hasResume = false
        hasPause = false
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
        println("App onActivitySaveInstanceState")
    }

    override fun onActivityStopped(activity: Activity?) {
        println("App onActivityStopped")
//        if(Utils.getTopApp(applicationContext) != packageName){
//            toast("应用到后台了")
//        }
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        println("App onActivityCreated")
        hasResume = false
        hasPause = false
    }

    companion object{
        val requestMap = mutableMapOf<String,String?>()
        var appCacheDirectory:String = ""
        @SuppressLint("StaticFieldLeak")
        var context: Context? = null
        var prefs: Prefs? = null
    }
    override fun onCreate() {
        super.onCreate()

        val cacheFile = applicationContext.externalCacheDir
        if (!cacheFile?.exists()!!) {
            cacheFile.mkdirs()
        }
        appCacheDirectory = cacheFile.path + File.separator
        println("app OnCreate $appCacheDirectory, ${applicationContext.getExternalFilesDir(null)}")
        context = applicationContext
        prefs = Prefs(applicationContext)
        requestMap["ver"] = applicationContext.getVersionName()
        requestMap["pk"] = applicationContext.packageName
        requestMap["re"] = applicationContext.readManifestKey("CHANNEL")
        requestMap["lang"] = Locale.getDefault().language
        requestMap["fis"] = applicationContext.firstInstallTime()
        requestMap["m"] = "json"

//        UMConfigure.init(applicationContext, "5d5113e10cafb26763000494", "google-play", UMConfigure.DEVICE_TYPE_PHONE, null)
//        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL)

        registerActivityLifecycleCallbacks(this)

//        CrashHandler.getInstance().apply {
//            init(applicationContext)
//        }
    }
}