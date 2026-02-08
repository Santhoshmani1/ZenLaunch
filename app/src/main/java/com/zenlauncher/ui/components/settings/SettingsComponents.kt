package com.zenlauncher.ui.components.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlauncher.helpers.Constants
import com.zenlauncher.helpers.SettingsUtils

@Composable
fun SettingsOption(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    destructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .width(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (destructive)
                    MaterialTheme.colorScheme.error
                else
                    Color.White.copy(alpha = 0.85f)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = Constants.Settings.Ui.OPTION_TEXT_SIZE.sp,
                color = if (destructive)
                    MaterialTheme.colorScheme.error
                else
                    Color.White
            )

            subtitle?.let {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = it,
                    fontSize = Constants.Settings.Ui.SUBTITLE_TEXT_SIZE.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}


@Composable
fun SettingsHeader(
    title: String,
    isSubHeader: Boolean = false
) {
    Text(
        text = title,
        fontSize = if (isSubHeader)
            Constants.Settings.Ui.SUB_HEADER_TEXT_SIZE.sp
        else
            Constants.Settings.Ui.HEADER_TEXT_SIZE.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(
            top = if (isSubHeader) 8.dp else 24.dp,
            bottom = 12.dp
        )
    )
}

@Composable
fun ExitZenLauncherDialog(
    context: Context,
    onDismiss: () -> Unit
) {
    var confirmStep by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Constants.Settings.Colors.OVERLAY_BG),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Constants.Settings.Colors.DIALOG_BG
            ),
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 340.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (confirmStep)
                        Constants.Settings.Texts.SELECT_LAUNCHER_TITLE
                    else
                        Constants.Settings.Texts.EXIT_TITLE,
                    fontSize = Constants.Settings.Ui.DIALOG_TITLE_SIZE.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = if (confirmStep)
                        Constants.Settings.Texts.SELECT_LAUNCHER_DESC
                    else
                        Constants.Settings.Texts.EXIT_CONFIRM,
                    fontSize = Constants.Settings.Ui.DIALOG_BODY_SIZE.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(Constants.Settings.Texts.CANCEL, color = Color.White)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (confirmStep) {
                                SettingsUtils.openDefaultLauncherSettings(context)
                            } else {
                                confirmStep = true
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Constants.Settings.Colors.PRIMARY
                        )
                    ) {
                        Text(
                            text = if (confirmStep)
                                Constants.Settings.Texts.OPEN_SETTINGS
                            else
                                Constants.Settings.Texts.EXIT,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
