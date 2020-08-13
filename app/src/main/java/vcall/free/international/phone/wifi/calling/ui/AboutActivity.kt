package vcall.free.international.phone.wifi.calling.ui

import android.os.Bundle
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.databinding.ActivityAboutBinding
import vcall.free.international.phone.wifi.calling.lib.BaseBackActivity
import vcall.free.international.phone.wifi.calling.utils.getVersionName

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