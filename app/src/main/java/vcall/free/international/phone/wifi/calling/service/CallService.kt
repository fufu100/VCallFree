package vcall.free.international.phone.wifi.calling.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import vcall.free.international.phone.wifi.calling.db.DBHelper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import vcall.free.international.phone.wifi.calling.lib.App
import org.pjsip.pjsua2.*
import vcall.free.international.phone.wifi.calling.MainActivity
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.pjsua.*
import vcall.free.international.phone.wifi.calling.ui.SplashActivity
import vcall.free.international.phone.wifi.calling.utils.*
import java.util.*

/**
 * Created by lyf on 2020/5/9.
 */
class CallService:Service(),MyAppObserver{
    var app:MyApp? = null
    var currentCall:MyCall? = null
    var accout:MyAccount? = null
    var accCfg:AccountConfig? = null
    var receiver:MyBroadcastReceiver? = null
    private val lastRegStatus = ""
    private var showAdOnLoad = true
    private var regStatus = -1
    private lateinit var connectivityManager:ConnectivityManager
//    private lateinit var mInterstitialAd: InterstitialAd
    val callStateChangeListeners:MutableList<CallStateChange> = mutableListOf()
    var regStateChangeListener:RegStateChange? = null
    var ipCountry:String? = null
    var getIpStatus = -1
    var onGetIpInfo:OnGetIpInfo? = null
    var registerStartTime = 0L
    override fun onBind(intent: Intent?): IBinder? {
        return CallBinder()
    }


    companion object{
        const val TAG = "CallService"
        const val INCOMING_CALL = 1
        const val CALL_STATE = 2
        const val REG_STATE = 3
        const val BUDDY_STATE = 4
        const val CALL_MEDIA_STATE = 5
        const val CHANGE_NETWORK = 6
        const val ACTION_SHOW_AD = "action_show_ad"
        const val ACTION_ON_AD_SHOW = "action_on_ad_show"
        const val ACTION_ON_AD_CLOSE = "action_on_ad_close"
        const val ACTION_ON_AD_LOAD_FAIL = "action_on_ad_load_fail"
    }

