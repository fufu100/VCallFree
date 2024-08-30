package vcall.free.international.phone.wifi.calling.ui

import android.annotation.SuppressLint
import android.content.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
import org.pjsip.pjsua2.*
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.api.Record
import vcall.free.international.phone.wifi.calling.databinding.ActivityCallBinding
import vcall.free.international.phone.wifi.calling.db.DBHelper
import vcall.free.international.phone.wifi.calling.lib.BaseBackActivity
import vcall.free.international.phone.wifi.calling.pjsua.MyApp
import vcall.free.international.phone.wifi.calling.service.CallService
import vcall.free.international.phone.wifi.calling.utils.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil


/**
 * Created by lyf on 2020/5/10.
 */
class CallActivity : BaseBackActivity<ActivityCallBinding>(), CallService.CallStateChange,
    SensorEventListener,AdManager.VCallAdListener {
    private val TAG = "CallActivity"
    private lateinit var conn: ServiceConnection
    private var callBinder: CallService.CallBinder? = null
    var expand = false//是否显示拨号键盘
    var phone: String? = ""
    var username:String = ""
    var rate: Int = 0
    var count = 0L
    var isAdShowing = false;
    var record: Record? = null
    var disposable: Disposable? = null
    lateinit var audioManager: AudioManager
    lateinit var powerManager: PowerManager
    lateinit var sensorManager: SensorManager
    var wakeLock: PowerManager.WakeLock? = null
    var headsetPlugReceiver: HeadsetPluginReceiver? = null
    var maxmiumDistance = 0f
    var dialEffectHelper: DialEffectHelper? = null
    override fun getLayoutRes(): Int = R.layout.activity_call

    @SuppressLint("InvalidWakeLockTag")
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        dataBinding.activity = this
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = false
        if (!LogUtils.test) {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val sensor:Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
            maxmiumDistance = sensor?.maximumRange?:10f
            println("$tag maxmiumDistance=$maxmiumDistance")
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        conn = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {}
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                callBinder = service as CallService.CallBinder
                callBinder?.addCallStateChangeListener(this@CallActivity)

                onCallStateChange(callBinder?.getCurrentCall()?.info)
            }
        }
        bindService(Intent(this, CallService::class.java), conn, Context.BIND_AUTO_CREATE)
        dialEffectHelper = DialEffectHelper(this)
        headsetPlugReceiver = HeadsetPluginReceiver()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(headsetPlugReceiver, IntentFilter().apply {
                addAction(Intent.ACTION_HEADSET_PLUG)
                addAction(CallService.ACTION_ON_AD_LOAD_FAIL)
                addAction(CallService.ACTION_ON_AD_SHOW)
            }, RECEIVER_NOT_EXPORTED)
        }else{
            registerReceiver(headsetPlugReceiver, IntentFilter().apply {
                addAction(Intent.ACTION_HEADSET_PLUG)
                addAction(CallService.ACTION_ON_AD_LOAD_FAIL)
                addAction(CallService.ACTION_ON_AD_SHOW)
            })
        }
        username = intent.getStringExtra("username")?:""
        phone = intent.getStringExtra("phone")?:""
        rate = intent.getIntExtra("rate", 0)
        if(username.isEmpty()){
            dataBinding.phone.text =
                String.format(Locale.getDefault(), "+%s %s", UserManager.get().country?.code,  phone )
        }else{
            dataBinding.phone.text = username
        }

        if (LogUtils.test) {
            startTimeCount()
        }
        AdManager.get().interstitialAdListener[AdManager.ad_close] = this
    }

    override fun onDestroy() {
        AdManager.get().interstitialAdListener.remove(AdManager.ad_close)
        super.onDestroy()
        if (::conn.isInitialized) {
            callBinder?.removeCallStateChangeListener(this)
            unbindService(conn)
        }
//        if (disposable?.isDisposed == false) {
//            disposable?.dispose()
//        }
        unregisterReceiver(headsetPlugReceiver)
        toneGeneratorHelper?.stopRingingTone()
        if (disposable?.isDisposed == false) {
            disposable?.dispose()
        }
        if (!LogUtils.test) {
            sensorManager.unregisterListener(this)
        }
    }

    fun onClick(v: View) {
        val number = v.tag.toString()
        LogUtils.println("onClick number=$number")
        dataBinding.phone2.insert(number)
        dialEffectHelper?.dialNumber(number)
        if (!LogUtils.test) {
            try {
                callBinder?.getCurrentCall()?.dialDtmf(number)
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    //点击静音按钮
    fun mute(v: View) {
        v.isSelected = !v.isSelected
    }
    //点击外放按钮
    @RequiresApi(Build.VERSION_CODES.S)
    fun horn(v: View) {
        v.isSelected = !v.isSelected
        audioManager.isSpeakerphoneOn = v.isSelected
    }

    fun numberPad(v: View) {
        if (expand) {
            expand = false
            dataBinding.numberPad.root.visibility = View.GONE
            dataBinding.phone.visibility = View.VISIBLE
            dataBinding.scrollView.visibility = View.GONE
            (dataBinding.duration.layoutParams as ConstraintLayout.LayoutParams).topToBottom =
                R.id.phone
        } else {
            expand = true
            dataBinding.numberPad.root.visibility = View.VISIBLE
            dataBinding.phone.visibility = View.GONE
            dataBinding.scrollView.visibility = View.VISIBLE
            (dataBinding.duration.layoutParams as ConstraintLayout.LayoutParams).topToBottom =
                R.id.scrollView
        }
    }

    //挂断电话，通话中挂断会回调到下面到onDisconnect方法，如果没有打通电话就手动调用onDisconnect方法
    fun hangup(flag: Boolean = true) {
//        dataBinding.hangupIv.isClickable = false
        LogUtils.println("hangup flat=$flag ${callBinder?.getCurrentCall() == null}")
        if(LogUtils.test){
            onDisconnect()
        }else {
            Log.d(TAG, "hangup 当前电话call: ${callBinder?.getCurrentCall() == null} ")
            if(callBinder?.getCurrentCall() == null){
                onDisconnect()
            }else {
                callBinder?.hangup()
            }
        }

    }

    private fun onDisconnect(){
        try {
            var state = 2//state=1接通
            var coin_cost = 0

            if (count > 0) {
                state = 1
                coin_cost = (rate * ceil(count * 1.0f / 60)).toInt()
            }
            Log.d(TAG, "onDisConnect: state=$state ")
            record = Record(
                0,
                0,
                0,
                phone ?: "",
                UserManager.get().country?.iso ?: "",
                UserManager.get().country?.code ?: "",
                UserManager.get().country?.prefix ?: "",
                username,
                0L,
                System.currentTimeMillis(),
                count,
                rate,
                coin_cost,
                state
            )
            Log.d(TAG, "after new Record")
            if (UserManager.get().user != null) {
                UserManager.get().user!!.points -= coin_cost
            }
            toneGeneratorHelper?.stopRingingTone()
            Log.d(TAG, "after cut points")
            DBHelper.get().addCallRecord(record)
            Log.d(TAG, "after add Record ${AdManager.get().interstitialAdMap[AdManager.ad_close] == null}")
            if (AdManager.get().interstitialAdMap[AdManager.ad_close] != null) {
                AdManager.get().showCloseInterstitialAd(this)
                isAdShowing = true
            } else {
                AdManager.get().loadInterstitialAd(this, AdManager.ad_close)
                Dispatcher.dispatch(this) {
                    navigate(CallResultActivity::class.java)
                    extra("record", record!!)
                    defaultAnimate()
                }.go()
                finish()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    fun del(v: View) {

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    val toneGeneratorHelper = if (LogUtils.test) null else ToneGenerateHelper()
    override fun onCallStateChange(callInfo: CallInfo?) {
        LogUtils.println("$tag onCallStateChange ${callInfo?.stateText},${callInfo?.state}")
        if (callInfo != null) {
            runOnUiThread {
                var callState = ""
                if (callInfo!!.state.swigValue() < pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.swigValue()) {
                    if (callInfo.role === pjsip_role_e.PJSIP_ROLE_UAS) {
                        callState = "Incoming call.."
                    } else {
                        //拨打电话，对方未接时候
                        callState = callInfo.stateText
                        LogUtils.println("$TAG 对方未接")
                        if (callInfo.state == pjsip_inv_state.PJSIP_INV_STATE_EARLY) {
                            toneGeneratorHelper?.startRingingTone()
                        }
                    }
                } else if (callInfo.state
                        .swigValue() >= pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.swigValue()
                ) {
                    toneGeneratorHelper?.stopRingingTone()
                    callState = callInfo.stateText
                    if (callInfo.state === pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                        Log.d(TAG, "已接听")
                        callState = "00:00:00"
                        startTimeCount()
//                buttonHangup.setText("Hangup") //已接听
                    } else if (callInfo.state === pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                        Log.d(TAG, "连接失败")
//                buttonHangup.setText("OK") //来电未接
                        callState = "Call disconnected: " + callInfo.lastReason
                        GlobalScope.launch {
                            delay(200)
                            withContext(Dispatchers.Main){
//                                hangup(true)
                                onDisconnect()
                            }
                        }
                    }
                }
                dataBinding.timeRemaining.text = callState
            }
        }

    }

    private fun startTimeCount() {
        count = 0
        disposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
            .compose(RxUtils.applySchedulers())
            .subscribe({
                count++
                dataBinding.duration.text = String.format(
                    Locale.getDefault(),
                    "%02d:%02d:%02d",
                    it / 3600,
                    it / 60,
                    it % 60
                )
                LogUtils.println("startTimeCount ${UserManager.get().user}")
                val totalMinRemaining = UserManager.get().user!!.points / rate - it / 60
                dataBinding.timeRemaining.text =
                    String.format(Locale.getDefault(), "%d min Remaining", totalMinRemaining)
                if(totalMinRemaining <=1){
                    hangup(true)
                    toast("Total remain time less than 1 min,auto hang up")
                    if (disposable?.isDisposed == false) {
                        disposable?.dispose()
                    }
                }
            }, {
                it.printStackTrace()
            })

    }

    private fun hasWireHeadSet(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val deviceInfo = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (audioDeviceInfo in deviceInfo) {
                if (audioDeviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    audioDeviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    audioDeviceInfo.type == AudioDeviceInfo.TYPE_USB_HEADSET
                ) {
                    return true
                }
            }
            false
        } else {
            audioManager.isWiredHeadsetOn()
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    private fun setScreenOnOff(flag: Boolean) {
        LogUtils.println("$TAG setScreenOnOff $flag")
        if (wakeLock == null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG)
        }
        if (flag) {
            wakeLock?.acquire()
        } else {
            wakeLock?.setReferenceCounted(false)
            wakeLock?.release()
            wakeLock = null
        }
    }

    inner class HeadsetPluginReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            LogUtils.println("HeadsetPluginReceiver onReceive ${intent?.action}")
            if (intent?.action == Intent.ACTION_HEADSET_PLUG) {
                val state = intent.getIntExtra("state", 0)
                LogUtils.println("HeadsetPluginReceiver state=$state")
                val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.isSpeakerphoneOn = state == 0
            } else if (intent?.action == CallService.ACTION_ON_AD_LOAD_FAIL || intent?.action == CallService.ACTION_ON_AD_CLOSE) {
                if (record != null) {
                    Dispatcher.dispatch(this@CallActivity) {
                        navigate(CallResultActivity::class.java)
                        extra("record", record!!)
                        defaultAnimate()
                    }.go()
                    finish()
                }
            }
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        val b = hasWireHeadSet()
        LogUtils.println("$TAG onSensorChanged $b")
        if (b) {
            return
        }
//        if(callBinder?.getCurrentCall()?.info?.state == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED || LogUtils.test){
        val distance = event?.values?.get(0) ?: 0f
        LogUtils.println("$TAG distance $distance $maxmiumDistance")
        setScreenOnOff(distance >= maxmiumDistance)
//        }
    }

    fun setMute(mute: Boolean) {
        // return immediately if we are not changing the current state
        var info: CallInfo? = null
        info = try {
            callBinder?.getCurrentCall()?.info
        } catch (exc: java.lang.Exception) {
            return
        }
        for (i in 0 until (info?.media?.size() ?: 0)) {
            val media: Media? = callBinder?.getCurrentCall()?.getMedia(i)
            val mediaInfo = info?.media!![i.toInt()]
            if (mediaInfo.type == pjmedia_type.PJMEDIA_TYPE_AUDIO && media != null && mediaInfo.status == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE
            ) {
                val audioMedia = AudioMedia.typecastFromMedia(media)

                // connect or disconnect the captured audio
                try {
                    val mgr = MyApp.ep.audDevManager()
                    if (mute) {
                        mgr.captureDevMedia.stopTransmit(audioMedia)
                    } else {
                        mgr.captureDevMedia.startTransmit(audioMedia)
                    }
                } catch (exc: java.lang.Exception) {
                }
            }
        }
    }

    inner class ToneGenerateHelper {
        var toneGenerator: org.pjsip.pjsua2.ToneGenerator? = null
        lateinit var toneDesc:ToneDesc
        lateinit var toneDescVector:ToneDescVector

        val kSPRingbackFrequency1 = 400
        val kSPRingbackFrequency2 = 480
        val kSPRingbackOnDuration = 1000
        val kSPRingbackOffDuration = 4000
        val kSPRingbackCount = 1
        val kSPRingbackInterval = 4000

        fun startRingingTone() {
            try {
                toneDescVector = ToneDescVector()
                toneDesc = ToneDesc()
                toneDesc.freq1 = kSPRingbackFrequency1.toShort()
                toneDesc.freq2 = kSPRingbackFrequency2.toShort()
                toneDesc.on_msec = kSPRingbackOnDuration.toShort()
                toneDesc.off_msec = kSPRingbackOffDuration.toShort()
                toneDescVector.add(toneDesc)

                toneGenerator = org.pjsip.pjsua2.ToneGenerator()
                toneGenerator!!.createToneGenerator()
                toneGenerator!!.play(toneDescVector, true)
                toneGenerator!!.startTransmit(MyApp.ep.audDevManager().playbackDevMedia)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        fun stopRingingTone() {
            try {
                if (toneGenerator != null) toneGenerator!!.stop()
                toneGenerator = null
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    override fun onAdClose() {
        LogUtils.println("$tag,onAdClose $isAdShowing")
        if(isAdShowing) {
            Dispatcher.dispatch(this) {
                navigate(CallResultActivity::class.java)
                extra("record", record!!)
                defaultAnimate()
            }.go()
            finish()
        }
    }

    override fun onAdShow() {

    }

    override fun onAdLoaded() {

    }

    override fun onAdLoadFail() {

    }

}