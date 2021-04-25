package de.honn.alarm

import android.util.DisplayMetrics
import java.util.*

class GridHandler {
    companion object {
        const val SQUARE = 0
        const val HORIZONTAL = 1
        const val VERTICAL = 2

        const val DEL = 3
        const val ADD = 4

        var positions: MutableMap<Int, Widget> = mutableMapOf()

        fun pixToNum(x: Int, y: Int, page: Int, metrics: DisplayMetrics): Int {
            var output = 1
            output += if (x < metrics.widthPixels / 2) y / (metrics.heightPixels / 2) * 2
            else y / (metrics.heightPixels / 2)

            output += page * 4
            return output
        }

        fun numToPix(num: Int, metrics: DisplayMetrics): Pair<Float, Float> { // returns top left coordinates of square without any margins
            val page = num / 4
            val squareWidth = metrics.widthPixels / 2

            val x = (num % 2) * squareWidth
            val y = metrics.heightPixels - squareWidth * ((2 - (num - 4 * page) / 2))

            return Pair(x.toFloat(), y.toFloat())
        }

        private fun isClear(vararg checks: Int): Boolean {
            var fit = false
            for (p in checks)
                fit = p !in positions.keys

            return fit
        }

        private fun isFitting(format: Int, pos: Int): Boolean {
            when (format) {
                this.SQUARE -> {
                    return this.isClear(pos)
                }
                this.HORIZONTAL -> {
                    return if (pos % 2 == 0) this.isClear(pos, pos + 1)
                    else this.isClear(pos - 1, pos)

                }
                this.VERTICAL -> {
                    return this.isClear(pos, pos + 2)
                }
                else            -> {
                    error("$format is not a valid format")
                }
            }
        }

        fun getNextClear(format: Int): Int {
            var pos = 0
            while (!isFitting(format, pos)) pos += 1

            return pos
        }

        fun updateMap(format: Int, position: Int, action: Int, item: Widget? = null) {
            if (action == this.ADD && item != null) {
                when (format) {
                    this.SQUARE -> {this.positions[position] = item}
                    this.HORIZONTAL -> {this.positions[position] = item; this.positions[position + 1] = item} // TODO: possible bug when left part of wide widget is given. But why should?
                    this.VERTICAL -> {this.positions[position] = item; this.positions[position + 2] = item}
                    else -> {error("Invalid Format for widget: $format")}
                }
            }
            else if (action == this.DEL) {
                when (format) {
                    this.SQUARE -> {this.positions[position]!!.delete()}
                    this.HORIZONTAL -> {this.positions[position]!!.delete(); this.positions[position + 1]!!.delete()} // TODO: possible bug when left part of wide widget is given. But why should?
                    this.VERTICAL -> {this.positions[position]!!.delete(); this.positions[position + 2]!!.delete()}
                    else -> {error("Invalid Format for widget: $format")}
                }
            }
            else {error("Invalid Action: $action")}
        }
    }
}