package vcall.free.international.phone.wifi.calling.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.app.ShareCompat
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.databinding.ActivityFeedbackBinding
import vcall.free.international.phone.wifi.calling.lib.BaseBackActivity
import vcall.free.international.phone.wifi.calling.utils.UserManager
import vcall.free.international.phone.wifi.calling.utils.toast

/**
 * Created by lyf on 2020/5/7.
 */
class FeedbackActivity:BaseBackActivity<ActivityFeedbackBinding>() {
    lateinit var items:Array<TextView>
    override fun getLayoutRes(): Int = R.layout.activity_feedback

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        dataBinding.activity = this
        items = arrayOf(
            dataBinding.item1,
            dataBinding.item2,
            dataBinding.item3,
            dataBinding.item4
        )
    }

    fun selectItem(v: View){
        items.forEach {
            it.isSelected = it == v
        }
    }

    fun submit(){
        val content = dataBinding.content.text.toString()
        var subject = ""
        items.forEach {
            if(it.isSelected){
                subject = it.text.toString()
            }
        }
        if(subject.isEmpty()){
            toast(R.string.tip_select_subject)
            return
        }
        if(content.isNotEmpty()) {
            ShareCompat.IntentBuilder.from(this).setType("message/rfc822")
                .addEmailTo("VCallFree_Feedback@hotmail.com")
                .setSubject(subject)
                .setHtmlText("$content <br>sip:${UserManager.get().user?.sip}")
                .setChooserTitle("Choose email")
                .startChooser();
        }else{
            toast(R.string.please_enter_your_suggestion)
        }
    }
}