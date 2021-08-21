package vcall.free.international.phone.wifi.calling.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import com.anythink.core.api.ATAdConst
import com.anythink.core.api.ATAdInfo
import com.anythink.core.api.AdError
import com.anythink.nativead.api.*
import com.newmotor.x5.db.DBHelper
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.databinding.DialogExitBinding
import vcall.free.international.phone.wifi.calling.lib.App
import vcall.free.international.phone.wifi.calling.lib.prefs
import vcall.free.international.phone.wifi.calling.nativead.NativeDemoRender
import vcall.free.international.phone.wifi.calling.utils.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * Created by lyf on 2020/8/19.
 */
class ExitDialog(context: Context, val callback: (flag: Int) -> Unit) :
    Dialog(context, R.style.CustomDialog) {
    val TAG = "ExitDialog"
    val dataBinding: DialogExitBinding =
        DataBindingUtil.inflate(layoutInflater, R.layout.dialog_exit, null, false)
    private lateinit var atNatives: ATNative
    private var anyThinkNativeAdView: ATNativeAdView? = null

    init {
        setContentView(dataBinding.root)
        dataBinding.dialog = this
        if (AdManager.get().adData?.ads?.count {
                it.adPlaceID == "topon_ystc"
            } == 1) {
            dataBinding.adContainer.visibility = View.VISIBLE
            loadAd()
        } else {
            dataBinding.adContainer.visibility = View.GONE
        }


        setOnDismissListener {
            loadAd()
        }

        setOnShowListener {
            LogUtils.println("ExitDialog setOnShowListener")
            if (UserManager.get().user != null && UserManager.get().user?.phone?.isEmpty() == true) {
                dataBinding.setNowTv.text = context.resources.getString(R.string.set_now)
                dataBinding.titleTv.text =
                    context.resources.getString(R.string.don_t_forget_to_set_phone_number)
            } else {
                val format = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
                val today = format.format(Date())
                var lastCheckDay = prefs.getStringValue("last_check_day", "")
                if (lastCheckDay != today) {
                    dataBinding.setNowTv.text = context.resources.getString(R.string.check_in)
                    dataBinding.titleTv.text =
                        context.resources.getString(R.string.don_t_forget_to_check_in)
                } else {
                    dataBinding.setNowTv.text = context.resources.getString(R.string.more_credits)
                    dataBinding.titleTv.text =
                        context.resources.getString(R.string.don_t_forget_to_get_more_credits)
                }
            }

        }
    }

    private fun loadAd() {
        val adID = getNativeAdID()
        println("ExitDialog loadAd adID=$adID")
        if (adID.isNotEmpty()) {
            atNatives = ATNative(context,adID,object :ATNativeNetworkListener{
                override fun onNativeAdLoadFail(p0: AdError?) {
                    Log.e(TAG, "onNativeAdLoadFail: ${p0?.desc}" )
                    p0?.printStackTrace()
                    dataBinding.adContainer.visibility = View.VISIBLE
                }

                override fun onNativeAdLoaded() {
                    Log.d(TAG, "onNativeAdLoaded:--- ")
                    showAd()
                }

            })
            val localMap: MutableMap<String, Any> = mutableMapOf()
            val adViewWidth = context.screenWidth() - context.dip2px(10) * 2
            val adViewHeight = context.dip2px(300)
            localMap[ATAdConst.KEY.AD_WIDTH] = adViewWidth
            localMap[ATAdConst.KEY.AD_HEIGHT] = adViewHeight
            atNatives.setLocalExtra(localMap)
            if(anyThinkNativeAdView == null){
                anyThinkNativeAdView = ATNativeAdView(context)
            }
            atNatives.makeAdRequest()

            if(dataBinding.adContainer.childCount == 1){
                dataBinding.adContainer.addView(anyThinkNativeAdView,0,FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,adViewHeight))
            }
        }
    }

    fun showAd(){
        val nativeAd: NativeAd? = atNatives.getNativeAd()
        if (nativeAd != null) {
//            if (mNativeAd != null) {
//                mNativeAd.destory()
//            }
//            nativeAd = nativeAd
            nativeAd.setNativeEventListener(object : ATNativeEventListener {
                override fun onAdImpressed(view: ATNativeAdView, entity: ATAdInfo) {
                    Log.i(TAG, "native ad onAdImpressed:\n$entity")
                }

                @SuppressLint("CheckResult")
                override fun onAdClicked(view: ATNativeAdView, entity: ATAdInfo) {
                    Log.i(TAG, "native ad onAdClicked:\n$entity")
                    val map = mutableMapOf<String, String>()
                    map["ver"] = App.context?.getVersionName() ?: ""
                    map["sip"] = UserManager.get().user?.sip ?: ""
                    map["type"] = "tuichu"
                    map["update_time"] = System.currentTimeMillis().toString()
                    map["ts"] = System.currentTimeMillis().toString()
                    Api.getApiService().addClick(map)
                        .compose(RxUtils.applySchedulers())
                        .subscribe({
                            if (it.isSuccessful) {
                                DBHelper.get().addAdClickCount()
                            }
                        }, {
                            it.printStackTrace()
                        })
                }

                override fun onAdVideoStart(view: ATNativeAdView) {
                    Log.i(TAG, "native ad onAdVideoStart")
                }

                override fun onAdVideoEnd(view: ATNativeAdView) {
                    Log.i(TAG, "native ad onAdVideoEnd")
                }

                override fun onAdVideoProgress(view: ATNativeAdView, progress: Int) {
                    Log.i(TAG, "native ad onAdVideoProgress:$progress")
                }
            })
            nativeAd.setDislikeCallbackListener(object : ATNativeDislikeListener() {
                override fun onAdCloseButtonClick(view: ATNativeAdView, entity: ATAdInfo) {
                    Log.i(TAG, "native ad onAdCloseButtonClick:")
                    if (view.parent != null) {
                        (view.parent as ViewGroup).removeView(view)
                    }
                }
            })
            val renderer = NativeDemoRender(context)
            try {
                nativeAd.renderAdView(anyThinkNativeAdView, renderer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            anyThinkNativeAdView!!.visibility = View.VISIBLE
            nativeAd.prepare(anyThinkNativeAdView, renderer.getClickView(), null)
        }else{

        }
    }

    fun getNativeAdID(): String {
        var adID = ""
        if (AdManager.get().adData != null) {
            for (i in 0 until AdManager.get().adData!!.ads.size) {
                if (AdManager.get().adData!!.ads[i].adPlaceID == AdManager.ad_quite) {
                    adID = AdManager.get().adData!!.ads[i].adSources[0].adPlaceID
                    break
                }
            }
        }
        return adID
    }

    fun exit() {
        callback(0)
        dismiss()
    }

    fun setNow() {
        if (dataBinding.setNowTv.text.toString() == context.resources.getString(R.string.set_now)) {
            Dispatcher.dispatch(context) {
                navigate(SetPhoneNumberActivity::class.java)
                defaultAnimate()
            }.go()
        } else if (dataBinding.setNowTv.text.toString() == context.resources.getString(R.string.check_in)) {
            callback(2)
        } else if (dataBinding.setNowTv.text.toString() == context.resources.getString(R.string.more_credits)) {
            callback(3)
        }
        dismiss()
    }
}