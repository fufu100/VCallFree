package ufree.call.international.phone.wifi.vcallfree.ui

import android.os.Bundle
import com.newmotor.x5.db.DBHelper
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.api.Record
import ufree.call.international.phone.wifi.vcallfree.databinding.ActivityCallResultBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseBackActivity
import java.util.*

/**
 * Created by lyf on 2020/5/11.
 */
class CallResultActivity:BaseBackActivity<ActivityCallResultBinding>() {
    override fun getLayoutRes(): Int = R.layout.activity_call_result
    lateinit var record: Record
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        record = intent.getParcelableExtra("record")
        dataBinding.phone = "+" + record.code + record.phone
        dataBinding.country = DBHelper.get().getCountry(record.iso)
        val date = Date(record.addTime)
        dataBinding.date = String.format(Locale.getDefault(),"%tA, %tB %td, %tl:%tM %tp",date,date,date,date,date,date)
        dataBinding.coinCost.text = record!!.coinCost.toString()
        dataBinding.duration.text = String.format(Locale.getDefault(),"%d:%02d min",record.duration / 60,record.duration % 60)

        println("CallResultActivity duration=${record.duration}")
        if(record.duration > 10){
            RateDialog(this).show()
        }
    }
}