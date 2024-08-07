package vcall.free.international.phone.wifi.calling.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import vcall.free.international.phone.wifi.calling.db.DBHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vcall.free.international.phone.wifi.calling.MainActivity
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.adapter.BaseAdapter
import vcall.free.international.phone.wifi.calling.api.Record
import vcall.free.international.phone.wifi.calling.databinding.FragmentTabRecentsBinding
import vcall.free.international.phone.wifi.calling.lib.BaseDataBindingFragment
import vcall.free.international.phone.wifi.calling.lib.BaseFragment
import vcall.free.international.phone.wifi.calling.utils.Dispatcher
import vcall.free.international.phone.wifi.calling.utils.UserManager
import vcall.free.international.phone.wifi.calling.widget.WrapLinearLayoutManager

/**
 * Created by lyf on 2020/4/28.
 */
class RecentFragment:BaseDataBindingFragment<FragmentTabRecentsBinding>() ,BaseAdapter.OnItemClick<Record>{
    val list: MutableList<Record> = mutableListOf()
    override fun getLayoutResId(): Int = R.layout.fragment_tab_recents

    override fun initView(v: View) {
        dataBinding.recyclerView.layoutManager =
            WrapLinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        dataBinding.recyclerView.adapter = BaseAdapter<Record>(list){
            R.layout.item_call_record
        }.apply {
            mOnItemClickListener = this@RecentFragment
            mShowFooterItem = false
            onBindView = { dataBinding, position ->
                run {
                    dataBinding?.root?.setOnLongClickListener {
                        AlertDialog.Builder(context).setMessage(R.string.delete_call_record)
                            .setPositiveButton(R.string.confirm){_,_ ->
                                DBHelper.get().deleteCallRecord(list[position].id)
                                list.removeAt(position)
                                this@RecentFragment.dataBinding.recyclerView.adapter?.notifyDataSetChanged()
                            }
                            .setNegativeButton(R.string.cancel,null)
                            .create()
                            .show()
                        true
                    }
                }
            }
        }
        dataBinding.dial.setOnClickListener { dial() }


    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            getRecentCalls()
            withContext(Dispatchers.Main){
                dataBinding.recyclerView.adapter?.notifyDataSetChanged()
                if(list.size == 0){
                    dataBinding.emptyLayout.visibility = View.VISIBLE
                }else{
                    dataBinding.emptyLayout.visibility = View.GONE
                }
            }
        }
    }

    fun getRecentCalls(){
        list.clear()
        list.addAll(DBHelper.get().getCallRecords())
    }

    override fun onItemClick(id: Int, position: Int, t: Record) {
        if(id == R.id.invite){
            Dispatcher.dispatch(context){
                action(Intent.ACTION_SENDTO)
                data(Uri.parse("smsto:${t.phone}"))
                extra("sms_body",context?.getString(R.string.invite_text,
                    UserManager.get().user?.invite ?: "") ?: "")
                defaultAnimate()
            }.go()
        }else{
            (activity as MainActivity).dial(t.phone,t.iso,t.username?:"")
        }
    }

    private fun dial(){
        (activity as MainActivity).dial()
    }
}