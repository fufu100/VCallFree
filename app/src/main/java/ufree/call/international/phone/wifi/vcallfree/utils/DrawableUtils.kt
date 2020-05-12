package ufree.call.international.phone.wifi.vcallfree.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable

class DrawableUtils {
    var radius: FloatArray = floatArrayOf(0f,0f,0f,0f,0f,0f,0f,0f)
    var strokeColor: Int = 0
    var solidColor: Int = 0
    var strokeWidth: Int = 1

    var solidCheckedColor: Int = 0
    var strokeCheckColor: Int = 0
    var solidPressColor: Int = 0
    var strokePressColor: Int = 0

    var state: Int = 0//0普通；1radiobutton;2点击
    companion object{
        fun generate(body:DrawableUtils.() -> Drawable):Drawable{
            return with(DrawableUtils()){
                body()
            }
        }
    }

    fun build(): Drawable {
        val drawable = GradientDrawable()
        drawable.cornerRadii = radius
        drawable.setStroke(strokeWidth, strokeColor)
        drawable.setColor(solidColor)
        when (state) {
            0 -> return drawable
            1 -> {
                val checkedDrawable = GradientDrawable()
                checkedDrawable.cornerRadii = radius
                checkedDrawable.setColor(solidCheckedColor)
                checkedDrawable.setStroke(strokeWidth, strokeCheckColor)
                val stateListDrawable = StateListDrawable()
                stateListDrawable.addState(
                    intArrayOf(android.R.attr.state_checked),
                    checkedDrawable
                )
                stateListDrawable.addState(
                    intArrayOf(android.R.attr.state_selected),
                    checkedDrawable
                )
                stateListDrawable.addState(intArrayOf(), drawable)
                return stateListDrawable
            }
            2 -> {
                val pressDrawable = GradientDrawable()
                pressDrawable.cornerRadii = radius
                pressDrawable.setColor(solidPressColor)
                pressDrawable.setStroke(strokeWidth, strokePressColor)
                val stateListDrawable2 = StateListDrawable()
                stateListDrawable2.addState(intArrayOf(android.R.attr.state_pressed), pressDrawable)
                stateListDrawable2.addState(intArrayOf(), drawable)
                return stateListDrawable2
            }
            else -> return drawable
        }
    }

    fun radius(radius: Int): DrawableUtils {
        for (i in this.radius.indices) {
            this.radius[i] = radius.toFloat()
        }
        return this
    }

    fun topLeftRadius(radisu: Float): DrawableUtils {
        this.radius[0] = radisu
        this.radius[1] = radisu
        return this
    }

    fun topRightRadius(radius: Float): DrawableUtils {
        this.radius[2] = radius
        this.radius[3] = radius
        return this
    }

    fun bottomRightRadius(radius: Float): DrawableUtils {
        this.radius[4] = radius
        this.radius[5] = radius
        return this
    }

    fun bottomLeftRadius(radius: Float): DrawableUtils {
        this.radius[6] = radius
        this.radius[7] = radius
        return this
    }

    fun strokeColor(color: Int): DrawableUtils {
        this.strokeColor = color
        return this
    }

    fun solidColor(color: Int): DrawableUtils {
        this.solidColor = color
        return this
    }

    fun strokeWidth(width: Int): DrawableUtils {
        this.strokeWidth = width
        return this
    }

    fun solidCheckedColor(color: Int): DrawableUtils {
        this.solidCheckedColor = color
        return this
    }

    fun strokeCheckedColor(color: Int): DrawableUtils {
        this.strokeCheckColor = color
        return this
    }

    fun solidPressColor(color: Int): DrawableUtils {
        this.solidPressColor = color
        return this
    }

    fun strokePressColor(color: Int): DrawableUtils {
        this.strokePressColor = color
        return this
    }

    fun state(state: Int): DrawableUtils {
        this.state = state
        return this
    }
}