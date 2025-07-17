package com.zenlauncher.reciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import com.zenlauncher.AppInfo

class PackageAddedReceiver(
    private val onAppAdded: (AppInfo) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val pkg = intent?.data?.schemeSpecificPart ?: return
        val pm = context?.packageManager ?: return

        try {
            val appLabel = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
            val activityIntent = pm.getLaunchIntentForPackage(pkg)
            val activityName = activityIntent?.component?.className ?: ""

            val appInfo = AppInfo(
                label = appLabel,
                packageName = pkg,
                className = activityName
            )
            onAppAdded(appInfo)
            Log.d("ZenLauncher", "App installed: $pkg")
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }
}