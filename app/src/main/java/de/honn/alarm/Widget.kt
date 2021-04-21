package de.honn.alarm

import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

open class Widget(
    posX: Float, posY: Float, inflater: LayoutInflater,
    private val parentLayout: ConstraintLayout
) {
    val lv: View = inflater.inflate(R.layout.weather_widget_wide, null)

    fun delete() {
        parentLayout.removeView(lv)
    }

    open suspend fun refresh() {}
    open fun createWidget() {}
}