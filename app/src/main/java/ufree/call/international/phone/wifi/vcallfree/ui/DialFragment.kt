package ufree.call.international.phone.wifi.vcallfree.ui

import android.view.View
import com.newmotor.x5.db.DBHelper
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.databinding.FragmentDialBinding
import ufree.call.international.phone.wifi.vcallfree.lib.BaseDataBindingFragment
import ufree.call.international.phone.wifi.vcallfree.utils.UserManager

/**
 * Created by lyf on 2020/5/22.
 */
class DialFragment:BaseDataBindingFragment<FragmentDialBinding>() {
    override fun getLayoutResId(): Int = R.layout.fragment_dial

    override fun initView(v: View) {
        if (UserManager.get().country == null) {
            UserManager.get().country = DBHelper.get().getCountry("US")
        }
        dataBinding.country = UserManager.get().country
        dataBinding.activity = this
    }

    fun onClick(v:View){

    }

    fun goBack(v:View){

    }

    fun del(v:View){

    }

    fun call(v:View){

    }
}