package vcall.free.international.phone.wifi.calling.lib

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

/**
 * Created by lyf on 2020/4/27.
 */
abstract class BaseFragment: Fragment() {
    val fragmentTag = javaClass.name.substringAfterLast(".")
    abstract fun getLayoutResId():Int
    abstract fun initView(v: View)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(getLayoutResId(),null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    override fun onPause() {
        super.onPause()
        println("fragment onPause $fragmentTag")
    }

    override fun onResume() {
        super.onResume()
        println("fragment onResume $fragmentTag")
    }
}