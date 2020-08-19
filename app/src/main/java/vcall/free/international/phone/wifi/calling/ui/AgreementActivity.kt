package vcall.free.international.phone.wifi.calling.ui

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
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
        val content = SpannableString(FileUtils.readStringFromAssetFile(this.assets,"agreement.txt"))
        val span2 = object : AgreementDialog.MyClickSpan(this.resources.getColor(R.color.blue)){
            override fun onClick(widget: View) {
                println("click1")
                Dispatcher.dispatch(this@AgreementActivity){
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
}