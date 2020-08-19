package vcall.free.international.phone.wifi.calling.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.newmotor.x5.db.DBHelper
import kotlinx.android.synthetic.main.fragment_tab_coins.*
import kotlinx.coroutines.*
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.databinding.FragmentTabCoinsBinding
import vcall.free.international.phone.wifi.calling.lib.BaseDataBindingFragment
import vcall.free.international.phone.wifi.calling.lib.prefs
import vcall.free.international.phone.wifi.calling.utils.*
import vcall.free.international.phone.wifi.calling.widget.CoinLayout
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.max

/**
 * Created by lyf on 2020/4/28.
 */
class CoinsFragment:BaseDataBindingFragment<FragmentTabCoinsBinding>(),CoinLayout.CanScrollVerticalChecker,AdManager.VCallAdListener {
    var playCount = 0
    var strategy:PointStrategy? = null
    var job:Job? = null
    var adState = 0
    var pointsToAdd = 0
    override fun getLayoutResId(): Int = R.layout.fragment_tab_coins
    override fun initView(v: View) {
        dataBinding.fragment = this
        dataBinding.coinLayout.checker = this


        dataBinding.totalPlayCount.text = UserManager.get().user?.max_wheel.toString()
        playCount = DBHelper.get().getPlayCount()
        dataBinding.playCountTv.text = playCount.toString()
        AdManager.get().interstitialAdListener.add(this)
        AdManager.get().rewardedAdListener.add(this)


    }

    override fun onResume() {
        super.onResume()
        dataBinding.totalCoins= UserManager.get().user?.points.toString()
        if(context?.isNetworkConnected() == false){
            dataBinding.goIv.isClickable = false
            dataBinding.goIv.setImageResource(R.drawable.ic_go2)
        }
    }

    override fun onDestroy() {
        AdManager.get().interstitialAdListener.remove(this)
        AdManager.get().rewardedAdListener.remove(this)
        job?.cancel()
        super.onDestroy()
    }

    fun showRewardedAd(v:View){
        if(AdManager.get().rewardedAd?.isLoaded == true) {
            AdManager.get().rewardedAd?.show(activity, object : RewardedAdCallback() {
                var earned = false
                override fun onUserEarnedReward(p0: RewardItem) {
                    Log.d(fragmentTag, "onUserEarnedReward $p0 ")
                    earned = true
                }

                override fun onRewardedAdClosed() {
                    if(earned){
                        pointsToAdd = PointStrategy.videoPoints[getVideotPoints()]
                        GameResultDialog(context!!,{
                            if(AdManager.get().interstitialAdMap[AdManager.ad_point]?.isLoaded == true) {
                                adState = 5
                                AdManager.get().showPointInterstitialAd()
                            }else{
                                AdManager.get().loadInterstitialAd(AdManager.ad_point)
                            }
                        }).apply {
                            setResult("+$pointsToAdd")
                        }.show()
                    }

                }
            })
        }else{
            Log.d(fragmentTag, "showRewardedAd-- not load ")
            GlobalScope.launch(Dispatchers.Main) {
                dataBinding.rewardedVideoTv.text = "Retry"
                withContext(Dispatchers.IO){
                    delay(2000)
                }
                dataBinding.rewardedVideoTv.text = resources.getString(R.string.reward_video_points)
            }
        }
    }

    fun inviteFriends(v: View){
        Dispatcher.dispatch(context){
            navigate(InviteFriendsActivity::class.java)
            defaultAnimate()
        }.go()
    }

    fun signIn(v:View){
        CheckInDialog(context!!){
            addPoint(it,"checkin")
        }.show()
    }

    fun start(v: View){
        if(!dataBinding.coinLayout.isOpen) {
           adState = 0
            pointsToAdd = 0
            //如果加载好了preclick广告则先展示广告，如果没加载到就转到thanks
            if(AdManager.get().interstitialAdMap[AdManager.ad_preclick]?.isLoaded == true) {
                AdManager.get().showPreclickInterstitialAd()
            }else{
                wheelStartRotate()
            }
        }
    }

