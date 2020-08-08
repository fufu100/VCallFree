package ufree.call.international.phone.wifi.vcallfree.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import androidx.databinding.DataBindingUtil
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.databinding.DialogAgreementBinding
import ufree.call.international.phone.wifi.vcallfree.lib.prefs
import ufree.call.international.phone.wifi.vcallfree.utils.Dispatcher
import ufree.call.international.phone.wifi.vcallfree.utils.FileUtils

/**
 * Created by lyf on 2020/7/21.
 */
class AgreementDialog(context: Context, val callback:(result:Boolean) -> Unit) : Dialog(context, R.style.CustomDialog) {
    val dataBinding: DialogAgreementBinding = DataBindingUtil.inflate(layoutInflater,R.layout.dialog_agreement,null,false)

    init {
        dataBinding.dialog = this
        setContentView(dataBinding.root)
        setCancelable(false)
        val content = SpannableString(FileUtils.readStringFromAssetFile(context.assets,"agreement.txt"))
        val span2 = object :MyClickSpan(context.resources.getColor(R.color.blue)){
            override fun onClick(widget: View) {
                println("click1")
                Dispatcher.dispatch(context){
                    action(Intent.ACTION_VIEW)
                    data(Uri.parse("https://www.privacypolicies.com/privacy/view/65134b8f86392b2bd81420bc0ac6597e"))
                    defaultAnimate()
                }.go()
            }
        }
        content.setSpan(UnderlineSpan(),content.length - 31,content.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        content.setSpan(span2,content.length - 31,content.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        dataBinding.text.text = content
        dataBinding.text.movementMethod = LinkMovementMethod.getInstance()
        dataBinding.text.setHintTextColor(Color.TRANSPARENT)
    }
    fun yes(){
        prefs.save("is_first",false)
        callback(true)
        dismiss()
    }
    fun no(){
        dismiss()
        callback(false)
    }

    public open class MyClickSpan(val color:Int): ClickableSpan(){
        override fun onClick(widget: View) {

        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = color
            ds.isUnderlineText = true
        }

    }
}