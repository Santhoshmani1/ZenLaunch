package com.zenlauncher

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    private val context: Context,
    private var apps: List<AppInfo>
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    inner class AppViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val textView = TextView(context).apply {
            textSize = 18f
            setTextColor(Color.WHITE)
            setPadding(16, 24, 16, 24)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return AppViewHolder(textView)
    }

    fun updateList(newList: List<AppInfo>) {
        apps = newList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.textView.text = app.label
        holder.textView.contentDescription = app.label
        holder.itemView.alpha = 1f
        holder.textView.setOnClickListener {
            val intent = Intent().apply {
                setClassName(app.packageName, app.className)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }

        holder.textView.setOnLongClickListener {
            showOptionsDialog(app)
            true
        }
    }


    private fun showOptionsDialog(app: AppInfo) {
        val options = arrayOf("Add to Favorites", "App Info", "Uninstall")
        AlertDialog.Builder(context)
            .setTitle(app.label)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> addToFavorites(app)
                    1 -> openAppInfo(app)
                    2 -> uninstallApp(app)
                }
            }
            .show()
    }

    private fun addToFavorites(app: AppInfo) {
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

    private fun openAppInfo(app: AppInfo) {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:${app.packageName}".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun uninstallApp(app: AppInfo) {
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

    override fun getItemCount(): Int = apps.size
}
