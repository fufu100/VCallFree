package ufree.call.international.phone.wifi.vcallfree.ui

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.newmotor.x5.db.DBHelper
import kotlinx.android.synthetic.main.fragment_tab_recents.*
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.adapter.BaseAdapter
import ufree.call.international.phone.wifi.vcallfree.api.Record
import ufree.call.international.phone.wifi.vcallfree.lib.BaseFragment
import ufree.call.international.phone.wifi.vcallfree.utils.Dispatcher

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
            R.layout.item_contacts
        }.apply {
            mOnItemClickListener = this@RecentFragment
            mShowFooterItem = false
        }
        dial.setOnClickListener { dial() }
    }

    suspend fun getRecentCalls(){
        list.addAll(DBHelper.get().getCallRecords())
        recyclerView.adapter?.notifyDataSetChanged()
        if(list.size == 0){
            emptyLayout.visibility = View.VISIBLE
        }else{
            emptyLayout.visibility = View.GONE
        }
    }

    override fun onItemClick(id: Int, position: Int, t: Record) {

    }

    private fun dial(){
        Dispatcher.dispatch(context){
            navigate(DialActivity::class.java)
            defaultAnimate()
        }.go()
    }
}