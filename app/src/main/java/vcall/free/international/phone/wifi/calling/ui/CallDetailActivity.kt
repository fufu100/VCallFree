package vcall.free.international.phone.wifi.calling.ui

import android.os.Bundle
import com.newmotor.x5.db.DBHelper
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.api.Record
import vcall.free.international.phone.wifi.calling.databinding.ActivityCallDetailBinding
import vcall.free.international.phone.wifi.calling.lib.BaseBackActivity
import vcall.free.international.phone.wifi.calling.utils.LogUtils
import java.util.*

/**
 * Created by lyf on 2020/5/12.
 */
class CallDetailActivity:BaseBackActivity<ActivityCallDetailBinding>() {
    var record:Record? = null
    override fun getLayoutRes(): Int = R.layout.activity_call_detail
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        record = intent.getParcelableExtra("record")
        dataBinding.phone = "+" + record?.code + record?.phone
        LogUtils.println("CallDetailActivity $record")
        dataBinding.country = DBHelper.get().getCountry(record!!.iso)
        val date = Date(record!!.addTime)
        dataBinding.date = String.format(Locale.getDefault(),"%tA, %tB %td,%tl:%tM %tp",date,date,date,date,date,date)
        dataBinding.coinCost.text = record!!.coinCost.toString()
        dataBinding.duration.text = String.format(Locale.getDefault(),"%d:%02d min",record!!.duration / 60,record!!.duration % 60)
    }
}