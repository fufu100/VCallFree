package vcall.free.international.phone.wifi.calling

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.telephony.TelephonyManager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.databinding.ActivityMainBinding
import vcall.free.international.phone.wifi.calling.lib.App
import vcall.free.international.phone.wifi.calling.lib.BaseActivity
import vcall.free.international.phone.wifi.calling.lib.BaseDataBindingActivity
import vcall.free.international.phone.wifi.calling.lib.prefs
import vcall.free.international.phone.wifi.calling.service.CallService
import vcall.free.international.phone.wifi.calling.ui.*
import vcall.free.international.phone.wifi.calling.utils.*
import vcall.free.international.phone.wifi.calling.widget.Loading
import java.util.*

class MainActivity : BaseDataBindingActivity<ActivityMainBinding>(),InstallStateUpdatedListener {
    private val PERMISSION_REQUEST_CODE = 0

    // 所需的全部权限
    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_CONTACTS
    )
    private lateinit var appUpdateManager:AppUpdateManager
    private lateinit var conn: ServiceConnection
    private var callBinder: CallService.CallBinder? = null
    override fun getLayoutRes(): Int = R.layout.activity_main
    private lateinit var exitDialog: ExitDialog
    private lateinit var loading:Loading
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(dataBinding.toolbar)
        dataBinding.toolbar.setNavigationOnClickListener {
            if(it.layoutDirection == View.LAYOUT_DIRECTION_RTL){
                dataBinding.drawerLayout.openDrawer(GravityCompat.END)
            }else{
                dataBinding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, IndexFragment(), "Index").commitAllowingStateLoss()
        dataBinding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerClosed(drawerView: View) {}

            override fun onDrawerOpened(drawerView: View) {
                dataBinding.navigationView.getHeaderView(0).findViewById<TextView>(R.id.coin_num).text =
                    UserManager.get().user?.points.toString()
            }

        })

        dataBinding.navigationView.itemTextColor = getStateListColor(
            intArrayOf(
                getColorFromRes(R.color.text_light),
                getColorFromRes(R.color.text_light)
            )
        )
        dataBinding.navigationView.itemIconTintList = null
        dataBinding.navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.invite ->
                    Dispatcher.dispatch(this) {
                        navigate(InviteFriendsActivity::class.java)
                        defaultAnimate()
                    }.go()
                R.id.call_rate ->
                    Dispatcher.dispatch(this) {
                        navigate(CallRatesActivity::class.java)
                        defaultAnimate()
                    }.go()
                R.id.settings ->
                    Dispatcher.dispatch(this) {
                        navigate(SettingActivity::class.java)
                        defaultAnimate()
                    }.go()
                R.id.rate_us ->
                    RateDialog(this).show()
                R.id.feedback ->
                    Dispatcher.dispatch(this) {
                        navigate(FeedbackActivity::class.java)
                        defaultAnimate()
                    }.go()
            }
            true
        }
        loading = Loading(this)

        conn = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {
            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                callBinder = service as CallService.CallBinder
                callBinder?.setSignupListener(object :CallService.OnSignup{
                    override fun onSignup() {
                        (supportFragmentManager.findFragmentByTag("Index") as IndexFragment).refreshUser()
                        dataBinding.navigationView.getHeaderView(0).findViewById<TextView>(R.id.coin_num).text =
                            UserManager.get().user?.points.toString()
                    }

                })
                callBinder?.signup()
                Log.d(tag, "onServiceConnected:${callBinder?.getGetIpStatus()} ${getCountry()}")
//                if(getCountry() == null){
//                    callBinder?.getIpInfo()
//                }
                if (!NotificationUtils.get().areNotificationsEnable()) {
                    NotificationUtils.get().remindOpenNotification(this@MainActivity)
                }else{
                    callBinder?.createNotification()
                }

            }
        }
        bindService(Intent(this, CallService::class.java), conn, Context.BIND_AUTO_CREATE)


        val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()
