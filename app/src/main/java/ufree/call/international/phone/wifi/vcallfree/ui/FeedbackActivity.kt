package ufree.call.international.phone.wifi.vcallfree.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.databinding.ActivityFeedbackBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseBackActivity

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
}