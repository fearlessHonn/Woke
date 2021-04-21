package de.honn.alarm

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginStart
import kotlinx.android.synthetic.main.daily_weather_full.view.*
import kotlinx.android.synthetic.main.weather_full_screen.view.*
import kotlinx.android.synthetic.main.weekly_weather.view.*
import org.json.JSONObject
import java.util.*

class WeatherFullScreen {
    companion object {
        fun enter(w: WeatherWidget) {
            val metrics = w.context.resources.displayMetrics
            val lv = w.inflater.inflate(R.layout.weather_full_screen, null)

            val dialog = AlertDialog.Builder(w.context).setView(lv).create()

            dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.attributes.y = metrics.heightPixels - WidgetEditor.dialog.window!!.attributes.height

            readData(w, lv)

            dialog.show()
            dialog.window!!.setLayout(metrics.widthPixels, WidgetEditor.dialog.window!!.attributes.height)

            lv.closeButton.setOnClickListener { dialog.dismiss() }
            lv.trashButton.setOnClickListener {
                w.delete()
                dialog.dismiss()
            }
            lv.editButton.setOnClickListener { WidgetEditor.editWidget(w.parentLayout, w.context, w.inflater) }

        }

        @SuppressLint("SetTextI18n")
        private fun readData(w: WeatherWidget, v: View) {
            val days = listOf(
                v.weeklyWeather.oneDayWeather,
                v.weeklyWeather.twoDayWeather,
                v.weeklyWeather.threeDayWeather,
                v.weeklyWeather.fourDayWeather,
                v.weeklyWeather.fiveDayWeather,
                v.weeklyWeather.sixDayWeather,
                v.weeklyWeather.sevenDayWeather)

            val countries = JSONObject(w.context.assets.open("countries.json").bufferedReader().use { it.readText() })
            val calendar = Calendar.getInstance(TimeZone.getTimeZone(w.gsonForecast.timezone!!))

            v.location.text = w.location
            v.country.text = countries.getString(w.country).toUpperCase(Locale.ROOT)

            calendar.timeInMillis = w.gsonForecast.daily!![0].dt!! * 1000L
            v.todayTitle.text = "Today, ${monthMap[calendar.get(Calendar.MONTH)]} ${calendar.get(Calendar.DAY_OF_MONTH)}"

            v.todayTemperature.text = "${w.gsonForecast.current!!.temp!!.convert(w.unit)}${shortUnit(w.unit)}"
            v.todayDescription.text = w.gsonForecast.current!!.weather!![0].description
            v.todayIndicator.setImageResource(iconMap[w.gsonForecast.current!!.weather!![0].main!!]!!.second)

            v.windSpeed.text = "${w.gsonForecast.current!!.wind_speed} m/s"

            val rain = if (w.gsonForecast.daily!![0].rain != null) w.gsonForecast.daily!![0].rain!! else 0.0

            v.rainPercentage.text = "$rain mm"
            v.sunriseTime.text = w.gsonForecast.daily!![0].sunrise!!.unixToTime(w.gsonForecast.timezone!!)
            v.sunsetTime.text = w.gsonForecast.daily!![0].sunset!!.unixToTime(w.gsonForecast.timezone!!)

            for ((i, day) in days.withIndex()) {                                // TODO: Adjust margin so days are inline width title
                day.weekDay.text = w.gsonForecast.daily!![i].dt!!.unixToWeekDay(w.gsonForecast.timezone!!).take(2)
                day.highTemperature.text = "${w.gsonForecast.daily!![i].temp!!.max!!.convert(w.unit)}°"
                day.lowTemperature.text = "${w.gsonForecast.daily!![i].temp!!.min!!.convert(w.unit)}°"
                day.weatherIndicator.setImageResource(iconMap[w.gsonForecast.daily!![i].weather!![0].main!!]!!.second)
            }
        }
    }
}