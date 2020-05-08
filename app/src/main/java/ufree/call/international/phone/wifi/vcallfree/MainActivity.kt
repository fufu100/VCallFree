package ufree.call.international.phone.wifi.vcallfree

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.newmotor.x5.db.DBHelper
import com.translate.english.voice.lib.App
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ufree.call.international.phone.wifi.vcallfree.api.Api
import ufree.call.international.phone.wifi.vcallfree.api.Country
import ufree.call.international.phone.wifi.vcallfree.lib.BaseActivity
import ufree.call.international.phone.wifi.vcallfree.ui.*
import ufree.call.international.phone.wifi.vcallfree.utils.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*

class MainActivity : BaseActivity() {
    private val PERMISSION_REQUEST_CODE = 0

    // 所需的全部权限
    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CONTACTS
    )
    private val fragments = mutableListOf<Fragment>()
    private lateinit var tabManager: TabManager
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
        tabManager = TabManager(this, fragments, R.id.content, radioGroup)

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

        initData()
        test()
    }

    private fun initData() {
        GlobalScope.launch {
            val count = DBHelper.get().getCountryCount()
            println("count=$count")
            if (count == 0) {
                val inputStream = assets.open("dial_plan.txt")
                val br: BufferedReader = BufferedReader(InputStreamReader(inputStream))
                var line: String? = null

                do {
                    line = br.readLine()
                    if (line != null) {
                        val regex = Regex("(\\{|\\}|\\s|\")+")
                        line = line.replace(regex, "")
                        println("$line")
                        val array = line.split(",")
                        if (array.size > 4) {
                            val country =
                                Country(array[0], array[1], array[2], array[3].toInt(), array[4])
                            DBHelper.get().addCountry(country)
                        }
                    }
                } while (line != null)
            }
            val dest = App.appCacheDirectory + "flags.zip"
            val flagDirectory = File(App.appCacheDirectory + "flags")
            if(!flagDirectory.exists()){
                FileUtils.copyFromAssets(assets,"flags.zip",dest,false)
                UnZip.unzip(dest,App.appCacheDirectory)
                Log.d("MainActivity","国旗解压缩成功")
            }else{
                if((flagDirectory.listFiles()?.size ?: 0) < 224){
                    flagDirectory.delete()
                    FileUtils.copyFromAssets(assets,"flags.zip",dest,false)
                    UnZip.unzip(dest,App.appCacheDirectory)
                    Log.d("MainActivity","国旗有缺失，已重新解压缩")
                }
            }

        }

    }

    private fun test(){
        compositeDisposable.add(Api.getApiService().signup(getDeviceId())
            .compose(RxUtils.applySchedulers())
            .subscribe({
                if(it.errcode == 0){
                    UserManager.get().user = it
                    navigationView.getHeaderView(0).findViewById<TextView>(R.id.coin_num).text = it.points.toString()
                }
            },{
                it.printStackTrace()
            }))
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (lackPermission()) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
}
