package ufree.call.international.phone.wifi.vcallfree.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseDataBindingFragment<T:ViewDataBinding>:BaseFragment() {

    lateinit var dataBinding:T
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        dataBinding = DataBindingUtil.inflate(inflater,getLayoutResId(),container,false)
        return dataBinding.root
    }
}