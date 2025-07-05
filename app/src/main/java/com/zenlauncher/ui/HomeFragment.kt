package com.zenlauncher.ui

import android.R
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.zenlauncher.AppInfo
import com.zenlauncher.helpers.AppUtils
import com.zenlauncher.helpers.Constants
import com.zenlauncher.helpers.lockDevice
import com.zenlauncher.helpers.setPaddingAll
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private val selectedApps = mutableListOf<AppInfo>()
    private lateinit var appsLayout: LinearLayout
    private var lastTapTime = 0L
    private val dateFormatter = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FrameLayout(requireContext()).apply {
        setBackgroundColor(Color.BLACK)

        val context = requireContext()

        val clock = TextClock(context).apply {
            format24Hour = "HH:mm"
            textSize = Constants.Sizes.CLOCK_TEXT_SIZE
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setOnClickListener {
                val intent = Intent(Settings.ACTION_DATE_SETTINGS)
                if (intent.resolveActivity(context.packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        context,
                        Constants.Toasts.CLOCK_NOT_AVAILABLE,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val dateText = TextView(context).apply {
            text = dateFormatter.format(Date())
            textSize = Constants.Sizes.DATE_TEXT_SIZE
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
        }

        val favTitle = TextView(context).apply {
            text = "Favourites"
            textSize = Constants.Sizes.FAV_TITLE_TEXT_SIZE
            setTextColor(Color.LTGRAY)
            setPadding(
                Constants.Sizes.FAV_TITLE_PADDING_H,
                Constants.Sizes.FAV_TITLE_PADDING_TOP,
                Constants.Sizes.FAV_TITLE_PADDING_H,
                Constants.Sizes.FAV_TITLE_PADDING_BOTTOM
            )
        }

        appsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                Constants.Sizes.APPS_LAYOUT_PADDING_H,
                0,
                Constants.Sizes.APPS_LAYOUT_PADDING_H,
                0
            )
        }

        val verticalLayout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            setPadding(0, Constants.Sizes.VERTICAL_LAYOUT_PADDING_TOP, 0, 0)
            addView(clock)
            addSpacer(Constants.Sizes.SPACE_SMALL)
            addView(dateText)
            addSpacer(Constants.Sizes.SPACE_LARGE)
            addView(favTitle)
            addView(appsLayout)
        }

        addView(verticalLayout)

        addView(
            createQuickAccessIcon(
                context,
                R.drawable.ic_menu_call,
                Gravity.BOTTOM or Gravity.START,
                rotation = 280f
            ) {
                startActivity(Intent(Intent.ACTION_DIAL))
            })

        addView(
            createQuickAccessIcon(
                context,
                R.drawable.ic_menu_camera,
                Gravity.BOTTOM or Gravity.END
            ) {
                startActivity(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
            })

        setOnClickListener {
            val now = System.currentTimeMillis()
            if (now - lastTapTime < Constants.Sizes.DOUBLE_TAP_THRESHOLD_MS) {
                lockDevice(context)
            }
            lastTapTime = now
        }
    }

    private fun LinearLayout.addSpacer(height: Int) {
        addView(Space(context).apply { minimumHeight = height })
    }

    private fun createQuickAccessIcon(
        context: Context,
        iconResId: Int,
        gravity: Int,
        rotation: Float = 0f,
        onClick: () -> Unit
    ) = ImageView(context).apply {
        setImageResource(iconResId)
        setColorFilter(Color.WHITE)
        setPaddingAll(Constants.Sizes.ICON_PADDING)
        layoutParams = FrameLayout.LayoutParams(
            Constants.Sizes.ICON_WIDTH,
            Constants.Sizes.ICON_HEIGHT
        ).apply {
            this.gravity = gravity
            marginStart = Constants.Sizes.ICON_MARGIN_SIDE
            marginEnd = Constants.Sizes.ICON_MARGIN_SIDE
            bottomMargin = Constants.Sizes.ICON_MARGIN_BOTTOM
        }
        this.rotation = rotation
        setOnClickListener { onClick() }
    }

    override fun onResume() {
        super.onResume()
        val newFavorites = AppUtils.loadFavorites(requireContext()).sortedBy { it.label }
        if (selectedApps != newFavorites) {
            selectedApps.clear()
            selectedApps.addAll(newFavorites)
            showSelectedApps()
        }
    }

    private fun showSelectedApps() {
        appsLayout.removeAllViews()
        selectedApps.forEach { app ->
            appsLayout.addView(createAppLabelView(app))
        }
    }

    private fun createAppLabelView(app: AppInfo) = TextView(requireContext()).apply {
        text = app.label
        textSize = Constants.Sizes.APP_LABEL_TEXT_SIZE
        setTextColor(Color.WHITE)
        setPadding(0, Constants.Sizes.APP_LABEL_PADDING_V, 0, Constants.Sizes.APP_LABEL_PADDING_V)
        setOnClickListener { AppUtils.launchApp(requireContext(), app) }
        setOnLongClickListener {
            AppUtils.confirmAndRemoveFromFavorites(requireContext(), app, selectedApps) {
                showSelectedApps()
            }
            true
        }
    }
}