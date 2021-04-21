package de.honn.alarm

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import kotlinx.android.synthetic.main.new_widget_layout.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlin.math.roundToInt

class WidgetEditor {
    companion object {
        lateinit var dialog: AlertDialog
        lateinit var lv: View
        var widgets: HashMap<String, Widget> = HashMap()
        private val pass: Unit = Unit

        private var dataSpinner: ArrayList<Spinner> = arrayListOf()

        private fun createAdapter(list: Int, context: Context): ArrayAdapter<CharSequence> {
            return ArrayAdapter.createFromResource(context, list, R.layout.spinner_item).also {
                it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }

        private fun Int.toPx(metrics: DisplayMetrics): Int {
            return (this * metrics.density).roundToInt()
        }

        private fun setParams(dp: Int, metrics: DisplayMetrics) {
            lv.view.layoutParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, dp.toPx(metrics))
        }

        private fun setVisibleSpinners(n: Int, metrics: DisplayMetrics) {
            lv.dataGroup1.isVisible = false
            lv.dataGroup2.isVisible = false
            lv.dataGroup3.isVisible = false
            lv.dataGroup4.isVisible = false

            val buttonParams = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, 48.toPx(metrics))
            buttonParams.topMargin = 12.toPx(metrics)

            buttonParams.leftToLeft = dataSpinner[n - 1].id
            buttonParams.topToBottom = dataSpinner[n - 1].id
            lv.saveButton.layoutParams = buttonParams

            setParams(340 + n * 60, metrics)

            if (n > 0) lv.dataGroup1.isVisible = true
            if (n > 1) lv.dataGroup2.isVisible = true
            if (n > 2) lv.dataGroup3.isVisible = true
            if (n > 3) lv.dataGroup4.isVisible = true
        }

        fun editWidget(
            parent: ConstraintLayout,
            context: Context,
            layoutInflater: LayoutInflater,
            oldTitle: String = "",
            oldType: String = "Stock",
            oldLocation: String = ""
        ) {
            val layout = R.layout.new_widget_layout
            val metrics = context.resources.displayMetrics
            lv = layoutInflater.inflate(layout, null)
            var oldItem = "Stock"

            dialog = AlertDialog.Builder(context).setView(lv).create()

            dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window!!.attributes.y = metrics.heightPixels - dialog.window!!.attributes.height

            dataSpinner.add(lv.dataSpinner1)
            dataSpinner.add(lv.dataSpinner2)
            dataSpinner.add(lv.dataSpinner3)
            dataSpinner.add(lv.dataSpinner4)

            val typeAdapter = createAdapter(R.array.types, context)
            val currencyAdapter = createAdapter(R.array.currencies, context)
            val timeAdapter = createAdapter(R.array.times, context)
            val covidAdapter = createAdapter(R.array.covidData, context)
            val unitAdapter = createAdapter(R.array.tempUnits, context)

            lv.widgetTypeSpinner.adapter = typeAdapter
            lv.dataSpinner1.adapter = timeAdapter
            lv.dataSpinner2.adapter = currencyAdapter

            lv.editTextStockSymbol.isEnabled = false

            lv.editTextWidgetTitle.setText(oldTitle)
            lv.editTextStockSymbol.setText(oldLocation)
            lv.widgetTypeSpinner.setSelection(typeAdapter.getPosition(oldType))

            dialog.show()
            dialog.window!!.setLayout(metrics.widthPixels, dialog.window!!.attributes.height)

            lv.widgetTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                @SuppressLint("SetTextI18n")
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val spinnerItem = lv.widgetTypeSpinner.selectedItem
                    if (spinnerItem != oldItem) {
                        oldItem = spinnerItem.toString()
                        when (spinnerItem) {
                            "Stock" -> {
                                lv.stockSymbol.text = "STOCK SYMBOL"
                                lv.dataField1.text = "SHOW DATA FROM"
                                lv.dataField2.text = "CURRENCY"
                                lv.dataSpinner1.adapter = timeAdapter
                                lv.dataSpinner2.adapter = currencyAdapter
                                setVisibleSpinners(2, metrics)

                            }
                            "Covid-19 (Data)" -> {
                                lv.stockSymbol.text = "US-STATE"
                                lv.dataField1.text = "DATA FIELD 1"
                                lv.dataField2.text = "DATA FIELD 2"

                                lv.dataSpinner1.adapter = covidAdapter
                                lv.dataSpinner2.adapter = covidAdapter
                                lv.dataSpinner3.adapter = covidAdapter
                                lv.dataSpinner4.adapter = covidAdapter

                                setVisibleSpinners(4, metrics)
                            }
                            "Covid-19 (Graph)" -> {
                                lv.stockSymbol.text = "US-STATE"
                                lv.dataField1.text = "SHOW DATA FROM"
                                lv.dataField2.text = "DATA FIELD"

                                lv.dataSpinner1.adapter = timeAdapter
                                lv.dataSpinner2.adapter = covidAdapter
                                setVisibleSpinners(2, metrics)
                            }
                            "Weather" -> {
                                lv.stockSymbol.text = "LOCATION"
                                lv.dataField1.text = "TEMPERATURE UNIT"
                                lv.dataSpinner1.adapter = unitAdapter

                                setVisibleSpinners(1, metrics)
                            }
                            else -> {
                                Log.e("Invalid widget type", spinnerItem.toString())
                            }
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    pass
                }
            }

            lv.switch1.setOnCheckedChangeListener { _, isChecked ->
                lv.stockSymbol.isEnabled = isChecked
                if (!isChecked) {
                    lv.stockSymbol.text = lv.widgetTitle.text
                }
            }

            lv.editTextWidgetTitle.doOnTextChanged { text, _, _, _ ->
                val type = lv.widgetTypeSpinner.selectedItem
                if (!lv.switch1.isChecked) {
                    lv.editTextStockSymbol.setText(text)
                }
                if (widgets.keys.contains(text.toString() + type) && (text.toString() + type) != (oldTitle + oldType)) lv.editTextWidgetTitle.setTextColor(Color.parseColor("#FF0000"))
                else lv.editTextWidgetTitle.setTextColor(Color.parseColor("#FFFFFF"))
            }

            lv.saveButton.setOnClickListener {
                if (oldTitle != "") {
                    widgets[oldTitle + oldType]!!.delete()
                    widgets.remove(oldTitle + oldType)
                }

                val title = lv.editTextWidgetTitle.text.toString().trim().replace("\n", "")
                val location = lv.editTextStockSymbol.text.toString().trim().replace("\n", "")
                if (title != "") {
                    val type = lv.widgetTypeSpinner.selectedItem
                    if (type == "Weather") {
                        if (!widgets.keys.contains(title + type)) {
                            widgets[title + type] = WeatherWidget(
                                location, title, lv.dataSpinner1.selectedItem.toString(), layoutInflater, context, parent, 0f, 0f
                            )
                            GlobalScope.async(Dispatchers.Main) {
                                widgets[title + type]!!.refresh()
                                Log.d("createWidget", widgets.keys.toString())
                            }
                        }
                    }
                }
            }

            lv.closeButton.setOnClickListener {
                dialog.dismiss()
            }

            lv.trashButton.setOnClickListener {
                if (oldTitle != "") {
                    Log.d("trashButton.Listener", "deleted ${oldTitle + oldType}")
                    widgets[oldTitle + oldType]!!.delete()
                    widgets.remove(oldTitle + oldType)
                    dialog.dismiss()
                }
            }
        }
    }
}
