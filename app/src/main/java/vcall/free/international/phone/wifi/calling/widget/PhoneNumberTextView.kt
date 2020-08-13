package vcall.free.international.phone.wifi.calling.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import androidx.appcompat.widget.AppCompatTextView
import vcall.free.international.phone.wifi.calling.R
import vcall.free.international.phone.wifi.calling.utils.getColorFromRes
import kotlin.math.abs

/**
 * Created by lyf on 2020/5/13.
 */
class PhoneNumberTextView(context: Context,attributeSet: AttributeSet?,defStyle:Int):
    AppCompatTextView(context,attributeSet,defStyle),Runnable {
    constructor(context: Context,attributeSet: AttributeSet?):this(context,attributeSet,0)
    constructor(context: Context):this(context,null,0)

    private val cursor:Drawable = ColorDrawable(context.getColorFromRes(R.color.colorAccent))
    var cursorEnd = true
    var cursorShow = true
    var offset = 0
    var selection = 0
    init {
        addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable) {
                println("afterTextChange ${s.length} $offset $selection")
                if(selection == s.length - 1){
                    val scrollView = parent as HorizontalScrollView
                    scrollView.scrollTo(width,0)
//                    scrollView.fullScroll(ScrollView.FOCUS_RIGHT)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
        maxLines = 1
        post(this)
    }
    fun delete(){
        println("delete selection=$selection")
        if(selection > 0) {
            val t = StringBuilder(text.toString())
            selection--
            offset = paint.measureText(text,0,selection).toInt()
            text = t.deleteCharAt(selection)

        }
    }
    fun insert(char: Any){
        val t = StringBuilder(text.toString())
        text = t.insert(selection,char).toString()
        selection++
        offset = paint.measureText(text.toString(),0,selection).toInt()

    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if(cursorShow) {
            if (cursorEnd) {
                offset = paint.measureText(text.toString()).toInt()
                selection = length()
            }
            cursor.setBounds(
                offset,
                height / 2 - paint.textSize.toInt() / 2,
                offset + 3,
                height / 2 + paint.textSize.toInt() / 2
            )
            cursor.draw(canvas)

        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action == MotionEvent.ACTION_DOWN){
            val x = event.x
            cursorEnd = false
            calculateOffset(x)
            invalidate()
        }
        return super.onTouchEvent(event)
    }

    private fun calculateOffset(x:Float){
        if(x > paint.measureText(text.toString())){
            cursorEnd = true
            selection = text.length
        }else {
            var last = 0f
            for (i in text.indices) {
                val l = paint.measureText(text, 0, i)
                if(x < l && x > last){
                    if(abs(x - last) > abs(x - l)){
                        offset = l.toInt()
                        selection = i
                    }else{
                        offset = last.toInt()
                        selection = i - 1
                    }
                    break
                }
                last = l
            }
        }
    }

    override fun run() {
        cursorShow = !cursorShow
        invalidate()
        postDelayed(this,500)
    }
}