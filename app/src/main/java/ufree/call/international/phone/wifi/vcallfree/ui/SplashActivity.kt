package ufree.call.international.phone.wifi.vcallfree.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_splash.*
import ufree.call.international.phone.wifi.vcallfree.MainActivity
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.lib.BaseActivity
import ufree.call.international.phone.wifi.vcallfree.utils.Dispatcher
import ufree.call.international.phone.wifi.vcallfree.utils.DrawableUtils
import ufree.call.international.phone.wifi.vcallfree.utils.dip2px
import java.util.concurrent.TimeUnit

/**
 * Created by lyf on 2020/5/19.
 */
class SplashActivity:BaseActivity() {
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
        startCountDownTime(3)
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
}