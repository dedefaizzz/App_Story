package com.dicoding.appstory.customview

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.dicoding.appstory.R

class MyButton : AppCompatButton {

    private var textColor: Int = 0

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initialize()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        setTextColor(textColor)
        textSize = 12f
        gravity = Gravity.CENTER
        text = if (isEnabled) {
            context.getString(R.string.submit)
        } else {
            context.getString(R.string.not_valid_data)
        }
    }

    private fun initialize() {
        textColor = ContextCompat.getColor(context, android.R.color.background_light)
    }
}
