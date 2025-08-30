package com.zenlauncher.helpers

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.net.toUri
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zenlauncher.data.db.LauncherDatabase
import com.zenlauncher.data.db.entities.FavoriteEntity
import com.zenlauncher.data.db.entities.RenamedAppEntity
import com.zenlauncher.data.models.AppInfo
import com.zenlauncher.helpers.Constants.DIGITAL_WELLBEING_ACTIVITY
import com.zenlauncher.helpers.Constants.DIGITAL_WELLBEING_PACKAGE_NAME
import com.zenlauncher.helpers.Constants.DIGITAL_WELLBEING_SAMSUNG_ACTIVITY
import com.zenlauncher.helpers.Constants.DIGITAL_WELLBEING_SAMSUNG_PACKAGE_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AppUtils {

    fun addToFavorites(context: Context, app: AppInfo) {
        val dao = LauncherDatabase.getDatabase(context).favoriteDao()
        CoroutineScope(Dispatchers.IO).launch {
            dao.insertFavorite(
                FavoriteEntity(
                    label = app.label,
                    packageName = app.packageName,
                    className = app.className
                )
            )
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "${app.label} added to favorites", Toast.LENGTH_SHORT).show()
            }
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

        if (!isSystemApp && intent.resolveActivity(pm) != null) {
            context.startActivity(Intent.createChooser(intent, "Uninstall ${app.label}"))
        } else {
            Toast.makeText(context, "System app ${app.label} cannot be uninstalled", Toast.LENGTH_LONG).show()
        }
    }

    fun launchApp(context: Context, app: AppInfo) {
        val (packageName, className) = when (app.packageName) {
            DIGITAL_WELLBEING_PACKAGE_NAME -> DIGITAL_WELLBEING_PACKAGE_NAME to DIGITAL_WELLBEING_ACTIVITY
            DIGITAL_WELLBEING_SAMSUNG_PACKAGE_NAME -> DIGITAL_WELLBEING_SAMSUNG_PACKAGE_NAME to DIGITAL_WELLBEING_SAMSUNG_ACTIVITY
            else -> app.packageName to app.className
        }

        val intent = Intent(Intent.ACTION_MAIN).apply {
            setClassName(packageName, className)
            addCategory(Intent.CATEGORY_LAUNCHER)
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
                    val updatedApp = app.copy(label = newLabel)

                    // Update in selectedApps list
                    val index = selectedApps.indexOfFirst {
                        it.packageName == app.packageName && it.className == app.className
                    }
                    if (index != -1) selectedApps[index] = updatedApp

                    // Persist rename in DB
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = LauncherDatabase.getDatabase(context)
                        db.renamedAppDao().insertOrUpdateRename(
                            RenamedAppEntity(app.packageName, app.className, newLabel)
                        )

                        // If in favorites, also update there
                        db.favoriteDao().renameFavorite(app.packageName, app.className, newLabel)
                    }

                    // Update UI
                    onUpdated(updatedApp)
                    Toast.makeText(context, "Renamed to $newLabel", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                } else {
                    editText.error = "Name cannot be empty"
                }
            }

            editText.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }

        dialog.show()
    }

    suspend fun loadRenames(context: Context): Map<String, String> {
        val dao = LauncherDatabase.getDatabase(context).renamedAppDao()
        return dao.getRenamedApps().associateBy(
            { it.packageName + it.className },
            { it.customLabel }
        )
    }

    suspend fun saveRename(context: Context, app: AppInfo) {
        val db = LauncherDatabase.getDatabase(context)
        val dao = db.renamedAppDao()
        dao.insertOrUpdateRename(
            RenamedAppEntity(
                packageName = app.packageName,
                className = app.className,
                customLabel = app.label
            )
        )
    }

    suspend fun getRenamedLabel(context: Context, packageName: String, className: String): String? {
        val db = LauncherDatabase.getDatabase(context)
        val dao = db.renamedAppDao()
        return dao.getRename(packageName, className)?.customLabel
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

    suspend fun loadFavorites(context: Context): List<AppInfo> {
        val dao = LauncherDatabase.getDatabase(context).favoriteDao()
        return dao.getFavorites().map { AppInfo(it.label, it.packageName, it.className) }
    }

    fun saveFavorites(context: Context, favorites: List<AppInfo>) {
        val dao = LauncherDatabase.getDatabase(context).favoriteDao()
        CoroutineScope(Dispatchers.IO).launch {
            dao.getFavorites().forEach { dao.deleteFavorite(it) } // clear
            favorites.forEach { app ->
                dao.insertFavorite(
                     FavoriteEntity(label=app.label,
                                    packageName = app.packageName,
                                    className = app.className)
                )
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
                val dao = LauncherDatabase.getDatabase(context).favoriteDao()
                CoroutineScope(Dispatchers.IO).launch {
                    dao.deleteByApp(app.packageName, app.className)
                    selectedApps.remove(app)
                    withContext(Dispatchers.Main) {
                        onUpdated()
                        Toast.makeText(context, "${app.label} removed from favorites", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
