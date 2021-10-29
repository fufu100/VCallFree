package vcall.free.international.phone.wifi.calling.widget

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by lyf on 9/24/21.
 */
class WrapLinearLayoutManager(context: Context?,orientation:Int,reverse:Boolean):LinearLayoutManager(context,orientation,reverse) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        }catch (e:Exception){
            e.printStackTrace()
        }

    }
}