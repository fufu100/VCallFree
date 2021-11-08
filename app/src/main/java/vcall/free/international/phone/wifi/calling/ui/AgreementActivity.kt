package vcall.free.international.phone.wifi.calling.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import android.view.View
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.databinding.ActivityAgreementBinding
import vcall.free.international.phone.wifi.calling.lib.BaseBackActivity
import vcall.free.international.phone.wifi.calling.utils.Dispatcher
import vcall.free.international.phone.wifi.calling.utils.FileUtils

/**
 * Created by lyf on 2020/8/19.
 */
class AgreementActivity:BaseBackActivity<ActivityAgreementBinding>() {
    override fun getLayoutRes(): Int = R.layout.activity_agreement
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val content = SpannableString(Html.fromHtml(FileUtils.readStringFromAssetFile(assets,"agreement.txt")))
        val span2 = object : AgreementDialog.MyClickSpan(resources.getColor(R.color.blue)){
            override fun onClick(widget: View) {
                println("click1")
                Dispatcher.dispatch(this@AgreementActivity){
                    navigate(WebActivity::class.java)
                    extra("url","http://vcallfree.com/VCallFree_privacy.html")
                    extra("title","")
                    defaultAnimate()
                }.go()
            }
        }
        val span3 = object : AgreementDialog.MyClickSpan(resources.getColor(R.color.blue)){
            override fun onClick(widget: View) {
                println("click1")
                TermUseDialog(this@AgreementActivity).show()
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
        dataBinding.text.maxHeight = Integer.MAX_VALUE
        dataBinding.text.movementMethod = LinkMovementMethod.getInstance()
        dataBinding.text.setHintTextColor(Color.TRANSPARENT)
    }
}