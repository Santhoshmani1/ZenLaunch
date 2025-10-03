package com.zenlauncher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zenlauncher.helpers.AppUtils

/**
 * Listens for app uninstall events (`ACTION_PACKAGE_REMOVED`).
 *
 * - Calls [onAppRemoved] with the removed package name.
 * - Clears and persists favorites via [AppUtils.saveFavorites].
 * - Triggers [onFavoritesUpdated] after changes.
 *
 * Register with:
 * ```
 * IntentFilter(Intent.ACTION_PACKAGE_REMOVED).apply { addDataScheme("package") }
 * ```
 */
class PackageRemovedReceiver(
    private val onAppRemoved: (String) -> Unit,
    private val onFavoritesUpdated: () -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_PACKAGE_REMOVED) {
            val pkg = intent.data?.schemeSpecificPart ?: return
            onAppRemoved(pkg)
            AppUtils.saveFavorites(context!!, emptyList())
            onFavoritesUpdated()
        }
    }
}
