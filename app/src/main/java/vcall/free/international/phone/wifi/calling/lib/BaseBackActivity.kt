package vcall.free.international.phone.wifi.calling.lib

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.databinding.ViewDataBinding
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.utils.closeKeyBoard

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