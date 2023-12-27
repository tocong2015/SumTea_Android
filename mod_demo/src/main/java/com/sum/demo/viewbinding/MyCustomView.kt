package com.sum.demo.viewbinding

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.sum.framework.ext.dp

class MyCustomView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F22323")
        style = Paint.Style.STROKE
        // strokeWidth = 2f.dp().toFloat()
        textSize = 10f.dp().toFloat()
    }
    var drawText: String = ""
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(100f.dp(), 100f.dp())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (drawText.isNotEmpty()) {
            canvas?.drawText(drawText, 0f.dp().toFloat(), 20f.dp().toFloat(), paint)
        }


    }

    fun setText(context: String) {
        drawText = context
        invalidate()
    }


}