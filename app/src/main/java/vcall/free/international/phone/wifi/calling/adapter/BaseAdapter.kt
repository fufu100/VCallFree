package vcall.free.international.phone.wifi.calling.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import vcall.free.international.phone.wifi.calling.BR
import vcall.free.international.phone.wifi.calling.R

open class BaseAdapter<T>(protected val list:MutableList<T>?,val itemViewCrator: (Int) -> Int): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object{
        const val TYPE_FOOTER = 0
        const val TYPE_FOOTER2 = 1
        const val TYPE_HEADER = 2
        const val TYPE_COMMON = 100
    }

    var headView: View? = null
    var footView: View? = null
    var mOnItemClickListener:OnItemClick<T>? = null
    var mShowFooterItem = true
    var isFooterLoading =  ObservableBoolean(true)
    var onBindView:((binding:ViewDataBinding?,position:Int) -> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            TYPE_HEADER -> HeadViewHolder(headView!!)
            TYPE_FOOTER2 -> HeadViewHolder(footView!!)
            TYPE_FOOTER -> FootViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.item_comm_footer,parent,false))
            else -> generateViewHolder(parent,viewType)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val type = if (headView != null && position == 0) {
            TYPE_HEADER
        } else if (mShowFooterItem && position == itemCount - 1) {
            TYPE_FOOTER
        } else if (footView != null && ((mShowFooterItem && position == itemCount - 2) || (!mShowFooterItem && position == itemCount - 1))) {
            TYPE_FOOTER2
        } else {
            TYPE_COMMON
        }
        return type
    }

    open fun generateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        BaseViewHolder<T>(LayoutInflater.from(parent.context).inflate(itemViewCrator(viewType),parent,false))

    open fun getRealPosition(position: Int):Int{
        return if(headView == null) position else position - 1
    }

    override fun getItemCount(): Int {
        val begin = if (headView == null) 0 else 1
        val end = if (mShowFooterItem) 1 else 0
        val end2 = if (footView == null) 0 else 1
        val listSize = list?.size ?: 0
        return begin + listSize+ end + end2
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is BaseViewHolder<*>){
            val realPosition = getRealPosition(position)
            onBindView?.let { it(holder.binding,realPosition) }
            (holder as BaseViewHolder<T>).setBinding(list!![realPosition],realPosition,mOnItemClickListener)
        }else if(holder is BaseAdapter<*>.FootViewHolder){
            holder.setBinding()
        }
    }

    inner class HeadViewHolder(itemView:View):RecyclerView.ViewHolder(itemView)

    inner class FootViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var binding: ViewDataBinding? = DataBindingUtil.bind(itemView)

        fun setBinding(){
            binding?.setVariable(BR.obj,this@BaseAdapter)
            binding?.executePendingBindings()
        }
    }

    interface  OnItemClick<T>{
        fun onItemClick(id:Int,position:Int,t:T)
    }
}