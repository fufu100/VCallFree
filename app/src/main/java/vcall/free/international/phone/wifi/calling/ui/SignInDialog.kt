package vcall.free.international.phone.wifi.calling.ui

import android.app.Dialog
import android.content.Context
import android.view.View
import androidx.databinding.DataBindingUtil
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.databinding.DialogSignInBinding

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