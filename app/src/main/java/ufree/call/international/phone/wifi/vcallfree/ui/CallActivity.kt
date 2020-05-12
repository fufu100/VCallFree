package ufree.call.international.phone.wifi.vcallfree.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.newmotor.x5.db.DBHelper
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_call.*
import kotlinx.coroutines.runBlocking
import org.pjsip.pjsua2.CallInfo
import org.pjsip.pjsua2.pjsip_inv_state
import org.pjsip.pjsua2.pjsip_role_e
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.api.Record
import ufree.call.international.phone.wifi.vcallfree.databinding.ActivityCallBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseBackActivity
import ufree.call.international.phone.wifi.vcallfree.service.CallService
import ufree.call.international.phone.wifi.vcallfree.utils.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.ceil

/**
 * Created by lyf on 2020/5/10.
 */
class CallActivity : BaseBackActivity<ActivityCallBinding>(), CallService.CallStateChange {
    private val TAG = "CallActivity"
    private lateinit var conn: ServiceConnection
    private var callBinder: CallService.CallBinder? = null
    var expand = false
    var phone: String? = ""
    var rate: Int = 0
    var count = 0L
    var disposable: Disposable? = null
    override fun getLayoutRes(): Int = R.layout.activity_call
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        dataBinding.activity = this
        conn = object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName?) {}
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                callBinder = service as CallService.CallBinder
                callBinder?.addCallStateChangeListener(this@CallActivity)

                onCallStateChange(callBinder?.getCurrentCall()?.info)
            }
        }
        bindService(Intent(this, CallService::class.java), conn, Context.BIND_AUTO_CREATE)
        phone = intent.getStringExtra("phone")
        rate = intent.getIntExtra("rate", 0)
        dataBinding.phone.text =
            String.format(Locale.getDefault(), "+%s %s", UserManager.get().country?.code, phone)

        if (LogUtils.test) {
            startTimeCount()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::conn.isInitialized) {
            callBinder?.removeCallStateChangeListener(this)
            unbindService(conn)
        }
        if (disposable?.isDisposed == false) {
            disposable?.dispose()
        }
    }

    fun onClick(v: View) {

    }

    fun mute(v: View) {
        v.isSelected = !v.isSelected
    }

    fun horn(v: View) {
        v.isSelected = !v.isSelected
    }

    fun numberPad(v: View) {
        if (expand) {
            expand = false
            number_pad.visibility = View.GONE
            (dataBinding.phone.layoutParams as ConstraintLayout.LayoutParams).topMargin =
                dip2px(141)
        } else {
            expand = true
            number_pad.visibility = View.VISIBLE
            (dataBinding.phone.layoutParams as ConstraintLayout.LayoutParams).topMargin = dip2px(41)
        }
    }

    fun hangup(v: View) {
        callBinder?.hangup()
        val state =
            if (callBinder?.getCurrentCall()?.info?.state == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) 1 else 2
        val coin_cost = (rate * ceil(count * 1.0f / 60)).toInt()
        val record = Record(
            0,
            0,
            phone!!,
            UserManager.get().country!!.iso,
            UserManager.get().country!!.code,
            UserManager.get().country!!.prefix,
            "",
            0L,
            System.currentTimeMillis(),
            count,
            rate,
            coin_cost,
            state
        )
        DBHelper.get().addCallRecord(record)
        Dispatcher.dispatch(this) {
            navigate(CallResultActivity::class.java)
            extra("record", record)
            defaultAnimate()
        }.go()
        finish()
    }

    fun del(v: View) {

    }

    override fun onCallStateChange(callInfo: CallInfo?) {
        if(callInfo != null) {
            runOnUiThread {
                var callState = ""
                if (callInfo!!.state.swigValue() < pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.swigValue()) {
                    if (callInfo.role === pjsip_role_e.PJSIP_ROLE_UAS) {
                        callState = "Incoming call.."
                    } else {
                        //拨打电话，对方未接时候
                        callState = callInfo.stateText
                        println("$TAG 对方未接")
                    }
                } else if (callInfo.state
                        .swigValue() >= pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED.swigValue()
                ) {
                    callState = callInfo.stateText
                    if (callInfo.state === pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                        Log.d(TAG, "已接听")
                        callState = "00:00:00"
                        startTimeCount()
//                buttonHangup.setText("Hangup") //已接听
                    } else if (callInfo.state === pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                        Log.d(TAG, "连接失败")
//                buttonHangup.setText("OK") //来电未接
                        callState = "Call disconnected: " + callInfo.lastReason
                    }
                }
                dataBinding.timeRemaining.text = callState
            }
        }

    }

    private fun startTimeCount() {
        count = 0
        disposable = Observable.interval(1000, TimeUnit.MILLISECONDS)
            .compose(RxUtils.applySchedulers())
            .subscribe({
                count++
                dataBinding.duration.text = String.format(
                    Locale.getDefault(),
                    "%02d:%02d:%02d",
                    it / 3600,
                    it / 60,
                    it % 60
                )
                val totalMinRemaining = UserManager.get().user!!.points / rate - it / 60
                dataBinding.timeRemaining.text =
                    String.format(Locale.getDefault(), "%d min Remaining", totalMinRemaining)
            }, {
                it.printStackTrace()
            })
    }

}