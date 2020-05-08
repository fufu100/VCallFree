package ufree.call.international.phone.wifi.vcallfree.ui

import android.view.View
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.databinding.FragmentTabCoinsBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseDataBindingFragment
import ufree.call.international.phone.wifi.vcallfree.utils.Dispatcher

/**
 * Created by lyf on 2020/4/28.
 */
class CoinsFragment:BaseDataBindingFragment<FragmentTabCoinsBinding>() {
    override fun getLayoutResId(): Int = R.layout.fragment_tab_coins

    override fun initView(v: View) {
        dataBinding.fragment = this
    }

    fun start(v: View){
        dataBinding.panView.startRotate(3)
    }

    fun setPhoneNumber(v: View){
        Dispatcher.dispatch(context){
            navigate(SetPhoneNumberActivity::class.java)
            defaultAnimate()
        }.go()
    }
}