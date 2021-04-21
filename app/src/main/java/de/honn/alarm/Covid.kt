package de.honn.alarm

import com.finnhub.api.apis.DefaultApi
import com.finnhub.api.infrastructure.ApiClient
import com.finnhub.api.models.CovidInfo
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.math.round
import kotlin.math.roundToInt

class Covid(private var widgetState: String) { // TODO: add other countries than the US

    private var client: DefaultApi = DefaultApi()
    var coroutine: Deferred<Unit>
    private var index: Int
    private lateinit var states: List<CovidInfo>

    init {
        ApiClient.apiKey["token"] = "c1hlf2v48v6q1s3o403g"
        this.index = 0
        coroutine =
        GlobalScope.async {
            states = client.covid19()
            getIndex()
        }
    }

    private fun getIndex() {
        var i = 0
        while (i < states.size) {
            if (states[i].state == widgetState) {
                this.index = i
                break
            }
            i++
        }
    }

    fun getTotalCases(): Int {
        return this.states[this.index].case!!.roundToInt()
    }

    fun getDeaths(): Int {
        return this.states[this.index].death!!.roundToInt()
    }

}