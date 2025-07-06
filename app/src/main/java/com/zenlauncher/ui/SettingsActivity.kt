package com.zenlauncher.ui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.zenlauncher.helpers.Constants
import com.zenlauncher.helpers.setPaddingAll
import com.zenlauncher.listener.DeviceAdmin

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.apply {
            title = Constants.Settings.TITLE
            setDisplayHomeAsUpEnabled(true)
            elevation = 0f
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                Constants.Settings.PADDING_HORIZONTAL,
                Constants.Settings.PADDING_VERTICAL,
                Constants.Settings.PADDING_HORIZONTAL,
                Constants.Settings.PADDING_VERTICAL
            )
        }

        layout.apply {
            addView(
                createOption("Set as Default Launcher") { openDefaultLauncherSettings() },
                addSpacing(Constants.Settings.SPACING_MEDIUM)
            )
            addView(
                createOption("Deactivate Device Admin") { deactivateDeviceAdmin() },
                addSpacing(Constants.Settings.SPACING_MEDIUM)
            )

            addView(createHeader("Support Us"), addSpacing(Constants.Settings.SPACING_XLARGE))
            addView(
                createOption("Share ZenLauncher") { shareApp() },
                addSpacing(Constants.Settings.SPACING_SMALL)
            )
            addView(
                createOption("⭐ Star us on GitHub") { openGitHub() },
                addSpacing(Constants.Settings.SPACING_LARGE)
            )

            addView(createSpacer(Constants.Settings.SPACING_XXLARGE)) // Breathing room before Exit
            addView(
                createOption("Exit Launcher") { finishAffinity() },
                addSpacing(Constants.Settings.SPACING_LARGE)
            )
        }

        setContentView(layout)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun createOption(
        text: String,
        action: () -> Unit
    ): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = Constants.Settings.TEXT_SIZE_OPTION
            setPaddingAll(Constants.Settings.TEXT_PADDING_ALL)
            gravity = Gravity.START
            setTextColor(Constants.Settings.TEXT_COLOR_WHITE)
            setBackgroundColor(Constants.Settings.BACKGROUND_TRANSPARENT)

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            setOnClickListener { action() }
        }
    }

    private fun createHeader(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = Constants.Settings.TEXT_SIZE_HEADER
            setPadding(30, 50, 30, 20)
            gravity = Gravity.CENTER
            setTextColor(Constants.Settings.TEXT_COLOR_WHITE)
        }
    }

    private fun createSpacer(height: Int): TextView {
        return TextView(this).apply {
            text = ""
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                height
            )
        }
    }

    private fun addSpacing(bottomMargin: Int): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            this.bottomMargin = bottomMargin
        }
    }

    private fun deactivateDeviceAdmin() {
        val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val compName = ComponentName(this, DeviceAdmin::class.java)
        if (dpm.isAdminActive(compName)) {
            dpm.removeActiveAdmin(compName)
            Toast.makeText(this, Constants.Toasts.DEVICE_ADMIN_DISABLED, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, Constants.Toasts.DEVICE_ADMIN_INACTIVE, Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, Constants.APP_TITLE)
            putExtra(Intent.EXTRA_TEXT, Constants.Texts.SHARE_TEXT)
        }
        startActivity(Intent.createChooser(intent, Constants.Intents.SHARE_VIA))
    }

    private fun openDefaultLauncherSettings() {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Unable to open default launcher settings.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGitHub() {
        val intent = Intent(Intent.ACTION_VIEW, Constants.GITHUB_REPO.toUri())
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No browser found to open link.", Toast.LENGTH_SHORT).show()
        }
    }
}
