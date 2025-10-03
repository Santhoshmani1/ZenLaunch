package com.zenlauncher.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zenlauncher.helpers.Constants
import com.zenlauncher.listener.DeviceAdmin
import com.zenlauncher.receiver.TimeChangedReceiver
import com.zenlauncher.ui.components.homescreen.FavoritesList
import com.zenlauncher.ui.components.homescreen.QuickAccessButtons
import com.zenlauncher.ui.components.homescreen.TimeDateDisplay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val currentDate = remember {
        SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    val lastTapTime = remember { mutableLongStateOf(0L) }
    val timeFormatter = remember { DateFormat.getTimeFormat(context) }
    var currentTime by remember { mutableStateOf(timeFormatter.format(Date())) }

    DisposableEffect(Unit) {
        val receiver = TimeChangedReceiver { updatedTime ->
            currentTime = updatedTime
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
        }

        context.registerReceiver(receiver, filter)

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                val now = System.currentTimeMillis()
                if (now - lastTapTime.longValue < Constants.Sizes.DOUBLE_TAP_THRESHOLD_MS) {
                    lockDevice(context)
                }
                lastTapTime.longValue = now
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 10.dp)
        ) {

            TimeDateDisplay(currentTime, currentDate, context, modifier = Modifier)
            FavoritesList(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            QuickAccessButtons(context, modifier = Modifier)
        }
    }
}


fun lockDevice(context: Context) {
    val devicePolicyManager =
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val compName = ComponentName(context, DeviceAdmin::class.java)
    if (devicePolicyManager.isAdminActive(compName)) {
        devicePolicyManager.lockNow()
    } else {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                Constants.Intents.DEVICE_ADMIN_INFO
            )
        }
        context.startActivity(intent)
    }
}
