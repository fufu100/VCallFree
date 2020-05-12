package ufree.call.international.phone.wifi.vcallfree.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import ufree.call.international.phone.wifi.vcallfree.MainActivity
import ufree.call.international.phone.wifi.vcallfree.R
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
        val drawable = DrawableUtils.generate {
            solidColor(context!!.getColorFromRes(R.color.colorPrimary))
            radius(context!!.dip2px(15))
            build()
        }
        dataBinding.totalCoinsTv.background = drawable
        dataBinding.coinCost.background = drawable

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
            ObjectAnimator.ofFloat(dataBinding.goIv,"scaleY",1f,1.2f,0.9f ,1.0f))
        animatorSet.duration = 300
        animatorSet.start()
        dataBinding.panView.startRotate(3)
        playCount++
        dataBinding.totalCoinsTv.text = (UserManager.get().user?.points?:0 - (playCount * 100)).toString()
        dataBinding.coinCost.text = "${playCount * 100}/100"
    }

    fun setPhoneNumber(v: View){
        Dispatcher.dispatch(context){
            navigate(SetPhoneNumberActivity::class.java)
            defaultAnimate()
        }.go()
    }

    override fun canScrollVertical(): Boolean = true
}