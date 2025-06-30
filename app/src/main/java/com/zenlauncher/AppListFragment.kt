package com.zenlauncher

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
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

    private fun showSettingsOptions() {
        val options = arrayOf("Deactivate Device Admin", "Share ZenLauncher", "Exit ZenLauncher")

        AlertDialog.Builder(requireContext())
            .setTitle("Settings")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> deactivateDeviceAdmin()
                    1 -> shareApp()
                    2 -> exitZenLauncher()
                }
            }
            .show()
    }

    private fun deactivateDeviceAdmin() {
        val dpm = requireContext().getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
        val compName = ComponentName(requireContext(), com.zenlauncher.listener.DeviceAdmin::class.java)
        if (dpm.isAdminActive(compName)) {
            dpm.removeActiveAdmin(compName)
        } else {
            toast("Device Admin not active")
        }
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "ZenLauncher")
            putExtra(Intent.EXTRA_TEXT, "Try ZenLauncher for a minimal, fast Android experience.")
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    private fun exitZenLauncher() {
        activity?.finishAffinity()
    }

    private fun toast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupIndexBar(root: View) {
        val parent = root.findViewById<LinearLayout>(R.id.indexBarContainer)
        val indexBar = root.findViewById<LinearLayout>(R.id.indexBar)
        val overlay = root.findViewById<TextView>(R.id.indexLetterOverlay)
        indexBar.removeAllViews()
        indexBar.setPadding(4.dp, 40.dp, 4.dp, 8.dp)

        // Material settings icon
        val settingsIcon = ImageView(requireContext()).apply {
            setImageResource(R.drawable.ic_settings)
            setColorFilter(Color.LTGRAY)
            layoutParams = LinearLayout.LayoutParams(32.dp, 32.dp).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                setMargins(0, 0, 0, 8.dp)
            }
            background = ContextCompat.getDrawable(requireContext(), R.drawable.ripple_circle)
            isClickable = true
            isFocusable = true
            setPadding(6.dp)

            setOnClickListener {
                animate().scaleX(1.1f).scaleY(1.1f).setDuration(80).withEndAction {
                    animate().scaleX(1f).scaleY(1f).setDuration(80).start()
                    showSettingsOptions()
                }.start()
            }
        }
        parent.addView(settingsIcon, 0)

        val letterViews = mutableListOf<TextView>()
        for (letter in 'A'..'Z') {
            val tv = TextView(requireContext()).apply {
                text = letter.toString()
                textSize = 10f
                setTextColor(Color.LTGRAY)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    20.dp
                ).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                }
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
