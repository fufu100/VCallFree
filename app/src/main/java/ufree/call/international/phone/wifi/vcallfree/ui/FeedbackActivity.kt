package ufree.call.international.phone.wifi.vcallfree.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.databinding.ActivityFeedbackBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseBackActivity
import ufree.call.international.phone.wifi.vcallfree.utils.UserManager
import ufree.call.international.phone.wifi.vcallfree.utils.toast

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
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "message/rfc822" // 设置邮件格式
            intent.putExtra(Intent.EXTRA_EMAIL, "rxjava@qq.com") // 接收人
            intent.putExtra(Intent.EXTRA_SUBJECT, subject) // 主题
            intent.putExtra(Intent.EXTRA_TEXT, "$content${UserManager.get().user?.sip}") // 正文
            startActivity(Intent.createChooser(intent, "请选择邮件类应用"))
        }else{
            toast(R.string.please_enter_your_suggestion)
        }
    }
}