package com.zenlauncher.ui.screens

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.MediaStore
import android.provider.Settings
import android.text.format.DateFormat
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlauncher.AppInfo
import com.zenlauncher.R
import com.zenlauncher.helpers.AppUtils
import com.zenlauncher.helpers.Constants
import com.zenlauncher.listener.DeviceAdmin
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val currentDate = remember {
        SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date())
    }

    val lastTapTime = remember { mutableStateOf(0L) }
    val timeFormatter = remember { DateFormat.getTimeFormat(context) }
    var currentTime by remember { mutableStateOf(timeFormatter.format(Date())) }

    DisposableEffect(Unit) {
        var formatter = DateFormat.getTimeFormat(context)
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_TIME_TICK -> {
                        currentTime = formatter.format(Date())
                    }

                    Intent.ACTION_TIME_CHANGED -> {
                        formatter = DateFormat.getTimeFormat(context)
                        currentTime = formatter.format(Date())
                    }
                }
            }
        }
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable {
                val now = System.currentTimeMillis()
                if (now - lastTapTime.value < Constants.Sizes.DOUBLE_TAP_THRESHOLD_MS) {
                    lockDevice(context)
                }
                lastTapTime.value = now
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Text(
                text = currentTime,
                fontSize = Constants.Sizes.CLOCK_TEXT_SIZE.sp,
                color = Color.White,
                modifier = Modifier.clickable {
                    val intent = Intent(Settings.ACTION_DATE_SETTINGS)
                    if (intent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(
                            context,
                            Constants.Toasts.CLOCK_NOT_AVAILABLE,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = currentDate,
                fontSize = Constants.Sizes.DATE_TEXT_SIZE.sp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(30.dp))

            FavoritesList(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }

        val dialIntent = remember { Intent(Intent.ACTION_DIAL) }
        val cameraIntent = remember {
            Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                context.packageManager.queryIntentActivities(
                    this,
                    0
                ).firstOrNull()?.activityInfo?.let {
                    setClassName(it.packageName, it.name)
                }
            }
        }

        val onDialClick = remember {
            { context.startActivity(dialIntent) }
        }

        val onCameraClick = remember {
            {
                if (cameraIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(cameraIntent)
                } else {
                    Toast.makeText(
                        context, "No camera app available",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        QuickAccessIcon(
            iconRes = R.drawable.call,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
            onClick = onDialClick
        )

        QuickAccessIcon(
            iconRes = R.drawable.ic_camera,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            onClick = onCameraClick
        )
    }
}

@Composable
fun FavoritesList(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedApps = remember { mutableStateListOf<AppInfo>() }

    LaunchedEffect(Unit) {
        val apps = AppUtils.loadFavorites(context).sortedBy { it.label }
        if (selectedApps != apps) {
            selectedApps.clear()
            selectedApps.addAll(apps)
        }
    }

    Column(modifier = modifier) {
        Text(
            text = context.getString(R.string.favourites),
            fontSize = Constants.Sizes.FAV_TITLE_TEXT_SIZE.sp,
            color = Color.LightGray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            items(
                items = selectedApps,
                key = { it.packageName }
            ) { app ->
                Text(
                    text = app.label,
                    fontSize = Constants.Sizes.APP_LABEL_TEXT_SIZE.sp,
                    color = Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .combinedClickable(
                            onClick = { AppUtils.launchApp(context, app) },
                            onLongClick = {
                                AppUtils.confirmAndRemoveFromFavorites(context, app, selectedApps) {
                                    selectedApps.remove(app)
                                    Toast.makeText(
                                        context,
                                        "${app.label} removed from favorites",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                )
            }
        }
    }
}

@Composable
fun QuickAccessIcon(iconRes: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(26.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.fillMaxSize()
        )
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
