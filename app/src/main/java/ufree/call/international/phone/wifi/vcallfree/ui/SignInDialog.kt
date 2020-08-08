package ufree.call.international.phone.wifi.vcallfree.ui

import android.app.Dialog
import android.content.Context
import android.view.View
import androidx.databinding.DataBindingUtil
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.databinding.DialogSignInBinding

/**
 * Created by lyf on 2020/5/25.
 */
class SignInDialog(context: Context):Dialog(context,R.style.CustomDialog) {
    val dataBinding:DialogSignInBinding = DataBindingUtil.inflate(layoutInflater, R.layout.dialog_sign_in,
        null,false)

    init {
        dataBinding.dialog = this
        setContentView(dataBinding.root)
    }
    fun signin(v: View){

    }
}