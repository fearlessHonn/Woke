package de.honn.alarm

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.finnhub.api.apis.DefaultApi
import com.finnhub.api.infrastructure.ApiClient
import com.finnhub.api.models.CompanyProfile2
import com.finnhub.api.models.Quote
import com.finnhub.api.models.StockCandles
import kotlinx.android.synthetic.main.stock_widget_square.view.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import java.util.*
import kotlin.time.milliseconds

class StockWidgetSquare(
    val stock: String,
    val unit: String,
    val inflater: LayoutInflater,
    val context: Context,
    val parentLayout: ConstraintLayout,
    override val title: String,
    override val position: Int
) : Widget(inflater, parentLayout, GridHandler.SQUARE, title, "Stock", R.layout.stock_widget_square) {

    private val metrics = context.resources.displayMetrics
    private val key = "c2477niad3i8ss342oeg"

    private val positionX = GridHandler.numToPix(position, metrics).first
    private val positionY = GridHandler.numToPix(position, metrics).second

    private val client = DefaultApi()

    lateinit var coroutine: Deferred<Unit>
    lateinit var profile: CompanyProfile2
    lateinit var quote: Quote
    lateinit var candles: StockCandles

    private var created = false

    override fun createWidget() {
        Log.d("CreateWidget", "$stock @ x: $positionX & y: $positionY")
        this.created = true
        lv.stockText.text = stock

        lv.x = positionX
        lv.y = positionY

        lv.setOnLongClickListener {
            WidgetEditor.editWidget(parentLayout, context, inflater, position)
            false
        }
        parentLayout.addView(lv)
    }

    override suspend fun refresh() {
        Log.d("refreshStockSquare", "got refresh")
        val currentTime = System.currentTimeMillis() / 1000L
        val threeMonthsAgo = currentTime - (3 * 30 * 24 * 60 * 60)
        Log.d("refresh", "current time: $currentTime, three months ago: $threeMonthsAgo")

        ApiClient.apiKey["token"] = key

        coroutine = GlobalScope.async {
            profile = client.companyProfile2(stock, "US5949181045", "023135106")
            quote = client.quote(stock)
            candles = client.stockCandles(stock, "D", threeMonthsAgo, currentTime, null)
        }

        while (coroutine.isActive) delay(10)
        WidgetEditor.dialog.dismiss()
        if (!this.created) this.createWidget()
        readData()
    }

    private fun readData() {
        lv.fullCompanyName.text = profile.name!!
        lv.currentPrice.text = "€${this.quote.c!!}"

        val percentage = (this.quote.c!! / this.quote.pc!! - 1) * 100
        val increase = this.quote.c!! - this.quote.pc!!

        lv.stockChange.setTextColor(
            Color.parseColor(
                when {
                    percentage < 0f  -> {"#CC3219"}
                    percentage == 0f -> {"#F0F0F0"}
                    percentage > 0f  -> {"#5CC73B"}
                    else             -> {error("Invalid Percentage Given")}
                }
            )
        )

        lv.stockIndicator.rotation = when {
            percentage < 0f  -> {180f}
            percentage == 0f -> {90f}
            percentage > 0f  -> {0f}
            else             -> {error("Invalid percentage given")}
        }

        lv.stockIndicator.setColorFilter(Color.parseColor(
            when {
                percentage < 0f  -> {"#CC3219"}
                percentage == 0f -> {"#F0F0F0"}
                percentage > 0f  -> {"#5CC73B"}
                else             -> {error("Invalid Percentage Given")}
            }
        ))
        lv.stockChange.text = "€$increase ($percentage%)"

        drawGraph()
    }

    private fun drawGraph() {

        val minVal = candles.c!!.min()
        val maxVal = candles.c!!.max()
        val diff = maxVal!! - minVal!!
        val days = candles.c!!.size

        val imageView = lv.graph
        val width = imageView.layoutParams.width
        val height = imageView.layoutParams.height

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {color = Color.parseColor("#F0F0F0")}

        var lastX = 0f; var lastY = 0f
        for ((day, value) in candles.c!!.withIndex()) {
            val x = ((day.toFloat() / days.toFloat() * (width - 20)) + 10)
            val y = ((maxVal - value) / diff * (height - 20))

            if (day != 0)
                canvas.drawLine(lastX, lastY, x, y, paint)

            lastX = x; lastY = y
        }
        imageView.setImageBitmap(bitmap)
    }
}