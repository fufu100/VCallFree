package ufree.call.international.phone.wifi.vcallfree.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.adapter.BaseAdapter
import ufree.call.international.phone.wifi.vcallfree.api.Api
import ufree.call.international.phone.wifi.vcallfree.databinding.ActivityCallRatesBinding
import ufree.call.international.phone.wifi.vcallfree.databinding.ItemCallRateBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseBackActivity
import ufree.call.international.phone.wifi.vcallfree.utils.Dispatcher
import ufree.call.international.phone.wifi.vcallfree.utils.RxUtils

/**
 * Created by lyf on 2020/5/7.
 */
class CallRatesActivity : BaseBackActivity<ActivityCallRatesBinding>() {
    val list: MutableList<Array<String>> = mutableListOf()
    private var iso = ""
    override fun getLayoutRes(): Int = R.layout.activity_call_rates
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        dataBinding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        dataBinding.recyclerView.adapter = BaseAdapter<Array<String>>(list) {
            R.layout.item_call_rate
        }.apply {
            mShowFooterItem = false
            onBindView = { dataBinding, position ->
                val binding: ItemCallRateBinding = dataBinding as ItemCallRateBinding
                if (position == 0) {
                    binding.letterTv.text = list[position][3].substring(0, 1)
                } else {
                    if (list[position - 1][3].first() != list[position][3].first()) {
                        binding.letterTv.text = list[position][3].substring(0, 1)
                    } else {
                        binding.letterTv.text = ""
                    }
                }
            }
        }
        dataBinding.search.setOnClickListener {
            Dispatcher.dispatch(this) {
                navigate(CountriesActivity::class.java)
                requestCode(1)
                defaultAnimate()
            }.go()
        }
        requestData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_call_rates, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.reset -> {
            iso = ""
            requestData()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                val str = data?.getStringExtra("iso") ?: ""
                if (str != iso) {
                    iso = str
                    requestData()
                }
            }
        }
    }

    private fun requestData() {
        dataBinding.loading.show()
        dataBinding.pinnedLetter.visibility = View.GONE
        compositeDisposable.add(Api.getApiService().getCallRates(iso)
            .compose(RxUtils.applySchedulers())
            .subscribe({
                dataBinding.loading.hide()
                dataBinding.pinnedLetter.visibility = View.VISIBLE
                if (it.errcode == 0) {
                    list.clear()
                    list.addAll(it.rates)
                    dataBinding.recyclerView.adapter?.notifyDataSetChanged()
                }
            }, {
                it.printStackTrace()
                dataBinding.pinnedLetter.visibility = View.VISIBLE
                dataBinding.loading.hide()
            })
        )
    }
}