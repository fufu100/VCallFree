package ufree.call.international.phone.wifi.vcallfree.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.animation.addListener
import com.newmotor.x5.db.DBHelper
import kotlinx.android.synthetic.main.fragment_tab_coins.*
import kotlinx.android.synthetic.main.side_header.*
import kotlinx.coroutines.*
import ufree.call.international.phone.wifi.vcallfree.MainActivity
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.api.Api
import ufree.call.international.phone.wifi.vcallfree.api.User
import ufree.call.international.phone.wifi.vcallfree.databinding.FragmentTabCoinsBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseDataBindingFragment
import ufree.call.international.phone.wifi.vcallfree.utils.*
import ufree.call.international.phone.wifi.vcallfree.widget.CoinLayout

/**
 * Created by lyf on 2020/4/28.
 */
class CoinsFragment:BaseDataBindingFragment<FragmentTabCoinsBinding>(),CoinLayout.CanScrollVerticalChecker {
    var playCount = 0
    var strategy:PointStrategy? = null
    var job:Job? = null
    override fun getLayoutResId(): Int = R.layout.fragment_tab_coins
    override fun initView(v: View) {
        dataBinding.fragment = this
        dataBinding.coinLayout.checker = this


        dataBinding.totalPlayCount.text = UserManager.get().user?.max_wheel.toString()
        playCount = DBHelper.get().getPlayCount()
        dataBinding.playCountTv.text = playCount.toString()
    }

    override fun onResume() {
        super.onResume()
        dataBinding.totalCoins= UserManager.get().user?.points.toString()
    }

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }

    fun inviteFriends(v: View){
        Dispatcher.dispatch(context){
            navigate(InviteFriendsActivity::class.java)
            defaultAnimate()
        }.go()
    }

    fun signIn(v:View){
        SignInDialog(context!!).show()
    }

    fun start(v: View){
        if(!dataBinding.coinLayout.isOpen) {
            val endPos = getPoints()
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(dataBinding.goIv, "scaleX", 1f, 1.2f, 0.9f, 1.0f),
                ObjectAnimator.ofFloat(dataBinding.goTv, "scaleX", 1f, 1.2f, 0.9f, 1.0f),
                ObjectAnimator.ofFloat(dataBinding.goIv, "scaleY", 1f, 1.2f, 0.9f, 1.0f),
                ObjectAnimator.ofFloat(dataBinding.goTv, "scaleY", 1f, 1.2f, 0.9f, 1.0f)
            )
            animatorSet.duration = 300
            animatorSet.start()
            dataBinding.panView.startRotate(PointStrategy.points.size - endPos + 1) {
                if (endPos != 0) {
                    showObtainCoinsAlert(endPos)
                }
            }
            v.isClickable = false
        }
    }

    private fun showObtainCoinsAlert(pos:Int){
        AlertDialog.Builder(context!!)
            .setMessage("您获得了${PointStrategy.points[pos]}个金币")
            .setNegativeButton("放弃"){_,_->
                goIv.isClickable = true
            }
            .setPositiveButton("Get it"){_,_ ->
                playCount++
                DBHelper.get().addPlayCount(playCount)
                dataBinding.playCountTv.text = playCount.toString()
                Api.getApiService().addPoints(
                    mutableMapOf(
                        "uuid" to context!!.getDeviceId(),
                        "type" to "wheel",
                        "ts" to System.currentTimeMillis(),
                        "points" to PointStrategy.points[pos]
                    )
                )
                    .compose(RxUtils.applySchedulers())
                    .subscribe({
                        if (it.errcode == 0) {
                            UserManager.get().user?.points = it.points
                            dataBinding.totalCoins = UserManager.get().user!!.points.toString()
                            startTimeCount()
                        } else {
                            goIv.isClickable = true
                            context?.toast(it.errormsg)
                        }
                    }, {
                        goIv.isClickable = true
                        it.printStackTrace()
                    })
            }
            .setCancelable(false)
            .create()
            .show()
    }

    private fun startTimeCount(){
        dataBinding.goIv.isClickable = false
        dataBinding.goIv.setImageResource(R.drawable.ic_go2)
        job = GlobalScope.launch {
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
                    dataBinding.goIv.isClickable = true
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

    fun getPoints():Int{
        if(UserManager.get().user != null) {
            val x = UserManager.get().user!!.wheel_points / UserManager.get().user!!.max_wheel
            if(strategy == null){
                strategy = PointStrategy()
            }
            return strategy!!.getRandom(if(x < 50) PointStrategy.pointStrategy1 else PointStrategy.pointStrategy2)
        }else{
            return 0
        }
    }

    override fun canScrollVertical(): Boolean = true

    override fun onExpandStateChange(expand: Boolean) {
        dataBinding.drawerIv.animate().rotationBy(180f).setDuration(300).start()
    }
}