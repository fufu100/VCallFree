package vcall.free.international.phone.wifi.calling.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.lib.prefs

/**
 * Created by lyf on 2020/5/24.
 */
class DialEffectHelper(val context: Context) {
    private val mToneGeneratorLock = Any()
//    private var mToneGenerator: ToneGenerator? = null
    lateinit var soundPool:SoundPool
    val toneMap = mutableMapOf<String,Int>()
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
                soundPool = SoundPool.Builder().setMaxStreams(1).setAudioAttributes(
                    AudioAttributes.Builder().setContentType(
                        AudioAttributes.CONTENT_TYPE_MUSIC).setUsage(AudioAttributes.USAGE_MEDIA).build()).build()
                toneMap.put("0",soundPool.load(context, R.raw._0,0))
                toneMap.put("1",soundPool.load(context, R.raw._1,0))
                toneMap.put("2",soundPool.load(context, R.raw._2,0))
                toneMap.put("3",soundPool.load(context, R.raw._3,0))
                toneMap.put("4",soundPool.load(context, R.raw._4,0))
                toneMap.put("5",soundPool.load(context, R.raw._5,0))
                toneMap.put("6",soundPool.load(context, R.raw._6,0))
                toneMap.put("7",soundPool.load(context, R.raw._7,0))
                toneMap.put("8",soundPool.load(context, R.raw._8,0))
                toneMap.put("9",soundPool.load(context, R.raw._9,0))
                toneMap.put("*",soundPool.load(context, R.raw.star,0))
                toneMap.put("#",soundPool.load(context, R.raw.pound,0))
            } catch (e: Exception) {
                e.printStackTrace()
                mDTMFToneEnabled = false
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
        println("dialNumber $number $mDTMFToneEnabled")
        if(mDTMFToneEnabled){
            soundPool.play(toneMap[number]!!,1f,1f,0,0,1f)
        }
        if(canVibrator){
            vibrate(80)
        }
    }
}