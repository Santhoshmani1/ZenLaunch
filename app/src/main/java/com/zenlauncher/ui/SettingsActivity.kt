package com.zenlauncher.ui

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zenlauncher.helpers.Constants
import com.zenlauncher.listener.DeviceAdmin

class SettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 60, 40, 60)
        }


        val deactivateAdmin = createOption("Deactivate Device Admin") {
            deactivateDeviceAdmin()
        }

        val shareApp = createOption("Share ZenLauncher") {
            shareApp()
        }

        val exitLauncher = createOption("Exit Launcher") {
            finishAffinity()
        }

        layout.apply {
            addView(deactivateAdmin)
            addView(shareApp)
            addView(exitLauncher)
        }

        setContentView(layout)
    }

    private fun createOption(text: String, action: () -> Unit): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 18f
            setPadding(20, 40, 20, 40)
            setOnClickListener { action() }
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
}
