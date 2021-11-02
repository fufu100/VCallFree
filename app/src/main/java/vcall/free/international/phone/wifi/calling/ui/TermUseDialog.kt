package vcall.free.international.phone.wifi.calling.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.text.Html
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import android.view.View
import androidx.databinding.DataBindingUtil
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.databinding.DialogTermUseBinding
import vcall.free.international.phone.wifi.calling.utils.Dispatcher
import vcall.free.international.phone.wifi.calling.utils.FileUtils

class TermUseDialog(context:Context): Dialog(context, R.style.CustomDialog){
    val dataBinding: DialogTermUseBinding = DataBindingUtil.inflate(layoutInflater,R.layout.dialog_term_use,null,false)
    init {
        dataBinding.dialog = this
        setContentView(dataBinding.root)
        setCancelable(false)
        val content =  SpannableString(Html.fromHtml(FileUtils.readStringFromAssetFile(context.assets,"term_service.txt")))
        val span2 = object : AgreementDialog.MyClickSpan(context.resources.getColor(R.color.blue)){
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
        val target = "Privacy Policy and Terms of use"
        val start = content.indexOf(target)
        content.setSpan(UnderlineSpan(),start,start + target.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        content.setSpan(span2,start,start + target.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        dataBinding.text.text = content
        dataBinding.text.movementMethod = LinkMovementMethod.getInstance()
        dataBinding.text.setHintTextColor(Color.TRANSPARENT)
    }

    fun confirm(){
        dismiss()
    }
}