//        phoneNumberUtil.getRegionCodeForNumber(Phonenumber.PhoneNumber())
        val phoneNumber = phoneNumberUtil.parse("+12134883500", "CH")
        try {
            val phoneNumber2 = phoneNumberUtil.parseAndKeepRawInput("+1647-555-0123", null)
//        phoneNumber2.rawInput = "+12134883500"
            val iso = phoneNumberUtil.getRegionCodeForNumber(phoneNumber2)
            val nationalNumber = phoneNumberUtil.getNationalSignificantNumber(phoneNumber2)
            LogUtils.println(
                "MainActivity phone:${phoneNumber.countryCode},${phoneNumberUtil.isPossibleNumber(
                    phoneNumber2
                )} ,iso=$iso,nationalNumber=$nationalNumber"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }


        exitDialog = ExitDialog(this){
            if(it == 0){
                if(UserManager.get().user == null){
                    Dispatcher.dispatch(this){
                        action(Intent.ACTION_MAIN)
                        flag(Intent.FLAG_ACTIVITY_NEW_TASK)
                        category(Intent.CATEGORY_HOME)
                    }.go()
                }else {
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(100)
                    finish()
                    System.exit(0)
                }
            }else {

                if(it == 3){
                    changeTab(2)
                    val fragment = supportFragmentManager.findFragmentByTag("Index")
                    if(fragment != null){
                        (fragment as IndexFragment).autoPlay()
                    }
                }else{
                    changeTab(it)
                }
            }
        }
//        val a = PhoneNumberToTimeZonesMapper()
        appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener {appUpdateInfo ->
            println("appUpdateInfo.updateAvailability()=${appUpdateInfo.updateAvailability()}")
//            toast("addOnSuccessListener ${appUpdateInfo.updateAvailability()}")
            if(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE){
                appUpdateManager.registerListener(this)
                val updatePriority = appUpdateInfo.updatePriority()
                if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo,AppUpdateType.IMMEDIATE,this,2)
                }else if(appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)){
                    appUpdateManager.startUpdateFlowForResult(appUpdateInfo,AppUpdateType.FLEXIBLE,this,2)
                }


            }else if(appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED){
                popupSnackBarForUpdateCompletion()
            }
        }
        appUpdateInfoTask.addOnFailureListener {
            it.printStackTrace()
//            toast(it.message?:"addOnFailureListener")
        }

//        if(LogUtils.LOG_ON) {
//            ATSDK.integrationChecking(applicationContext)
//        }

