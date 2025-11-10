package com.zenlauncher.ui.handlers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import com.zenlauncher.data.models.AppInfo
import com.zenlauncher.helpers.AppUtils
import com.zenlauncher.receiver.PackageAddedReceiver
import com.zenlauncher.receiver.PackageRemovedReceiver

/**
 * PackageChangeHandler
 *
 * A lifecycle-aware Composable that listens for app install/uninstall events
 * and keeps the app list in sync with the device state.
 *
 * - Updates [allApps] when apps are installed or removed.
 * - Keeps [apps] filtered according to [searchQuery].
 * - Removes deleted apps from [selectedApps] and persists favourites.
 *
 */
@Composable
fun PackageChangeHandler(
    context: Context,
    searchQuery: String,
    allApps: MutableState<List<AppInfo>>,
    apps: MutableState<List<AppInfo>>,
    selectedApps: MutableList<AppInfo>
) {
    DisposableEffect(searchQuery) {

        fun updateApps() {
            apps.value = if (searchQuery.isBlank()) {
                allApps.value
            } else {
                allApps.value.filter {
                    it.label.contains(searchQuery.trim(), ignoreCase = true)
                }
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
            onFavouritesUpdated = { AppUtils.saveFavourites(context, selectedApps) }
        )

        // Register receivers for install/remove events
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
