package com.zenlauncher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zenlauncher.helpers.AppUtils

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
