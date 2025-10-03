package com.zenlauncher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.zenlauncher.data.models.AppInfo


/**
 * Listens for app install events (`ACTION_PACKAGE_ADDED`)
 * and delivers the installed app as [AppInfo] via [onAppAdded].
 *
 * Register with:
 * ```
 * IntentFilter(Intent.ACTION_PACKAGE_ADDED).apply { addDataScheme("package") }
 * ```
 */
class PackageAddedReceiver(
    private val onAppAdded: (AppInfo) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val pkg = intent?.data?.schemeSpecificPart ?: return
        val pm = context?.packageManager ?: return

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
    }
}