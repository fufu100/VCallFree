package ufree.call.international.phone.wifi.vcallfree.lib

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.databinding.ViewDataBinding
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.utils.closeKeyBoard

/**
 * Created by lyf on 2020/4/28.
 */
abstract class BaseBackActivity<T: ViewDataBinding>:BaseDataBindingActivity<T>() {
    protected var toolbar: Toolbar? = null

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        toolbar = findViewById(R.id.toolbar)
        if(toolbar != null) {
            setSupportActionBar(toolbar)
            toolbar!!.setNavigationOnClickListener {
                closeKeyBoard()
                finish()
            }
        }
    }
}