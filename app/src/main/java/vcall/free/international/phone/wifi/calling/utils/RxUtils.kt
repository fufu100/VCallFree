package vcall.free.international.phone.wifi.calling.utils

import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

@Suppress("UNCHECKED_CAST")
class RxUtils {
    companion object {
        val schedulersTransformer: ObservableTransformer<Any, Any>
            get() = ObservableTransformer<Any, Any> { o ->
                (o as Observable).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            }

        fun <T> applySchedulers(): ObservableTransformer<T, T> {
            return schedulersTransformer as ObservableTransformer<T, T>
        }
    }
}