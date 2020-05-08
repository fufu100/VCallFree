package ufree.call.international.phone.wifi.vcallfree.utils

import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

class TabManager(private val fragmentActivity: AppCompatActivity,private val fragments:List<Fragment>,private val fragmentContentId: Int,val rgs:RadioGroup):RadioGroup.OnCheckedChangeListener{
    private var currentTab: Int = 0
    private var onRadioGroupCheckedChangedListener: OnRadioGroupCheckedChangedListener? = null

   init {
       val b = fragments[0].isAdded
       LogUtils.d("TabManager","whether NewFragment is added:$b")
       if (!b) {
           val ft = fragmentActivity.supportFragmentManager.beginTransaction()
           ft.add(fragmentContentId, fragments[0])
           ft.commit()
       }

       rgs.setOnCheckedChangeListener(this)
   }

    override fun onCheckedChanged(radioGroup: RadioGroup, checkedId: Int) {
        LogUtils.d("TabManager"," onCheckedChanged ............." + (radioGroup.findViewById<View>(checkedId) as RadioButton).text + "," + getCurrentTab())
        for (i in 0 until rgs.childCount) {
            if (rgs.getChildAt(i).id == checkedId && i != getCurrentTab()) {
                LogUtils.d("TabManager"," onCheckedChanged :$i")
                val fragment = fragments[i]
                val ft = obtainFragmentTransaction(i)
                getCurrentFragment().onPause()
                if (fragment.isAdded) {
                    fragment.onResume()
                } else {
                    ft.add(fragmentContentId, fragment)
                }
                showTab(i)
                ft.commitAllowingStateLoss()
                if (null != onRadioGroupCheckedChangedListener) {
                    onRadioGroupCheckedChangedListener!!.onRgsExtraCheckedChanged(radioGroup, checkedId, i)
                }

            }
        }

    }

    /**
     * 切换tab
     *
     * @param idx
     */
    fun showTab(idx: Int) {
        for (i in fragments.indices) {
            val fragment = fragments[i]
            val ft = obtainFragmentTransaction(idx)

            if (idx == i) {
                ft.show(fragment)
            } else {
                ft.hide(fragment)
            }
            ft.commitAllowingStateLoss()
        }
        currentTab = idx // 更新目标tab为当前tab
    }

    /**
     * 获取一个带动画的FragmentTransaction
     *
     * @param index
     * @return
     */
    private fun obtainFragmentTransaction(index: Int): FragmentTransaction {
// 设置切换动画
        //        if(index > currentTab){
        //            ft.setCustomAnimations(R.anim.in_from_right, R.anim.out_to_left);
        //        }else{
        //            ft.setCustomAnimations(R.anim.in_from_left, R.anim.out_to_right);
        //        }
        return fragmentActivity.supportFragmentManager.beginTransaction()
    }

    fun getCurrentTab(): Int {
        return currentTab
    }

    fun getCurrentFragment(): Fragment {
        return fragments[currentTab]
    }

    fun getOnRgsExtraCheckedChangedListener(): OnRadioGroupCheckedChangedListener? {
        return onRadioGroupCheckedChangedListener
    }

    fun setOnRadioGroupCheckedChangedListener(onRgsExtraCheckedChangedListener: OnRadioGroupCheckedChangedListener) {
        this.onRadioGroupCheckedChangedListener = onRgsExtraCheckedChangedListener
    }

    /**
     * 切换tab额外功能功能接口
     */
    interface OnRadioGroupCheckedChangedListener {
        fun onRgsExtraCheckedChanged(radioGroup: RadioGroup, checkedId: Int, index: Int)
    }

}