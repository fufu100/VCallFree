package vcall.free.international.phone.wifi.calling.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.snackbar.Snackbar
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.lib.prefs
import java.util.*

fun Context.getVersionName():String{
    try {
        return this.packageManager.getPackageInfo(packageName,0).versionName
    }catch (e:Exception){
        e.printStackTrace()
        return ""
    }
}
fun Context.getVersionCode():Int{
    try {
        return this.packageManager.getPackageInfo(packageName,0).versionCode
    }catch (e:Exception){
        e.printStackTrace()
        return 0
    }
}

fun Context.readManifestKey(key:String):String?{
    val appinfo = this.packageManager.getApplicationInfo(
            this.packageName,
            PackageManager.GET_META_DATA)
    return appinfo.metaData.getString(key)
}

fun Context.firstInstallTime():String{
    try {
        return (this.packageManager.getPackageInfo(packageName,0).firstInstallTime / 1000).toString()
    }catch (e:Exception){
        e.printStackTrace()
        return ""
    }
}

fun Context.screenWidth(): Int {
    val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val metric = DisplayMetrics()
    manager.defaultDisplay.getMetrics(metric)
    return metric.widthPixels
}

fun Context.screenHeight(): Int {
    val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val metric = DisplayMetrics()
    manager.defaultDisplay.getMetrics(metric)
    return metric.heightPixels
}

fun Context.screenDensity(): Float {
    val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val metric = DisplayMetrics()
    manager.defaultDisplay.getMetrics(metric)
    return metric.density
}


fun Context.dip2px(px:Int):Int{
    val scale = resources.displayMetrics.density
    return (px * scale + 0.5).toInt()
}

fun Activity.isNotDestroy():Boolean{
    return !this.isFinishing && !this.isDestroyed
}

fun Context.isNotDestroy():Boolean{
    return (this is Activity && this.isNotDestroy()) || this !is Activity
}

fun Context.checkIfCanDrawOverlay():Boolean{
    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
        return Settings.canDrawOverlays(this)
    }else{
        return true
    }
}

fun Drawable.tint(colors: ColorStateList): Drawable {
    val wrappedDrawable = DrawableCompat.wrap(this.mutate())
    DrawableCompat.setTintList(wrappedDrawable, colors)
    return wrappedDrawable
}

fun Drawable.tint(colors: Int): Drawable {
    val wrappedDrawable = DrawableCompat.wrap(this.mutate())
    DrawableCompat.setTint(wrappedDrawable, colors)
    return wrappedDrawable
}

fun Context.getStateListDrawable(drawableRes: Int, color: Int): StateListDrawable {
    val drawable = StateListDrawable()
    val normal = resources.getDrawable(drawableRes, theme)
    val select = resources.getDrawable(drawableRes, theme).mutate().tint(color)
    drawable.addState(intArrayOf(android.R.attr.state_pressed), select)
    drawable.addState(intArrayOf(android.R.attr.state_checked), select)
    drawable.addState(intArrayOf(), normal)
    return drawable
}

fun Context.getColorFromRes(colorRes: Int): Int {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return resources.getColor(colorRes)
    } else {
        return getColor(colorRes)
    }
}

fun Context.dispatcher(): Dispatcher {
    return Dispatcher(this)
}


fun Context.toast(message:String){
    Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
}

fun Context.toast(message:Int){
    Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
}

fun Activity.snackBar(message: String){
    if(!isFinishing) {
        Snackbar.make(window.decorView.findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }
}

fun Activity.snackBar(message: Int){
    if(!isFinishing) {
        Snackbar.make(window.decorView.findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }
}

fun Context.getSimCountryIso():String{
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
            val telephoneManager: TelephonyManager =
                getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager;
            return telephoneManager.simCountryIso
        }
    }
    return ""
}

fun Context.getIMEI():String{
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
            try {
                val telephoneManager: TelephonyManager =
                    getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager;
                return telephoneManager.getDeviceId(0)
            }catch (e:Exception){
                e.printStackTrace()
            }

        }
    }else{
        val telephoneManager: TelephonyManager =
            getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager;
        return telephoneManager.getDeviceId(0)
    }
    return ""
}

fun Context.getSimSerialNumber():String{
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
            try {
                val telephoneManager: TelephonyManager =
                    getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager;
                return telephoneManager.simSerialNumber
            }catch (e:Exception){
                e.printStackTrace()
            }

        }
    }else{
        val telephoneManager: TelephonyManager =
            getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager;
        return telephoneManager.simSerialNumber
    }
    return ""
}

fun Context.getAndroidID():String{
    return Settings.System.getString(contentResolver, Settings.System.ANDROID_ID)
}

fun Context.getFirstInstallTime():Long{
    val packageInfo = packageManager.getPackageInfo(this.packageName,0)
    return packageInfo.firstInstallTime
}

fun Context.getSerial():String{
//    var serial:String? = null
    val m_szDevIDShort = "35" +
            Build.BOARD.length % 10 + Build.BRAND.length % 10 +

            Build.CPU_ABI.length % 10 + Build.DEVICE.length % 10 +

            Build.DISPLAY.length % 10 + Build.HOST.length % 10 +

            Build.ID.length % 10 + Build.MANUFACTURER.length % 10 +

            Build.MODEL.length % 10 + Build.PRODUCT.length % 10 +

            Build.TAGS.length % 10 + Build.TYPE.length % 10 +

            Build.USER.length % 10; //13 位

//    try {
//            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//                if(ContextCompat.checkSelfPermission(this,
//                        Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
//                    serial = android.os.Build.getSerial();
//                }
//            }
//            serial = Build.SERIAL
////        return UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
//    } catch (exception: Exception) {
//        serial = "serial"
//    }

    return m_szDevIDShort
}

fun Context.getDeviceId():String{
    var savedDeviceId = prefs.getStringValue("device_id","")
    if(savedDeviceId.isEmpty()) {
        savedDeviceId = UUID(getAndroidID().hashCode().toLong(), getSerial().hashCode().toLong()).toString()
        prefs.save("device_id",savedDeviceId)
    }
    return savedDeviceId
}

fun Context.hasPermission(permission:String):Boolean{
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
        ContextCompat.checkSelfPermission(this,permission) == PackageManager.PERMISSION_DENIED
    }else{
        true
    }
}

fun Activity.closeKeyBoard(){
    val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
}

fun Context.isNetworkConnected():Boolean{
    try {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.state == NetworkInfo.State.CONNECTED
    }catch (e:Exception){
        e.printStackTrace()
        return false
    }
}

fun Context.hasSim():Boolean{
    if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simState = telephonyManager.simState
        println("hasSim simState=$simState")
        return !(simState == TelephonyManager.SIM_STATE_ABSENT || simState == TelephonyManager.SIM_STATE_UNKNOWN)
    }else{
        return false
    }
}

fun Context.getCountry():String?{
    if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val simState = telephonyManager.simState
        println("getCountry simState=$simState")
        if(simState == TelephonyManager.SIM_STATE_ABSENT || simState == TelephonyManager.SIM_STATE_UNKNOWN){
            return null
        }else{
            println("getCountry simCountryIso=${telephonyManager.simCountryIso} ${telephonyManager.networkCountryIso}")
            return telephonyManager.simCountryIso.toUpperCase(Locale.ENGLISH)
//            return null
        }
    }else {
        return null
    }
}

