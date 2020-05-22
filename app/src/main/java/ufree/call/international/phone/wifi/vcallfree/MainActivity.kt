package ufree.call.international.phone.wifi.vcallfree

import android.Manifest
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
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.android.synthetic.main.activity_main.*
import ufree.call.international.phone.wifi.vcallfree.adapter.CacheFragmentStatePagerAdapter
import ufree.call.international.phone.wifi.vcallfree.api.Api
import ufree.call.international.phone.wifi.vcallfree.lib.BaseActivity
import ufree.call.international.phone.wifi.vcallfree.service.CallService
import ufree.call.international.phone.wifi.vcallfree.ui.*
import ufree.call.international.phone.wifi.vcallfree.utils.*
import java.util.*

class MainActivity : BaseActivity(),RadioGroup.OnCheckedChangeListener,ViewPager.OnPageChangeListener {
    private val PERMISSION_REQUEST_CODE = 0

    // 所需的全部权限
    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CONTACTS
    )
    private val fragments = mutableListOf<Fragment>()
    private lateinit var conn: ServiceConnection
    private var callBinder:CallService.CallBinder? = null
    override fun getLayoutRes(): Int = R.layout.activity_main
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        fragments.add(ContractsFragment())
        fragments.add(RecentFragment())
        fragments.add(CoinsFragment())
        viewPager.adapter = object :CacheFragmentStatePagerAdapter(supportFragmentManager){
            override fun createItem(position: Int): Fragment = fragments[position]
            override fun getCount(): Int = fragments.size
        }
        viewPager.addOnPageChangeListener(this)
        viewPager.offscreenPageLimit = 2
        radioGroup.setOnCheckedChangeListener(this)
        drawerLayout.addDrawerListener(object :DrawerLayout.DrawerListener{
            override fun onDrawerStateChanged(newState: Int) {}

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerClosed(drawerView: View) { }

            override fun onDrawerOpened(drawerView: View) {
                navigationView.getHeaderView(0).findViewById<TextView>(R.id.coin_num).text = UserManager.get().user?.points.toString()
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
                        requestCode(1)
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

        conn = object :ServiceConnection{
            override fun onServiceDisconnected(name: ComponentName?) {

            }
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                callBinder = service as CallService.CallBinder
            }
        }
        bindService(Intent(this,CallService::class.java),conn, Context.BIND_AUTO_CREATE)
        test()

        val phoneNumberUtil:PhoneNumberUtil = PhoneNumberUtil.getInstance()
//        phoneNumberUtil.getRegionCodeForNumber(Phonenumber.PhoneNumber())
        val phoneNumber = phoneNumberUtil.parse("+12134883500","CH")
        try {
            val phoneNumber2 = phoneNumberUtil.parseAndKeepRawInput("+1647-555-0123",null)
//        phoneNumber2.rawInput = "+12134883500"
            val iso = phoneNumberUtil.getRegionCodeForNumber(phoneNumber2)
            val nationalNumber = phoneNumberUtil.getNationalSignificantNumber(phoneNumber2)
            println("MainActivity phone:${phoneNumber.countryCode},${phoneNumberUtil.isPossibleNumber(phoneNumber2)} ,iso=$iso,nationalNumber=$nationalNumber")
        }catch (e:Exception){
            e.printStackTrace()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (lackPermission()) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE)
            }
        }

    }

    public fun changeTab(tab:Int){
        println("Main changeTab $tab")
        (radioGroup.getChildAt(tab) as RadioButton).isChecked = true
    }

    private fun test(){
        compositeDisposable.add(Api.getApiService().signup(getDeviceId())
            .compose(RxUtils.applySchedulers())
            .subscribe({
                if(it.errcode == 0){
                    UserManager.get().user = it
                    (fragments[2] as CoinsFragment).refreshUser()
                    navigationView.getHeaderView(0).findViewById<TextView>(R.id.coin_num).text = it.points.toString()
                    callBinder?.initAccount()
                }
            },{
                it.printStackTrace()
            }))
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {
        super.onDestroy()
        if(::conn.isInitialized){
            unbindService(conn)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //通知到每个fragment
        fragments.forEach {
            it.onRequestPermissionsResult(requestCode,permissions,grantResults)
        }
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
            if (System.currentTimeMillis() - mExitTime > 2000) {
                toast( "再按一次退出程序")
                mExitTime = System.currentTimeMillis()
            } else {
                finish()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        when(checkedId){
            R.id.radio_contacts ->
                viewPager.currentItem = 0
            R.id.radio_recents ->
                viewPager.currentItem = 1
            R.id.radio_coins ->
                viewPager.currentItem = 2
        }
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        (radioGroup.getChildAt(position) as RadioButton).isChecked = true
    }
}
