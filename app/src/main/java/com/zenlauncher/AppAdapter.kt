package com.zenlauncher

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.zenlauncher.helpers.addToFavorites
import com.zenlauncher.helpers.openAppInfo
import com.zenlauncher.helpers.uninstallApp

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
                    0 -> addToFavorites(context, app)
                    1 -> openAppInfo(context,app)
                    2 -> uninstallApp(context, app)
                }
            }
            .show()
    }


    override fun getItemCount(): Int = apps.size
}
