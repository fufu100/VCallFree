package ufree.call.international.phone.wifi.vcallfree.lib

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

/**
 * Created by lyf on 2019-09-20.
 */
abstract class BaseDataBindingActivity<T: ViewDataBinding> : BaseActivity() {
    lateinit var dataBinding:T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dataBinding = DataBindingUtil.setContentView(this,getLayoutRes())
        initView(savedInstanceState)
    }

    open fun initView(savedInstanceState: Bundle?){

    }
}