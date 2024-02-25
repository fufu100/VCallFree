package vcall.free.international.phone.wifi.calling.ui

import android.animation.*
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardItem
import vcall.free.international.phone.wifi.calling.db.DBHelper
import kotlinx.coroutines.*
import vcall.free.international.phone.wifi.calling.BuildConfig
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
    var animator: ObjectAnimator? = null
    var currentPlayTime = 0L
    override fun getLayoutResId(): Int = R.layout.fragment_tab_coins
    override fun initView(v: View) {
        dataBinding.fragment = this
        dataBinding.coinLayout.checker = this
        loading = Loading(requireActivity())

        dataBinding.totalPlayCount.text = (UserManager.get().user?.max_wheel?:1000).toString()
        playCount = DBHelper.get().getPlayCount()
        dataBinding.playCountTv.text = playCount.toString()
        AdManager.get().interstitialAdListener[AdManager.ad_preclick] = this
        AdManager.get().interstitialAdListener[AdManager.ad_point] = this
        AdManager.get().interstitialAdListener[AdManager.ad_rewarded] = rewardAdLoadCallback
//        AdManager.get().rewardedAdListener.add(this)

    }

    override fun onResume() {
        super.onResume()
        AdManager.get().interstitialAdListener[AdManager.ad_preclick] = this
        AdManager.get().interstitialAdListener[AdManager.ad_point] = this
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
            println("$tag 没有网络")
            dataBinding.goIv.isClickable = false
            dataBinding.goIv.setImageResource(R.drawable.ic_go2)
        }

        val format = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
        val lastCheckDay = prefs.getStringValue("last_check_day","")
        val today = format.format(Date())
        if(today == lastCheckDay){
            dataBinding.checkInBtn.setBackgroundResource(R.drawable.bg_gray_round)
        }else{
            dataBinding.checkInBtn.setBackgroundResource(R.drawable.bg_blue_round)
        }
        val count = DBHelper.get().getLuckyCreditsClickCount()
        LogUtils.println("$tag onResume showLuckyCredits=${AdManager.get().showLuckyCredits} count=$count")
        if(AdManager.get().showLuckyCredits && count < 10){
            dataBinding.luckyCreditsTv.setBackgroundResource(R.drawable.bg_blue_round)
            if(animator == null) {
                animator = tata(dataBinding.luckyCreditsIv, 1f)
                animator?.repeatCount = ValueAnimator.INFINITE
            }else{
                animator?.currentPlayTime = currentPlayTime
            }
            animator?.start()
        }else{
            dataBinding.luckyCreditsTv.setBackgroundResource(R.drawable.bg_gray_round)
        }
        if(AdManager.get().rewardedAd != null && AdManager.get().interstitialAdLoadStatus[AdManager.ad_rewarded] == 1){
            dataBinding.rewardedVideoTv.setBackgroundResource(R.drawable.bg_blue_round)
        }else{
            dataBinding.rewardedVideoTv.setBackgroundResource(R.drawable.bg_gray_round)
        }

        refreshAdStatusLayout()
    }

    override fun onPause() {
        super.onPause()
        if(!AdManager.get().showLuckyCredits){
            if(animator?.isRunning == true){
                currentPlayTime = animator!!.currentPlayTime
                animator?.cancel()
            }
        }
    }

    override fun onDestroy() {
        AdManager.get().interstitialAdListener.remove(AdManager.ad_point)
        AdManager.get().interstitialAdListener.remove(AdManager.ad_preclick)
        AdManager.get().interstitialAdListener.remove(AdManager.ad_rewarded)
//        AdManager.get().rewardedAdListener.remove(this)
        job?.cancel()
        super.onDestroy()
    }

    fun showRewardedAd(v:View){
        if(AdManager.get().rewardedAd != null) {
            showRewardPointsTipDialog {
                AdManager.get().rewardedAd?.fullScreenContentCallback =
                    object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            AdManager.get().rewardedAd = null
                            AdManager.get().loadRewardedAd(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            AdManager.get().rewardedAd = null
                        }

                        override fun onAdShowedFullScreenContent() {
                            Log.d(tag, "onAdShowedFullScreenContent---")
                        }
                    }
                AdManager.get().rewardedAd?.show(requireActivity(), OnUserEarnedRewardListener {
                    pointsToAdd = PointStrategy.videoPoints[getVideotPoints()]
                    GameResultDialog(requireContext(), {
                        if (AdManager.get().interstitialAdMap[AdManager.ad_point] != null) {
                            adState = 5
                            AdManager.get().showPointInterstitialAd(activity)
                        } else {
                            AdManager.get().loadInterstitialAd(context, AdManager.ad_point)
                            //如果广告没有加载仍然增加积分
                            addPoint(pointsToAdd, "reward")
                        }

                    }).apply {
                        setResult("+$pointsToAdd")
                    }.show()
                })
            }
        }else{
            Log.d(fragmentTag, "showRewardedAd-- not load ")
            AdManager.get().loadRewardedAd(context)
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

    fun luckyCredits(v: View){
        val count = DBHelper.get().getLuckyCreditsClickCount()
        Log.d(fragmentTag, "luckyCredits count=$count,showLuckyCredits=${AdManager.get().showLuckyCredits} context:${context == null}")
        if(count < 10) {
            if (AdManager.get().showLuckyCredits) {
                if(context == null){
                    return
                }
                DBHelper.get().addLuckyCreditsClickCount()
                GameResultDialog(requireContext(), {
                    if (AdManager.get().rewardedAd != null) {
//                        adState = 10
//                        AdManager.get().showPointInterstitialAd(activity)
                        AdManager.get().rewardedAd?.fullScreenContentCallback = object :FullScreenContentCallback(){
                            override fun onAdDismissedFullScreenContent() {
                                Log.d(tag, "onAdDismissedFullScreenContent--- ")
                                AdManager.get().rewardedAd = null
                                AdManager.get().loadRewardedAd(context)
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                Log.d(tag, "onAdFailedToShowFullScreenContent--- $p0")
                                AdManager.get().rewardedAd = null
                            }
                        }
                        AdManager.get().rewardedAd?.show(requireActivity(),object :OnUserEarnedRewardListener{
                            override fun onUserEarnedReward(p0: RewardItem) {
                                addPoint(pointsToAdd,"reward")
                            }

                        })
                    }
                }).apply {
                    val r = Random()
                    pointsToAdd = (r.nextInt(5) + 5) * 10
                    setResult("+$pointsToAdd")
                }.show()
                if(animator != null) {
                    currentPlayTime = animator!!.currentPlayTime
                    animator?.cancel()
                }
                AdManager.get().showLuckyCredits = false
                dataBinding.luckyCreditsTv.setBackgroundResource(R.drawable.bg_gray_round)
            }
        }

    }

    fun signIn(v:View){
        CheckInDialog(requireContext()){
//            AdManager.get().showPointInterstitialAd()
//            addPoint(it,"checkin")
            pointsToAdd = it
            GameResultDialog(requireContext(),{
                if(AdManager.get().interstitialAdMap[AdManager.ad_point] != null) {
                    adState = 7
                    AdManager.get().showPointInterstitialAd(activity)
                }else{
                    AdManager.get().loadInterstitialAd(context,AdManager.ad_point)
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
                if(isDeviceInVPN() && prefs.getBooleanValue("show_tip_vpn",true)){
                    showVPNDialog()
                }else{
                    LogUtils.println("$fragmentTag start--${AdManager.get().interstitialAdMap[AdManager.ad_preclick] != null}")
                    if (AdManager.get().interstitialAdMap[AdManager.ad_preclick] != null) {
                        AdManager.get().showPreclickInterstitialAd(activity)
                    } else {
                        AdManager.get().loadInterstitialAd(context,AdManager.ad_preclick)
                        wheelStartRotate(1000)
                    }
                }
            }else{
                LogUtils.println("过期了，重新获取")
//                val map = mutableMapOf<String,String>()
//                map.putAll(App.requestMap)
//                map.put("uuid",requireContext().getDeviceId())
//                Api.getApiService().signup(map)
//                    .compose(RxUtils.applySchedulers())
//                    .subscribe({
//                        if (it.errcode == 0) {
//                            it.time = System.currentTimeMillis()
//                            UserManager.get().user = it
//                            start(v)
//                        }
//                    }, {
//                        it.printStackTrace()
//                    })

            }
        }
    }

    private fun showVPNDialog(){
        prefs.save("show_tip_vpn",false)
        AlertDialog.Builder(requireContext()).setMessage(R.string.tip_vpn)
            .setPositiveButton(R.string.confirm){_,_ ->
                LogUtils.println("$fragmentTag start--${AdManager.get().interstitialAdMap[AdManager.ad_preclick] != null}")
                if (AdManager.get().interstitialAdMap[AdManager.ad_preclick] != null) {
                    AdManager.get().showPreclickInterstitialAd(activity)
                } else {
                    AdManager.get().loadInterstitialAd(context,AdManager.ad_preclick)
                    wheelStartRotate(1000)
                }
            }
            .create()
            .show()
    }

    private fun showRewardPointsTipDialog(callback:() -> Unit){
        AlertDialog.Builder(requireContext()).setMessage(R.string.tip_reward_video)
            .setPositiveButton("Get it"){_,_ ->
                callback()
            }
            .create()
            .show()
    }

    private fun wheelStartRotate(duration:Long = 0){
        var endPos = getPoints()
        if(adState == 0){
            LogUtils.println("$fragmentTag adState=0")
//            endPos = 0
//            AdManager.get().loadInterstitialAd(AdManager.ad_preclick)
        }
        if((AdManager.get().interstitialAdMap[AdManager.ad_point] == null) and (AdManager.get().rewardedAd == null)){
            LogUtils.println("$fragmentTag 积分广告还没加载")
            if(AdManager.get().interstitialAdMap[AdManager.ad_preclick] != null) {
                endPos = 0
            }else{
                endPos = 0
            }
            AdManager.get().loadInterstitialAd(context,AdManager.ad_point)
        }else if((AdManager.get().interstitialAdMap[AdManager.ad_point] == null) and (AdManager.get().rewardedAd != null)){
            endPos = 3//积分广告没加载 激励视频加载了，给50积分
            AdManager.get().loadInterstitialAd(context,AdManager.ad_point)
        }else if((AdManager.get().interstitialAdMap[AdManager.ad_point] != null) and (AdManager.get().interstitialAdMap[AdManager.ad_preclick] == null)){
            endPos = 0//积分广告加载了，预触发广告没加载，给5积分，2021-2-10新增
        }
        if(endPos == 0){
            adState = -1
        }
        println("CoinFragment wheelStartRotate endPos=$endPos ,${AdManager.get().interstitialAdMap[AdManager.ad_preclick] != null} ${AdManager.get().interstitialAdMap[AdManager.ad_point] != null} ${AdManager.get().rewardedAd != null}")
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
//            if (endPos != 0) {
                showObtainCoinsAlert(endPos)
//            }else{
////                goIv.isClickable = true
//                startTimeCount()
//            }
        }
        dataBinding.goIv.isClickable = false
    }

    private fun showObtainCoinsAlert(pos:Int){
        if(context != null) {
            GameResultDialog(requireContext(), {
                if ((AdManager.get().interstitialAdMap[AdManager.ad_point] == null) and (AdManager.get().rewardedAd != null)) {
                    pointsToAdd = PointStrategy.points[pos]
                    AdManager.get().rewardedAd?.fullScreenContentCallback = object :FullScreenContentCallback(){
                        override fun onAdDismissedFullScreenContent() {
                            Log.d(tag, "onAdDismissedFullScreenContent--- ")
                            AdManager.get().rewardedAd = null
                            AdManager.get().loadRewardedAd(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            Log.d(tag, "onAdFailedToShowFullScreenContent--- $p0")
                            AdManager.get().rewardedAd = null
                        }
                    }
                    AdManager.get().rewardedAd?.show(requireActivity(),rewardedAdCallback)
                } else if ((AdManager.get().interstitialAdMap[AdManager.ad_point] != null)) {
                    pointsToAdd = PointStrategy.points[pos]
                    if (adState != 2) {
                        adState = 2
                    }
                    AdManager.get().showPointInterstitialAd(activity)
                } else {
                    pointsToAdd = PointStrategy.points[pos]
                    addPoint(pointsToAdd, "wheel")
                }
            }, {
                showRewardPointsTipDialog {
                    pointsToAdd = PointStrategy.points[pos] * 2
                    AdManager.get().rewardedAd?.fullScreenContentCallback = object :FullScreenContentCallback(){
                        override fun onAdDismissedFullScreenContent() {
                            AdManager.get().rewardedAd = null
                            AdManager.get().loadRewardedAd(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            AdManager.get().rewardedAd = null
                        }
                    }
                    AdManager.get().rewardedAd?.show(requireActivity(),rewardedAdCallback)
                }
            }).apply {
                setResult("+${PointStrategy.points[pos]}")
//            pointsToAdd = min(200,PointStrategy.points[pos] * 2)//如果转到500就不翻倍了
                if ((AdManager.get().rewardedAd != null) and (AdManager.get().interstitialAdMap[AdManager.ad_point] != null) and (pos < 6)) {
                    showMore()
                } else {
                    if (AdManager.get().rewardedAd == null) {
                        AdManager.get().loadRewardedAd(context)
                    }
                }
            }.show()
        }
    }

    @SuppressLint("CheckResult")
    private fun addPoint(points:Int, type:String){
        if(type == "wheel") {
            playCount++
            DBHelper.get().addPlayCount(playCount)
            dataBinding.playCountTv.text = playCount.toString()
        }
        loading.show()
        val ts = System.currentTimeMillis()
        val key = AESUtils.encrypt(ts.toString(),(ts - Api.ts).toString())
        Api.getApiService().addPoints(
            mutableMapOf(
                "uuid" to requireContext().getDeviceId(),
                "type" to type,
                "ver" to requireContext().getVersionName(),
                "ts" to System.currentTimeMillis(),
                "points" to points,
                "country" to Locale.getDefault().country,
                "ts" to ts,
                "key" to key
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
                    Api.ts = ts
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
                        dataBinding.checkInBtn.setBackgroundResource(R.drawable.bg_gray_round)
                    }
                    adState = -1
                    Dispatcher.dispatch(context){
                        action("refresh_notification")
                    }.send()
                } else {
                    if(type == "wheel") {
                        dataBinding.goIv.isClickable = true
                        context?.toast(it.errormsg)
                    }else if(type == "checkin"){
                        context?.toast(it.errormsg)
                    }
                }

            }, {
                loading.dismiss()
                if(type == "wheel" && dataBinding.goIv != null) {
                    dataBinding.goIv.isClickable = true
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
            if(strategy == null){
                strategy = PointStrategy()
            }
            if(isDeviceInVPN()){
                println("当前有VPN，匹配低分策略---")
                return strategy!!.getRandom(PointStrategy.pointStrategy1)
            }
            return if(UserManager.get().user!!.wheel_points < 7500){
                strategy!!.getRandom(PointStrategy.pointStrategy1)
            }else if((UserManager.get().user!!.wheel_points >= 7500) and (UserManager.get().user!!.wheel_points < 10000)){
                strategy!!. getRandom(PointStrategy.pointStrategy2)
            }else if((UserManager.get().user!!.wheel_points >= 10000) and (UserManager.get().user!!.wheel_points < 15000)){
                strategy!!.getRandom(PointStrategy.pointStrategy3)
            }else if((UserManager.get().user!!.wheel_points >= 15000) and (UserManager.get().user!!.wheel_points < 20000)){
                strategy!!.getRandom(PointStrategy.pointStrategy4)
            }else if((UserManager.get().user!!.wheel_points >= 20000) and (UserManager.get().user!!.wheel_points < 35000)){
                strategy!!.getRandom(PointStrategy.pointStrategy5)
            }else if((UserManager.get().user!!.wheel_points >= 35000) and (UserManager.get().user!!.wheel_points < 50000)){
                strategy!!.getRandom(PointStrategy.pointStrategy6)
            }else if((UserManager.get().user!!.wheel_points >= 50000) and (UserManager.get().user!!.wheel_points < 75000)){
                strategy!!.getRandom(PointStrategy.pointStrategy7)
            }else if((UserManager.get().user!!.wheel_points >= 75000) and (UserManager.get().user!!.wheel_points < 100000)){
                strategy!!.getRandom(PointStrategy.pointStrategy8)
            }else{
                strategy!!. getRandom(PointStrategy.pointStrategy9)
            }
        }else{
            return 0
        }
    }

    private fun getVideotPoints():Int{
        if(UserManager.get().user != null) {
            if(strategy == null){
                strategy = PointStrategy()
            }
            return if(UserManager.get().user!!.reward_points < 7500){
                strategy!!.getRandom(PointStrategy.pointStrategy1)
            }else if((UserManager.get().user!!.reward_points >= 7500) and (UserManager.get().user!!.reward_points < 10000)){
                strategy!!.getRandom(PointStrategy.pointStrategy2)
            }else if((UserManager.get().user!!.reward_points >= 10000) and (UserManager.get().user!!.reward_points < 15000)){
                strategy!!.getRandom(PointStrategy.pointStrategy3)
            }else if((UserManager.get().user!!.reward_points >= 15000) and (UserManager.get().user!!.reward_points < 20000)){
                strategy!!.getRandom(PointStrategy.pointStrategy4)
            }else if((UserManager.get().user!!.reward_points >= 20000) and (UserManager.get().user!!.reward_points < 35000)){
                strategy!!.getRandom(PointStrategy.pointStrategy5)
            }else if((UserManager.get().user!!.reward_points >= 35000) and (UserManager.get().user!!.reward_points < 50000)){
                strategy!!.getRandom(PointStrategy.pointStrategy6)
            }else if((UserManager.get().user!!.reward_points >= 50000) and (UserManager.get().user!!.reward_points < 75000)){
                strategy!!.getRandom(PointStrategy.pointStrategy7)
            }else if((UserManager.get().user!!.reward_points >= 75000) and (UserManager.get().user!!.reward_points < 100000)){
                strategy!!.getRandom(PointStrategy.pointStrategy8)
            }else{
                strategy!!.getRandom(PointStrategy.pointStrategy9)
            }
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
            AdManager.get().loadInterstitialAd(context,AdManager.ad_preclick)
        }else if(adState == 4){
            AdManager.get().loadInterstitialAd(context,AdManager.ad_point)
            addPoint(pointsToAdd,"wheel")
        }else if(adState == 7){
            AdManager.get().loadInterstitialAd(context,AdManager.ad_point)
            addPoint(pointsToAdd,"reward")
        }else if(adState == 9){
            AdManager.get().loadInterstitialAd(context,AdManager.ad_point)
            addPoint(pointsToAdd,"checkin")
        }else if(adState == 12){
            AdManager.get().loadInterstitialAd(context,AdManager.ad_point)
            addPoint(pointsToAdd,"reward")
        }
        refreshAdStatusLayout()
    }

    override fun onAdShow() {
        adState++
        LogUtils.println("${fragmentTag}  onAdShow-- $adState")
        if(!AdManager.get().showLuckyCredits){
            if(animator?.isRunning == true){
                currentPlayTime = animator!!.currentPlayTime
                animator?.cancel()
            }
        }
    }

    override fun onAdLoaded() {
        println("$fragmentTag onAdLoaded ${job?.isActive} ${AdManager.get().showLuckyCredits}")
        if(job?.isActive != true) {
            dataBinding.goIv.isClickable = true
            dataBinding.goIv.setImageResource(R.drawable.ic_go3)
        }
        refreshAdStatusLayout()

    }

    override fun onAdLoadFail() {
        if((AdManager.get().interstitialAdMap[AdManager.ad_preclick] == null) and (AdManager.get().interstitialAdMap[AdManager.ad_point] == null) and (AdManager.get().rewardedAd == null)){
            dataBinding.goIv.isClickable = false
            dataBinding.goIv.setImageResource(R.drawable.ic_go2)
        }else{
            if(job?.isActive != true) {
                dataBinding.goIv.isClickable = true
                dataBinding.goIv.setImageResource(R.drawable.ic_go3)
            }
        }
        refreshAdStatusLayout()
    }

    private val rewardedAdCallback = object : OnUserEarnedRewardListener {
        override fun onUserEarnedReward(rewardItem: RewardItem) {
            Log.d(tag, "onUserEarnedReward--- $rewardItem ")
            addPoint(pointsToAdd,"wheel")
        }
    }

    private val rewardAdLoadCallback = object :AdManager.VCallAdListener{
        override fun onAdClose() {

        }

        override fun onAdShow() {
        }

        override fun onAdLoaded() {
            dataBinding.rewardedVideoTv.setBackgroundResource(R.drawable.bg_blue_round)
            refreshAdStatusLayout()
        }

        override fun onAdLoadFail() {
            dataBinding.rewardedVideoTv.setBackgroundResource(R.drawable.bg_gray_round)
            refreshAdStatusLayout()
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

    fun tata(view:View,shakeFactor:Float):ObjectAnimator{
        val pvhScaleX: PropertyValuesHolder = PropertyValuesHolder.ofKeyframe(
            View.SCALE_X,
            Keyframe.ofFloat(0f, 1f),
            Keyframe.ofFloat(.1f, .9f),
            Keyframe.ofFloat(.2f, .9f),
            Keyframe.ofFloat(.3f, 1.1f),
            Keyframe.ofFloat(.4f, 1.1f),
            Keyframe.ofFloat(.5f, 1.1f),
            Keyframe.ofFloat(.6f, 1.1f),
            Keyframe.ofFloat(.7f, 1.1f),
            Keyframe.ofFloat(.8f, 1.1f),
            Keyframe.ofFloat(.9f, 1.1f),
            Keyframe.ofFloat(1f, 1f)
        )

        val pvhScaleY: PropertyValuesHolder = PropertyValuesHolder.ofKeyframe(
            View.SCALE_Y,
            Keyframe.ofFloat(0f, 1f),
            Keyframe.ofFloat(.1f, .9f),
            Keyframe.ofFloat(.2f, .9f),
            Keyframe.ofFloat(.3f, 1.1f),
            Keyframe.ofFloat(.4f, 1.1f),
            Keyframe.ofFloat(.5f, 1.1f),
            Keyframe.ofFloat(.6f, 1.1f),
            Keyframe.ofFloat(.7f, 1.1f),
            Keyframe.ofFloat(.8f, 1.1f),
            Keyframe.ofFloat(.9f, 1.1f),
            Keyframe.ofFloat(1f, 1f)
        )

        val pvhRotate: PropertyValuesHolder = PropertyValuesHolder.ofKeyframe(
            View.ROTATION,
            Keyframe.ofFloat(0f, 0f),
            Keyframe.ofFloat(.1f, -3f * shakeFactor),
            Keyframe.ofFloat(.2f, -3f * shakeFactor),
            Keyframe.ofFloat(.3f, 3f * shakeFactor),
            Keyframe.ofFloat(.4f, -3f * shakeFactor),
            Keyframe.ofFloat(.5f, 3f * shakeFactor),
            Keyframe.ofFloat(.6f, -3f * shakeFactor),
            Keyframe.ofFloat(.7f, 3f * shakeFactor),
            Keyframe.ofFloat(.8f, -3f * shakeFactor),
            Keyframe.ofFloat(.9f, 3f * shakeFactor),
            Keyframe.ofFloat(1f, 0f)
        )

        return ObjectAnimator.ofPropertyValuesHolder(
            view,
            pvhScaleX,
            pvhScaleY,
            pvhRotate
        ).setDuration(1000)
    }

    private fun refreshAdStatusLayout() {
        if(BuildConfig.DEBUG) {
            dataBinding.adStatusLayout.visibility = View.VISIBLE
            println("refreshAdStatusLayout ${AdManager.get().interstitialAdLoadStatus}")
            dataBinding.adStatusLayout.removeAllViews()
            AdManager.get().interstitialAdLoadStatus.forEach {
//                var p = -1
//                if (it.value == 1) {
//                    p =
//                        AdManager.get().interstitialAdMap[it.key]?.checkAdStatus()?.atTopAdInfo?.adsourceIndex
//                            ?: -1
//                    if (it.key == AdManager.ad_rewarded) {
//                        p = AdManager.get().rewardedAd?.checkAdStatus()?.atTopAdInfo?.adsourceIndex
//                            ?: -1
//                    }
//                }
                dataBinding.adStatusLayout.addView(TextView(context).apply {
                    text = String.format(
                        "%s:%s,",
                        it.key,
                        if (it.value === 3) "加载中" else if (it.value === 1) "加载成功" else "加载失败"
                    )
                    setTextColor(Color.RED)
                })
            }
        }
    }
}