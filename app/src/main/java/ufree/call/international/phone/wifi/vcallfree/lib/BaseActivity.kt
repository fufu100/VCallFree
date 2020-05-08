package ufree.call.international.phone.wifi.vcallfree.lib

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import ufree.call.international.phone.wifi.vcallfree.R

@SuppressLint("Registered")
abstract class BaseActivity :AppCompatActivity() {

    protected val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayoutRes())
    }

    abstract fun getLayoutRes():Int

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.left_slip_enter_anim, R.anim.right_slip_exit_anim)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
            compositeDisposable.clear()
        }
    }
}