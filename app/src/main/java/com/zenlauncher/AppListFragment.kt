package com.zenlauncher

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

    val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_main, container, false)

        recyclerView = view.findViewById(R.id.appRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadApps()
        buildLetterIndexMap()
        setupIndexBar(view)
        setupSearch(view)

        return view
    }

    private fun loadApps() {
        val context = requireContext()
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
    }

    private fun setupSearch(root: View) {
        val searchView = root.findViewById<SearchView>(R.id.appSearchView)
        searchView.isIconified = false
        searchView.queryHint = "Search apps"

        val id = androidx.appcompat.R.id.search_src_text
        val searchEditText = searchView.findViewById<EditText>(id)
        searchEditText.setHintTextColor(Color.GRAY)
        searchEditText.setTextColor(Color.WHITE)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                appList = if (newText.isNullOrBlank()) {
                    originalList
                } else {
                    originalList.filter {
                        it.label.contains(newText, ignoreCase = true)
                    }
                }
                adapter.updateList(appList)
                buildLetterIndexMap()
                return true
            }
        })
    }

    private fun buildLetterIndexMap() {
        letterPositionMap.clear()
        appList.forEachIndexed { index, app ->
            val firstChar = app.label.firstOrNull()?.uppercaseChar() ?: '#'
            letterPositionMap.putIfAbsent(firstChar, index)
        }
    }

    private fun animateLetter(view: TextView, targetSizeSp: Float, targetColor: Int) {
        val startSize = view.textSize / view.resources.displayMetrics.scaledDensity
        ValueAnimator.ofFloat(startSize, targetSizeSp).apply {
            duration = 80
            addUpdateListener { view.setTextSize(TypedValue.COMPLEX_UNIT_SP, it.animatedValue as Float) }
            start()
        }
        ObjectAnimator.ofArgb(view, "textColor", view.currentTextColor, targetColor).setDuration(80).start()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupIndexBar(root: View) {
        val indexBar = root.findViewById<LinearLayout>(R.id.indexBar)
        val overlay = root.findViewById<TextView>(R.id.indexLetterOverlay)
        indexBar.removeAllViews()
        indexBar.setPadding(0, 40.dp, 0, 0)

        val letterViews = mutableListOf<TextView>()
        for (letter in 'A'..'Z') {
            val tv = TextView(requireContext()).apply {
                text = letter.toString()
                textSize = 10f
                setTextColor(Color.LTGRAY)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    20.dp
                )
            }
            letterViews.add(tv)
            indexBar.addView(tv)
        }

        indexBar.setOnTouchListener { _, event ->
            val y = event.y.toInt()

            letterViews.forEachIndexed { index, tv ->
                if (y in tv.top..tv.bottom) {
                    val selectedLetter = tv.text.first()

                    overlay.text = selectedLetter.toString()
                    overlay.visibility = View.VISIBLE

                    letterPositionMap[selectedLetter]?.let { pos ->
                        (recyclerView.layoutManager as LinearLayoutManager)
                            .scrollToPositionWithOffset(pos, 0)
                    }

                    highlightAppsByLetter(selectedLetter)

                    letterViews.forEachIndexed { i, letterTv ->
                        animateLetter(letterTv, if (i == index) 12f else 9f, Color.LTGRAY)
                    }
                }
            }

            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                letterViews.forEach { animateLetter(it, 9f, Color.LTGRAY) }
                overlay.visibility = View.GONE
                resetAppHighlight()
            }
            true
        }

    }

    private fun highlightAppsByLetter(letter: Char) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val first = layoutManager.findFirstVisibleItemPosition()
        val last = layoutManager.findLastVisibleItemPosition()
        for (i in first..last) {
            val holder = recyclerView.findViewHolderForAdapterPosition(i) ?: continue
            val app = appList.getOrNull(i) ?: continue
            val firstChar = app.label.firstOrNull()?.uppercaseChar()
            holder.itemView.alpha = if (firstChar == letter) 1f else 0.3f
        }
    }

    private fun resetAppHighlight() {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val first = layoutManager.findFirstVisibleItemPosition()
        val last = layoutManager.findLastVisibleItemPosition()
        for (i in first..last) {
            val holder = recyclerView.findViewHolderForAdapterPosition(i) ?: continue
            holder.itemView.alpha = 1f
        }
    }
}
