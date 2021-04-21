package de.honn.alarm

import android.content.Context
import android.graphics.*
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout

class Square(
    context: Context,
    private val layout: ConstraintLayout,
    private val inpX: Float,
    private val inpY: Float,
    private val inpWidth: Int,
    private val inpHeight: Int,

    var isStockSquare: Boolean = false,
    var customTitle: Boolean = false,
    var stock: Stock = Stock(""),
    var priceIncrease: Float = 0f,
    var percentage: Float = 0f,
    var currentPrice: Float = 0f,
    var companyName: String = "",
    var closes: List<Float> = listOf(),
    var currency: String = "dollar",
    var timePeriod: Int = 30,

    var isCovidSquare: Boolean = false,
    var covid: Covid = Covid(""),
    var totalCases: Int = 0,
    var deaths: Int = 0

) : FrameLayout(context) {

    fun create() {
        this.background = AppCompatResources.getDrawable(context, R.drawable.square)
        this.x = inpX
        this.y = inpY
        this.layoutParams = ViewGroup.LayoutParams(inpWidth, inpHeight)
        this.elevation = 2f
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
        align: Int = Gravity.START,
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

    fun drawGraph(data: List<Float>) {
        val smallestVal = data.min()
        val highestVal = data.max()
        val interval = highestVal!! - smallestVal!!
        val days = data.size

        val width = 410
        val height = 190

        val imageView = ImageView(context)
        imageView.x = 50f
        imageView.y = 170f
        imageView.layoutParams = ViewGroup.LayoutParams(width, height)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)
        val graphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFFFFF")
        }

        var lastX = 0f; var lastY = 0f

        for ((day, value) in data.withIndex()) {
            val x = ((day.toFloat()/days.toFloat() * (width - 20)) + 10)
            val y = ((highestVal - value) / interval * (height - 20))

            if (day != 0)
                c.drawLine(lastX, lastY, x, y, graphPaint)

            lastX = x; lastY = y
        }

        imageView.setImageBitmap(bitmap)
        this.addView(imageView)
    }
}