    override fun onCreate() {
        super.onCreate()
        receiver = MyBroadcastReceiver()
        val intentFilter = IntentFilter(
            ConnectivityManager.CONNECTIVITY_ACTION
        )
        intentFilter.addAction(ACTION_SHOW_AD)
        intentFilter.addAction("refresh_notification")
        intentFilter.addAction("stop")
        registerReceiver(receiver, intentFilter)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy---- ")
        if (receiver != null) {
            unregisterReceiver(receiver)
        }
        app?.deinit()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
    var builder:NotificationCompat.Builder? = null
    fun createNotification(){
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                "default", "VCallFree",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        builder = NotificationCompat.Builder(this,"default")
        builder?.setSmallIcon(R.mipmap.ic_notification_logo)
        builder?.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
        builder?.setContentTitle(resources.getString(R.string.app_name))
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        builder?.setContentIntent(PendingIntent.getActivity(this,0,intent,0))
        builder?.setContentTitle("You can get some lucky credits today!")
        builder?.setContentText(getContent())
        builder?.setOnlyAlertOnce(true)
        manager.notify(100,builder?.build())
    }

    private fun getContent():String{
        println("getContent : ${UserManager.get().user}")
        return String.format("Today's credits:%d,Wheel count:%d/%d",
            DBHelper.get().getTodayCredits(),
            DBHelper.get().getPlayCount(),UserManager.get().user?.max_wheel ?: 0)
    }

    inner class CallBinder:Binder(){
        fun initAccount(){
            if(!LogUtils.test) {
                GlobalScope.launch {
                    registerStartTime = System.currentTimeMillis()
                    Log.d(TAG, "initAccount, whether app is null:${app == null}")
                    if (app == null) {
                        app = MyApp()
                        Log.d(TAG, "delay结束 执行init---- ")
                        app?.init(this@CallService, App.appCacheDirectory)
                    }
                    regStatus = 0

//                    try {
//                        Thread.sleep(5000)
//                    } catch (e: InterruptedException) {
//                    }
//                    delay(4000)

                    println("$TAG initAccount ${UserManager.get().user}")
                    UserManager.get().user?.also {
                        if(it.servers != null && it.servers.isNotEmpty()) {
                            try {
                                accCfg = AccountConfig()
                                accCfg?.idUri =
                                    "sip:${it.sip}@${it.servers[0].host}:${it.servers[0].port}"
                                accCfg?.regConfig?.registrarUri =
                                    "sip:${it.servers[0].host}:${it.servers[0].port}"
                                val creds: AuthCredInfoVector? = accCfg?.sipConfig?.authCreds
                                creds?.clear()
                                creds?.add(AuthCredInfo("Digest", "*", it.sip, 0, it.passwd))
                                accCfg?.natConfig?.iceEnabled = true
                                accCfg?.videoConfig?.autoShowIncoming = true
                                accCfg?.videoConfig?.autoShowIncoming = true
                                accout = app?.addAcc(accCfg)

                                LogUtils.println("$TAG initAccount 成功 ${accCfg?.idUri},${accCfg?.regConfig}")
                            }catch (e:Exception){
                                e.printStackTrace()
                                LogUtils.println("$TAG initAccount 失败--")
                            }

                        }
                    }
                }

            }
        }

        fun addBuddy(buddyURI:String){
            val buddyConfig = BuddyConfig()
            buddyConfig.uri = buddyURI
            buddyConfig.subscribe = false
            accout?.addBuddy(buddyConfig)
        }

        fun makeCall(phone: String):Boolean{
            if(!LogUtils.test && accout != null) {
                val buddyURI =
                    "sip:$phone@${UserManager.get().user?.servers!![0].host}:${UserManager.get().user?.servers!![0].port}"
                LogUtils.println("$TAG buddyURI=$buddyURI")
                addBuddy(buddyURI)
                val call = MyCall(accout, -1)
                val prm = CallOpParam(true)
                try {
                    call.makeCall(buddyURI, prm)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.delete()
                    return false
                }
                currentCall = call
                LogUtils.println("$TAG makeCall currentCall: ${currentCall?.info?.remoteUri}")
            }else{
                LogUtils.println("$TAG,makeCall失败 ${accout == null}")
            }
            return true
        }

        fun hangup(){
            if(currentCall != null){
                val prm = CallOpParam()
                prm.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
                try {
                    println("hangup 挂断电话---")
                    currentCall?.hangup(prm)
                }catch (e:Exception){
                    LogUtils.println("$TAG,挂断电话失败")
                    e.printStackTrace()
                    currentCall?.delete()
                }
            }
        }

        fun addCallStateChangeListener(listener:CallStateChange){
            if(!callStateChangeListeners.contains(listener)) {
                callStateChangeListeners.add(listener)
            }
        }

        fun removeCallStateChangeListener(listener: CallStateChange){
            if(callStateChangeListeners.contains(listener)){
                callStateChangeListeners.remove(listener)
            }
        }

        fun getCurrentCall():MyCall?{
            return currentCall
        }


        fun setShowAdOnLoad(f:Boolean){
            showAdOnLoad = f
        }

        fun setRegStateChangeListener(listener: RegStateChange?){
            regStateChangeListener = listener
        }

        fun setOnGetIpInfoListener(listener: OnGetIpInfo){
            onGetIpInfo = listener
        }

        fun getRegStatus():Int{
            return regStatus
        }

        fun reRegistration(){
            println("$TAG reRegistration regStatus=$regStatus")
            if(regStatus == 1 || regStatus == 2){
                try {
                    accout?.setRegistration(true)
                }catch (e:Exception){
                    Log.e(TAG, "reRegistration fail--- ")
                    e.printStackTrace()
                }

                regStatus = 0
            }
        }
        fun getGetIpStatus():Int = getIpStatus
        @SuppressLint("CheckResult")
        fun getIpInfo(){
            println("getIpInfo getIpStatus=$getIpStatus ipCountry=$ipCountry")
//            toast("开始请求ipinfo.io获取国家...")
            if(getIpStatus != 0) {
                getIpStatus = 0
                Api.getApiService().getIpInfo().compose(RxUtils.applySchedulers())
                    .subscribe({
//                        toast("请求ipinfo.io获取国家成功")
                        println("ipinfo $it")
                        getIpStatus = 1
                        ipCountry = it.country
                        onGetIpInfo?.onGetIpInfo(ipCountry)
                    }, {
                        Log.e(TAG, "getIpInfo: 通过IP获取国家失败!!!")
//                        toast("请求ipinfo.io获取国家失败")
                        it.printStackTrace()
                        getIpStatus = 2
                        ipCountry = "IN"
                        onGetIpInfo?.onGetIpInfo(ipCountry)
                    })
            }else{
                if(ipCountry != null) {
                    onGetIpInfo?.onGetIpInfo(ipCountry)
                }
            }
        }

        fun getIpCountry():String?{
            println("CallService getIpCountry $ipCountry")
            return ipCountry
        }

        fun createNotification(){
            this@CallService.createNotification()
        }
    }

    private val networkCallback = object :ConnectivityManager.NetworkCallback(){
        override fun onAvailable(network: Network) {
            Log.d(TAG,"net onAvailable $network")
//            ActivityUtils.from(this@TranslateService).action(ACTION_SHOW_AD).send()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            Log.d(TAG,"net onCapabilitiesChanged，$network")

        }
    }

    inner class MyBroadcastReceiver : BroadcastReceiver() {
        private var conn_name = ""
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            LogUtils.println("MyBroadcastReceiver ${intent.action}")
            if(intent.action == ACTION_SHOW_AD){
                LogUtils.d(TAG, "ACTION_SHOW_AD  ")
                Dispatcher.dispatch(this@CallService){
                    navigate(SplashActivity::class.java)
                    flag(Intent.FLAG_ACTIVITY_NEW_TASK)
                    extra("only_show_ad",true)
                }.go()
            } else if (intent.action == "refresh_notification") {
                builder?.setContentText(getContent())
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.notify(100,builder?.build())
            } else if (intent.action == "stop") {
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(100)
            }else {
                if (isNetworkChange(context)) {
                    notifyChangeNetwork()
                }
            }
        }

        private fun isNetworkChange(context: Context): Boolean {
            var network_changed = false
            val connectivity_mgr = context.getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager
            val net_info = connectivity_mgr.activeNetworkInfo
            if (net_info != null && net_info.isConnectedOrConnecting &&
                !conn_name.equals("", ignoreCase = true)
            ) {
                val new_con = net_info.extraInfo
                if (new_con != null && !new_con.equals(
                        conn_name,
                        ignoreCase = true
                    )
                ) network_changed = true
                conn_name = new_con ?: ""
            } else {
                if (conn_name.equals("", ignoreCase = true)) conn_name = net_info?.extraInfo?:""
            }
            return network_changed
        }
    }

