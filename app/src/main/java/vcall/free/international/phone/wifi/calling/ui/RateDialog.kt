package vcall.free.international.phone.wifi.calling.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import androidx.core.app.ShareCompat
import androidx.databinding.DataBindingUtil
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.databinding.DialogRateBinding
import vcall.free.international.phone.wifi.calling.utils.Dispatcher
import vcall.free.international.phone.wifi.calling.utils.UserManager
import vcall.free.international.phone.wifi.calling.utils.getVersionCode
import vcall.free.international.phone.wifi.calling.utils.getVersionName


/**
 * Created by lyf on 2020/5/12.
 */
class RateDialog(val activity: Activity):Dialog(activity,R.style.CustomDialog) {
    val dataBinding:DialogRateBinding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_rate,null,false)

    init {
        setContentView(dataBinding.root)
        dataBinding.dialog = this
    }

    fun hate(v:View){
        val str = String.format(
            "Please give us your suggestions and questions. If you help us to improve successfully, you will be rewarded 50000 points.<br>" +
                    "VersionName:%s<br>VersionCode:%d<br>sip:%s<br>Device Manufacture:%s<br>Device Brand/Model:%s/%s<br>System Version:%d",
            context.getVersionName(),
            context.getVersionCode(),
            UserManager.get().user?.getDecryptSip() ?: "",
            Build.MANUFACTURER,
            Build.BRAND,
            Build.MODEL,
            Build.VERSION.SDK_INT
        )

        ShareCompat.IntentBuilder.from(activity).setType("message/rfc822")
            .addEmailTo("VCallFree_Feedback@hotmail.com")
            .setSubject("VCallFree")
            .setHtmlText(str)
            .setChooserTitle("Choose email")
            .startChooser();

        dismiss()
    }

    fun like(v:View){
        Dispatcher.dispatch(context){
            action(Intent.ACTION_VIEW)
            data(Uri.parse("market://details?id=" + context.packageName))
            defaultAnimate()
        }.go()
        dismiss()
    }
}