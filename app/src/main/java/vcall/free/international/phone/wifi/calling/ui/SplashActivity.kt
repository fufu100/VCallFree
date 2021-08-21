package vcall.free.international.phone.wifi.calling.ui

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.dialog_call.*
import kotlinx.coroutines.*
import vcall.free.international.phone.wifi.calling.MainActivity
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.api.AdResp
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.lib.BaseActivity
import vcall.free.international.phone.wifi.calling.lib.prefs
import vcall.free.international.phone.wifi.calling.service.CallService
import vcall.free.international.phone.wifi.calling.utils.*
import java.net.URLDecoder
import java.util.concurrent.TimeUnit

/**
 * Created by lyf on 2020/5/19.
 */
class SplashActivity:BaseActivity(),AdManager.VCallAdListener {
    private var isAdShowing = false
    private var adLoaded = false
    private var onlyShowAd = false
    private var isFirst = prefs.getBooleanValue("is_first", true)
    private lateinit var conn: ServiceConnection
    private var callBinder: CallService.CallBinder? = null
    override fun getLayoutRes(): Int = R.layout.activity_splash

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.decorView?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window?.decorView?.systemUiVisibility =
                window?.decorView?.systemUiVisibility!!.or(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
        }
        onlyShowAd = intent.getBooleanExtra("only_show_ad",false)
        window?.statusBarColor = Color.TRANSPARENT
        if(!onlyShowAd) {
            if (!isFirst) {
                startCountDownTime(29)
            } else {
                AgreementDialog(this) {
                    if (it) {
                        viewPager.visibility = View.VISIBLE
                        viewPager.adapter = ImagePageAdapter()
                    } else {
                        finish()
                    }
                }.show()
            }
        }else{
            startCountDownTime(29)
        }

        conn = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {}
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                callBinder = service as CallService.CallBinder
                if(ContextCompat.checkSelfPermission(this@SplashActivity,
                        Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
                    if(!this@SplashActivity.hasSim()){
                        callBinder?.getIpInfo()
                    }
                }
            }
        }
        bindService(Intent(this, CallService::class.java), conn, Context.BIND_AUTO_CREATE)
        registerReceiver(receiver,IntentFilter().apply {
            addAction(CallService.ACTION_ON_AD_CLOSE)
            addAction(CallService.ACTION_ON_AD_SHOW)
        })

        if(onlyShowAd){
            AdManager.get().showSplashInterstitialAd(this)
        }else {
            getAdData()
        }
        AdManager.get().interstitialAdListener.add(this)
        //for test
//        prefs.save("last_check_day","20200826")
//        prefs.save("check_max_day",1)


    }

    override fun onStart() {
        super.onStart()

    }

    override fun onStop() {
        super.onStop()
        if(AdManager.get().interstitialAdMap[AdManager.ad_splash]?.isAdReady == false){
            println("$tag onStop 加载广告")
            AdManager.get().loadInterstitialAd(AdManager.ad_splash)
        }
    }

    override fun onDestroy() {
        AdManager.get().interstitialAdListener.remove(this)
        super.onDestroy()
        unbindService(conn)
        unregisterReceiver(receiver)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private val receiver:BroadcastReceiver = object :BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if(CallService.ACTION_ON_AD_SHOW == intent?.action){
                onAdShow()
            }else if(CallService.ACTION_ON_AD_CLOSE == intent?.action){
                onAdClose()
            }
        }

    }

    private fun startCountDownTime(time: Long) {
//        GlobalScope.launch {
//            delay(1500)
//            while (progressBar.progress < 100) {
//                delay(65)
//                withContext(Dispatchers.Main) {
//                    progressBar.progress += 1
//                }
//            }
//
//        }
        compositeDisposable.add(Observable.interval(1000, 1000, TimeUnit.MILLISECONDS)//设置0延迟，每隔一秒发送一条数据
            .take(time + 1) //设置循环次数
            .map {
                time - it
            }
            .observeOn(AndroidSchedulers.mainThread())//操作UI主要在UI线程
            .subscribe({ it ->
//                jumpBtn.visibility = View.VISIBLE
//                jumpBtn.text = "${it}"
            }, {
                it.printStackTrace()
            }, {
                if(!isAdShowing) {
//                    callBinder?.setShowAdOnLoad(false)
                    Dispatcher.dispatch(this) {
                        navigate(MainActivity::class.java)
                        defaultAnimate()
                    }.go()
                    finish()
                }
            })
        )
    }

    private fun getAdData(){
        compositeDisposable.add(Api.getApiService().getAd()
            .compose(RxUtils.applySchedulers())
            .subscribe({
                AdManager.get().adData = it
                AdManager.get().loadInterstitialAd(AdManager.ad_splash)

            },{
                it.printStackTrace()
            }))
    }

    inner class ImagePageAdapter:PagerAdapter(){
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return `object` is View && `object` == view
        }
        override fun getCount(): Int = 3
        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val res = when(position){
                0 -> R.mipmap.ic_introduce1
                1 -> R.mipmap.ic_introduce2
                else -> R.mipmap.ic_introduce3
            }
            val iv = ImageView(this@SplashActivity).apply {
                setImageResource(res)
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            if(position == 2){
                iv.setOnClickListener {
                    Dispatcher.dispatch(this@SplashActivity) {
                        navigate(MainActivity::class.java)
                        defaultAnimate()
                    }.go()
                    finish()
                }
            }
            container.addView(iv)
            return iv
        }
    }

    override fun onAdClose() {
        isAdShowing = false
        if(onlyShowAd){
            finish()
        }else {
            Log.d(tag, "onAdClose----- ")
            GlobalScope.launch {
                delay(20)
                withContext(Dispatchers.Main) {
                    Log.d(tag, "onAdClose-----  跳转到MainActivity")
                    Dispatcher.dispatch(this@SplashActivity) {
                        navigate(MainActivity::class.java)
                        defaultAnimate()
                    }.go()
                    finish()
                }
            }
        }

    }

    override fun onAdShow() {
        isAdShowing = true
        compositeDisposable.dispose()
        jumpBtn.visibility = View.GONE
        progressBar.visibility = View.GONE
    }

    override fun onAdLoaded() {
        LogUtils.println("$tag onAdLoaded--- $adLoaded")
        if(!adLoaded) {
            adLoaded = true
            if(!isFirst) {
                AdManager.get().showSplashInterstitialAd(this)
            }
            AdManager.get().loadInterstitialAd(AdManager.ad_preclick)
            AdManager.get().loadInterstitialAd(AdManager.ad_point)
            AdManager.get().loadInterstitialAd(AdManager.ad_close)
            AdManager.get().loadRewardedAd()
        }
    }

    override fun onAdLoadFail() {
        LogUtils.d(tag,"启动页广告加载失败，进入首页")
        Dispatcher.dispatch(this) {
            navigate(MainActivity::class.java)
            defaultAnimate()
        }.go()
        finish()
    }
}