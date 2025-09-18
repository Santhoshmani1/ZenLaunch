package com.zenlauncher.helpers

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.database.sqlite.SQLiteConstraintException
import android.provider.Settings
import android.view.WindowManager
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

    private fun ioLaunch(block: suspend CoroutineScope.() -> Unit) {
        CoroutineScope(Dispatchers.IO).launch(block = block)
    }

    private suspend fun withMain(block: suspend () -> Unit) =
        withContext(Dispatchers.Main) { block() }

    private fun Context.showToast(msg: String) {
        ioLaunch { withMain { Toast.makeText(this@showToast, msg, Toast.LENGTH_SHORT).show() } }
    }

    fun addToFavorites(context: Context, app: AppInfo) {
        val dao = LauncherDatabase.getDatabase(context).favoriteDao()
        ioLaunch {
            try {
                dao.insertFavorite(
                    FavoriteEntity(label=app.label, packageName=app.packageName, className=app.className)
                )
                withMain { context.showToast("${app.label} added to favorites") }
            } catch (_: SQLiteConstraintException) {
                withMain { context.showToast("${app.label} is already in favorites") }
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
        val pm = context.packageManager
        val appInfo = pm.getInstalledApplications(0).find { it.packageName == app.packageName }
        val isSystemApp = appInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM) != 0

        if (!isSystemApp) {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = "package:${app.packageName}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Uninstall ${app.label}"))
        } else {
            context.showToast("System app ${app.label} cannot be uninstalled")
        }
    }

    fun launchApp(context: Context, app: AppInfo) {
        val (pkg, cls) = when (app.packageName) {
            DIGITAL_WELLBEING_PACKAGE_NAME -> DIGITAL_WELLBEING_PACKAGE_NAME to DIGITAL_WELLBEING_ACTIVITY
            DIGITAL_WELLBEING_SAMSUNG_PACKAGE_NAME -> DIGITAL_WELLBEING_SAMSUNG_PACKAGE_NAME to DIGITAL_WELLBEING_SAMSUNG_ACTIVITY
            else -> app.packageName to app.className
        }
        val intent = Intent(Intent.ACTION_MAIN).apply {
            setClassName(pkg, cls)
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
        val activity = context as? Activity ?: return context.showToast("Rename requires an Activity")

        val editText = EditText(activity).apply {
            setText(app.label)
            hint = "Enter new app name"
            setSingleLine()
            textSize = 16f
            setPadding(48, 32, 48, 32)
            background = null
        }

        val dialog = MaterialAlertDialogBuilder(activity)
            .setTitle("Rename ${app.label}")
            .setView(editText)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val newLabel = editText.text.toString().trim()
                if (newLabel.isBlank()) {
                    editText.error = "Name cannot be empty"
                    return@setOnClickListener
                }

                val updatedApp = app.copy(label = newLabel)
                selectedApps.replaceAll { if (it.packageName == app.packageName && it.className == app.className) updatedApp else it }

                ioLaunch {
                    val db = LauncherDatabase.getDatabase(activity)
                    db.renamedAppDao().insertOrUpdateRename(
                        RenamedAppEntity(app.packageName, app.className, newLabel)
                    )
                    db.favoriteDao().renameFavorite(app.packageName, app.className, newLabel)
                }

                onUpdated(updatedApp)
                activity.showToast("Renamed to $newLabel")
                dialog.dismiss()
            }

            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            editText.post {
                val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }
        }
        dialog.show()
    }

    suspend fun loadRenames(context: Context): Map<String, String> =
        LauncherDatabase.getDatabase(context).renamedAppDao()
            .getRenamedApps()
            .associate { it.packageName + it.className to it.customLabel }

    suspend fun saveRename(context: Context, app: AppInfo) =
        LauncherDatabase.getDatabase(context).renamedAppDao()
            .insertOrUpdateRename(RenamedAppEntity(app.packageName, app.className, app.label))

    suspend fun getRenamedLabel(context: Context, pkg: String, cls: String): String? =
        LauncherDatabase.getDatabase(context).renamedAppDao().getRename(pkg, cls)?.customLabel

    fun showOptionsDialog(
        context: Context,
        app: AppInfo,
        selectedApps: MutableList<AppInfo>,
        onUpdated: (AppInfo) -> Unit
    ) {
        val activity = context as? Activity ?: return context.showToast("Options require an Activity")
        AlertDialog.Builder(activity)
            .setTitle(app.label)
            .setItems(Constants.APP_OPTIONS) { _, which ->
                when (which) {
                    0 -> addToFavorites(activity, app)
                    1 -> renameApp(activity, app, selectedApps, onUpdated)
                    2 -> openAppInfo(activity, app)
                    3 -> uninstallApp(activity, app)
                }
            }
            .show()
    }

    suspend fun loadFavorites(context: Context): List<AppInfo> =
        LauncherDatabase.getDatabase(context).favoriteDao()
            .getFavorites().map { AppInfo(it.label, it.packageName, it.className) }

    fun saveFavorites(context: Context, favorites: List<AppInfo>) {
        val dao = LauncherDatabase.getDatabase(context).favoriteDao()
        ioLaunch {
            dao.clearAll()
            favorites.forEach { dao.insertFavorite(FavoriteEntity(label = it.label, packageName = it.packageName, className =  it.className)) }
        }
    }

    fun confirmAndRemoveFromFavorites(
        context: Context,
        app: AppInfo,
        selectedApps: MutableList<AppInfo>,
        onUpdated: () -> Unit
    ) {
        val activity = context as? Activity ?: return context.showToast("This requires an Activity")
        AlertDialog.Builder(activity)
            .setMessage("Remove ${app.label} from favorites?")
            .setPositiveButton("Remove") { _, _ ->
                val dao = LauncherDatabase.getDatabase(activity).favoriteDao()
                ioLaunch {
                    dao.deleteByApp(app.packageName, app.className)
                    selectedApps.removeAll { it.packageName == app.packageName && it.className == app.className }
                    withMain {
                        onUpdated()
                        activity.showToast("${app.label} removed from favorites")
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
