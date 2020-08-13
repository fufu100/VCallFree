package vcall.free.international.phone.wifi.calling.utils

import android.os.Build
import android.util.Log
import vcall.free.international.phone.wifi.calling.BuildConfig
import java.lang.Exception

object LogUtils {
    private val LOG_ON = BuildConfig.DEBUG
    public val test = Build.SUPPORTED_ABIS.contains("x86")

    fun DebugLog(message: String){
        if(LOG_ON) {
            println(message)
        }
    }

    fun d(tag:String,message:String){
        if(LOG_ON) {
            Log.d(tag, message)
        }
    }

    fun e(tag:String,message:String){
        if(LOG_ON) {
            Log.d(tag, message)
        }
    }

    fun i(tag:String,message:String){
        if(LOG_ON) {
            Log.d(tag, message)
        }
    }

    fun e(exception: Exception){
        if(LOG_ON){
            exception.printStackTrace()
        }
    }
    fun e(exception: Throwable?){
        if(LOG_ON){
            exception?.printStackTrace()
        }
    }
}