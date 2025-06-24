package com.simplelauncher

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AppListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppAdapter
    private lateinit var appList: List<AppInfo>
    private lateinit var originalList: List<AppInfo>
    private val letterPositionMap = mutableMapOf<Char, Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_main, container, false)
        val context = requireContext()

        recyclerView = view.findViewById(R.id.appRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val launchableApps = context.packageManager.queryIntentActivities(intent, 0)
            .sortedBy { it.loadLabel(context.packageManager).toString().uppercase() }

        originalList = launchableApps.map {
            AppInfo(
                it.loadLabel(context.packageManager).toString(),
                it.activityInfo.packageName,
                it.activityInfo.name
            )
        }

        appList = originalList
        adapter = AppAdapter(context, appList)
        recyclerView.adapter = adapter

        buildLetterIndexMap()
        setupIndexBar(view)

        val searchView = view.findViewById<SearchView>(R.id.appSearchView)
        searchView.isIconified = false
        searchView.queryHint = "Search apps"
        searchView.clearFocus()

        val id = androidx.appcompat.R.id.search_src_text
        val searchEditText = searchView.findViewById<EditText>(id)
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
                setupIndexBar(view)
                return true
            }
        })

        return view
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

    @SuppressLint("ClickableViewAccessibility")
    private fun setupIndexBar(root: View) {
        val indexBar = root.findViewById<LinearLayout>(R.id.indexBar)
        indexBar.removeAllViews()

        val letterViews = mutableListOf<TextView>()

        for (letter in 'A'..'Z') {
            val tv = TextView(requireContext()).apply {
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
                    recyclerView.smoothScrollToPosition(pos)
                }

                letterViews.forEachIndexed { i, tv ->
                    animateLetter(tv,
                        if (i == touchedIndex) 18f else 14f,
                        if (i == touchedIndex) "#CCCCCC".toColorInt() else Color.LTGRAY
                    )
                }

                lastSelectedIndex = touchedIndex
            }

            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                letterViews.forEach {
                    animateLetter(it, 14f, Color.LTGRAY)
                }
                lastSelectedIndex = -1
            }

            true
        }
    }
}
