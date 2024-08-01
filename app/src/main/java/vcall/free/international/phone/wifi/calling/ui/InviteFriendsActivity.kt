package vcall.free.international.phone.wifi.calling.ui

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import vcall.free.international.phone.wifi.calling.db.DBHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.databinding.ActivityInviteFriendsBinding
import vcall.free.international.phone.wifi.calling.lib.BaseBackActivity
import vcall.free.international.phone.wifi.calling.utils.UserManager
import vcall.free.international.phone.wifi.calling.utils.dip2px
import vcall.free.international.phone.wifi.calling.utils.getColorFromRes
import vcall.free.international.phone.wifi.calling.utils.screenWidth

/**
 * Created by lyf on 2020/5/8.
 */
class InviteFriendsActivity:BaseBackActivity<ActivityInviteFriendsBinding>() {
    override fun getLayoutRes(): Int = R.layout.activity_invite_friends

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        dataBinding.activity = this
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


        GlobalScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main){
                val tv = TextView(this@InviteFriendsActivity)
                tv.setTextColor(getColorFromRes(R.color.text_normal))
                tv.textSize = 12f
                tv.paint.flags = Paint.FAKE_BOLD_TEXT_FLAG
                tv.text = SpannableString("Bonus 3,000 rewards").apply {
                    val start = indexOf("3,000")
                    setSpan(ForegroundColorSpan(getColorFromRes(R.color.text_yellow)),start,start + 5,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                val rowSpec = GridLayout.spec(0,1)
                val columnsSpec = GridLayout.spec(0,3)
                val lp = GridLayout.LayoutParams(rowSpec,columnsSpec).apply {
                    bottomMargin = dip2px(10)
                    topMargin = dip2px(20)
                }
                dataBinding.gridLayout.addView(tv,lp)
            }
            DBHelper.get().getCountriesByISOs(UserManager.get().user?.inviteCountryPoints?._3000).forEachIndexed { i, country ->
                withContext(Dispatchers.Main){
                    val tv = TextView(this@InviteFriendsActivity,null,R.style.RewardItem)
                    tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.blue_dot,0,0,0)
                    tv.compoundDrawablePadding = dip2px(8)
                    tv.text = country.country
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
            val row = (UserManager.get().user?.inviteCountryPoints?._3000?.size ?:0) / 3 + 2
            withContext(Dispatchers.Main){
                val tv = TextView(this@InviteFriendsActivity)
                tv.setTextColor(getColorFromRes(R.color.text_normal))
                tv.textSize = 12f
                tv.paint.flags = Paint.FAKE_BOLD_TEXT_FLAG
                tv.text = SpannableString("Bonus 10,000 rewards").apply {
                    setSpan(ForegroundColorSpan(getColorFromRes(R.color.text_yellow)),6,12,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                val rowSpec = GridLayout.spec(row,1)
                val columnsSpec = GridLayout.spec(0,3)
                val lp = GridLayout.LayoutParams(rowSpec,columnsSpec).apply {
                    bottomMargin = dip2px(10)
                    topMargin = dip2px(10)
                }
                dataBinding.gridLayout.addView(tv,lp)
            }
//
            DBHelper.get().getCountriesByISOs(UserManager.get().user?.inviteCountryPoints?._10000).forEachIndexed { i, country ->
                withContext(Dispatchers.Main){
                    val tv = TextView(this@InviteFriendsActivity,null,R.style.RewardItem)
                    tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.blue_dot,0,0,0)
                    tv.compoundDrawablePadding = dip2px(8)
                    tv.text = country.country
                    tv.setTextColor(getColorFromRes(R.color.text_normal))
                    tv.textSize = 12f
                    val rowSpec = GridLayout.spec(i / 3 + 1 + row,1)
                    val columnsSpec = GridLayout.spec(i % 3,1)
                    val lp = GridLayout.LayoutParams(rowSpec,columnsSpec).apply {
                        width = (screenWidth() - dip2px(28)) / 3
                        bottomMargin = dip2px(10)
                    }
                    dataBinding.gridLayout.addView(tv,lp)
                }
            }
        }
    }

    fun invite(v: View){
        val textIntent = Intent(Intent.ACTION_SEND)
        textIntent.type = "text/plain"
        textIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.invite_text,UserManager.get().user?.invite ?: ""))
        startActivity(Intent.createChooser(textIntent, "Invite Friends"))
    }
}