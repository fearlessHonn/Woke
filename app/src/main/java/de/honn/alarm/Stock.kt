package de.honn.alarm

import com.finnhub.api.apis.DefaultApi
import com.finnhub.api.infrastructure.ApiClient
import com.finnhub.api.models.CompanyProfile2
import com.finnhub.api.models.Quote
import com.finnhub.api.models.StockCandles
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.math.round
import kotlin.math.roundToInt

class  Stock(private var symbol: String) {   // TODO: Check if symbol is correct/available

    private var apiClient: DefaultApi = DefaultApi()
    private lateinit var profile: CompanyProfile2
    private lateinit var quote: Quote
    lateinit var candles: StockCandles
    var coroutine: Deferred<Unit>

    init {
        ApiClient.apiKey["token"] = "c1hlf2v48v6q1s3o403g"
        coroutine =
        GlobalScope.async {
            profile = apiClient.companyProfile2(symbol, "US5949181045", "023135106")
            quote = apiClient.quote(symbol)
            candles = apiClient.stockCandles(symbol, "D", 1615417200, 1633212000, null)
        }
    }

    fun getProfile(): String {
        return this.profile.name!!
    }

    fun getPriceIncrease(): Float {
        return (this.quote.c!! - this.quote.pc!!).roundToNDigits(3)
    }

    fun getPercentage(): Float {
        return ((this.quote.c!!/this.quote.pc!! - 1) * 100).roundToNDigits(3)
    }

    fun getCurrentPrice(): Float {
        return this.quote.c!!.roundToNDigits(5)
    }

    private fun Float.roundToNDigits(digits: Int): Float {

        fun numberOfDigits(n: Int): Int =
            when (n) {
                in -9..9 -> 1
                else -> 1 + numberOfDigits(n / 10)
            }

        fun Float.roundToNDDecimals(n: Int): Float {
            var multiplier = 1.0
            repeat(n) { multiplier *= 10 }
            return (round(this * multiplier) / multiplier).toFloat()
        }

        return when {
            numberOfDigits(this.roundToInt()) > digits -> {
                error("Stock Price/Increase/Percentage is higher than the threshold value. This can't be displayed.")
            }

            numberOfDigits(this.roundToInt()) == digits -> {
                this.roundToInt().toFloat()
            }

            else -> {
                this.roundToNDDecimals(digits - numberOfDigits(this.roundToInt()))
            }
        }



    }



}