package ufree.call.international.phone.wifi.vcallfree.widget

import android.animation.*
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageView
import ufree.call.international.phone.wifi.vcallfree.R
import ufree.call.international.phone.wifi.vcallfree.utils.dip2px


/**
 * Created by lyf on 2020/5/8.
 */
class WheelPanView(context:Context,attributes: AttributeSet?,defStyle:Int):View(context,attributes,defStyle) {
    constructor(context: Context,attributes: AttributeSet?):this(context,attributes,0)
    constructor(context: Context):this(context,null,0)

    var paint:Paint = Paint()
    var textPaint:Paint = Paint()
    val panColors = intArrayOf(0xFFF5CB57.toInt(),0xFFF2A93C.toInt())
    val count = 8
    val texts = arrayOf("THANKS","+500","+5","+10","+200","+50","+20","+100")
    val coinDrawable: Drawable? = context.getDrawable(R.drawable.ic_coin2)
    var rect:RectF = RectF()
    val textPath:Path = Path()
    init {
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL_AND_STROKE
        textPaint.isAntiAlias = true
        textPaint.color = 0xFFFAF2D7.toInt()
        textPaint.style = Paint.Style.FILL_AND_STROKE
        textPaint.textSize = context.dip2px(24).toFloat()
        textPaint.flags = TextPaint.FAKE_BOLD_TEXT_FLAG
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val radius = (width / 2 - 50).toFloat()
        val center = (width / 2).toFloat()
        rect.set(center - radius,center - radius,center + radius,center + radius)
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(width,MeasureSpec.EXACTLY))
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val mAngle = 360.0f / count
        var startAngle: Float = -mAngle / 2 - 90
        val radius = (width / 2 - 50).toFloat()
        val center = (width / 2).toFloat()
        for(i in 0 until count){
            paint.color = panColors[i % 2]
            canvas?.drawArc(rect,startAngle,mAngle,true,paint)

            val drawableRadisu = radius / 10
            val angle = Math.toRadians((startAngle + mAngle / 2).toDouble())
            val x = (center  + radius * 0.38 * Math.cos(angle)).toFloat()
            val y = (center  + radius * 0.38 * Math.sin(angle)).toFloat()
            coinDrawable?.setBounds((x - drawableRadisu).toInt(),(y - drawableRadisu).toInt(),(x + drawableRadisu).toInt(),(y + drawableRadisu).toInt())
            coinDrawable?.draw(canvas!!)

            textPath.reset()
            textPath.addArc(rect,startAngle,mAngle)
            val textWidth = textPaint.measureText(texts[i])
            val hOffset = Math.toRadians(mAngle.toDouble()) * radius / 2 - textWidth / 2
            canvas?.drawTextOnPath(texts[i],textPath,hOffset.toFloat(),radius / 4,textPaint)

            startAngle += mAngle
        }
    }

    private var currAngle = 0f
    private val mMinTimes = 3L
    private val mVarTime = 75
    private val rotateListener: RotateListener? = null
    //记录上次的位置
    private var lastPosition = 0
    fun startRotate(pos: Int) {
        val mAngle = 360.0f / count
        //最低圈数是mMinTimes圈
        val newAngle:Float =
            360 * mMinTimes + (pos - 1) * mAngle + currAngle - if (lastPosition == 0) 0f else (lastPosition - 1) * mAngle
        //计算目前的角度划过的扇形份数
        val num = ((newAngle - currAngle) / mAngle).toInt()
        val anim: ObjectAnimator =
            ObjectAnimator.ofFloat(this, "rotation", currAngle, newAngle)
        currAngle = newAngle.toFloat()
        lastPosition = pos
        // 动画的持续时间，执行多久？
        anim.duration = num * mVarTime.toLong()
        anim.addUpdateListener { animation -> //将动画的过程态回调给调用者
            if (rotateListener != null) rotateListener.rotating(animation)
        }
        val f = floatArrayOf(0f)
        anim.interpolator = TimeInterpolator { t ->
            val f1 =
                (Math.cos((t + 1) * Math.PI) / 2.0f).toFloat() + 0.5f
            Log.e("HHHHHHHh", "" + t + "     " + (f[0] - f1))
            f[0] =
                (Math.cos((t + 1) * Math.PI) / 2.0f).toFloat() + 0.5f
            f[0]
        }
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                //当旋转结束的时候回调给调用者当前所选择的内容
                if (rotateListener != null) {
//                    if (mType === 1) {
                        //去空格和前后空格后输出
                        val des: String = texts.get(
                            (count - pos + 1) %
                                    count
                        ).trim().replace(" ", "")
                        rotateListener.rotateEnd(pos, des)
//                    } else {
//                        rotateListener.rotateEnd(pos, "")
//                    }
                }
            }
        })
        // 正式开始启动执行动画
        anim.start()
    }

    interface RotateListener{
        /**
         * 动画结束 返回当前位置 注意 位置是最上面是1 然后依次逆时针递增
         *
         * @param position
         * @param des      所指分区文字描述
         */
        fun rotateEnd(position: Int, des: String?)

        /**
         * 动画进行中 返回动画中间量
         *
         * @param valueAnimator
         */
        fun rotating(valueAnimator: ValueAnimator?)

        /**
         * 点击了按钮 但是没有旋转 调用者可以在这里处理一些逻辑 比如弹出对话框确定用户是否要抽奖
         *
         * @param goImg
         */
        fun rotateBefore(goImg: ImageView?)
    }
}