package vcall.free.international.phone.wifi.calling.ui

import android.annotation.SuppressLint
import android.os.Bundle
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.databinding.ActivityWebBinding
import vcall.free.international.phone.wifi.calling.lib.BaseBackActivity
import vcall.free.international.phone.wifi.calling.lib.BaseDataBindingActivity

class WebActivity: BaseBackActivity<ActivityWebBinding>() {
    override fun getLayoutRes(): Int = R.layout.activity_web
    @SuppressLint("SetJavaScriptEnabled")
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        val title = intent.getStringExtra("title")
        toolbar?.title = title
        val url = intent.getStringExtra("url")
        dataBinding.webView.settings.javaScriptEnabled = true
        dataBinding.webView.loadUrl(url?:"")
    }
}