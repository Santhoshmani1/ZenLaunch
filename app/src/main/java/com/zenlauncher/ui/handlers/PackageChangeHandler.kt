package com.zenlauncher.ui.handlers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import com.zenlauncher.data.models.AppInfo
import com.zenlauncher.helpers.AppUtils
import com.zenlauncher.reciever.PackageAddedReceiver
import com.zenlauncher.reciever.PackageRemovedReceiver

@Composable
fun PackageChangeHandler(
    context: Context,
    searchQuery: String,
    allApps: MutableState<List<AppInfo>>,
    apps: MutableState<List<AppInfo>>,
    selectedApps: MutableList<AppInfo>
) {
    DisposableEffect(searchQuery) {   // Re-run filter if search changes
    fun updateApps() {
        apps.value = if (searchQuery.isBlank()) allApps.value
        else allApps.value.filter {
            it.label.contains(searchQuery.trim(), ignoreCase = true)
        }
    }

        val addedReceiver = PackageAddedReceiver { newApp ->
            allApps.value = (allApps.value + newApp).sortedBy { it.label.lowercase() }
            updateApps()
        }

        val removedReceiver = PackageRemovedReceiver(
            onAppRemoved = { pkg ->
                allApps.value = allApps.value.filter { it.packageName != pkg }
                selectedApps.removeAll { it.packageName == pkg }
                updateApps()
            },
            onFavoritesUpdated = { AppUtils.saveFavorites(context, selectedApps) }
        )

        context.registerReceiver(
            addedReceiver,
            IntentFilter(Intent.ACTION_PACKAGE_ADDED).apply { addDataScheme("package") }
        )
        context.registerReceiver(
            removedReceiver,
            IntentFilter(Intent.ACTION_PACKAGE_REMOVED).apply { addDataScheme("package") }
        )

        onDispose {
            runCatching { context.unregisterReceiver(addedReceiver) }
            runCatching { context.unregisterReceiver(removedReceiver) }
        }
    }
}
