package com.zenlauncher.ui.components.homescreen

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlauncher.helpers.Constants


@Composable
fun TimeDateDisplay(
    currentTime: String,
    currentDate: String,
    context: Context,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Spacer(modifier = Modifier.height(30.dp))

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
            letterSpacing = (-0.3).sp,
            color = Color.LightGray
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}
