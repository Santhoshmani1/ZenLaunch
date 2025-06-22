package com.simplelauncher

import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import android.widget.TextView
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(private val context: Context, private var apps: List<AppInfo>) :
    RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

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

    /**
     * Updates the list of apps to be shown with new apps when an event like search is triggered
     */
    fun updateList(newList: List<AppInfo>) {
        apps = newList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.textView.text = app.label
        holder.textView.contentDescription = app.label
        holder.textView.setOnClickListener {
            val intent = Intent().apply {
                setClassName(app.packageName, app.className)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = apps.size
}
