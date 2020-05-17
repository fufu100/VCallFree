package ufree.call.international.phone.wifi.vcallfree.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_call.view.*
import ufree.call.international.phone.wifi.vcallfree.utils.dip2px
import ufree.call.international.phone.wifi.vcallfree.utils.screenHeight
import ufree.call.international.phone.wifi.vcallfree.utils.screenWidth
import kotlin.math.abs

/**
 * 转盘页面的layout，控制折叠和展开
 */
class CoinLayout(context: Context, attributeSet: AttributeSet?, defStyle: Int) :
    ViewGroup(context, attributeSet, defStyle) {
    private val TAG = "CoinLayout"
    lateinit var mapView: View
    private lateinit var drawerLayout: ViewGroup
    private var drawerHeight: Int = 0
    var isOpen: Boolean = false
    private var lastY: Float = 0f
    private var isDragging: Boolean = false
    private var isDownInDrawerLayout: Boolean = false
    private val touchSlop: Int = ViewConfiguration.get(context).scaledTouchSlop
    private var fromDown2Up = false
    var checker:CanScrollVerticalChecker? = null

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    override fun onFinishInflate() {
        super.onFinishInflate()
        mapView = getChildAt(0)
        drawerLayout = getChildAt(1) as ViewGroup
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        mapView.measure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(
                measuredHeight ,
                MeasureSpec.AT_MOST
            )
        )
        drawerHeight = height - mapView.measuredHeight
        drawerLayout.measure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
        )
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),MeasureSpec.getSize(heightMeasureSpec))
    }


    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
//        println("$TAG $changed $l,$t,$r,$b")
        if (isOpen) {
            mapView.layout(0, t, r - l, b - drawerHeight)
            drawerLayout.layout(
                0,
                b - drawerLayout.measuredHeight,
                r - l,
                b
            )
        } else {
            mapView.layout(0, t, r - l, b - drawerHeight)
            drawerLayout.layout(
                0,
                b - drawerHeight,
                r -l,
                b - drawerHeight + drawerLayout.measuredHeight
            )
        }

    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> {
                lastY = ev.y
                isDownInDrawerLayout = lastY > drawerLayout.top
            }
            MotionEvent.ACTION_MOVE -> {
                val y = ev!!.y
                val diff = y - lastY
                if (isOpen) {
                    if (checker!!.canScrollVertical()  && isDownInDrawerLayout && abs(diff) > touchSlop && diff > 0 && !isDragging) {
                        isDragging = true
                    }
                } else {
                    if (isDownInDrawerLayout && abs(diff) > touchSlop && !isDragging) {
                        isDragging = true
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
            }


        }
        return isDragging
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!isDragging) {
            return super.onTouchEvent(event)
        }
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
            }
            MotionEvent.ACTION_MOVE -> {

                if (isDragging) {
                    var diff = ((event.y - lastY) * 0.75f).toInt()
//                    println("diff=$diff,top=${drawerLayout.top}")
                    fromDown2Up = diff < 0
                    if ((isOpen && !fromDown2Up) || (!isOpen && fromDown2Up)) {
                        if(fromDown2Up){
                            if(drawerLayout.bottom + diff < height){
                                diff = height - drawerLayout.bottom
                            }
                        }else{
                            if(drawerLayout.top + diff > mapView.measuredHeight){
                                diff = mapView.measuredHeight - drawerLayout.top
                            }
                        }
//                        println("diff2 = $diff")
                        drawerLayout.offsetTopAndBottom(diff)
                        lastY = event.y
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDragging = false
                drawerLayout.clearAnimation()
                if (fromDown2Up) {
//                    animateToUpPosition.start()
                    ObjectAnimator.ofInt(drawerLayout.top, height - drawerLayout.height).apply {
                        addUpdateListener {
                            val t = it.animatedValue as Int
                            drawerLayout.offsetTopAndBottom(t - drawerLayout.top)
                        }
                        duration = 200
                        addListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                isOpen = true
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    mapView.foreground = ColorDrawable(0x40000000)
                                }
                                checker?.onExpandStateChange(true)
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationStart(animation: Animator?) {
                            }

                        })
                    }.start()
                } else {
//                    animateToDownPosition.start()
                    ObjectAnimator.ofInt(drawerLayout.top, mapView.height).apply {
                        addUpdateListener {
                            val t = it.animatedValue as Int
                            drawerLayout.offsetTopAndBottom(t - drawerLayout.top)
                        }
                        addListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {
                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                isOpen = false
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    mapView.foreground = null
                                }
                                checker?.onExpandStateChange(false)
                            }

                            override fun onAnimationCancel(animation: Animator?) {
                            }

                            override fun onAnimationStart(animation: Animator?) {
                            }

                        })
                        duration = 200
                    }.start()
                }
            }

        }
        return true
    }

    interface CanScrollVerticalChecker{
        fun canScrollVertical():Boolean
        fun onExpandStateChange(expand:Boolean)
    }


}