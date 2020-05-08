package ufree.call.international.phone.wifi.vcallfree.ui

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.TextView
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.databinding.ActivityInviteFriendsBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseBackActivity
import ufree.call.international.phone.wifi.vcallfree.utils.UserManager
import ufree.call.international.phone.wifi.vcallfree.utils.dip2px
import ufree.call.international.phone.wifi.vcallfree.utils.getColorFromRes
import ufree.call.international.phone.wifi.vcallfree.utils.screenWidth

/**
 * Created by lyf on 2020/5/8.
 */
class InviteFriendsActivity:BaseBackActivity<ActivityInviteFriendsBinding>() {
    override fun getLayoutRes(): Int = R.layout.activity_invite_friends

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        dataBinding.reward1.text = SpannableString("Invite a friend to get 1000 credits").apply {
            val start = indexOf("1000")
            setSpan(ForegroundColorSpan(getColorFromRes(R.color.text_yellow)),start,start + 4,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        dataBinding.reward2.text = SpannableString("If you invite friends from these countries,you can get up to 3000/10000 bonus credits").apply {
            val start = indexOf("these countries")
            val start2 = indexOf("3000/10000")
            setSpan(ForegroundColorSpan(getColorFromRes(R.color.text_yellow)),start,start + 15,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(getColorFromRes(R.color.text_yellow)),start2,start2+ 10,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        dataBinding.bonus3000.text = SpannableString("Bonus 3,000 rewards").apply {
            val start = indexOf("3,000")
            setSpan(ForegroundColorSpan(getColorFromRes(R.color.text_yellow)),start,start + 5,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        UserManager.get().user?.invite_country_points?._3000?.forEachIndexed {i,s ->
            val tv = TextView(this,null,R.style.RewardItem)
            tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.blue_dot,0,0,0)
            tv.compoundDrawablePadding = dip2px(8)
            tv.text = s
            tv.setTextColor(getColorFromRes(R.color.text_normal))
            tv.textSize = 12f
            val rowSpec = GridLayout.spec(i / 3 + 1,1)
            val columnsSpec = GridLayout.spec(i % 3,1)
            val lp = GridLayout.LayoutParams(rowSpec,columnsSpec).apply {
                width = (screenWidth() - dip2px(28)) / 3
                bottomMargin = dip2px(10)
            }
            dataBinding.gridLayout.addView(tv,lp)
        }
    }
}