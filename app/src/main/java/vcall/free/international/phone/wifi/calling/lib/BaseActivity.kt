package vcall.free.international.phone.wifi.calling.lib

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import vcall.free.international.phone.wifi.calling.R

@SuppressLint("Registered")
abstract class BaseActivity :AppCompatActivity() {
    val tag = javaClass.name.substringAfterLast(".")
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

    override fun onResume() {
        super.onResume()
        println("onResume  $tag ${supportFragmentManager.fragments.size}")
    }

    override fun onPause() {
        super.onPause()
        println("onPause $tag ${supportFragmentManager.fragments.size}")
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
            compositeDisposable.clear()
        }
    }
}