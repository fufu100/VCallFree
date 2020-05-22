package ufree.call.international.phone.wifi.vcallfree.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.newmotor.x5.db.DBHelper
import kotlinx.coroutines.*
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.adapter.BaseAdapter
import ufree.call.international.phone.wifi.vcallfree.api.Api
import ufree.call.international.phone.wifi.vcallfree.databinding.ActivityCallRatesBinding
import ufree.call.international.phone.wifi.vcallfree.databinding.ItemCallRateBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseBackActivity
import ufree.call.international.phone.wifi.vcallfree.utils.RxUtils
import ufree.call.international.phone.wifi.vcallfree.utils.UserManager

/**
 * Created by lyf on 2020/5/7.
 */
class CallRatesActivity : BaseBackActivity<ActivityCallRatesBinding>() , TextWatcher,BaseAdapter.OnItemClick<Array<String>> {
    val list: MutableList<Array<String>> = mutableListOf()
    private var iso = ""
    var countryShowing = false
    override fun getLayoutRes(): Int = R.layout.activity_call_rates
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        dataBinding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        dataBinding.recyclerView.adapter = BaseAdapter<Array<String>>(list) {
            R.layout.item_call_rate
        }.apply {
            mShowFooterItem = false
            mOnItemClickListener = this@CallRatesActivity
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
        dataBinding.recyclerView.addOnScrollListener(object :RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if(newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    closeKeyboard()
                }
            }
        })
        dataBinding.search.setOnClickListener {
//            Dispatcher.dispatch(this) {
//                navigate(CountriesActivity::class.java)
//                requestCode(1)
//                defaultAnimate()
//            }.go()
            if(!countryShowing) {
                countryShowing = true
                requestData()
            }
        }
        dataBinding.search.addTextChangedListener(this)
        requestData()
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.menu_call_rates, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
//        R.id.reset -> {
//            iso = ""
//            requestData()
//            true
//        }
//        else -> super.onOptionsItemSelected(item)
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == 1) {
//                val str = data?.getStringExtra("iso") ?: ""
//                if (str != iso) {
//                    iso = str
//                    requestData()
//                }
//            }
//        }
//    }
    var job: Job? = null
    private fun requestData() {
        dataBinding.loading.show()
        dataBinding.pinnedLetter.visibility = View.GONE
        if(countryShowing){
            job?.cancel()
            job = GlobalScope.launch {
                delay(500)
                list.clear()
                list.addAll(DBHelper.get().getAllCountries2(dataBinding.search.text.toString()))
                withContext(Dispatchers.Main){
                    dataBinding.recyclerView.adapter?.notifyDataSetChanged()
//                    if(list.size > 0) {
//                        dataBinding.pinnedLetter.text = list[0][3].substring(0, 1)
//                    }else{
//                        dataBinding.pinnedLetter.visibility = View.GONE
//                    }
                    dataBinding.loading.hide()
                }
            }
        }else {
            compositeDisposable.add(
                Api.getApiService().getCallRates(iso)
                    .compose(RxUtils.applySchedulers())
                    .subscribe({
                        dataBinding.loading.hide()
                        dataBinding.pinnedLetter.visibility = View.VISIBLE
                        if (it.errcode == 0) {
                            list.clear()
                            list.addAll(it.rates)
                            dataBinding.recyclerView.adapter?.notifyDataSetChanged()
//                            if(list.size > 0) {
//                                dataBinding.pinnedLetter.text = list[0][3].substring(0, 1)
//                            }else{
//                                dataBinding.pinnedLetter.visibility = View.GONE
//                            }
                        }
                    }, {
                        it.printStackTrace()
                        dataBinding.pinnedLetter.visibility = View.VISIBLE
                        dataBinding.loading.hide()
                    })
            )
        }
    }

    fun closeKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }

    override fun afterTextChanged(s: Editable?) {
        requestData()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }

    override fun onItemClick(id: Int, position: Int, t: Array<String>) {
        if(countryShowing){
            iso = t[0]
            countryShowing = false
            requestData()
        }
//            setResult(Activity.RESULT_OK, Intent().apply {
//                putExtra("iso",t[0])
//            })
//            UserManager.get().country = DBHelper.get().getCountry(t[0])
//            finish()
//        }
    }
}