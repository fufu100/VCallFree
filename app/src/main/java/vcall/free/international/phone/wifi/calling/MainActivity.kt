package vcall.free.international.phone.wifi.calling

import android.Manifest
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.anythink.core.api.ATAdInfo
import com.anythink.core.api.ATSDK
import com.anythink.core.api.AdError
import com.anythink.interstitial.api.ATInterstitial
import com.anythink.interstitial.api.ATInterstitialListener
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.android.synthetic.main.activity_main.*
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.lib.App
import vcall.free.international.phone.wifi.calling.lib.BaseActivity
import vcall.free.international.phone.wifi.calling.lib.prefs
import vcall.free.international.phone.wifi.calling.service.CallService
import vcall.free.international.phone.wifi.calling.service.DaemonService
import vcall.free.international.phone.wifi.calling.ui.*
import vcall.free.international.phone.wifi.calling.utils.*
import vcall.free.international.phone.wifi.calling.widget.Loading
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity(),InstallStateUpdatedListener {
    private val PERMISSION_REQUEST_CODE = 0

    // 所需的全部权限
    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
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
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, IndexFragment(), "Index").commit()
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerClosed(drawerView: View) {}

            override fun onDrawerOpened(drawerView: View) {
                navigationView.getHeaderView(0).findViewById<TextView>(R.id.coin_num).text =
                    UserManager.get().user?.points.toString()
            }

        })

        navigationView.itemTextColor = getStateListColor(
            intArrayOf(
                getColorFromRes(R.color.text_light),
                getColorFromRes(R.color.text_light)
            )
        )
        navigationView.itemIconTintList = null
        navigationView.setNavigationItemSelectedListener {
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
        LogUtils.println("MainActivity firstInstallTime=${getFirstInstallTime()} ")
        LogUtils.println("MainActivity simCountryIso=${getSimCountryIso()} ")
        LogUtils.println("MainActivity simSerialNum=${getSimSerialNumber()} ")
        LogUtils.println("MainActivity androidID=${getAndroidID()} ")
        LogUtils.println("MainActivity imei=${getIMEI()} ")
        LogUtils.println("MainActivity build Id=${Build.ID} ")
        LogUtils.println("MainActivity build user=${Build.USER} ")
        LogUtils.println("MainActivity getSerial=${getSerial()} ")
        LogUtils.println("MainActivity getDeviceId=${getDeviceId()} ")

        val uuid = UUID(0L, 1L)
        LogUtils.println("uuid=$uuid")
        val uuid2 = UUID(0L, 1L)
        LogUtils.println("uuid2=$uuid2")
        LogUtils.println("uuid2=${Build.SERIAL}")
        loading = Loading(this)

        conn = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {

            }

            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                callBinder = service as CallService.CallBinder
            }
        }
        bindService(Intent(this, CallService::class.java), conn, Context.BIND_AUTO_CREATE)
        test()

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

        if (!NotificationUtils.get().areNotificationsEnable()) {
            NotificationUtils.get().remindOpenNotification(this)
        }else{
            if(Build.VERSION.SDK_INT >= 26) {
                LogUtils.println("$tag onCreate startForegroundService--")
                val intent = Intent(applicationContext, DaemonService::class.java)
                startForegroundService(intent)
            }else{
                val intent = Intent(applicationContext, DaemonService::class.java)
                startService(intent)
            }
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
                    finish()
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
            toast("addOnSuccessListener ${appUpdateInfo.updateAvailability()}")
            if(appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)){
                appUpdateManager.registerListener(this)
                appUpdateManager.startUpdateFlowForResult(appUpdateInfo,AppUpdateType.FLEXIBLE,this,2)
            }else if(appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED){
                popupSnackBarForUpdateCompletion()
            }
        }
        appUpdateInfoTask.addOnFailureListener {
            it.printStackTrace()
            toast(it.message?:"addOnFailureListener")
        }

        ATSDK.integrationChecking(applicationContext)
    }


    public fun changeTab(tab: Int) {
        val fragment = supportFragmentManager.findFragmentByTag("Index")
        if(fragment != null){
            (fragment as IndexFragment).changeTab(tab)
        }
    }

    private fun test() {
        loading.show()
        val map = mutableMapOf<String,String>()
        App.requestMap["from"] = AdManager.get().referrer
        map.putAll(App.requestMap)
        map.put("uuid",getDeviceId())
        compositeDisposable.add(Api.getApiService().signup(map)
            .compose(RxUtils.applySchedulers())
            .subscribe({
                loading.dismiss()
                if (it.errcode == 0) {
                    it.time = System.currentTimeMillis()
                    UserManager.get().user = it
                    (supportFragmentManager.findFragmentByTag("Index") as IndexFragment).refreshUser()
                    navigationView.getHeaderView(0).findViewById<TextView>(R.id.coin_num).text =
                        it.points.toString()
                    callBinder?.initAccount()
                    Dispatcher.dispatch(this){
                        action("refresh_notification")
                    }.send()
                }
            }, {
                it.printStackTrace()
                loading.dismiss()
            })
        )
    }

    override fun onResume() {
        super.onResume()
        testATAd()
    }

    override fun onDestroy() {
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

    fun testATAd(){
        val mInterstitialAd = ATInterstitial(this,"b5f984badd3116")
        mInterstitialAd.setAdListener(object :ATInterstitialListener{
            override fun onInterstitialAdLoadFail(p0: AdError?) {
                println("$tag onInterstitialAdLoadFail, ${p0?.desc}")
                p0?.printStackTrace()
            }

            override fun onInterstitialAdLoaded() {
                println("$tag onInterstitialAdLoaded")
                mInterstitialAd.show(this@MainActivity)
            }

            override fun onInterstitialAdVideoEnd(p0: ATAdInfo?) {

            }

            override fun onInterstitialAdShow(p0: ATAdInfo?) {
                println("$tag onInterstitialAdShow")
            }

            override fun onInterstitialAdVideoError(p0: AdError?) {
                TODO("Not yet implemented")
            }

            override fun onInterstitialAdClicked(p0: ATAdInfo?) {
                TODO("Not yet implemented")
            }

            override fun onInterstitialAdVideoStart(p0: ATAdInfo?) {
                TODO("Not yet implemented")
            }

            override fun onInterstitialAdClose(p0: ATAdInfo?) {
                println("$tag onInterstitialAdClose")
            }

        })
        mInterstitialAd.load()
    }

}
