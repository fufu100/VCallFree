package ufree.call.international.phone.wifi.vcallfree.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.dialog_call.*
import ufree.call.international.phone.wifi.vcallfree.MainActivity
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.lib.BaseActivity
import ufree.call.international.phone.wifi.vcallfree.lib.prefs
import ufree.call.international.phone.wifi.vcallfree.service.CallService
import ufree.call.international.phone.wifi.vcallfree.utils.Dispatcher
import ufree.call.international.phone.wifi.vcallfree.utils.DrawableUtils
import ufree.call.international.phone.wifi.vcallfree.utils.dip2px
import java.util.concurrent.TimeUnit

/**
 * Created by lyf on 2020/5/19.
 */
class SplashActivity:BaseActivity() {
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
        window?.statusBarColor = Color.TRANSPARENT
        jumpBtn.background = DrawableUtils.generate {
            solidColor(0x4D000000)
            radius(dip2px(16))
            build()
        }
        if(!prefs.getBooleanValue("is_first",true)) {
            startCountDownTime(5)
        }else{
            AgreementDialog(this){
                if(it){
//                    startCountDownTime(5)
//                    callBinder?.showFullScreenAd()
                    viewPager.visibility = View.VISIBLE
                    viewPager.adapter = ImagePageAdapter()
                }else{
                    finish()
                }
            }.show()
        }

        conn = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {}
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                callBinder = service as CallService.CallBinder
                callBinder?.initInterstitialAd(!prefs.getBooleanValue("is_first",true))
            }
        }
        bindService(Intent(this, CallService::class.java), conn, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(conn)
    }

    private fun startCountDownTime(time: Long) {
        jumpBtn.visibility = View.VISIBLE
        compositeDisposable.add(Observable.interval(0, 1, TimeUnit.SECONDS)//设置0延迟，每隔一秒发送一条数据
            .take(time + 1) //设置循环次数
            .map {
                time - it
            }
            .observeOn(AndroidSchedulers.mainThread())//操作UI主要在UI线程
            .subscribe({ it ->
                jumpBtn.text = "${it}秒跳过"
            }, {
                it.printStackTrace()
            }, {
//                handler.removeMessages(0)
                Dispatcher.dispatch(this) {
                    navigate(MainActivity::class.java)
                    defaultAnimate()
                }.go()
                finish()
            })
        )
    }

    fun jump(v:View){
        Dispatcher.dispatch(this) {
            navigate(MainActivity::class.java)
            defaultAnimate()
        }.go()
        finish()
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
}