//        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
//            Log.d(tag, " 已经有权限 开始signup---")
//            signup()
//            val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//            println("simCountryIso:${telephonyManager.simCountryIso}")
////            println("simSerialNumber:${telephonyManager.simSerialNumber}")
//        }else{
//            println("$tag onCreate 没有READ_PHONE_STATE权限！！")
//        }

        val isProxy = isWifiProxy()
        println("$tag isProxy=$isProxy ${isDeviceInVPN()} ")

        val savedDeviceId = UUID(getAndroidID().hashCode().toLong(), getSerial().hashCode().toLong()).toString()
        Log.d("Ext", "getDeviceId: $savedDeviceId , ${getAndroidID().hashCode().toLong()} ${getSerial().hashCode().toLong()} ${getAndroidID()} ${getSerial()} ")

    }


    public fun changeTab(tab: Int) {
        val fragment = supportFragmentManager.findFragmentByTag("Index")
        if(fragment != null){
            (fragment as IndexFragment).changeTab(tab)
        }
    }

    public fun signup() {
        val map = mutableMapOf<String,String>()
        var country:String? = getCountry()
        if(country == null){
            println("尝试通过IP获取国家。。。 ${callBinder?.getIpCountry()}")
            if(callBinder?.getIpCountry() != null){
                country = callBinder?.getIpCountry()
            }else {
                println("$tag getIpInfo ${callBinder == null}")
                callBinder?.getIpInfo()
                return
            }
        }
        loading.show()

        val ts = System.currentTimeMillis()
        Api.ts = ts
        val deviceId = getAppDeviceId()
        val uuid = "vcall.${EncryptUtils.md5(deviceId)}"
        App.requestMap["from"] = AESUtils.encrypt(deviceId,AdManager.get().referrer)
        map.putAll(App.requestMap)
        map.put("uuid",uuid)
        map.put("country",country!!)
        map.put("key",ts.toString())
        map.put("sign",EncryptUtils.generateSignature(uuid,packageName,ts.toString(),uuid))
        println("$tag $deviceId signup $map")
        compositeDisposable.add(Api.getApiService().signup(map)
//            .delay(4000,TimeUnit.MILLISECONDS)
            .compose(RxUtils.applySchedulers())
            .subscribe({
                loading.dismiss()
                if (it.code == 20000) {
                    it.data.time = System.currentTimeMillis()
                    UserManager.get().user = it.data

                }
            }, {
                it.printStackTrace()
                loading.dismiss()
            })
        )
    }

    override fun onDestroy() {
        sendBroadcast(Intent("stop"))
        super.onDestroy()
        if (::conn.isInitialized) {
            unbindService(conn)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("$tag onActivityResult $requestCode $resultCode")
        supportFragmentManager.findFragmentByTag("Dial")?.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 2){
            if(resultCode == Activity.RESULT_OK){
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //通知到每个fragment
//        fragments.forEach {
//            it.onRequestPermissionsResult(requestCode,permissions,grantResults)
//        }
    }

    private fun lackPermission(): Boolean {
        for (permission in PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) == PackageManager.PERMISSION_DENIED
            ) {
                return true
            }
        }
        return false
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(supportFragmentManager.findFragmentByTag("Dial")  != null){
                goBack()
            }else {
//                if (System.currentTimeMillis() - mExitTime > 2000) {
//                    toast("再按一次退出程序")
//                    mExitTime = System.currentTimeMillis()
//                } else {
//                    finish()
//                }

                exitDialog.show()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    fun dial(phone: String? = "", isoStr: String = "",username:String = "") {
        var _isoStr:String = isoStr
        if(isoStr.isEmpty()){
            _isoStr = prefs.getStringValue("iso","US")
        }
        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(
            R.anim.bootom_slide_enter,
            R.anim.top_slide_exit,
            0,
            R.anim.bootom_slide_exit
        )
        ft.add(
            R.id.frameLayout,
            DialFragment().apply {
                arguments = Bundle().apply {
                    putString("phone", phone)
                    putString("iso", _isoStr)
                    putString("username", username)
                    if(phone?.isEmpty() == true){
                        putBoolean("can_input",true)
                    }
                }
            },
            "Dial"
        )
        ft.addToBackStack("Dial")
        ft.commit()
    }

    fun goBack() {
        supportFragmentManager.popBackStack()
    }

    override fun onStateUpdate(state: InstallState) {
        if(state.installStatus() == InstallStatus.DOWNLOADED){
            popupSnackBarForUpdateCompletion()
        }
    }

    fun popupSnackBarForUpdateCompletion(){
        Snackbar.make(window.decorView.findViewById(android.R.id.content), "An update has just been downloaded.", Snackbar.LENGTH_INDEFINITE)
            .apply {
                setAction("Restart"){
                    appUpdateManager.completeUpdate()
                }
            }
            .show()
    }

//    fun testATAd(){
//        val mInterstitialAd = ATInterstitial(this,"b5f984badd3116")
//        mInterstitialAd.setAdListener(object :ATInterstitialListener{
//            override fun onInterstitialAdLoadFail(p0: AdError?) {
//                println("$tag onInterstitialAdLoadFail, ${p0?.desc}")
//                p0?.printStackTrace()
//            }
//
//            override fun onInterstitialAdLoaded() {
//                println("$tag onInterstitialAdLoaded")
//                mInterstitialAd.show(this@MainActivity)
//            }
//
//            override fun onInterstitialAdVideoEnd(p0: ATAdInfo?) {
//
//            }
//
//            override fun onInterstitialAdShow(p0: ATAdInfo?) {
//                println("$tag onInterstitialAdShow")
//            }
//
//            override fun onInterstitialAdVideoError(p0: AdError?) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onInterstitialAdClicked(p0: ATAdInfo?) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onInterstitialAdVideoStart(p0: ATAdInfo?) {
//                TODO("Not yet implemented")
//            }
//
//            override fun onInterstitialAdClose(p0: ATAdInfo?) {
//                println("$tag onInterstitialAdClose")
//            }
//
//        })
//        mInterstitialAd.load()
//    }

}
