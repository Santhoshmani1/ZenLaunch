package com.simplelauncher

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

        // Favorite app labels container
        appsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }

        // Add favorite app row
        val addRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(48, 90, 48, 0)
            setOnClickListener { openAppPicker() }

            val text = TextView(context).apply {
                text = "Add favorite"
                setTextColor(Color.LTGRAY)
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val addIcon = ImageView(context).apply {
                setImageResource(android.R.drawable.ic_menu_add)
                setColorFilter(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(80, 80).apply {
                    leftMargin = 16
                }
            }

            addView(text)
            addView(addIcon)
        }


        val verticalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            setPadding(0, 100, 0, 0)
            addView(clock)
            addView(Space(context).apply { minimumHeight = 12 })
            addView(dateText)
            addView(Space(context).apply { minimumHeight = 120 })
            addView(addRow)
            addView(appsLayout)
        }

        val rootLayout = FrameLayout(context).apply {
            setBackgroundColor(Color.BLACK)
            addView(
                verticalLayout,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.TOP or Gravity.CENTER_HORIZONTAL
                )
            )
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

    private fun openAppPicker() {
        val context = requireContext()
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val launchableApps = pm.queryIntentActivities(intent, 0)
            .sortedBy { it.loadLabel(pm).toString() }

        val appInfos = launchableApps.map {
            AppInfo(
                it.loadLabel(pm).toString(),
                it.activityInfo.packageName,
                it.activityInfo.name
            )
        }

        val appNames = appInfos.map { it.label }.toTypedArray()

        AlertDialog.Builder(context)
            .setTitle("Choose App")
            .setItems(appNames) { _, which ->
                val chosenApp = appInfos[which]
                selectedApps.add(chosenApp)
                showSelectedApps()
            }
            .show()
    }

    private fun showSelectedApps() {
        appsLayout.removeAllViews()

        for ((index, app) in selectedApps.withIndex()) {
            val labelView = TextView(requireContext()).apply {
                text = app.label
                textSize = 16f
                setTextColor(Color.WHITE)
                setPadding(0, 16, 0, 16)
                setOnClickListener {
                    val launchIntent = Intent().apply {
                        component = android.content.ComponentName(app.packageName, app.className)
                        action = Intent.ACTION_MAIN
                        addCategory(Intent.CATEGORY_LAUNCHER)
                    }
                    startActivity(launchIntent)
                }
                setOnLongClickListener {
                    AlertDialog.Builder(context)
                        .setMessage("Remove ${app.label} from favorites?")
                        .setPositiveButton("Remove") { _, _ ->

                            selectedApps.removeAt(index)
                            showSelectedApps()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()

                    true
                }
            }
            appsLayout.addView(labelView)
        }
    }
}
