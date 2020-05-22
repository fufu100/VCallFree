package ufree.call.international.phone.wifi.vcallfree.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newmotor.x5.db.DBHelper
import kotlinx.coroutines.*
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.adapter.BaseAdapter
import ufree.call.international.phone.wifi.vcallfree.api.Country
import ufree.call.international.phone.wifi.vcallfree.databinding.ActivityCoutriesBinding
import ufree.call.international.phone.wifi.vcallfree.databinding.ItemCountryBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseBackActivity

/**
 * Created by lyf on 2020/5/7.
 */
class CountriesActivity:BaseBackActivity<ActivityCoutriesBinding>(),BaseAdapter.OnItemClick<Country> ,TextWatcher{
    val list:MutableList<Country> = mutableListOf()
    override fun getLayoutRes(): Int = R.layout.activity_coutries

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        dataBinding.recyclerView.layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.VERTICAL,false)
        dataBinding.recyclerView.adapter = BaseAdapter<Country>(list){
            R.layout.item_country
        }.apply {
            mShowFooterItem = false
            mOnItemClickListener = this@CountriesActivity
            onBindView = { dataBinding, position ->
                val binding: ItemCountryBinding = dataBinding as ItemCountryBinding
                if (position == 0) {
                    binding.letterTv.text = list[position].country.subSequence(0,1)
                } else {
                    if (list[position - 1].country.first() != list[position].country.first()) {
                        binding.letterTv.text = list[position].country.substring(0, 1)
                    } else {
                        binding.letterTv.text = ""
                    }
                }
            }
        }

        dataBinding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstPosition = linearLayoutManager.findFirstVisibleItemPosition()
                dataBinding.pinnedLetter.text = list[firstPosition].country.first().toString()
            }
        })
        dataBinding.search.addTextChangedListener(this)

        GlobalScope.launch(Dispatchers.IO) {
            loadData()
            withContext(Dispatchers.Main){
                dataBinding.recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    var job:Job? = null

    private fun search(keyword: String){
        job?.cancel()
        job = GlobalScope.launch {
            delay(500)
            loadData(keyword)
            withContext(Dispatchers.Main){
                dataBinding.recyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }

    private fun loadData(keyword: String = "") {
        list.clear()
        list.addAll(DBHelper.get().getAllCountries(keyword))
    }

    override fun onItemClick(id: Int, position: Int, t: Country) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("iso",t.iso)
        })
        finish()
    }

    override fun afterTextChanged(s: Editable?) {
        search(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }
}