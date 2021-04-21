package de.honn.alarm

import android.content.Context
import android.util.Log
import android.widget.ImageView
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import java.util.*
import kotlin.concurrent.schedule

class TimeSpinnerHandler (parent: ConstraintLayout, private val context: Context, private val base: AppCompatActivity) {

    val images = listOf(
        R.drawable.night, //--> 23 bis 5 Uhr
        R.drawable.dawn, // --> 5 bis 7 Uhr
        R.drawable.sunrise, // --> 7 bis 8:30 Uhr
        R.drawable.early_morning, // --> 8:30 Uhr bis 10 Uhr
        R.drawable.day, // --> 10 Uhr bis 16 Uhr
        R.drawable.golden_hour, // --> 16 bis 18 Uhr
        R.drawable.dusk // --> 18 bis 23 Uhr
    )

    val background = parent.findViewById<ImageView>(R.id.background)
    val background2 = parent.findViewById<ImageView>(R.id.background2)

    private val picker = parent.findViewById<TimePicker>(R.id.TimePicker)
    init {
        picker.setIs24HourView(true)
        picker.setOnTimeChangedListener{ _, hour, minute ->
            changeBG(hour, minute)
        }
    }

    private fun changeBG (hour: Int, minute: Int) {
        Log.d("changeBG", "$hour h $minute min")
        when {
            hour < 5 -> {switchImage(images[0])}
            hour < 7 -> {switchImage(images[1])}
            hour < 8 -> {switchImage(images[2])}
            hour < 10 -> {switchImage(images[3])}
            hour < 16 -> {switchImage(images[4])}
            hour < 18 -> {switchImage(images[5])}
            hour < 23 -> {switchImage(images[6])}
            else -> {switchImage(images[0])}
        }
    }

    private fun fade(v1: ImageView, v2: ImageView) {
        v2.animate().alpha(1f).duration = 500
        Timer("timer", false).schedule(500) {
            base.runOnUiThread{
            v1.animate().alpha(0f).duration = 500
            }
        }
    }

    private fun switchImage (image: Int) {
        if (background.alpha == 1f) {
            background2.setImageResource(image)
            fade(background, background2)
        } else {
            background.setImageResource(image)
            fade(background2, background)
        }
    }
}