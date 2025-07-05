package com.zenlauncher.helpers

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.provider.Settings
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.net.toUri
import android.view.inputmethod.InputMethodManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zenlauncher.AppInfo

object AppUtils {

    private const val DELIMITER = "|"
    private const val ENTRY_SEPARATOR = "::"

    fun addToFavorites(context: Context, app: AppInfo) {
        val prefs = context.getSharedPreferences(Constants.Prefs.LAUNCHER_PREFS, Context.MODE_PRIVATE)
        val current = prefs.getString(Constants.Prefs.FAVORITES_KEY, "") ?: ""
        val entry = "${app.label}$ENTRY_SEPARATOR${app.packageName}$ENTRY_SEPARATOR${app.className}"

        val favorites = current.split(DELIMITER).filter { it.isNotBlank() }.toMutableSet()

        if (!favorites.add(entry)) {
            Toast.makeText(context, "${app.label} is already in favorites", Toast.LENGTH_SHORT).show()
        } else {
            prefs.edit {
                putString(Constants.Prefs.FAVORITES_KEY, favorites.sorted().joinToString(DELIMITER))
            }
            Toast.makeText(context, "${app.label} added to favorites", Toast.LENGTH_SHORT).show()
        }
    }

    fun openAppInfo(context: Context, app: AppInfo) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:${app.packageName}".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun uninstallApp(context: Context, app: AppInfo) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = "package:${app.packageName}".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val pm = context.packageManager
        val appInfo = pm.getInstalledApplications(0).find { it.packageName == app.packageName }
        val isSystemApp = appInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) != 0

        if (!isSystemApp && intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(Intent.createChooser(intent, "Uninstall ${app.label}"))
        } else {
            Toast.makeText(context, "System app ${app.label} cannot be uninstalled", Toast.LENGTH_LONG).show()
        }
    }

    fun launchApp(context: Context, app: AppInfo) {
        val intent = Intent().apply {
            setClassName(app.packageName, app.className)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }



    fun renameApp(
        context: Context,
        app: AppInfo,
        selectedApps: MutableList<AppInfo>,
        onUpdated: (AppInfo) -> Unit
    ) {
        val editText = EditText(context).apply {
            setText(app.label)
            hint = "Enter new app name"
            setSingleLine()
            textSize = 16f
            setPadding(48, 32, 48, 32)
            background = null
        }

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle("Rename ${app.label}")
            .setView(editText)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.setOnClickListener {
                val newLabel = editText.text.toString().trim()
                if (newLabel.isNotBlank()) {
                    val updatedApp = AppInfo(newLabel, app.packageName, app.className)

                    val favIndex = selectedApps.indexOfFirst {
                        it.packageName == app.packageName && it.className == app.className
                    }
                    if (favIndex != -1) {
                        selectedApps[favIndex] = updatedApp
                    }

                    val favorites = loadFavorites(context).toMutableList()
                    val storedIndex = favorites.indexOfFirst {
                        it.packageName == app.packageName && it.className == app.className
                    }
                    if (storedIndex != -1) {
                        favorites[storedIndex] = updatedApp
                        saveFavorites(context, favorites)
                    }

                    onUpdated(updatedApp)
                    Toast.makeText(context, "Renamed to $newLabel", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    editText.error = "Name cannot be empty"
                }
            }

            // Auto-focus and keyboard
            editText.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }

        dialog.show()
    }


    fun showOptionsDialog(
        context: Context,
        app: AppInfo,
        selectedApps: MutableList<AppInfo>,
        onUpdated: (AppInfo) -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle(app.label)
            .setItems(Constants.APP_OPTIONS) { _, which ->
                when (which) {
                    0 -> addToFavorites(context, app)
                    1 -> renameApp(context, app, selectedApps, onUpdated)
                    2 -> openAppInfo(context, app)
                    3 -> uninstallApp(context, app)
                }
            }
            .show()
    }

    fun saveFavorites(context: Context, favorites: List<AppInfo>) {
        val prefs = context.getSharedPreferences(Constants.Prefs.LAUNCHER_PREFS, Context.MODE_PRIVATE)
        val serialized = favorites.joinToString(DELIMITER) {
            "${it.label}$ENTRY_SEPARATOR${it.packageName}$ENTRY_SEPARATOR${it.className}"
        }
        prefs.edit { putString(Constants.Prefs.FAVORITES_KEY, serialized) }
    }

    fun loadFavorites(context: Context): List<AppInfo> {
        val prefs = context.getSharedPreferences(Constants.Prefs.LAUNCHER_PREFS, Context.MODE_PRIVATE)
        val serialized = prefs.getString(Constants.Prefs.FAVORITES_KEY, "") ?: return emptyList()
        if (serialized.isBlank()) return emptyList()
        return serialized.split(DELIMITER).mapNotNull { entry ->
            entry.split(ENTRY_SEPARATOR).takeIf { it.size == 3 }?.let { (label, pkg, className) ->
                AppInfo(label, pkg, className)
            }
        }
    }

    fun confirmAndRemoveFromFavorites(
        context: Context,
        app: AppInfo,
        selectedApps: MutableList<AppInfo>,
        onUpdated: () -> Unit
    ) {
        AlertDialog.Builder(context)
            .setMessage("Remove ${app.label} from favorites?")
            .setPositiveButton("Remove") { _, _ ->
                selectedApps.remove(app)
                saveFavorites(context, selectedApps)
                onUpdated()
                Toast.makeText(context, "${app.label} removed from favorites", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
