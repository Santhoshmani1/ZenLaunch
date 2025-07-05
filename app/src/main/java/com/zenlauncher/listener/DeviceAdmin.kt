package com.zenlauncher.listener

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.zenlauncher.helpers.Constants


class DeviceAdmin : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        Toast.makeText(context, Constants.Toasts.DEVICE_ADMIN_ENABLED, Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Toast.makeText(context, Constants.Toasts.DEVICE_ADMIN_DISABLED, Toast.LENGTH_SHORT).show()
    }
}