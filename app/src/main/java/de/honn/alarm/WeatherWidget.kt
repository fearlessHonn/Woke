package de.honn.alarm

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import kotlinx.android.synthetic.main.daily_weather.view.*
import kotlinx.android.synthetic.main.new_widget_layout.view.*
import kotlinx.android.synthetic.main.weather_widget_wide.view.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.lang.Exception
import java.util.*
import kotlin.math.round
import kotlin.math.roundToInt

val dayMap = mapOf(
    Pair(1, "Sunday"),
    Pair(2, "Monday"),
    Pair(3, "Tuesday"),
    Pair(4, "Wednesday"),
    Pair(5, "Thursday"),
    Pair(6, "Friday"),
    Pair(7, "Saturday")
)
val monthMap = mapOf(
    Pair(0, "January"),
    Pair(1, "February"),
    Pair(2, "March"),
    Pair(3, "April"),
    Pair(4, "May"),
    Pair(5, "June"),
    Pair(6, "July"),
    Pair(7, "August"),
    Pair(8, "September"),
    Pair(9, "October"),
    Pair(10, "November"),
    Pair(11, "December")
)

val iconMap = mutableMapOf(
    Pair("Clear", Pair(R.drawable.sunny_weather, R.drawable.sunny_weather_color_icon)),
    Pair("Clouds", Pair(R.drawable.partly_cloudy_icon, R.drawable.partly_cloudy_color_icon)),
    Pair("Rain", Pair(R.drawable.rain_icon, R.drawable.rain_color_icon)),
    Pair("Drizzle",Pair(R.drawable.rain_icon, R.drawable.rain_color_icon)),
    Pair("Thunderstorm", Pair(R.drawable.thunderstorm_icon, R.drawable.thunderstorm_color_icon)),
    Pair("Snow", Pair(R.drawable.snowy_icon, R.drawable.snowy_color_icon)),
    Pair("Atmosphere", Pair(R.drawable.mist_icon, R.drawable.cloudy_color_icon))
)

fun Double.convert(u: String): Int {
    return when (u) {
        "Celcius" -> round((this - 273.15)).toInt()
        "Fahrenheit" -> round((this * 9 / 5 - 459.67)).toInt()
        "Kelvin" -> round((this)).toInt()
        else         -> 0
    }
}

fun shortUnit(u: String): String {
    return when (u) {
        "Celcius" -> "°C"
        "Fahrenheit" -> "°F"
        "Kelvin" -> "K"
        else         -> ""
    }
}

fun Int.unixToTime(t: String): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone(t))
    calendar.timeInMillis = this * 1000L
    val minute = calendar.get(Calendar.MINUTE)

    val min = if (minute / 10 == 0) "0${minute}" else minute.toString()
    return "${calendar.get(Calendar.HOUR_OF_DAY)}:$min"
}

fun Int.unixToWeekDay(t: String): String {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone(t))
    calendar.timeInMillis = this * 1000L
    return dayMap[calendar.get(Calendar.DAY_OF_WEEK)]!!
}

