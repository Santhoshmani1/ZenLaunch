package com.simplelauncher

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private val selectedApps = mutableListOf<AppInfo>()
    private lateinit var appsLayout: LinearLayout
    private val prefsKey = "favorites"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()

        // Clock
        val clock = TextClock(context).apply {
            format24Hour = "HH:mm"
            textSize = 36f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        // Date
        val dateText = TextView(context).apply {
            val dateFormat = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
            text = dateFormat.format(Date())
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        // "Favourites" title
        val favTitle = TextView(context).apply {
            text = "Favourites"
            setTextColor(Color.LTGRAY)
            textSize = 18f
            setPadding(48, 90, 48, 16)
        }

        // Favorite apps container
        appsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 0, 48, 0)
        }

        val verticalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            setPadding(0, 100, 0, 0)
            addView(clock)
            addView(Space(context).apply { minimumHeight = 12 })
            addView(dateText)
            addView(Space(context).apply { minimumHeight = 120 })
            addView(favTitle)
            addView(appsLayout)
        }

        val rootLayout = FrameLayout(context).apply {
            setBackgroundColor(Color.BLACK)
            addView(verticalLayout)
        }

        clock.setOnClickListener {
            val intent = Intent(Settings.ACTION_DATE_SETTINGS)
            if (intent.resolveActivity(context.packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(context, "Clock settings not available", Toast.LENGTH_SHORT).show()
            }
        }

        return rootLayout
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
        showSelectedApps()
    }

    private fun showSelectedApps() {
        selectedApps.sortBy { it.label }
        appsLayout.removeAllViews()

        for (app in selectedApps) {
            val labelView = TextView(requireContext()).apply {
                text = app.label
                textSize = 16f
                setTextColor(Color.WHITE)
                setPadding(0, 16, 0, 16)
                setOnClickListener {
                    val launchIntent = Intent().apply {
                        component = ComponentName(app.packageName, app.className)
                        action = Intent.ACTION_MAIN
                        addCategory(Intent.CATEGORY_LAUNCHER)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    }
                    startActivity(launchIntent)
                }
                setOnLongClickListener {
                    AlertDialog.Builder(context)
                        .setMessage("Remove ${app.label} from favorites?")
                        .setPositiveButton("Remove") { _, _ ->
                            selectedApps.remove(app)
                            saveFavorites()
                            showSelectedApps()
                            Toast.makeText(context, "${app.label} removed from favorites",
                                            Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                    true
                }
            }
            appsLayout.addView(labelView)
        }
    }

    private fun saveFavorites() {
        val prefs = requireContext().getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val serialized = selectedApps.joinToString("|") { "${it.label}::${it.packageName}::${it.className}" }
        prefs.edit().putString(prefsKey, serialized).apply()
    }

    private fun loadFavorites() {
        val prefs = requireContext().getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
        val serialized = prefs.getString(prefsKey, "") ?: return
        val entries = serialized.split("|").filter { it.contains("::") }

        selectedApps.clear()
        for (entry in entries) {
            val parts = entry.split("::")
            if (parts.size == 3) {
                selectedApps.add(AppInfo(parts[0], parts[1], parts[2]))
            }
        }
    }
}
