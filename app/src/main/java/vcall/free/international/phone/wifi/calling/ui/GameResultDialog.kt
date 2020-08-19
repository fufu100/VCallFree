package vcall.free.international.phone.wifi.calling.ui

import android.app.Dialog
import android.content.Context
import android.view.View
import androidx.databinding.DataBindingUtil
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.databinding.DialogPlayGameResultBinding

/**
 * Created by lyf on 2020/8/13.
 */
class GameResultDialog(context: Context,val getCallback:() -> Unit,val moreCallback:(() -> Unit)? = null) : Dialog(context, R.style.CustomDialog) {
    val dataBinding = DataBindingUtil.inflate<DialogPlayGameResultBinding>(
        layoutInflater,
        R.layout.dialog_play_game_result,
        null,
        false
    )

    init {
        dataBinding.dialog = this
        setContentView(dataBinding.root)
        setCancelable(false)
    }

    fun setResult(res:String){
        dataBinding.resultTv.text = res
    }

    fun showMore(){
        dataBinding.morePointTv.visibility = View.VISIBLE
    }

    fun get(){
        getCallback()
        dismiss()
    }

    fun more(){
        moreCallback?.let { it() }
        dismiss()
    }
}