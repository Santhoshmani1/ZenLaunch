package com.zenlauncher

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zenlauncher.helpers.AppUtils.launchApp
import com.zenlauncher.helpers.AppUtils.showOptionsDialog
import com.zenlauncher.helpers.Constants
import com.zenlauncher.helpers.setPaddingAll

class AppAdapter(
    private val context: Context,
    private var apps: List<AppInfo>,
    private val selectedApps: MutableList<AppInfo> = mutableListOf(),
    private val onUpdated: () -> Unit = {}
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    inner class AppViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val textView = TextView(context).apply {
            textSize = Constants.Sizes.APP_LABEL_TEXT_SIZE
            setTextColor(Color.WHITE)
            setPaddingAll(Constants.Sizes.APP_LABEL_PADDING_V)
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
            launchApp(context, app)
        }

        holder.textView.setOnLongClickListener {
            showOptionsDialog(context, app, selectedApps) { updatedApp ->
                apps = apps.toMutableList().apply {
                    this[position] = updatedApp
                }
                notifyItemChanged(position)
            }
            true
        }
    }

    override fun getItemCount(): Int = apps.size
}
