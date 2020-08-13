package vcall.free.international.phone.wifi.calling.ui

import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.adapter.CacheFragmentStatePagerAdapter
import vcall.free.international.phone.wifi.calling.databinding.FragmentIndexBinding
import vcall.free.international.phone.wifi.calling.lib.BaseDataBindingFragment

/**
 * Created by lyf on 2020/5/22.
 */
class IndexFragment:BaseDataBindingFragment<FragmentIndexBinding>() ,RadioGroup.OnCheckedChangeListener,
    ViewPager.OnPageChangeListener{
    private val fragments = mutableListOf<Fragment>()
    override fun getLayoutResId(): Int = R.layout.fragment_index

    override fun initView(v: View) {
        fragments.add(ContractsFragment())
        fragments.add(RecentFragment())
        fragments.add(CoinsFragment())
        dataBinding.viewPager.adapter = object : CacheFragmentStatePagerAdapter(childFragmentManager){
            override fun createItem(position: Int): Fragment = fragments[position]
            override fun getCount(): Int = fragments.size
        }
        dataBinding.viewPager.addOnPageChangeListener(this)
        dataBinding.viewPager.offscreenPageLimit = 2
        dataBinding.radioGroup.setOnCheckedChangeListener(this)
    }

    fun refreshUser(){
        (fragments[2] as CoinsFragment).refreshUser()
    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        when(checkedId){
            R.id.radio_contacts ->
                dataBinding.viewPager.currentItem = 0
            R.id.radio_recents ->
                dataBinding.viewPager.currentItem = 1
            R.id.radio_coins ->
                dataBinding.viewPager.currentItem = 2
        }
    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        (dataBinding.radioGroup.getChildAt(position) as RadioButton).isChecked = true
    }
}