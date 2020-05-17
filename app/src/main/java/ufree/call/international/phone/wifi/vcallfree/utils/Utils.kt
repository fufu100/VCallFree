package ufree.call.international.phone.wifi.vcallfree.utils

import android.Manifest
import android.app.usage.UsageStatsManager
import android.content.Context
import com.google.gson.Gson
import org.json.JSONObject
import android.app.ActivityManager
import android.content.ContentUris
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.translate.english.voice.lib.App
import kotlinx.android.synthetic.main.fragment_tab_contracts.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.api.Contact
import ufree.call.international.phone.wifi.vcallfree.widget.CircleTransform
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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


