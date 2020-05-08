package ufree.call.international.phone.wifi.vcallfree.adapter

import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView

open class BaseViewHolder<T>(itemView: View) :RecyclerView.ViewHolder(itemView){

    var binding:ViewDataBinding? = DataBindingUtil.bind(itemView)

    open fun setBinding( obj:T,position:Int,listener: BaseAdapter.OnItemClick<T>?){
        binding?.setVariable(BR.obj,obj)
        binding?.setVariable(BR.position,position)
        binding?.setVariable(BR.onItemClickListener,listener)
        binding?.executePendingBindings()
    }
}