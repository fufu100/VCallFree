package ufree.call.international.phone.wifi.vcallfree.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.core.animation.addListener
import kotlinx.coroutines.*
import ufree.call.international.phone.wifi.vcallfree.MainActivity
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.api.Api
import ufree.call.international.phone.wifi.vcallfree.databinding.FragmentTabCoinsBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseDataBindingFragment
import ufree.call.international.phone.wifi.vcallfree.utils.*
import ufree.call.international.phone.wifi.vcallfree.widget.CoinLayout

/**
 * Created by lyf on 2020/4/28.
 */
class CoinsFragment:BaseDataBindingFragment<FragmentTabCoinsBinding>(),CoinLayout.CanScrollVerticalChecker {
    var playCount = 0
    override fun getLayoutResId(): Int = R.layout.fragment_tab_coins
    override fun initView(v: View) {
        dataBinding.fragment = this
        dataBinding.coinLayout.checker = this

        dataBinding.totalCoins= UserManager.get().user?.points.toString()
        dataBinding.totalPlayCount.text = UserManager.get().user?.max_wheel.toString()
        val drawable = DrawableUtils.generate {
            solidColor(context!!.getColorFromRes(R.color.colorPrimary))
            radius(context!!.dip2px(15))
            build()
        }
        dataBinding.totalCoinsTv.background = drawable
        dataBinding.playCount.background = drawable

//        dataBinding.goTv.paint.strokeWidth = 3f
//        dataBinding.goTv.paint.color = Color.GREEN

        println("CoinsFragment initView--")
    }

    fun inviteFriends(v: View){
        Dispatcher.dispatch(context){
            navigate(InviteFriendsActivity::class.java)
            defaultAnimate()
        }.go()
    }

    fun start(v: View){
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(dataBinding.goIv,"scaleX",1f,1.2f,0.9f ,1.0f),
            ObjectAnimator.ofFloat(dataBinding.goTv,"scaleX",1f,1.2f,0.9f ,1.0f),
            ObjectAnimator.ofFloat(dataBinding.goIv,"scaleY",1f,1.2f,0.9f ,1.0f),
            ObjectAnimator.ofFloat(dataBinding.goTv,"scaleY",1f,1.2f,0.9f ,1.0f))
        animatorSet.duration = 300
        animatorSet.addListener({
            Api.getApiService().addPoints(mutableMapOf("uuid" to context!!.getDeviceId(),"type" to "wheel","ts" to System.currentTimeMillis(),"points" to "500"))
                .compose(RxUtils.applySchedulers())
                .subscribe({
                    if(it.errcode == 0){
                        UserManager.get().user?.points = it.points
                        dataBinding.totalCoins = UserManager.get().user!!.points.toString()
                    }else{
                        context?.toast(it.errormsg)
                    }
                },{
                    it.printStackTrace()
                })
        },{},{},{})
        animatorSet.start()
        dataBinding.panView.startRotate(3)
        playCount++
        dataBinding.playCountTv.text = playCount.toString()

        v.isClickable = false
        dataBinding.goIv.setImageResource(R.drawable.ic_go2)
        GlobalScope.launch {
            for(i in UserManager.get().user!!.interval downTo  0){
                withContext(Dispatchers.Main){
                    dataBinding.goTv.text = i.toString()
                }
                println("i=$i")
                delay(1000)
            }
            withContext(Dispatchers.Main) {
                dataBinding.goTv.text = "GO"
                if(playCount < UserManager.get().user!!.max_wheel){
                    v.isClickable = true
                    dataBinding.goIv.setImageResource(R.drawable.ic_go3)
                }
            }
        }
    }

    fun refreshUser(){
        if(isDataBindingInitialized()){
            dataBinding.totalCoins= UserManager.get().user?.points.toString()
            dataBinding.totalPlayCount.text = UserManager.get().user?.max_wheel.toString()
        }

    }

    fun setPhoneNumber(v: View){
        Dispatcher.dispatch(context){
            navigate(SetPhoneNumberActivity::class.java)
            defaultAnimate()
        }.go()
    }

    override fun canScrollVertical(): Boolean = true

    override fun onExpandStateChange(expand: Boolean) {
        dataBinding.drawerIv.animate().rotationBy(180f).setDuration(300).start()
    }
}