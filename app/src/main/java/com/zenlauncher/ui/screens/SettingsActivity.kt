package com.zenlauncher.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zenlauncher.helpers.Constants
import com.zenlauncher.helpers.SettingsUtils
import com.zenlauncher.ui.components.SettingsHeader
import com.zenlauncher.ui.components.SettingsOption
import com.zenlauncher.ui.components.ZenTopAppBar

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
    val context = LocalContext.current
    Scaffold(
        topBar = {
            ZenTopAppBar(
                title = Constants.Settings.TITLE,
                onBackPressed = onBackPressed
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(
                    horizontal = Constants.Settings.PADDING_HORIZONTAL.dp,
                    vertical = Constants.Settings.PADDING_VERTICAL.dp
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            SettingsHeader("General")

            Spacer(modifier = Modifier.height(Constants.Settings.SPACING_MEDIUM.dp))

            SettingsOption("Set as Default Launcher") {
                SettingsUtils.openDefaultLauncherSettings(context)
            }

            Spacer(modifier = Modifier.height(Constants.Settings.SPACING_SMALL.dp))

            SettingsOption("Deactivate Device Admin") {
                SettingsUtils.deactivateDeviceAdmin(context)
            }

            Spacer(modifier = Modifier.height(Constants.Settings.SPACING_MEDIUM.dp))

            SettingsHeader("Support Us")

            Spacer(modifier = Modifier.height(Constants.Settings.SPACING_SMALL.dp))

            SettingsOption("Share ZenLauncher") {
                SettingsUtils.shareApp(context)
            }

            Spacer(modifier = Modifier.height(Constants.Settings.SPACING_SMALL.dp))

            SettingsOption("Star us on GitHub") {
                SettingsUtils.openGitHub(context)
            }

            Spacer(modifier = Modifier.height(Constants.Settings.SPACING_LARGE.dp))

            SettingsOption("Exit Zen Launcher") {
                (context as? ComponentActivity)?.finishAffinity()
            }
        }
    }
}
