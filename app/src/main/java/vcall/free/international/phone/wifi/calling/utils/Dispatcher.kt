package vcall.free.international.phone.wifi.calling.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import vcall.free.international.phone.wifi.calling.R
import java.io.Serializable
import java.util.ArrayList

class Dispatcher (private val context: Context?){
    var intent: Intent = Intent()
    private var options: ActivityOptionsCompat? = null
    private var requestCode = -1
    private var mFlag: Int = 0
    private var serviceConn: ServiceConnection? = null
    companion object{
        fun dispatch(context: Context?,body: Dispatcher.() -> Dispatcher): Dispatcher {
            return with(Dispatcher(context)){
                body()
            }
        }
        fun dispatch(context: Context?): Dispatcher {
            return Dispatcher(context)
        }
    }

    init {
        intent = Intent()
    }

    fun extra(key: String, value: String): Dispatcher {
        intent.putExtra(key, value)
        return this
    }

    fun extra(key: String, value: Array<String>): Dispatcher {
        intent.putExtra(key, value)
        return this
    }

    fun extra(key: String, value: Int): Dispatcher {
        intent.putExtra(key, value)
        return this
    }

    fun extra(key: String, value: Long): Dispatcher {
        intent.putExtra(key, value)
        return this
    }

    fun extra(key: String, value: Boolean): Dispatcher {
        intent.putExtra(key, value)
        return this
    }

    fun extra(key: String, value: Double): Dispatcher {
        intent.putExtra(key, value)
        return this
    }

    fun extra(bundle: Bundle): Dispatcher {
        intent.putExtras(bundle)
        return this
    }

    fun extra(key: String, value: Parcelable): Dispatcher {
        intent.putExtra(key, value)
        return this
    }

    fun extra(key: String, value: Array<Parcelable>): Dispatcher {
        intent.putExtra(key, value)
        return this
    }

    fun extra(key: String, value: Serializable): Dispatcher {
        intent.putExtra(key, value)
        return this
    }

    fun extra(key: String, list: ArrayList<out Parcelable>): Dispatcher {
        intent.putParcelableArrayListExtra(key, list)
        return this
    }
    fun data(uri: Uri): Dispatcher {
        intent.data = uri
        return this
    }

    fun flag(flag: Int): Dispatcher {
        intent.flags = flag
        return this
    }

    fun requestCode(code: Int): Dispatcher {
        this.requestCode = code
        return this
    }

    fun defaultAnimate(): Dispatcher {
        return animate(R.anim.right_slide_enter_anim, R.anim.left_slide_exit_anim)
    }

    fun animate(enterResId: Int, exitResId: Int): Dispatcher {
        if(context != null) {
            options = ActivityOptionsCompat.makeCustomAnimation(context, enterResId, exitResId)
        }
        return this
    }

    fun animate(transView: View, transName: String): Dispatcher {
        options = ActivityOptionsCompat.makeSceneTransitionAnimation(context as Activity, transView, transName)
        return this
    }

    fun action(action: String): Dispatcher {
        intent.action = action
        return this
    }
    fun type(type: String): Dispatcher {
        intent.type = type
        return this
    }

    fun navigate(classes: Class<*>): Dispatcher {
        if(context != null) {
            intent.component = ComponentName(context, classes)
        }
        return this
    }

    fun go(){
        var bundle: Bundle? = null
        if (options != null) {
            bundle = options!!.toBundle()
        }
        if(context != null && context.packageManager.resolveActivity(intent,0) != null) {
            if (context is Activity) {
                ActivityCompat.startActivityForResult(
                    context,
                    intent,
                    requestCode,
                    bundle
                )
            } else {
                ContextCompat.startActivities(context, arrayOf(intent), bundle)
            }
        }
    }

    fun send() {
        context?.sendBroadcast(intent)
    }

    fun bind(@NonNull connection: ServiceConnection, flag: Int): Dispatcher {
        this.serviceConn = connection
        this.mFlag = flag
        return this
    }

    fun start() {
        if (serviceConn != null) {
            context?.bindService(intent, serviceConn!!, mFlag)
        } else {
            context?.startService(intent)
        }
    }
}