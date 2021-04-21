package de.honn.alarm

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout

class CovidSquare(
    context: Context,
    private val layout: ConstraintLayout,
    private val inpX: Float,
    private val inpY: Float,
    private val inpWidth: Int,
    private val inpHeight: Int,
    var covid: Covid
) : FrameLayout(context) {

    fun create() {
        this.background = AppCompatResources.getDrawable(context, R.drawable.square)
        this.x = inpX
        this.y = inpY
        this.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        this.layoutParams.height = inpHeight
        this.layoutParams.width = inpWidth
        layout.addView(this)
    }

    fun delete() {
        layout.removeView(this)
    }

    fun makeTitle(
        x: Float,
        y: Float,
        size: Float,
        text: String,
        font: Typeface,
        align: Int,
        colour: String = "#FFFFFF"
    ) {
        val title = TextView(context)

        title.text = text
        title.x = x
        title.y = y

        title.setTextColor(Color.parseColor(colour))
        title.textSize = size
        title.gravity = align
        title.typeface = font

        this.addView(title)
    }
}