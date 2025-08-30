package com.zenlauncher.ui.components

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlauncher.data.models.AppInfo
import com.zenlauncher.helpers.AppUtils.launchApp
import com.zenlauncher.helpers.AppUtils.showOptionsDialog
import com.zenlauncher.helpers.Constants

/**
 * Note:
 * - selectedApps is MutableList<AppInfo> (pass your mutableStateListOf from the screen)
 * - onFavoritesChanged() -> persist favorites (save to DB)
 * - onAppUpdated(updatedApp) -> update the parent apps/originalApps lists so UI reflects rename immediately
 */
@Composable
fun AppList(
    apps: List<AppInfo>,
    selectedApps: MutableList<AppInfo>,
    highlightLetter: Char?,
    fadeOthers: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onFavoritesChanged: () -> Unit,
    onAppUpdated: (AppInfo) -> Unit
) {
    val context = LocalContext.current
    LazyColumn(state = listState) {
        itemsIndexed(apps, key = { _, app -> app.packageName }) { _, app ->
            val matches = app.label.firstOrNull()?.uppercaseChar() == highlightLetter
            val alpha = if (fadeOthers) {
                if (matches) 1f else 0.3f
            } else 1f

            AppListItem(
                app = app,
                onClick = { launchApp(context, app) },
                onLongClick = {
                    showOptionsDialog(context, app, selectedApps) { updatedApp ->
                        onAppUpdated(updatedApp)
                        onFavoritesChanged()
                    }
                },
                alpha = alpha
            )
        }
    }
}

@Composable
fun AppListItem(
    app: AppInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    alpha: Float
) {
    val interactionSource = remember { MutableInteractionSource() }
    val indication: Indication = LocalIndication.current

    Text(
        text = app.label,
        color = Color.White.copy(alpha = alpha),
        fontSize = Constants.Sizes.APP_LABEL_TEXT_SIZE.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Constants.Sizes.APP_LABEL_PADDING_V.dp, horizontal = 16.dp)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = indication,
                onClick = onClick,
                onLongClick = onLongClick
            )
    )
}
