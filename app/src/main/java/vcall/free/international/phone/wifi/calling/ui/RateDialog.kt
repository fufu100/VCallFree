package vcall.free.international.phone.wifi.calling.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.databinding.DataBindingUtil
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.databinding.DialogRateBinding
import vcall.free.international.phone.wifi.calling.utils.Dispatcher


/**
 * Created by lyf on 2020/5/12.
 */
class RateDialog(context: Context):Dialog(context,R.style.CustomDialog) {
    val dataBinding:DialogRateBinding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_rate,null,false)

    init {
        setContentView(dataBinding.root)
        dataBinding.dialog = this
    }

    fun hate(v:View){
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc822" // 设置邮件格式
        intent.putExtra(Intent.EXTRA_EMAIL, "rxjava@qq.com") // 接收人
        intent.putExtra(Intent.EXTRA_SUBJECT, "这是邮件的主题部分") // 主题
        intent.putExtra(Intent.EXTRA_TEXT, "这是要反馈的内容") // 正文
        context.startActivity(Intent.createChooser(intent, "请选择邮件类应用"))
        dismiss()
    }

    fun like(v:View){
        Dispatcher.dispatch(context){
            action(Intent.ACTION_VIEW)
            data(Uri.parse("http://www.google.com"))
            defaultAnimate()
        }.go()
        dismiss()
    }
}