    override fun notifyRegState(code: pjsip_status_code?, reason: String?, expiration: Int) {
        var msg_str = ""
        msg_str += if (expiration == 0) "Unregistration" else "Registration"
        msg_str += if (code!!.swigValue() / 100 === 2) " successful" else " failed: $reason"
        LogUtils.println("$TAG notifyRegState $msg_str $expiration $reason")
        if(code!!.swigValue() / 100 == 2){
            regStatus = 1
            val t = System.currentTimeMillis()
            Log.d(TAG, "notifyRegState: 注册成功，注册用时${t- registerStartTime}")
        }else{
            regStatus = 2
        }
        regStateChangeListener?.onRegStateChange(regStatus)
    }

    override fun notifyCallState(call: MyCall?) {
        if(currentCall?.id != call?.id){
            return
        }
        var ci:CallInfo?
        try {
            ci = call?.info
        }catch (e:Exception){
            ci = null
        }
        callStateChangeListeners.forEach {
            it.onCallStateChange(ci)
        }
        if(ci?.state == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED){
            currentCall = null
        }
    }

    override fun notifyBuddyState(buddy: MyBuddy?) {

    }

    override fun notifyCallMediaState(call: MyCall?) {

    }

    override fun notifyChangeNetwork() {
        app?.handleNetworkChange()
    }

    override fun notifyIncomingCall(call: MyCall?) {

    }

    interface CallStateChange{
        fun onCallStateChange(callInfo: CallInfo?)
    }

    interface RegStateChange{
        fun onRegStateChange(state:Int)
    }

    interface OnGetIpInfo{
        fun onGetIpInfo(ip:String?)
    }
}