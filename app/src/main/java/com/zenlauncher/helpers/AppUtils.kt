package com.zenlauncher.helpers

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.net.toUri
import com.zenlauncher.AppInfo

fun addToFavorites(context: Context, app: AppInfo) {
    val prefs = context.getSharedPreferences("launcher_prefs", Context.MODE_PRIVATE)
    val key = "favorites"
    val current = prefs.getString(key, "") ?: ""
    val entry = "${app.label}::${app.packageName}::${app.className}"

    val favorites = current.split("|").filter { it.isNotBlank() }.toMutableSet()

    if (favorites.contains(entry)) {
        Toast.makeText(context, "${app.label} is already in favorites", Toast.LENGTH_SHORT).show()
    } else {
        favorites.add(entry)
        prefs.edit { putString(key, favorites.sorted().joinToString("|")) }
        Toast.makeText(context, "${app.label} added to favorites", Toast.LENGTH_SHORT).show()
    }
}


fun openAppInfo(context: Context, app: AppInfo) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = "package:${app.packageName}".toUri()
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}


fun uninstallApp(context:Context, app: AppInfo) {
    val uri = "package:${app.packageName}".toUri()
    val intent = Intent(Intent.ACTION_DELETE).apply {
        data = uri
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val appInfo = context.packageManager.getApplicationInfo(app.packageName, 0)
    val isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0

    if (!isSystemApp && intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(Intent.createChooser(intent, "Uninstall ${app.label}"))
    } else {
        Toast.makeText(context, "System app ${app.label} cannot be uninstalled", Toast.LENGTH_LONG).show()
    }
}