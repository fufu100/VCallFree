package ufree.call.international.phone.wifi.vcallfree.ui

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.newmotor.x5.db.DBHelper
import kotlinx.android.synthetic.main.fragment_tab_recents.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ufree.call.international.phone.wifi.vcallfree.MainActivity
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.adapter.BaseAdapter
import ufree.call.international.phone.wifi.vcallfree.api.Record
import ufree.call.international.phone.wifi.vcallfree.lib.BaseFragment
import ufree.call.international.phone.wifi.vcallfree.utils.Dispatcher
import ufree.call.international.phone.wifi.vcallfree.utils.UserManager

/**
 * Created by lyf on 2020/4/28.
 */
class RecentFragment:BaseFragment() ,BaseAdapter.OnItemClick<Record>{
    val list: MutableList<Record> = mutableListOf()
    override fun getLayoutResId(): Int = R.layout.fragment_tab_recents

    override fun initView(v: View) {
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = BaseAdapter<Record>(list){
            R.layout.item_call_record
        }.apply {
            mOnItemClickListener = this@RecentFragment
            mShowFooterItem = false
        }
        dial.setOnClickListener { dial() }


    }

    override fun onResume() {
        super.onResume()
        GlobalScope.launch {
            getRecentCalls()
            withContext(Dispatchers.Main){
                recyclerView.adapter?.notifyDataSetChanged()
                if(list.size == 0){
                    emptyLayout.visibility = View.VISIBLE
                }else{
                    emptyLayout.visibility = View.GONE
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
            (activity as MainActivity).dial(t.phone,t.iso)
        }
    }

    private fun dial(){
        (activity as MainActivity).dial()
    }
}