    private fun wheelStartRotate(){
        var endPos = getPoints()
        if(adState == 0){
            println("$fragmentTag adState=0")
            endPos = 0
            AdManager.get().loadInterstitialAd(AdManager.ad_preclick)
        }
        if(AdManager.get().interstitialAdMap[AdManager.ad_point]?.isLoaded == false){
            println("$fragmentTag 积分广告还没加载")
            endPos = 0
            AdManager.get().loadInterstitialAd(AdManager.ad_point)
        }
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
            }else{
                goIv.isClickable = true
            }
        }
        goIv.isClickable = false
    }

    private fun showObtainCoinsAlert(pos:Int){
        GameResultDialog(context!!,{
            pointsToAdd = PointStrategy.points[pos]
            AdManager.get().showPointInterstitialAd()
        },{
            AdManager.get().rewardedAd?.show(activity,rewardedAdCallback)
        }).apply {
            setResult("+${PointStrategy.points[pos]}")
            pointsToAdd = max(500,PointStrategy.points[pos] * 2)//如果转到500就不翻倍了
            if(AdManager.get().rewardedAd?.isLoaded == true){
                showMore()
            }else{
                AdManager.get().loadRewardedAd()
            }
        }.show()
    }

    @SuppressLint("CheckResult")
    private fun addPoint(points:Int, type:String){
        if(type == "wheel") {
            playCount++
            DBHelper.get().addPlayCount(playCount)
            dataBinding.playCountTv.text = playCount.toString()
        }
        Api.getApiService().addPoints(
            mutableMapOf(
                "uuid" to context!!.getDeviceId(),
                "type" to type,
                "ts" to System.currentTimeMillis(),
                "points" to points
            )
        )
            .compose(RxUtils.applySchedulers())
            .subscribe({
                if (it.errcode == 0) {
                    UserManager.get().user?.points = it.points
                    dataBinding.totalCoins = UserManager.get().user!!.points.toString()
                    if(type == "wheel") {
                        startTimeCount()
                    }else if(type == "checkin"){
                        var consecutive_check:Int = prefs.getIntValue("check_max_day",0)
                        prefs.save("check_max_day",consecutive_check+1)
                        val format = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
                        prefs.save("last_check_day",format.format(Date()))
                    }
                    adState = 0
                } else {
                    if(type == "wheel") {
                        goIv.isClickable = true
                        context?.toast(it.errormsg)
                    }
                }
            }, {
                if(type == "wheel") {
                    goIv.isClickable = true
                }
                it.printStackTrace()
            })
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

    private fun getPoints():Int{
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

    private fun getVideotPoints():Int{
        if(UserManager.get().user != null) {
            val x = UserManager.get().user!!.wheel_points / UserManager.get().user!!.max_wheel
            if(strategy == null){
                strategy = PointStrategy()
            }
            return strategy!!.getRandom(if(x < 50) PointStrategy.videoStrategy1 else PointStrategy.videoStrategy2)
        }else{
            return 0
        }
    }

    override fun canScrollVertical(): Boolean = true

    override fun onExpandStateChange(expand: Boolean) {
        dataBinding.drawerIv.animate().rotationBy(180f).setDuration(300).start()
    }

    override fun onAdClose() {
        adState++
        if(adState == 2){
            wheelStartRotate()
        }else if(adState == 4){
            addPoint(pointsToAdd,"wheel")
        }else if(adState == 5){
            addPoint(pointsToAdd,"reward")
        }
    }

    override fun onAdShow() {
        adState++
    }

    override fun onAdLoaded() {

    }

    private val rewardedAdCallback = object : RewardedAdCallback() {
        var earned = false
        override fun onUserEarnedReward(p0: RewardItem) {
            Log.d(fragmentTag, "onUserEarnedReward $p0 ")
            earned = true
        }

        override fun onRewardedAdClosed() {
            if(earned){
                addPoint(pointsToAdd,"wheel")
            }else{
                adState = 0
                Log.d(fragmentTag, "onRewardedAdClosed:earn is false ")
            }

        }
    }

    val receiver:BroadcastReceiver = object :BroadcastReceiver(){
        override fun onReceive(context: Context, intent: Intent) {
            if("network_change" == intent.action){
                if(adState == 0 && !dataBinding.goIv.isClickable && context.isNetworkConnected()){
                    dataBinding.goIv.isClickable = true
                    dataBinding.goIv.setImageResource(R.drawable.ic_go)
                }
            }
        }

    }
}