package com.zenlauncher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.util.Log
import java.util.Date

/**
 * Listens for system time updates (`ACTION_TIME_TICK`, `ACTION_TIME_CHANGED`)
 * and sends the formatted time string via [onTimeUpdated].
 *
 * Register with:
 * ```
 * IntentFilter().apply {
 *     addAction(Intent.ACTION_TIME_TICK)
 *     addAction(Intent.ACTION_TIME_CHANGED)
 * }
 * ```
 */
class TimeChangedReceiver(
    private val onTimeUpdated: (String) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        var formatter = DateFormat.getTimeFormat(context)

        when (intent.action) {
            Intent.ACTION_TIME_TICK -> {
                val currentTime = formatter.format(Date())
                onTimeUpdated(currentTime)
                Log.d("ZenLauncher", "Time tick: $currentTime")
            }
            Intent.ACTION_TIME_CHANGED -> {
                formatter = DateFormat.getTimeFormat(context)
                val currentTime = formatter.format(Date())
                onTimeUpdated(currentTime)
                Log.d("ZenLauncher", "Time changed: $currentTime")
            }
        }
    }
}
