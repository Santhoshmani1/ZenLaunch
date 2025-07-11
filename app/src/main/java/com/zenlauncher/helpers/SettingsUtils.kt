package com.zenlauncher.helpers

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.core.net.toUri
import com.zenlauncher.listener.DeviceAdmin

object SettingsUtils {
    fun openDefaultLauncherSettings(context: Context) {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Unable to open default launcher settings.", Toast.LENGTH_SHORT).show()
        }
    }

    fun deactivateDeviceAdmin(context: Context) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val compName = ComponentName(context, DeviceAdmin::class.java)
        if (dpm.isAdminActive(compName)) {
            dpm.removeActiveAdmin(compName)
            Toast.makeText(context, Constants.Toasts.DEVICE_ADMIN_DISABLED, Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, Constants.Toasts.DEVICE_ADMIN_INACTIVE, Toast.LENGTH_SHORT).show()
        }
    }

    fun shareApp(context: Context) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, Constants.APP_TITLE)
            putExtra(Intent.EXTRA_TEXT, Constants.Texts.SHARE_TEXT)
        }
        context.startActivity(Intent.createChooser(intent, Constants.Intents.SHARE_VIA))
    }

    fun openGitHub(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW, Constants.GITHUB_REPO.toUri())
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No browser found to open link.", Toast.LENGTH_SHORT).show()
        }
    }

}