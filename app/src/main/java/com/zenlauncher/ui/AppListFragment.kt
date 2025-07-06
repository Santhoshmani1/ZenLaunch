package com.zenlauncher.ui

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zenlauncher.AppAdapter
import com.zenlauncher.AppInfo
import com.zenlauncher.R
import com.zenlauncher.helpers.AppUtils
import com.zenlauncher.helpers.Constants
import com.zenlauncher.helpers.setPaddingAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AppAdapter
    private lateinit var appList: List<AppInfo>
    private lateinit var originalList: List<AppInfo>
    private val letterPositionMap = mutableMapOf<Char, Int>()
    private val letterViews = mutableListOf<TextView>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_main, container, false)

        recyclerView = view.findViewById(R.id.appRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        setupSearch(view)
        setupIndexBar(view)
        loadAppsInBackground()

        return view
    }

    private val selectedApps = mutableListOf<AppInfo>()

    private fun loadAppsInBackground() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val context = requireContext()
            val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
            val pm = context.packageManager

            val launchableApps = pm.queryIntentActivities(intent, 0)
                .sortedBy { it.loadLabel(pm).toString().uppercase() }

            originalList = launchableApps.map {
                AppInfo(
                    it.loadLabel(pm).toString(),
                    it.activityInfo.packageName,
                    it.activityInfo.name
                )
            }
            appList = originalList

            selectedApps.clear()
            selectedApps.addAll(AppUtils.loadFavorites(context))

            withContext(Dispatchers.Main) {
                adapter = AppAdapter(context, appList, selectedApps) {
                    refreshFavorites()
                }
                recyclerView.adapter = adapter
                buildLetterIndexMap()
            }
        }
    }

    private fun refreshFavorites() {
        selectedApps.clear()
        selectedApps.addAll(AppUtils.loadFavorites(requireContext()))
    }

    private fun setupSearch(root: View) {
        val searchView = root.findViewById<SearchView>(R.id.appSearchView).apply {
            isIconified = false
            queryHint = Constants.SEARCH_APPS_HINT
            findViewById<EditText>(androidx.appcompat.R.id.search_src_text).apply {
                setHintTextColor(Color.GRAY)
                setTextColor(Color.WHITE)
            }

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?) = false
                override fun onQueryTextChange(newText: String?): Boolean {
                    appList = if (newText.isNullOrBlank()) originalList else originalList.filter {
                        it.label.contains(newText, ignoreCase = true)
                    }
                    adapter.updateList(appList)
                    buildLetterIndexMap()
                    return true
                }
            })
        }
    }

    private fun buildLetterIndexMap() {
        letterPositionMap.clear()
        appList.forEachIndexed { index, app ->
            app.label.firstOrNull()?.uppercaseChar()?.let { c ->
                letterPositionMap.putIfAbsent(c, index)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupIndexBar(root: View) {
        val parent = root.findViewById<LinearLayout>(R.id.indexBarContainer)
        val indexBar = root.findViewById<LinearLayout>(R.id.indexBar)
        val overlay = root.findViewById<TextView>(R.id.indexLetterOverlay)

        indexBar.removeAllViews()
        indexBar.setPaddingAll(Constants.Sizes.INDEXBAR_PADDING_HORIZONTAL_DP.dp)

        val settingsIcon = ImageView(requireContext()).apply {
            setImageResource(R.drawable.ic_settings)
            setColorFilter(Constants.Colors.ICON_COLOR)
            layoutParams = LinearLayout.LayoutParams(
                Constants.Sizes.SETTINGS_ICON_SIZE_DP.dp,
                Constants.Sizes.SETTINGS_ICON_SIZE_DP.dp
            ).apply { gravity = Gravity.CENTER_HORIZONTAL }
            background = ContextCompat.getDrawable(requireContext(), R.drawable.ripple_circle)
            setPaddingAll(Constants.Sizes.SETTINGS_ICON_PADDING_DP.dp)
            setOnClickListener { showSettingsOptions() }
        }
        parent.addView(settingsIcon, 0)

        for (letter in 'A'..'Z') {
            val tv = TextView(requireContext()).apply {
                text = letter.toString()
                textSize = Constants.Sizes.LETTER_TEXT_SIZE_SP
                setTextColor(Constants.Colors.LETTER_COLOR_DEFAULT)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    Constants.Sizes.LETTER_HEIGHT_DP.dp
                ).apply { gravity = Gravity.CENTER_HORIZONTAL }
            }
            letterViews.add(tv)
            indexBar.addView(tv)
        }


        indexBar.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                v.performClick()
            }
            val y = event.y.toInt()
            val touchedLetterView = letterViews.find { y in it.top..it.bottom }
            touchedLetterView?.let { tv ->
                val letter = tv.text.first()
                overlay.text = letter.toString()
                overlay.visibility = View.VISIBLE

                letterPositionMap[letter]?.let { startPos ->
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val endPosExclusive = (startPos until appList.size).indexOfFirst {
                        appList[it].label.firstOrNull()?.uppercaseChar() != letter
                    }.let { if (it == -1) appList.size else startPos + it }

                    val endPos = endPosExclusive - 1
                    val visibleCount = layoutManager.findLastVisibleItemPosition() -
                            layoutManager.findFirstVisibleItemPosition() + 1

                    val groupSize = endPos - startPos + 1
                    val desiredPosition = if (groupSize < visibleCount) {
                        (startPos + groupSize / 2).coerceAtLeast(0)
                    } else {
                        startPos
                    }

                    highlightVisibleAppsByLetter(letter)
                    layoutManager.scrollToPositionWithOffset(desiredPosition, 0)
                }
            }

            if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
                overlay.visibility = View.GONE
                resetAppHighlight()
            }
            true
        }

    }

    private fun highlightVisibleAppsByLetter(letter: Char) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        for (i in layoutManager.findFirstVisibleItemPosition()..layoutManager.findLastVisibleItemPosition()) {
            recyclerView.findViewHolderForAdapterPosition(i)?.itemView?.alpha =
                if (appList.getOrNull(i)?.label?.firstOrNull()
                        ?.uppercaseChar() == letter
                ) 1f else 0.3f
        }
    }

    private fun resetAppHighlight() {
        for (i in 0 until recyclerView.childCount) {
            recyclerView.getChildAt(i)?.animate()
                ?.alpha(1f)
                ?.setDuration(Constants.Animation.DURATION_MS)
                ?.start()
        }
    }

    private val uninstallReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_PACKAGE_REMOVED) {
                val data = intent.data ?: return
                val packageName = data.schemeSpecificPart ?: return

                appList = appList.filter { it.packageName != packageName }
                originalList = originalList.filter { it.packageName != packageName }

                val removedFromFavorites = selectedApps.removeAll { it.packageName == packageName }

                // Save updated favorites persistently
                if (removedFromFavorites) {
                    AppUtils.saveFavorites(requireContext(), selectedApps)
                }

                adapter.updateList(appList)

            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(Intent.ACTION_PACKAGE_REMOVED).apply {
            addDataScheme("package")
        }
        requireContext().registerReceiver(uninstallReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        requireContext().unregisterReceiver(uninstallReceiver)
    }


    private fun showSettingsOptions() {
        startActivity(Intent(requireContext(), SettingsActivity::class.java))
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}