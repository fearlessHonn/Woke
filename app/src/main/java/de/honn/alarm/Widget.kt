package de.honn.alarm

import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout

open class Widget(
    inflater: LayoutInflater,
    private val parentLayout: ConstraintLayout,
    val format: Int,
    val apiValue: String,
    val type: String,
    private val baseLayout: Int,
    open val position: Int = -1,
    open val title: String = ""
) {
    val lv: View = inflater.inflate(baseLayout, null)

    fun delete() {
        GridHandler.updateMap(this.format, this.position, GridHandler.DEL)
        parentLayout.removeView(lv)
    }
    open suspend fun refresh() {}
    open fun createWidget() {}
}