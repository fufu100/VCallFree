package vcall.free.international.phone.wifi.calling.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import vcall.free.international.phone.wifi.calling.db.DBHelper
import kotlinx.coroutines.*
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.adapter.BaseAdapter
import vcall.free.international.phone.wifi.calling.api.Api
import vcall.free.international.phone.wifi.calling.databinding.ActivityCallRatesBinding
import vcall.free.international.phone.wifi.calling.databinding.ItemCallRateBinding
import vcall.free.international.phone.wifi.calling.lib.BaseBackActivity
import vcall.free.international.phone.wifi.calling.utils.RxUtils
import vcall.free.international.phone.wifi.calling.utils.closeKeyBoard
import vcall.free.international.phone.wifi.calling.widget.WrapLinearLayoutManager

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
            WrapLinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
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
                    closeKeyBoard()
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
                        if (it.code == 20000) {
                            list.clear()
                            list.addAll(it.data)
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