package vcall.free.international.phone.wifi.calling.ui

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.Html
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.UnderlineSpan
import android.view.View
import androidx.databinding.DataBindingUtil
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.databinding.DialogAgreementBinding
import vcall.free.international.phone.wifi.calling.lib.prefs
import vcall.free.international.phone.wifi.calling.utils.Dispatcher
import vcall.free.international.phone.wifi.calling.utils.FileUtils

/**
 * Created by lyf on 2020/7/21.
 */
class AgreementDialog(context: Context, val callback:(result:Boolean) -> Unit) : Dialog(context, R.style.CustomDialog) {
    val dataBinding: DialogAgreementBinding = DataBindingUtil.inflate(layoutInflater,R.layout.dialog_agreement,null,false)

    init {
        dataBinding.dialog = this
        setContentView(dataBinding.root)
        setCancelable(false)
        val content =  SpannableString(Html.fromHtml(FileUtils.readStringFromAssetFile(context.assets,"agreement.txt")))
        val span2 = object :MyClickSpan(context.resources.getColor(R.color.blue)){
            override fun onClick(widget: View) {
                println("click1")
                Dispatcher.dispatch(context){
                    navigate(WebActivity::class.java)
                    extra("url","http://vcallfree.com/VCallFree_privacy.html")
                    extra("title","")
                    defaultAnimate()
                }.go()
            }
        }
        val span3 = object :MyClickSpan(context.resources.getColor(R.color.blue)){
            override fun onClick(widget: View) {
                println("click1")
                TermUseDialog(context).show()
            }
        }
        val target = "Privacy Policy"
        val target2 = "Terms of Service"
        val start = content.indexOf(target)
        content.setSpan(UnderlineSpan(),start,start + target.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        content.setSpan(span2,start,start + target.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

        val start2 = content.indexOf(target2)
        content.setSpan(UnderlineSpan(),start2,start2 + target2.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        content.setSpan(span3,start2,start2 + target2.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)

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