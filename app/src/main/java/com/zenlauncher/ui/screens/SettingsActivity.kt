package com.zenlauncher.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zenlauncher.helpers.Constants
import com.zenlauncher.helpers.SettingsUtils
import com.zenlauncher.ui.components.settings.ExitZenLauncherDialog
import com.zenlauncher.ui.components.settings.SettingsHeader
import com.zenlauncher.ui.components.settings.SettingsOption
import com.zenlauncher.ui.components.settings.ZenTopAppBar

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZenLauncherSettingsScreen(
                onBackPressed = { finish() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZenLauncherSettingsScreen(onBackPressed: () -> Unit) {
    var showExitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            ZenTopAppBar(
                title = Constants.APP_TITLE,
                onBackPressed = onBackPressed
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->

        LazyColumn(
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = paddingValues.calculateTopPadding() + 16.dp,
                bottom = 24.dp
            )
        ) {

            item {
                SettingsHeader(
                    title = Constants.Settings.TITLE,
                    isSubHeader = true
                )
            }

            item {
                SettingsHeader(title = Constants.Settings.Texts.SECTION_GENERAL)
            }

            item {
                SettingsOption(
                    title = Constants.Settings.Texts.SET_DEFAULT_LAUNCHER_TITLE,
                    subtitle = Constants.Settings.Texts.SET_DEFAULT_LAUNCHER_SUB,
                    icon = Icons.Outlined.Home
                ) {
                    SettingsUtils.openDefaultLauncherSettings(context)
                }
            }

            item {
                SettingsOption(
                    title = Constants.Settings.Texts.DEACTIVATE_ADMIN_TITLE,
                    subtitle = Constants.Settings.Texts.DEACTIVATE_ADMIN_SUB,
                    icon = Icons.Outlined.Lock
                ) {
                    SettingsUtils.deactivateDeviceAdmin(context)
                }
            }

            item {
                SettingsHeader(title = Constants.Settings.Texts.SECTION_SUPPORT)
            }

            item {
                SettingsOption(
                    title = Constants.Settings.Texts.SHARE_TITLE,
                    subtitle = Constants.Settings.Texts.SHARE_SUB,
                    icon = Icons.Outlined.Share
                ) {
                    SettingsUtils.shareApp(context)
                }
            }

            item {
                SettingsOption(
                    title = Constants.Settings.Texts.GITHUB_TITLE,
                    subtitle = Constants.Settings.Texts.GITHUB_SUB,
                    icon = Icons.Outlined.Star
                ) {
                    SettingsUtils.openGitHub(context)
                }
            }

            item {
                SettingsOption(
                    title = Constants.Settings.Texts.EXIT_TITLE_OPTION,
                    subtitle = Constants.Settings.Texts.EXIT_SUB,
                    icon = Icons.AutoMirrored.Outlined.ExitToApp,
                    destructive = true
                ) {
                    showExitDialog = true
                }
            }
        }

        if (showExitDialog) {
            ExitZenLauncherDialog(
                context = context,
                onDismiss = { showExitDialog = false }
            )
        }
    }
}