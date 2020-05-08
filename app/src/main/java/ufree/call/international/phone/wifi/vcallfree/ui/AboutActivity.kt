package ufree.call.international.phone.wifi.vcallfree.ui

import android.os.Bundle
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.databinding.ActivityAboutBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseBackActivity
import ufree.call.international.phone.wifi.vcallfree.utils.getVersionName

/**
 * Created by lyf on 2020/4/7.
 */
class AboutActivity:BaseBackActivity<ActivityAboutBinding>() {
    override fun getLayoutRes(): Int = R.layout.activity_about

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        dataBinding.version = getVersionName()
    }
}