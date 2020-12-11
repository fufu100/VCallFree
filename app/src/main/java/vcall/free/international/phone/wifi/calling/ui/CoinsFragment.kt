package vcall.free.international.phone.wifi.calling.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.newmotor.x5.db.DBHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_tab_coins.*
import kotlinx.coroutines.*
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.databinding.FragmentTabCoinsBinding
import vcall.free.international.phone.wifi.calling.lib.App
import vcall.free.international.phone.wifi.calling.lib.BaseDataBindingFragment
import vcall.free.international.phone.wifi.calling.lib.prefs
import vcall.free.international.phone.wifi.calling.utils.*
import vcall.free.international.phone.wifi.calling.widget.CoinLayout
import vcall.free.international.phone.wifi.calling.widget.Loading
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.math.max
import kotlin.math.min

/**
 * Created by lyf on 2020/4/28.
 */
class CoinsFragment:BaseDataBindingFragment<FragmentTabCoinsBinding>(),CoinLayout.CanScrollVerticalChecker,AdManager.VCallAdListener {
    var playCount = 0
    var strategy:PointStrategy? = null
    var job:Job? = null
    var adState = -1
    var pointsToAdd = 0
    lateinit var loading:Loading
    override fun getLayoutResId(): Int = R.layout.fragment_tab_coins
    override fun initView(v: View) {
        dataBinding.fragment = this
        dataBinding.coinLayout.checker = this
        loading = Loading(activity!!)

        dataBinding.totalPlayCount.text = (UserManager.get().user?.max_wheel?:1000).toString()
        playCount = DBHelper.get().getPlayCount()
        dataBinding.playCountTv.text = playCount.toString()
        AdManager.get().interstitialAdListener.add(this)
        AdManager.get().rewardedAdListener.add(this)


    }

