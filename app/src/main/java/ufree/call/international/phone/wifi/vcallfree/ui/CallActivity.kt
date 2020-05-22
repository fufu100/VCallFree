package ufree.call.international.phone.wifi.vcallfree.ui

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
import androidx.constraintlayout.widget.ConstraintLayout
import com.newmotor.x5.db.DBHelper
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_call.*
import kotlinx.android.synthetic.main.side_header.*
import org.pjsip.pjsua2.*
import org.pjsip.pjsua2.pjmedia_type
import org.pjsip.pjsua2.pjsua_call_media_status
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.api.Record
import ufree.call.international.phone.wifi.vcallfree.databinding.ActivityCallBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseBackActivity
import ufree.call.international.phone.wifi.vcallfree.pjsua.MyApp
import ufree.call.international.phone.wifi.vcallfree.service.CallService
import ufree.call.international.phone.wifi.vcallfree.utils.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil


/**
 * Created by lyf on 2020/5/10.
 */
class CallActivity : BaseBackActivity<ActivityCallBinding>(), CallService.CallStateChange ,SensorEventListener{
    private val TAG = "CallActivity"
    private lateinit var conn: ServiceConnection
    private var callBinder: CallService.CallBinder? = null
    var expand = false//是否显示拨号键盘
    var phone: String? = ""
    var rate: Int = 0
    var count = 0L
    var disposable: Disposable? = null
    lateinit var audioManager:AudioManager
    lateinit var powerManager: PowerManager
    lateinit var sensorManager:SensorManager
    var wakeLock: PowerManager.WakeLock? = null
    var headsetPlugReceiver:HeadsetPluginReceiver? = null
    var maxmiumDistance = 0f
    override fun getLayoutRes(): Int = R.layout.activity_call
    @SuppressLint("InvalidWakeLockTag")
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        dataBinding.activity = this
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = false
        if(!LogUtils.test) {
            sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
            maxmiumDistance = sensor.maximumRange
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

        headsetPlugReceiver = HeadsetPluginReceiver()
        registerReceiver(headsetPlugReceiver,IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
        })

        phone = intent.getStringExtra("phone")
        rate = intent.getIntExtra("rate", 0)
        dataBinding.phone.text =
            String.format(Locale.getDefault(), "+%s %s", UserManager.get().country?.code, phone)

        if (LogUtils.test) {
            startTimeCount()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (::conn.isInitialized) {
            callBinder?.removeCallStateChangeListener(this)
            unbindService(conn)
        }
        if (disposable?.isDisposed == false) {
            disposable?.dispose()
        }
        unregisterReceiver(headsetPlugReceiver)
        toneGeneratorHelper?.stopRingingTone()
        if(disposable?.isDisposed == false){
            disposable?.dispose()
        }
        if(!LogUtils.test){
            sensorManager.unregisterListener(this)
        }
    }

    fun onClick(v: View) {

    }

    fun mute(v: View) {
        v.isSelected = !v.isSelected
    }

    fun horn(v: View) {
        v.isSelected = !v.isSelected
        audioManager.isSpeakerphoneOn = v.isSelected
    }

    fun numberPad(v: View) {
        if (expand) {
            expand = false
            number_pad.visibility = View.GONE
            (dataBinding.phone.layoutParams as ConstraintLayout.LayoutParams).topMargin =
                dip2px(141)
        } else {
            expand = true
            number_pad.visibility = View.VISIBLE
            (dataBinding.phone.layoutParams as ConstraintLayout.LayoutParams).topMargin = dip2px(41)
        }
    }

    fun hangup(flag:Boolean = true) {
        println("hangup flat=$flag")
        callBinder?.hangup()
        val state =
            if (callBinder?.getCurrentCall()?.info?.state == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) 1 else 2
        val coin_cost = (rate * ceil(count * 1.0f / 60)).toInt()
        val record = Record(
            0,
            0,
            phone!!,
            UserManager.get().country!!.iso,
            UserManager.get().country!!.code,
            UserManager.get().country!!.prefix,
            "",
            0L,
            System.currentTimeMillis(),
            count,
            rate,
            coin_cost,
            state
        )
        UserManager.get().user!!.points -= coin_cost
        DBHelper.get().addCallRecord(record)
        if(flag) {
            Dispatcher.dispatch(this) {
                navigate(CallResultActivity::class.java)
                extra("record", record)
                defaultAnimate()
            }.go()
            finish()
        }
    }

