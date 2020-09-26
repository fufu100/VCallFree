package vcall.free.international.phone.wifi.calling.widget

import android.app.Activity
import android.app.Dialog
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.utils.DrawableUtils
import vcall.free.international.phone.wifi.calling.utils.dip2px
import java.lang.Exception
import java.lang.ref.WeakReference

/**
 * Created by lyf on 2019-10-17.
 */
class Loading(act:Activity) {
    val activity:WeakReference<Activity> = WeakReference(act)
    var canCancel = true
    var dialog:Dialog? = null
    var progressBar:ProgressBar? = null;
    var tv:TextView? = null

    fun show(message:String){
        if(activity.get() != null && !(activity.get())!!.isFinishing){
            (activity.get())!!.runOnUiThread {
                if(dialog == null){
                    dialog = Dialog(activity.get()!!).apply {
                        setContentView(LayoutInflater.from(activity.get()!!).inflate(R.layout.dialog_loading,null).also {
                            progressBar = it.findViewById(R.id.loading)
                            tv = it.findViewById(R.id.text)
                            tv?.text = message;
                        })
                        if(window != null) {
                            window!!.setDimAmount(0.25f)
                            window!!.setBackgroundDrawable(DrawableUtils.generate {
                                solidColor(0x54000000)
                                radius(activity.get()!!.dip2px(8))
                                build()
                            })
                        }
                        setCancelable(this@Loading.canCancel)
                    }
                }
                dialog?.show()
            }
        }
    }

    fun show(message:Int){
        show(activity.get()?.resources?.getString(message) ?: "")
    }

    fun show(){
        show(activity.get()?.resources?.getString(R.string.loading) ?: "")
    }

    fun isShowing():Boolean = dialog != null && dialog?.isShowing ?: false

    fun dismiss(){
        if(activity.get() != null && !(activity.get())!!.isFinishing){
            (activity.get())!!.runOnUiThread{
                try {
                    if(isShowing()){
                        dialog?.dismiss()
                        dialog = null
                    }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
    }

}