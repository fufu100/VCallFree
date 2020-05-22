package ufree.call.international.phone.wifi.vcallfree.ui

import android.os.Bundle
import android.view.View
import ufree.call.international.phone.wifi.vcallfree.lib.prefs
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.databinding.ActivitySettingBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseBackActivity
import ufree.call.international.phone.wifi.vcallfree.utils.Dispatcher

/**
 * Created by lyf on 2020/4/7.
 */
class SettingActivity:BaseBackActivity<ActivitySettingBinding>() {
    override fun getLayoutRes(): Int  = R.layout.activity_setting
    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        dataBinding.activity = this

        prefs.getBooleanValue("play_tone",true).also {
            dataBinding.swPlayTone.isChecked = it
        }
        prefs.getBooleanValue("vibration",false).also {
            dataBinding.swVibration.isChecked= it
        }

        dataBinding.swPlayTone.setOnCheckedChangeListener { buttonView, isChecked ->
            prefs.save("play_tone",isChecked)
        }
        dataBinding.swVibration.setOnCheckedChangeListener { buttonView, isChecked ->
            prefs.save("vibration",isChecked)
        }
    }
    fun onClick(v: View){
        when(v.id){
            R.id.about ->
                Dispatcher.dispatch(this){
                    navigate(AboutActivity::class.java)
                    defaultAnimate()
                }.go()
        }
    }
}