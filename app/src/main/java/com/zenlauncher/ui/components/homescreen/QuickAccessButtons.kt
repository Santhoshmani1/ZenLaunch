package  com.zenlauncher.ui.components.homescreen

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.zenlauncher.R

@Composable
fun QuickAccessIcon(
    iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun QuickAccessButtons(
    context: Context,
    modifier: Modifier = Modifier
) {
    val dialIntent = remember { Intent(Intent.ACTION_DIAL) }
    val cameraIntent = remember {
        Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
            context.packageManager.queryIntentActivities(this, 0)
                .firstOrNull()?.activityInfo?.let {
                setClassName(it.packageName, it.name)
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        QuickAccessIcon(
            iconRes = R.drawable.call,
            onClick = { context.startActivity(dialIntent) }
        )

        QuickAccessIcon(
            iconRes = R.drawable.ic_camera,
            onClick = {
                if (cameraIntent.resolveActivity(context.packageManager) != null) {
                    context.startActivity(cameraIntent)
                } else {
                    Toast.makeText(context, "No camera app available", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