    fun del(v: View) {

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){

            return true
        }
        return super.onKeyDown(keyCode, event)
    }
    val toneGeneratorHelper = if(LogUtils.test) null else ToneGenerateHelper()
    override fun onCallStateChange(callInfo: CallInfo?) {
        if(callInfo != null) {
            runOnUiThread {
                var callState = ""
                if (callInfo!!.state.swigValue() < pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.swigValue()) {
                    if (callInfo.role === pjsip_role_e.PJSIP_ROLE_UAS) {
                        callState = "Incoming call.."
                    } else {
                        //拨打电话，对方未接时候
                        callState = callInfo.stateText
                        println("$TAG 对方未接")
                        if(callInfo.state == pjsip_inv_state.PJSIP_INV_STATE_EARLY) {
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
                        finish()
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
                println("startTimeCount ${UserManager.get().user}")
                val totalMinRemaining = UserManager.get().user!!.points / rate - it / 60
                dataBinding.timeRemaining.text =
                    String.format(Locale.getDefault(), "%d min Remaining", totalMinRemaining)
            }, {
                it.printStackTrace()
            })

    }

    private fun hasWireHeadSet():Boolean{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val deviceInfo = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (audioDeviceInfo in deviceInfo) {
                if(audioDeviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                    audioDeviceInfo.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                    audioDeviceInfo.type == AudioDeviceInfo.TYPE_USB_HEADSET ){
                    return true
                }
            }
            false
        }else{
            audioManager.isWiredHeadsetOn()
        }
    }

    @SuppressLint("InvalidWakeLockTag")
    private fun setScreenOnOff(flag:Boolean){
        println("$TAG setScreenOnOff $flag")
        if(wakeLock == null){
            wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,TAG)
        }
        if(flag){
            wakeLock?.acquire()
        }else{
            wakeLock?.setReferenceCounted(false)
            wakeLock?.release()
            wakeLock = null
        }
    }

    class HeadsetPluginReceiver : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action == Intent.ACTION_HEADSET_PLUG){
                val state = intent.getIntExtra("state",0)
                println("HeadsetPluginReceiver state=$state")
                val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audioManager.isSpeakerphoneOn = state == 0
            }
        }

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    override fun onSensorChanged(event: SensorEvent?) {
        val b = hasWireHeadSet()
        println("$TAG onSensorChanged $b")
        if(b){
            return
        }
//        if(callBinder?.getCurrentCall()?.info?.state == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED || LogUtils.test){
        val distance = event?.values?.get(0) ?: 0f
        println("$TAG distance $distance $maxmiumDistance")
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
            val media: Media?= callBinder?.getCurrentCall()?.getMedia(i)
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

    inner class ToneGenerateHelper{
        var toneGenerator:org.pjsip.pjsua2.ToneGenerator? = org.pjsip.pjsua2.ToneGenerator()
        var toneDesc = ToneDesc()
        var toneDescVector = ToneDescVector()

        val kSPRingbackFrequency1 = 400
        val kSPRingbackFrequency2 = 480
        val kSPRingbackOnDuration = 1000
        val kSPRingbackOffDuration = 4000
        val kSPRingbackCount = 1
        val kSPRingbackInterval = 4000

        fun  startRingingTone(){
            toneDesc.freq1 = kSPRingbackFrequency1.toShort()
            toneDesc.freq2 = kSPRingbackFrequency2.toShort()
            toneDesc.on_msec = kSPRingbackOnDuration.toShort()
            toneDesc.off_msec = kSPRingbackOffDuration.toShort()

            toneDescVector.add(toneDesc)

            try {
                toneGenerator!!.createToneGenerator()
                toneGenerator!!.play(toneDescVector, true)
                toneGenerator!!.startTransmit(MyApp.ep.audDevManager().playbackDevMedia)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        fun stopRingingTone(){
            try {
                if (toneGenerator != null) toneGenerator!!.stop()
                toneGenerator = null
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

}