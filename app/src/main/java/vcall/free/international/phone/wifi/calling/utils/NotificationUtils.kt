package vcall.free.international.phone.wifi.calling.utils

import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AlertDialog
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.lib.App
import java.lang.reflect.Field

/**
 * Created by lyf on 2020/8/26.
 */
class NotificationUtils {
    companion object{
        const val NOTICE_ID = 100
        private var instance: NotificationUtils? = null
            get() {
                if (field == null) {
                    field = NotificationUtils()
                }

                return field
            }
        @JvmStatic
        fun get(): NotificationUtils{
            return instance!!
        }
    }
    private val notificationManager: NotificationManager = App.context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    fun areNotificationsEnable():Boolean{
        if(Build.VERSION.SDK_INT >= 24){
            return notificationManager.areNotificationsEnabled()
        }else{
            try {
                val appOpsManager = App.context!!.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val uid = App.context!!.applicationInfo.uid
                val appOpsClass = Class.forName(AppOpsManager::class.java.name)
                val checkOpNoThrowMethod = appOpsClass.getMethod("checkOpNoThrow",Integer.TYPE,Integer.TYPE,String.javaClass)
                val opPostNotificationValue: Field = appOpsClass.getDeclaredField("OP_POST_NOTIFICATION")
                val value = opPostNotificationValue.get(Int::class.java) as Int
                return (checkOpNoThrowMethod.invoke(appOpsManager,value,uid,App.context!!.packageName) as Int) == AppOpsManager.MODE_ALLOWED
            }catch (e:Exception){
                e.printStackTrace()
            }
            return false
        }
    }

    fun remindOpenNotification(context: Context){
        AlertDialog.Builder(context)
            .setMessage(R.string.tip_open_notification)
            .setPositiveButton(R.string.confirm){_,_ ->
                Dispatcher.dispatch(context){
                    flag(Intent.FLAG_ACTIVITY_NEW_TASK)
                    action("android.settings.APPLICATION_DETAILS_SETTINGS")
                    data(Uri.fromParts("package",context.packageName,null))
                }.go()
            }
            .setNegativeButton(R.string.cancel,null)
            .create()
            .show()
    }
}