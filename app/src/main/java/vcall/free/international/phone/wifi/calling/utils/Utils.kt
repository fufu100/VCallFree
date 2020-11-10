package vcall.free.international.phone.wifi.calling.utils

import android.app.usage.UsageStatsManager
import android.content.Context
import android.app.ActivityManager
import android.content.ContentUris
import android.content.res.ColorStateList
import android.net.Uri
import android.provider.ContactsContract
import android.util.Base64
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import vcall.free.international.phone.wifi.calling.lib.App
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.api.Contact
import vcall.free.international.phone.wifi.calling.lib.encryptedPrefs
import vcall.free.international.phone.wifi.calling.widget.CircleTransform
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@BindingAdapter("url")
fun loadImage(imageView: ImageView, url: String?) {
    Glide.with(imageView.context).load(url)
        .placeholder(R.drawable.place_holder)
        .into(imageView)
}

@BindingAdapter("url_square")
fun loadSquareImage(imageView: ImageView, url: String?) {
    Glide.with(imageView.context).load(url)
        .placeholder(R.drawable.place_holder)
        .into(imageView)
}

@BindingAdapter("url_circle")
fun loadCircleImage(imageView: ImageView, url: String?) {
    Glide.with(imageView.context)
        .load(url)
        .bitmapTransform(CircleTransform(imageView.context))
        .into(imageView)
}

@BindingAdapter("contact_photo")
fun loadContactPhoto(imageView: ImageView,contact:Contact){
    if(contact.photoId != 0L){
        GlobalScope.launch(Dispatchers.IO) {
            val contentUri: Uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,contact.contractId)
            val photoUri: Uri = Uri.withAppendedPath(contentUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY)
            val cursor = imageView.context.contentResolver.query(photoUri,
                arrayOf(ContactsContract.Contacts.Photo.PHOTO),null,null,null)
            while (cursor?.moveToNext() == true){
                withContext(Dispatchers.Main){
                    Glide.with(imageView.context).load(cursor.getBlob(0)).bitmapTransform(CircleTransform(imageView.context)).into(imageView)
                }
            }
            cursor?.close()
        }
    }else{
        imageView.setImageResource(R.drawable.ic_avatar_default)
    }
}

@BindingAdapter("flag")
fun loadFlag(imageView: ImageView,iso:String?){
    if(iso != null && iso.isNotEmpty()){
        GlobalScope.launch (Dispatchers.IO){
            val file = File(App.appCacheDirectory + "flags/" + iso + ".png")
            withContext(Dispatchers.Main){
                Glide.with(imageView.context).load(file).into(imageView)
            }
        }
    }else{
        imageView.setImageResource(R.drawable.bg_setting_item)
    }
}

@BindingAdapter("flag_circle")
fun loadFlagCircle(imageView: ImageView,iso:String?){
    if(iso != null && iso.isNotEmpty()){
        GlobalScope.launch (Dispatchers.IO){
            val file = File(App.appCacheDirectory + "flags/" + iso + ".png")
            withContext(Dispatchers.Main){
                Glide.with(imageView.context).load(file).bitmapTransform(CircleTransform(imageView.context)).into(imageView)
            }
        }
    }else{
        imageView.setImageResource(R.drawable.bg_setting_item)
    }
}

@BindingAdapter("state")
fun loadCallState(imageView: ImageView,state:Int){
    if(state == 1){
        imageView.setImageResource(R.drawable.ic_call_connect_succ)
    }else if(state == 2){
        imageView.setImageResource(R.drawable.ic_call_connect_fail)
    }
}

fun getTopApp(context: Context): String {
    val usageStatesManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val now = System.currentTimeMillis()
    val stats =
        usageStatesManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - 3600 * 1000, now)
    var i = 0
    var top = ""
    for ((index, stat) in stats.withIndex()) {
        println(stat.packageName)
        if (stat.lastTimeUsed > stats[i].lastTimeUsed) {
            i = index
        }
        top = stats[i].packageName
    }
    println("top=$top,size=${stats.size}")
    return top
}

fun isAPPALive(mContext: Context, packageName: String): Boolean {
    var isAPPRunning = false
    // 获取activity管理对象
    val activityManager = mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    // 获取所有正在运行的app
    val appProcessInfoList = activityManager.runningAppProcesses
    // 遍历，进程名即包名
    for (appInfo in appProcessInfoList) {
        if (packageName == appInfo.processName) {
            isAPPRunning = true
            break
        }
    }
    return isAPPRunning
}

fun getStateListColor(color: IntArray): ColorStateList {
    return ColorStateList(Array(2) { IntArray(1) }.apply {
        this[0][0] = android.R.attr.state_selected
    }, color)
}

fun getStateListColor2(color: IntArray): ColorStateList {
    return ColorStateList(Array(2) { IntArray(1) }.apply {
        this[0][0] = android.R.attr.state_checked
    }, color)
}

fun formatTime(duration:Long):String{
    return SimpleDateFormat("MM-dd HH:mm",Locale.getDefault()).format(duration)
}

fun desEncrypt(data:String):String{
    try {
        val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
        val paramSpec = IvParameterSpec("87493871".toByteArray())
        val secretKeySpec = SecretKeySpec("86101100".toByteArray(),"DES")
        cipher.init(Cipher.ENCRYPT_MODE,secretKeySpec,paramSpec)
        return URLEncoder.encode(Base64.encodeToString(cipher.doFinal(data.toByteArray()),Base64.DEFAULT))
    }catch (e:Exception){
        e.printStackTrace()
        return ""
    }
}

fun desDecrypt(data:String):String{
    try {
        val cipher = Cipher.getInstance("DES/CBC/PKCS5Padding")
        val paramSpec = IvParameterSpec(encryptedPrefs.getKey("iv").toByteArray())
        val secretKeySpec = SecretKeySpec(encryptedPrefs.getKey("key").toByteArray(),"DES")
        cipher.init(Cipher.DECRYPT_MODE,secretKeySpec,paramSpec)
        return String(cipher.doFinal(Base64.decode(URLDecoder.decode(data),Base64.DEFAULT)))
    }catch (e:Exception){
        e.printStackTrace()
        return ""
    }
}





