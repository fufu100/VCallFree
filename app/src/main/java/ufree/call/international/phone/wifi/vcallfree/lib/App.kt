package ufree.call.international.phone.wifi.vcallfree.lib

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.newmotor.x5.db.DBHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ufree.call.international.phone.wifi.vcallfree.MainActivity
import ufree.call.international.phone.wifi.vcallfree.api.Country
import ufree.call.international.phone.wifi.vcallfree.utils.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*
//5ec6a4d1978eea0864b20201
val prefs: Prefs by lazy { App.prefs!! }
class App : Application(),Application.ActivityLifecycleCallbacks{
    var hasResume = false
    var hasPause = false
    override fun onActivityPaused(activity: Activity?) {
        println("App onActivityPaused ${activity?.localClassName}")
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

        CrashHandler.getInstance().apply {
            init(applicationContext)
        }
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