class WeatherWidget(
    val location: String,
    private val title: String,
    val unit: String,
    val inflater: LayoutInflater,
    val context: Context,
    val parentLayout: ConstraintLayout,
    val posX: Float,
    val posY: Float,
    val position: Int
) : Widget(posX, posY, inflater, parentLayout, GridHandler.HORIZONTAL, position) {

    private lateinit var weatherData: String
    private lateinit var weatherDataForecast: String

    private val metrics = context.resources.displayMetrics

    private val key = "7ee50f4bcc5f9c4d55e9064cd9a97e9e"
    lateinit var coroutine: Job
    lateinit var country: String
    var created = false

    private val backgroundView = lv.background
    private val locationText = lv.locationText
    private val locationState = lv.locationState

    private val bigWeatherIcon = lv.bigWeatherIcon
    private val weatherDescription = lv.weatherDescription
    private val bigTemperature = lv.bigTemperature

    private val todayWeather = lv.todayWeather
    private val tomorrowWeather = lv.tomorrowWeather
    private val dATomorrowWeather = lv.dATomorrowWeather

    private val todayCalendarDay = todayWeather.calendarDay
    private val todayWeekDay = todayWeather.weekDay
    private val todayIndicator = todayWeather.weatherIndicator
    private val todayHighTemperature = todayWeather.upTemperature
    private val todayDownTemperature = todayWeather.downTemperature

    private val tomorrowCalendarDay = tomorrowWeather.calendarDay
    private val tomorrowWeekDay = tomorrowWeather.weekDay
    private val tomorrowIndicator = tomorrowWeather.weatherIndicator
    private val tomorrowHighTemperature = tomorrowWeather.upTemperature
    private val tomorrowDownTemperature = tomorrowWeather.downTemperature

    private val dATomorrowCalendarDay = dATomorrowWeather.calendarDay
    private val dATomorrowWeekDay = dATomorrowWeather.weekDay
    private val dATomorrowIndicator = dATomorrowWeather.weatherIndicator
    private val dATomorrowHighTemperature = dATomorrowWeather.upTemperature
    private val dATomorrowDownTemperature = dATomorrowWeather.downTemperature

    private val windIndicator1 = lv.wind1
    private val windIndicator2 = lv.wind2
    private val windIndicator3 = lv.wind3

    private val rainIndicator1 = lv.drop1
    private val rainIndicator2 = lv.drop2
    private val rainIndicator3 = lv.drop3

    var gsonForecast = ForecastData()

    data class WeatherData(
        var main: String? = null,
        var description: String? = null
    )

    data class CurrentData(
        var temp: Double? = null,
        var wind_speed: Double? = null,
        var wind_deg: Int? = null,
        var weather: List<WeatherData>? = null
    )

    data class TempData(
        var min: Double? = null,
        var max: Double? = null
    )

    data class DailyData(
        var dt: Int? = null,
        var sunrise: Int? = null,
        var sunset: Int? = null,
        var temp: TempData? = null,
        var weather: List<WeatherData>? = null,
        var rain: Double? = null
    )

    data class ForecastData(
        var timezone: String? = null,
        var current: CurrentData? = null,
        var daily: List<DailyData>? = null
    )

    private fun Int.toPx(): Float {
        return (this * metrics.density).roundToInt().toFloat()
    }

    private fun apiErrorHandler(i: Int, e: Exception) {
        WidgetEditor.widgets.remove(title + "Weather")
        parentLayout.removeView(lv)
        WidgetEditor.lv.editTextWidgetTitle.setTextColor(Color.parseColor("#FF0000"))
        Log.e("Exception: ${i}. API call", e.toString())
    }

    override fun createWidget() {
        created = true
        todayWeekDay.text = "Today"
        tomorrowWeekDay.text = "Tomorrow"
        locationText.text = location
        lv.y = posY
        lv.x = posX

        lv.setOnLongClickListener {
            WidgetEditor.editWidget(
                parentLayout, context, inflater,  title, "Weather", location)
            false
        }

        lv.setOnClickListener {
            WeatherFullScreen.enter(this)
        }

        parentLayout.addView(lv)
    }

    override suspend fun refresh() {
        coroutine = GlobalScope.launch(Dispatchers.Default) {
            val url = "https://api.openweathermap.org/data/2.5/weather?q=${location}&appid=$key"
            val queue = Volley.newRequestQueue(context)

            val req = StringRequest(Request.Method.GET, url, { response ->
                weatherData = response.toString()

                val jsonWeatherData = JSONObject(weatherData)
                val lon = jsonWeatherData.getJSONObject("coord").getDouble("lon")
                val lat = jsonWeatherData.getJSONObject("coord").getDouble("lat")
                country = jsonWeatherData.getJSONObject("sys").getString("country")

                val scndUrl = "https://api.openweathermap.org/data/2.5/onecall?lat=$lat&lon=$lon&exclude=hourly,minutely&appid=$key"
                val scndQueue = Volley.newRequestQueue(context)

                val scndReq = StringRequest(Request.Method.GET, scndUrl, { scndResponse ->
                    weatherDataForecast = scndResponse.toString()
                }, {
                    apiErrorHandler(2, it)
                })

                scndQueue.add(scndReq)
            }, {
                apiErrorHandler(1, it)
            })

            queue.add(req)
        }
        coroutine.join()
        Log.d("finishedCoroutine", location)
        while (!this::weatherDataForecast.isInitialized) delay(10)
        Log.d("ForecastJSONAPI", weatherDataForecast)
        WidgetEditor.dialog.dismiss()
        if (!created)
            this.createWidget()
        readData()
    }

    @SuppressLint("SetTextI18n")
    private fun readData() {
        gsonForecast = Gson().fromJson(weatherDataForecast, ForecastData::class.java)
        Log.d("readData()", gsonForecast.toString())

        val countries = JSONObject(context.assets.open("countries.json").bufferedReader().use { it.readText() })

        locationState.text = countries.getString(country).toUpperCase(Locale.ROOT)

        weatherDescription.text = gsonForecast.current!!.weather!![0].description!!
        bigTemperature.text = gsonForecast.current!!.temp!!.convert(unit).toString() + shortUnit(unit)
        bigWeatherIcon.setImageResource(iconMap[gsonForecast.current!!.weather!![0].main]!!.first)

        todayIndicator.setImageResource(iconMap[gsonForecast.daily!![0].weather!![0].main]!!.first)
        todayHighTemperature.text = gsonForecast.daily!![0].temp!!.max!!.convert(unit).toString() + "°"
        todayDownTemperature.text = gsonForecast.daily!![0].temp!!.min!!.convert(unit).toString() + "°"

        tomorrowIndicator.setImageResource(iconMap[gsonForecast.daily!![1].weather!![0].main]!!.first)
        tomorrowHighTemperature.text = gsonForecast.daily!![1].temp!!.max!!.convert(unit).toString() + "°"
        tomorrowDownTemperature.text = gsonForecast.daily!![1].temp!!.min!!.convert(unit).toString() + "°"

        dATomorrowIndicator.setImageResource(iconMap[gsonForecast.daily!![2].weather!![0].main]!!.first)
        dATomorrowHighTemperature.text = gsonForecast.daily!![2].temp!!.max!!.convert(unit).toString() + "°"
        dATomorrowDownTemperature.text = gsonForecast.daily!![2].temp!!.min!!.convert(unit).toString() + "°"

        windIndicator1.setColorFilter(Color.parseColor("#797B7D"))
        windIndicator2.setColorFilter(Color.parseColor("#797B7D"))
        windIndicator3.setColorFilter(Color.parseColor("#797B7D"))
        val windSpeed = gsonForecast.current!!.wind_speed

        if (windSpeed != null) {
            when {
                windSpeed < 5.5 -> {
                    windIndicator1.setColorFilter(Color.parseColor("#F0F0F0"))
                }
                windSpeed < 8   -> {
                    windIndicator1.setColorFilter(Color.parseColor("#F0F0F0"))
                    windIndicator2.setColorFilter(Color.parseColor("#F0F0F0"))
                }
                else            -> {
                    windIndicator1.setColorFilter(Color.parseColor("#F0F0F0"))
                    windIndicator2.setColorFilter(Color.parseColor("#F0F0F0"))
                    windIndicator3.setColorFilter(Color.parseColor("#F0F0F0"))
                }
            }
        } else {
            Log.e("Error in getting wind", windSpeed.toString())
        }

        Log.d("Wind speed", windSpeed.toString())

        val rain = gsonForecast.daily!![0].rain
        rainIndicator1.setColorFilter(Color.parseColor("#797B7D"))
        rainIndicator2.setColorFilter(Color.parseColor("#797B7D"))
        rainIndicator3.setColorFilter(Color.parseColor("#797B7D"))

        if (rain != null) {
            when {
                rain == 0.0 -> {}
                rain < 4.5 -> {
                    rainIndicator1.setColorFilter(Color.parseColor("#F0F0F0"))
                }
                rain < 7.5 -> {
                    rainIndicator1.setColorFilter(Color.parseColor("#F0F0F0"))
                    rainIndicator2.setColorFilter(Color.parseColor("#F0F0F0"))
                }
                else       -> {
                    rainIndicator1.setColorFilter(Color.parseColor("#F0F0F0"))
                    rainIndicator2.setColorFilter(Color.parseColor("#F0F0F0"))
                    rainIndicator3.setColorFilter(Color.parseColor("#F0F0F0"))
                }
            }
        } else {
            Log.e("Error in getting rain", rain.toString())
        }
        Log.d("Rainfall", rain.toString())

        val calendar = Calendar.getInstance(TimeZone.getTimeZone(gsonForecast.timezone!!))

        calendar.timeInMillis = gsonForecast.daily!![2].dt!! * 1000L
        dATomorrowWeekDay.text = dayMap[calendar.get(Calendar.DAY_OF_WEEK)]
        dATomorrowCalendarDay.text = "${monthMap[calendar.get(Calendar.MONTH)]} ${calendar.get(Calendar.DAY_OF_MONTH)}"

        calendar.timeInMillis = gsonForecast.daily!![1].dt!! * 1000L
        tomorrowCalendarDay.text = "${monthMap[calendar.get(Calendar.MONTH)]} ${calendar.get(Calendar.DAY_OF_MONTH)}"

        calendar.timeInMillis = gsonForecast.daily!![0].dt!! * 1000L
        todayCalendarDay.text = "${monthMap[calendar.get(Calendar.MONTH)]} ${calendar.get(Calendar.DAY_OF_MONTH)}"
    }
}