package vcall.free.international.phone.wifi.calling.lib

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.newmotor.x5.db.DBHelper
import com.umeng.analytics.MobclickAgent
import com.umeng.commonsdk.UMConfigure
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import vcall.free.international.phone.wifi.calling.api.Country
import vcall.free.international.phone.wifi.calling.service.CallService
import vcall.free.international.phone.wifi.calling.utils.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*
//5ec6a4d1978eea0864b20201
val prefs: Prefs by lazy { App.prefs!! }
class App : Application(),Application.ActivityLifecycleCallbacks{
    var start = 0
    var stop = 0
    var isInBackgrounnd = false
    override fun onActivityPaused(activity: Activity?) {
        println("App onActivityPaused ${activity?.localClassName}")
    }

    override fun onActivityResumed(activity: Activity?) {
        println("App onActivityResumed ${activity!!::class.java.canonicalName} ")
        if(isInBackgrounnd){
//            toast("应用从后台回来了")
            isInBackgrounnd = false
            if(!activity!!::class.java.canonicalName!!.endsWith("CallActivity")) {
                Dispatcher.dispatch(applicationContext) {
                    action(CallService.ACTION_SHOW_AD)
                }.send()
            }
        }

    }

    override fun onActivityStarted(activity: Activity?) {
        println("App onActivityStarted ${activity!!::class.java.canonicalName}")
        println("App stop=$stop start=$start")
        start++
    }

    override fun onActivityDestroyed(activity: Activity?) {
        println("App onActivityDestroyed")
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
        println("App onActivitySaveInstanceState ${activity!!::class.java.canonicalName}")
    }

    override fun onActivityStopped(activity: Activity?) {
        println("App onActivityStopped ${activity!!::class.java.canonicalName}")
        stop++
        println("App stop=$stop start=$start")
        if(start == stop){
            isInBackgrounnd = true
//            toast("应用到后台了")
        }
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        println("App onActivityCreated ${activity!!::class.java.canonicalName}")
    }

    companion object{
        val requestMap = mutableMapOf<String,String>()
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
//        requestMap["re"] = applicationContext.readManifestKey("CHANNEL")

        requestMap["lang"] = Locale.getDefault().language
        requestMap["firstInstallTime"] = applicationContext.firstInstallTime()

//        UMConfigure.init(applicationContext, "5d5113e10cafb26763000494", "google-play", UMConfigure.DEVICE_TYPE_PHONE, null)
//        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL)

        registerActivityLifecycleCallbacks(this)

        CrashHandler.getInstance().apply {
            init(applicationContext)
        }
        UMConfigure.setLogEnabled(true)
        UMConfigure.init(
            this,
            "5ec6a4d1978eea0864b20201",
            "umeng",
            UMConfigure.DEVICE_TYPE_PHONE,
            null
        )
        MobclickAgent.setScenarioType(this, MobclickAgent.EScenarioType.E_UM_NORMAL)
        initData()
    }

    private fun initData() {
        GlobalScope.launch {
            val count = DBHelper.get().getCountryCount()
            println("count=$count")
            if (count == 0) {
                val inputStream = assets.open("dial_plan.txt")
                val br: BufferedReader = BufferedReader(InputStreamReader(inputStream))
                var line: String? = null

                do {
                    line = br.readLine()
                    if (line != null) {
                        val regex = Regex("(\\{|\\}|\\s|\")+")
                        line = line.replace(regex, "")
                        println("$line")
                        val array = line.split(",")
                        if (array.size > 4) {
                            val country =
                                Country(array[0], array[1], array[2], array[3].toInt(), array[4])
                            DBHelper.get().addCountry(country)
                        }
                    }
                } while (line != null)
            }
            val dest = appCacheDirectory + "flags.zip"
            val flagDirectory = File(appCacheDirectory + "flags")
            if(!flagDirectory.exists()){
                FileUtils.copyFromAssets(assets,"flags.zip",dest,false)
                UnZip.unzip(dest, appCacheDirectory)
                Log.d("MainActivity","国旗解压缩成功")
            }else{
                if((flagDirectory.listFiles()?.size ?: 0) < 224){
                    flagDirectory.delete()
                    FileUtils.copyFromAssets(assets,"flags.zip",dest,false)
                    UnZip.unzip(dest, appCacheDirectory)
                    Log.d("MainActivity","国旗有缺失，已重新解压缩")
                }
            }

        }

    }
}