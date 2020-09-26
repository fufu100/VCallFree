package vcall.free.international.phone.wifi.calling.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.utils.dip2px
import vcall.free.international.phone.wifi.calling.utils.getColorFromRes

/**
 * Created by lyf on 2020/9/11.
 */
class CircleProgressBar(context: Context, attributeSet: AttributeSet?, defStyle: Int):
    View(context,attributeSet,defStyle) {
    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    val paint = Paint()
    var progress:Float = 0f
    set(value) {
        field = value
        postInvalidate()
    }
    val strokeWidth:Float
    val rect:RectF = RectF()
    init {
        paint.isAntiAlias = true
        strokeWidth = context.dip2px(3).toFloat()
        paint.strokeWidth = strokeWidth
//        paint.color = context.getColorFromRes(R.color.colorAccent)
//        paint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
//        paint.style = Paint.Style.FILL
//        paint.color = 0x4D000000.toInt()
//        canvas.drawCircle(width.toFloat() / 2,height.toFloat() / 2,width.toFloat() / 2,paint)
        paint.style = Paint.Style.STROKE
        paint.color = context.getColorFromRes(R.color.colorAccent)
        rect.set(strokeWidth,strokeWidth,width.toFloat() - strokeWidth,height.toFloat() - strokeWidth)
        val sweepAngle = progress * 360.0f / 100
        canvas.drawArc(rect,-90f,sweepAngle,false,paint)
    }
}