    override fun onResume() {
        super.onResume()
        if(dataBinding.totalCoins != UserManager.get().user?.points.toString()){
            dataBinding.totalCoins= (UserManager.get().user?.points?:1000).toString()
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(
                ObjectAnimator.ofFloat(dataBinding.coinIv, "scaleX", 1f, 1.2f, 0.9f, 1.0f),
                ObjectAnimator.ofFloat(dataBinding.coinIv, "scaleX", 1f, 1.2f, 0.9f, 1.0f),
                ObjectAnimator.ofFloat(dataBinding.coinIv, "scaleY", 1f, 1.2f, 0.9f, 1.0f),
                ObjectAnimator.ofFloat(dataBinding.coinIv, "scaleY", 1f, 1.2f, 0.9f, 1.0f)
            )
            animatorSet.duration = 800
            animatorSet.startDelay = 1000
            animatorSet.start()
        }

        LogUtils.println("$fragmentTag onResume ${UserManager.get().user?.phone} ${UserManager.get().user?.phone?.isNotEmpty()}")
        if(UserManager.get().user?.phone?.isNotEmpty() == true){
            dataBinding.setPhoneNumberLayout.visibility = View.GONE
            GlobalScope.launch {
                delay(200)
                withContext(Dispatchers.Main){
                    if(!dataBinding.coinLayout.needSwipe){
                        dataBinding.drawerIv.visibility = View.INVISIBLE
                    }
                }
            }

        }

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
                    AdManager.get().loadRewardedAd()
                    if(earned){
                        pointsToAdd = PointStrategy.videoPoints[getVideotPoints()]
                        GameResultDialog(context!!,{
                            if(AdManager.get().interstitialAdMap[AdManager.ad_point]?.isLoaded == true) {
                                adState = 5
                                AdManager.get().showPointInterstitialAd()
                            }else{
                                AdManager.get().loadInterstitialAd(AdManager.ad_point)
                                //如果广告没有加载仍然增加积分
                                addPoint(pointsToAdd,"reward")
                            }

                        }).apply {
                            setResult("+$pointsToAdd")
                        }.show()
                    }

                }
            })
        }else{
            Log.d(fragmentTag, "showRewardedAd-- not load ")
            AdManager.get().loadRewardedAd()
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
//            AdManager.get().showPointInterstitialAd()
//            addPoint(it,"checkin")
            pointsToAdd = it
            GameResultDialog(context!!,{
                if(AdManager.get().interstitialAdMap[AdManager.ad_point]?.isLoaded == true) {
                    adState = 7
                    AdManager.get().showPointInterstitialAd()
                }else{
                    AdManager.get().loadInterstitialAd(AdManager.ad_point)
                    //如果广告没有加载仍然增加积分
                    addPoint(pointsToAdd,"checkin")
                }
            }).apply {
                setResult("+$pointsToAdd")
            }.show()

        }.show()
    }

    @SuppressLint("CheckResult")
    fun start(v: View?){
        if(!dataBinding.coinLayout.isOpen && UserManager.get().user != null) {
            if(!dataBinding.goIv.isClickable){
                return
            }
            val b:Calendar = Calendar.getInstance()
            val c:Calendar = Calendar.getInstance()
            c.timeInMillis = UserManager.get().user!!.time
            LogUtils.println("start ${UserManager.get().user!!.time} ${c.timeInMillis} ${b.timeInMillis}")
            if(c.get(Calendar.DAY_OF_MONTH) == b.get(Calendar.DAY_OF_MONTH) ) {
                LogUtils.println("没有过期 ${c.get(Calendar.DAY_OF_MONTH)}")
                adState = 0
                pointsToAdd = 0
                //如果加载好了preclick广告则先展示广告，如果没加载到就转到thanks
                LogUtils.println("$fragmentTag start--${AdManager.get().interstitialAdMap[AdManager.ad_preclick]?.isLoaded}")
                if (AdManager.get().interstitialAdMap[AdManager.ad_preclick]?.isLoaded == true) {
                    AdManager.get().showPreclickInterstitialAd()
                } else {
                    AdManager.get().loadInterstitialAd(AdManager.ad_preclick)
                    wheelStartRotate()
                }
            }else{
                LogUtils.println("过期了，重新获取")
                val map = mutableMapOf<String,String>()
                map.putAll(App.requestMap)
                map.put("uuid",context!!.getDeviceId())
                Api.getApiService().signup(map)
                    .compose(RxUtils.applySchedulers())
                    .subscribe({
                        if (it.errcode == 0) {
                            it.time = System.currentTimeMillis()
                            UserManager.get().user = it
                            start(v)
                        }
                    }, {
                        it.printStackTrace()
                    })

            }
        }
    }

    private fun wheelStartRotate(duration:Long = 0){
        var endPos = getPoints()
        if(adState == 0){
            LogUtils.println("$fragmentTag adState=0")
            endPos = 0
//            AdManager.get().loadInterstitialAd(AdManager.ad_preclick)
        }
        if(AdManager.get().interstitialAdMap[AdManager.ad_point]?.isLoaded == false && AdManager.get().rewardedAd?.isLoaded == false){
            LogUtils.println("$fragmentTag 积分广告还没加载")
            endPos = 0
            AdManager.get().loadInterstitialAd(AdManager.ad_point)
        }else if(AdManager.get().interstitialAdMap[AdManager.ad_point]?.isLoaded == false && AdManager.get().rewardedAd?.isLoaded == true){
            endPos = 4//积分广告没加载 激励视频加载了，给50积分
            AdManager.get().loadInterstitialAd(AdManager.ad_point)
        }
        if(endPos == 0){
            adState = -1
        }
        println("CoinFragment wheelStartRotate endPos=$endPos ,${AdManager.get().interstitialAdMap[AdManager.ad_point]} ${AdManager.get().interstitialAdMap[AdManager.ad_point]?.isLoaded}")
        val animatorSet = AnimatorSet()
        animatorSet.playTogether(
            ObjectAnimator.ofFloat(dataBinding.goIv, "scaleX", 1f, 1.2f, 0.9f, 1.0f),
            ObjectAnimator.ofFloat(dataBinding.goTv, "scaleX", 1f, 1.2f, 0.9f, 1.0f),
            ObjectAnimator.ofFloat(dataBinding.goIv, "scaleY", 1f, 1.2f, 0.9f, 1.0f),
            ObjectAnimator.ofFloat(dataBinding.goTv, "scaleY", 1f, 1.2f, 0.9f, 1.0f)
        )
        animatorSet.duration = 300
        animatorSet.start()
        dataBinding.panView.startRotate(PointStrategy.points.size - endPos + 1,duration) {
            if (endPos != 0) {
                showObtainCoinsAlert(endPos)
            }else{
//                goIv.isClickable = true
                startTimeCount()
            }
        }
        goIv.isClickable = false
    }

    private fun showObtainCoinsAlert(pos:Int){
        GameResultDialog(context!!,{
            if(AdManager.get().interstitialAdMap[AdManager.ad_point]?.isLoaded == false && AdManager.get().rewardedAd?.isLoaded == true){
                pointsToAdd = PointStrategy.points[pos]
                AdManager.get().rewardedAd?.show(activity,rewardedAdCallback)
            }else {
                pointsToAdd = PointStrategy.points[pos]
                AdManager.get().showPointInterstitialAd()
            }
        },{
            AdManager.get().rewardedAd?.show(activity,rewardedAdCallback)
        }).apply {
            setResult("+${PointStrategy.points[pos]}")
            pointsToAdd = min(500,PointStrategy.points[pos] * 2)//如果转到500就不翻倍了
            if(AdManager.get().rewardedAd?.isLoaded == true && AdManager.get().interstitialAdMap[AdManager.ad_point]?.isLoaded == true){
                showMore()
            }else{
                if(AdManager.get().rewardedAd?.isLoaded == false) {
                    AdManager.get().loadRewardedAd()
                }
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
        loading.show()
        Api.getApiService().addPoints(
            mutableMapOf(
                "uuid" to context!!.getDeviceId(),
                "type" to type,
                "ts" to System.currentTimeMillis(),
                "points" to points,
                "country" to Locale.getDefault().country
            )
        )
            .doOnNext {
                if(it.errcode == 0) {
                    DBHelper.get().setTodayCredits(DBHelper.get().getTodayCredits() + points)
                }
            }
            .compose(RxUtils.applySchedulers())
            .subscribe({
                loading.dismiss()
                if (it.errcode == 0) {
                    UserManager.get().user?.points = it.points
                    dataBinding.totalCoins = UserManager.get().user!!.points.toString()
                    val animatorSet = AnimatorSet()
                    animatorSet.playTogether(
                        ObjectAnimator.ofFloat(dataBinding.coinIv, "scaleX", 1f, 1.2f, 0.9f, 1.0f),
                        ObjectAnimator.ofFloat(dataBinding.coinIv, "scaleX", 1f, 1.2f, 0.9f, 1.0f),
                        ObjectAnimator.ofFloat(dataBinding.coinIv, "scaleY", 1f, 1.2f, 0.9f, 1.0f),
                        ObjectAnimator.ofFloat(dataBinding.coinIv, "scaleY", 1f, 1.2f, 0.9f, 1.0f)
                    )
                    animatorSet.duration = 800
                    animatorSet.start()
                    if(type == "wheel") {
                        startTimeCount()
                    }else if(type == "checkin"){
                        var consecutive_check:Int = prefs.getIntValue("check_max_day",0)
                        prefs.save("check_max_day",consecutive_check+1)
                        val format = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
                        prefs.save("last_check_day",format.format(Date()))
                    }
                    adState = -1
                    Dispatcher.dispatch(context){
                        action("refresh_notification")
                    }.send()
                } else {
                    if(type == "wheel") {
                        goIv.isClickable = true
                        context?.toast(it.errormsg)
                    }else if(type == "checkin"){
                        context?.toast(it.errormsg)
                    }
                }

            }, {
                loading.dismiss()
                if(type == "wheel") {
                    goIv.isClickable = true
                }
                context?.toast(R.string.net_error)
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
                LogUtils.println("i=$i")
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
            dataBinding.totalCoins= (UserManager.get().user?.points?:1000).toString()
            dataBinding.totalPlayCount.text = UserManager.get().user?.max_wheel.toString()
            if(UserManager.get().user?.phone?.isNotEmpty() == true){
                dataBinding.setPhoneNumberLayout.visibility = View.GONE
                GlobalScope.launch {
                    delay(200)
                    withContext(Dispatchers.Main){
                        if(!dataBinding.coinLayout.needSwipe){
                            dataBinding.drawerIv.visibility = View.INVISIBLE
                        }
                    }
                }

            }
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
        LogUtils.println("$fragmentTag onAdClose adState=$adState")
        if(adState == 2){
            wheelStartRotate(1000L)
            AdManager.get().loadInterstitialAd(AdManager.ad_preclick)
        }else if(adState == 4){
            AdManager.get().loadInterstitialAd(AdManager.ad_point)
            addPoint(pointsToAdd,"wheel")
        }else if(adState == 7){
            AdManager.get().loadInterstitialAd(AdManager.ad_point)
            addPoint(pointsToAdd,"reward")
        }else if(adState == 9){
            AdManager.get().loadInterstitialAd(AdManager.ad_point)
            addPoint(pointsToAdd,"checkin")
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
            //转盘的激励视频如果没有看完视频则不增加转盘积分
            AdManager.get().loadRewardedAd()
            if(earned){
                addPoint(pointsToAdd,"wheel")
            }else{
                adState = 0
                startTimeCount()
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