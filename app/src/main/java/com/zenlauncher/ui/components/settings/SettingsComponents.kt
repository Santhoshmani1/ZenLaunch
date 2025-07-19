package com.zenlauncher.ui.components.settings

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlauncher.helpers.Constants
import com.zenlauncher.helpers.SettingsUtils

@Composable
fun SettingsOption(text: String, onClick: () -> Unit) {
    BasicText(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(Constants.Settings.TEXT_PADDING_ALL.dp),
        style = LocalTextStyle.current.copy(
            fontSize = Constants.Settings.TEXT_SIZE_OPTION.sp,
            color = Color.White
        )
    )
}

@Composable
fun SettingsHeader(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 10.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun ExitZenLauncherDialog(
    context: Context,
    onDismiss: () -> Unit
) {
    var showSecondStep by remember { mutableStateOf(false) }

    val title = if (showSecondStep) "Select Default Launcher" else "Exit Zen Launcher"
    val message = if (showSecondStep)
        "To exit, please select another Home screen app as your default launcher."
    else
        "Are you sure you want to exit Zen Launcher?"
    val buttonText = if (showSecondStep) "Open Settings" else "Exit"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212).copy(alpha = 0.95f)),
                modifier = Modifier
                    .padding(24.dp)
                    .wrapContentHeight()
                    .widthIn(max = 340.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = message,
                        fontSize = 16.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Color.White)
                        }
                        Button(
                            onClick = {
                                if (showSecondStep) {
                                    SettingsUtils.openDefaultLauncherSettings(context)
                                } else {
                                    showSecondStep = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF069AED)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(buttonText, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
