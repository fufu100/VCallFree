package vcall.free.international.phone.wifi.calling.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.newmotor.x5.db.DBHelper
import vcall.free.international.phone.wifi.calling.MainActivity
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.utils.UserManager

/**
 * Created by lyf on 2020/8/26.
 */
class DaemonService: Service() {
    private lateinit var receiver: BroadcastReceiver
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        println("DaemonService onCreate---")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                "default", "VCallFree",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this,"default")
        builder.setSmallIcon(R.mipmap.ic_notification_logo)
        builder.setLargeIcon(BitmapFactory.decodeResource(resources,R.mipmap.ic_launcher))
        builder.setContentTitle(resources.getString(R.string.app_name))
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        builder.setContentIntent(PendingIntent.getActivity(this,0,intent,0))
        builder.setContentTitle("You can get some lucky credits today!")
        builder.setContentText(getContent())
        builder.setOnlyAlertOnce(true)
        startForeground(100,builder.build())

        receiver = object :BroadcastReceiver(){
            override fun onReceive(context: Context, intent: Intent) {
                println("DaemonService onReceive ${intent.action}")
                if(intent.action == "refresh_notification"){
                    builder.setContentText(getContent())
                    startForeground(100,builder.build())

                }
            }

        }
        registerReceiver(receiver, IntentFilter("refresh_notification"))
    }

    private fun getContent():String{
        println("getContent : ${UserManager.get().user}")
        return String.format("Today's credits:%d,Wheel count:%d/%d",DBHelper.get().getTodayCredits(),DBHelper.get().getPlayCount(),UserManager.get().user?.max_wheel)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
}