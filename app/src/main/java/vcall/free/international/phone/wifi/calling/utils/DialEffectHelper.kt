package vcall.free.international.phone.wifi.calling.utils

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import vcall.free.international.phone.wifi.calling.lib.prefs

/**
 * Created by lyf on 2020/5/24.
 */
class DialEffectHelper(val context: Context) {
    private val DTMF_DURATION_MS = 120
    private val mToneGeneratorLock = Any()
    private var mToneGenerator: ToneGenerator? = null
    private var mDTMFToneEnabled = false
    private var mVibrateEnable = false
    private var vibrator: Vibrator? = null
    private var canVibrator:Boolean = false

    init {
        canVibrator = prefs.getBooleanValue("vibration",false)
        if(prefs.getBooleanValue("play_tone",true)) {
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val ringerMode = audioManager.ringerMode
                if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
                    mDTMFToneEnabled = false
                } else if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                    mVibrateEnable = true
                    mDTMFToneEnabled = false
                } else {
                    mVibrateEnable = true
                    mDTMFToneEnabled = Settings.System.getInt(
                        context.contentResolver,
                        Settings.System.DTMF_TONE_WHEN_DIALING,
                        1
                    ) == 1
                }
                synchronized(mToneGeneratorLock) {
                    if (mDTMFToneEnabled && mToneGenerator == null) {
                        mToneGenerator = ToneGenerator(AudioManager.STREAM_DTMF, 80)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                mDTMFToneEnabled = false
                mToneGenerator = null
            }
        }
    }

    fun vibrate(time:Long){
        if(vibrator == null){
            vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    time,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }else{
            vibrator?.vibrate(time)
        }
    }

    fun dialNumber(number:String){
        if(mDTMFToneEnabled){
            synchronized(mToneGeneratorLock){
                mToneGenerator?.startTone(when(number){
                    "*" -> ToneGenerator.TONE_DTMF_S
                    "#" -> ToneGenerator.TONE_DTMF_P
                    else -> number.toInt()
                },DTMF_DURATION_MS)
            }
        }
        if(canVibrator){
            vibrate(80)
        }
    }
}