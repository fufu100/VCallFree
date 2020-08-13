package vcall.free.international.phone.wifi.calling.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import vcall.free.international.phone.wifi.calling.lib.App
import org.pjsip.pjsua2.*
import vcall.free.international.phone.wifi.calling.pjsua.*
import vcall.free.international.phone.wifi.calling.utils.Dispatcher
import vcall.free.international.phone.wifi.calling.utils.LogUtils
import vcall.free.international.phone.wifi.calling.utils.UserManager

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
    private lateinit var mInterstitialAd: InterstitialAd
    val callStateChangeListeners:MutableList<CallStateChange> = mutableListOf()
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
        registerReceiver(receiver, intentFilter)

    }

    override fun onDestroy() {
        super.onDestroy()
        if (receiver != null) {
            unregisterReceiver(receiver)
        }
        app?.deinit()
    }

    inner class CallBinder:Binder(){
        fun initAccount(){
            if(!LogUtils.test) {
                GlobalScope.launch {
                    if (app == null) {
                        app = MyApp()
                    }
                    try {
                        Thread.sleep(5000)
                    } catch (e: InterruptedException) {
                    }
                    app?.init(this@CallService, App.appCacheDirectory)
                    UserManager.get().user?.also {
                        accCfg = AccountConfig()
                        accCfg?.idUri = "sip:${it.sip}@${it.servers[0].host}:${it.servers[0].port}"
                        accCfg?.regConfig?.registrarUri =
                            "sip:${it.servers[0].host}:${it.servers[0].port}"
                        val creds: AuthCredInfoVector? = accCfg?.sipConfig?.authCreds
                        creds?.clear()
                        creds?.add(AuthCredInfo("Digest", "*", it.sip, 0, it.passwd))
                        accCfg?.natConfig?.iceEnabled = true
                        accCfg?.videoConfig?.autoShowIncoming = true
                        accCfg?.videoConfig?.autoShowIncoming = true
                        accout = app?.addAcc(accCfg)

                        println("$TAG initAccount 成功 ${accCfg?.idUri},${accCfg?.regConfig}")
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
            if(!LogUtils.test) {
                val buddyURI =
                    "sip:$phone@${UserManager.get().user?.servers!![0].host}:${UserManager.get().user?.servers!![0].port}"
                println("$TAG buddyURI=$buddyURI")
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
                println("$TAG makeCall currentCall: ${currentCall?.info?.remoteUri}")
            }
            return true
        }

        fun hangup(){
            if(currentCall != null){
                val prm = CallOpParam()
                prm.statusCode = pjsip_status_code.PJSIP_SC_DECLINE
                try {
                    currentCall?.hangup(prm)
                }catch (e:Exception){
                    println("$TAG,挂断电话失败")
                    e.printStackTrace()
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

        fun initInterstitialAd(){
            mInterstitialAd = InterstitialAd(this@CallService).apply {
                adUnitId = "ca-app-pub-3940256099942544/1033173712"
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d(TAG, "onAdLoaded--- ")
                        if (showAdOnLoad) {
                            mInterstitialAd.show()
                        }
                    }

                    override fun onAdFailedToLoad(errorCode: Int) {
                        Log.d(TAG, "onAdFailedToLoad,errorCode=$errorCode")
                        Dispatcher.dispatch(this@CallService){
                            action(ACTION_ON_AD_LOAD_FAIL)
                        }.send()
                    }

                    override fun onAdOpened() {
                        Log.d(TAG, "onAdOpened--- ")
                        Dispatcher.dispatch(this@CallService){
                            action(ACTION_ON_AD_SHOW)
                        }.send()
                    }

                    override fun onAdClosed() {
                        super.onAdClosed()
                        Log.d(TAG, "onAdClosed---")
                        Dispatcher.dispatch(this@CallService){
                            action(ACTION_ON_AD_CLOSE)
                        }.send()
                    }
                }

            }
            mInterstitialAd.loadAd(AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build())

        }

        fun setShowAdOnLoad(f:Boolean){
            showAdOnLoad = f
        }

        fun showFullScreenAd(){
            if(mInterstitialAd.isLoaded){
                mInterstitialAd.show()
            }
        }
        fun hideFullScreenAd(){
//            if(mInterstitialAd.isLoaded){
//                mInterstitialAd.
//            }
        }
        fun isInterstitialAdLoaded() = mInterstitialAd.isLoaded
    }

    inner class MyBroadcastReceiver : BroadcastReceiver() {
        private var conn_name = ""
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            println("MyBroadcastReceiver ${intent.action}")
            if(intent.action == ACTION_SHOW_AD){
                Log.d(TAG, "ACTION_SHOW_AD ${mInterstitialAd.isLoaded} ")
                if(!mInterstitialAd.isLoading){
                    showAdOnLoad = true
                    mInterstitialAd.loadAd(AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build())
                }
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
        println("$TAG notifyRegState $msg_str")
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
}