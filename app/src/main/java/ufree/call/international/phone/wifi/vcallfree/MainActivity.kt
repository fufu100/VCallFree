package ufree.call.international.phone.wifi.vcallfree

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
import android.view.KeyEvent
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.BundleCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_call.*
import ufree.call.international.phone.wifi.vcallfree.adapter.CacheFragmentStatePagerAdapter
import ufree.call.international.phone.wifi.vcallfree.api.Api
import ufree.call.international.phone.wifi.vcallfree.lib.BaseActivity
import ufree.call.international.phone.wifi.vcallfree.service.CallService
import ufree.call.international.phone.wifi.vcallfree.ui.*
import ufree.call.international.phone.wifi.vcallfree.utils.*
import java.util.*

class MainActivity : BaseActivity() {
    private val PERMISSION_REQUEST_CODE = 0

    // 所需的全部权限
    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CONTACTS
    )

    private lateinit var conn: ServiceConnection
    private var callBinder: CallService.CallBinder? = null
    override fun getLayoutRes(): Int = R.layout.activity_main
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
                R.id.feedback ->
                    Dispatcher.dispatch(this) {
                        navigate(FeedbackActivity::class.java)
                        defaultAnimate()
                    }.go()
            }
            true
        }
        println("MainActivity firstInstallTime=${getFirstInstallTime()} ")
        println("MainActivity simCountryIso=${getSimCountryIso()} ")
        println("MainActivity simSerialNum=${getSimSerialNumber()} ")
        println("MainActivity androidID=${getAndroidID()} ")
        println("MainActivity imei=${getIMEI()} ")
        println("MainActivity build Id=${Build.ID} ")
        println("MainActivity build user=${Build.USER} ")
        println("MainActivity getSerial=${getSerial()} ")
        println("MainActivity getDeviceId=${getDeviceId()} ")

        val uuid = UUID(0L, 1L)
        println("uuid=$uuid")
        val uuid2 = UUID(0L, 1L)
        println("uuid2=$uuid2")
        println("uuid2=${Build.SERIAL}")
//        DBHelper.get()

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
            println(
                "MainActivity phone:${phoneNumber.countryCode},${phoneNumberUtil.isPossibleNumber(
                    phoneNumber2
                )} ,iso=$iso,nationalNumber=$nationalNumber"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }



    }

    public fun changeTab(tab: Int) {
        println("Main changeTab $tab")
//        (radioGroup.getChildAt(tab) as RadioButton).isChecked = true
    }

    private fun test() {
        compositeDisposable.add(Api.getApiService().signup(getDeviceId())
            .compose(RxUtils.applySchedulers())
            .subscribe({
                if (it.errcode == 0) {
                    UserManager.get().user = it
//                    (fragments[2] as CoinsFragment).refreshUser()
                    (supportFragmentManager.findFragmentByTag("Index") as IndexFragment).refreshUser()
                    navigationView.getHeaderView(0).findViewById<TextView>(R.id.coin_num).text =
                        it.points.toString()
                    callBinder?.initAccount()
                }
            }, {
                it.printStackTrace()
            })
        )
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {
        super.onDestroy()
        if (::conn.isInitialized) {
            unbindService(conn)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        supportFragmentManager.findFragmentByTag("Dial")?.onActivityResult(requestCode, resultCode, data)
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

    private var mExitTime: Long = 0

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(supportFragmentManager.findFragmentByTag("Dial")  != null){
                goBack()
            }else {
                if (System.currentTimeMillis() - mExitTime > 2000) {
                    toast("再按一次退出程序")
                    mExitTime = System.currentTimeMillis()
                } else {
                    finish()
                }
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    fun dial(phone: String? = "", isoStr: String? = "US") {
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
                arguments = Bundle().apply { putString("phone", phone);putString("iso", isoStr); }
            },
            "Dial"
        )
        ft.addToBackStack("Dial")
        ft.commit()
    }

    fun goBack() {
        supportFragmentManager.popBackStack()
    }

}
