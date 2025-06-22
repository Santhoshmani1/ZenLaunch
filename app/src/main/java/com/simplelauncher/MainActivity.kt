package com.simplelauncher

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowInsetsController
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppAdapter
    private lateinit var appList: List<AppInfo>
    private lateinit var originalList: List<AppInfo>
    private val letterPositionMap = mutableMapOf<Char, Int>()   // Maps letters (A-Z) to positions in the list

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Hide App Name, as nobody wants to see launcher name of home screen
        supportActionBar?.hide()
        window.statusBarColor = Color.BLACK

        // status bar icons stay light on dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }

        recyclerView = findViewById(R.id.appRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Intent to grab all launchable apps from the system
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        // Get and sort apps alphabetically (case-insensitive)
        val launchableApps = packageManager.queryIntentActivities(intent, 0)
            .sortedBy { it.loadLabel(packageManager).toString().uppercase() }

        originalList = launchableApps.map {
            AppInfo(
                it.loadLabel(packageManager).toString(),
                it.activityInfo.packageName,
                it.activityInfo.name
            )
        }

        appList = originalList
        adapter = AppAdapter(this, appList)
        recyclerView.adapter = adapter

        buildLetterIndexMap()
        setupIndexBar()

        val searchView = findViewById<SearchView>(R.id.appSearchView)
        searchView.isIconified = false
        searchView.clearFocus()
        searchView.queryHint = "Search apps"

        val id = androidx.appcompat.R.id.search_src_text
        val searchEditText = searchView.findViewById<android.widget.EditText>(id)
        searchEditText.setHintTextColor(Color.GRAY)
        searchEditText.setTextColor(Color.WHITE)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val filtered = originalList.filter {
                    it.label.contains(newText.orEmpty(), ignoreCase = true)
                }
                adapter.updateList(filtered)
                appList = filtered
                buildLetterIndexMap()
                setupIndexBar()
                return true
            }
        })
    }

    private fun buildLetterIndexMap() {
        letterPositionMap.clear()
        for ((i, app) in appList.withIndex()) {
            val firstChar = app.label.firstOrNull()?.uppercaseChar() ?: '#'
            if (!letterPositionMap.containsKey(firstChar)) {
                letterPositionMap[firstChar] = i
            }
        }
    }

    private fun animateLetter(view: TextView, targetSizeSp: Float, targetColor: Int) {
        val startSize = view.textSize / view.resources.displayMetrics.scaledDensity
        val anim = ValueAnimator.ofFloat(startSize, targetSizeSp).setDuration(100)
        anim.addUpdateListener {
            val value = it.animatedValue as Float
            view.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
        }
        anim.start()

        ObjectAnimator.ofArgb(view, "textColor", view.currentTextColor, targetColor)
            .setDuration(100)
            .start()
    }

    // Creates the A-Z index bar and handles touch-based scrolling
    @SuppressLint("ClickableViewAccessibility")
    private fun setupIndexBar() {
        val indexBar = findViewById<LinearLayout>(R.id.indexBar)
        indexBar.removeAllViews()

        val letterViews = mutableListOf<TextView>()

        for (letter in 'A'..'Z') {
            val tv = TextView(this).apply {
                text = letter.toString()
                textSize = 14f
                setTextColor(Color.LTGRAY)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0, 1f
                )
            }
            letterViews.add(tv)
            indexBar.addView(tv)
        }

        var lastSelectedIndex = -1

        indexBar.setOnTouchListener { v, event ->
            v.performClick()

            val indexBarHeight = v.height
            val y = event.y.coerceIn(0f, indexBarHeight.toFloat())
            val letterHeight = indexBarHeight / letterViews.size
            val touchedIndex = (y / letterHeight).toInt()

            if (touchedIndex in letterViews.indices && touchedIndex != lastSelectedIndex) {
                val letter = letterViews[touchedIndex].text.first()
                letterPositionMap[letter]?.let { pos ->
                    recyclerView.smoothScrollToPosition(pos) // ✅ smooth scroll
                }

                letterViews.forEachIndexed { i, tv ->
                    animateLetter(tv,
                        if (i == touchedIndex) 18f else 14f,
                        if (i == touchedIndex) "#CCCCCC".toColorInt() else Color.LTGRAY
                    )
                }

                lastSelectedIndex = touchedIndex
            }

            // Reset highlight after user lifts finger
            when (event.action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    letterViews.forEach {
                        animateLetter(it, 14f, Color.LTGRAY)
                    }
                    lastSelectedIndex = -1
                }
            }

            true
        }
    }
}
