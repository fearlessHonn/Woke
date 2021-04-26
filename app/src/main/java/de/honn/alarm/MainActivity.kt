package de.honn.alarm

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.util.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val parent = findViewById<ConstraintLayout>(R.id.myLayout)
        val handler = TimeSpinnerHandler(parent, this, this@MainActivity)
        val newWidgetButton = findViewById<ImageButton>(R.id.NewWidgetButton)
        val nextPageButton = findViewById<ImageButton>(R.id.NextPageButton)
        val previousPageButton = findViewById<ImageButton>(R.id.PreviousPageButton)

        newWidgetButton.setOnClickListener {
            WidgetEditor.editWidget(parent, this, layoutInflater)
        }

        nextPageButton.setOnClickListener {
            GridHandler.changePage(GridHandler.currentPage + 1)
        }

        previousPageButton.setOnClickListener {
            GridHandler.changePage(GridHandler.currentPage - 1)
        }
    }

    private fun refresh() {
        GlobalScope.async {
            for (key in WidgetEditor.widgets.keys) {
                Log.d("MainActivity/refresh", "Refreshing $key")
                WidgetEditor.widgets[key]!!.refresh()
            }
        }
    }

    val timer = Timer().scheduleAtFixedRate(object: TimerTask(){
        override fun run(){
            Log.d("MainActivity/Timer", "Refreshing")
            refresh()
        }
    }, 0, 1000 * 60 * 5)
}