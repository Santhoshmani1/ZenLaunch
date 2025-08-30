    package com.zenlauncher.ui.components.homescreen

    import android.widget.Toast
    import androidx.compose.foundation.combinedClickable
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.LaunchedEffect
    import androidx.compose.runtime.mutableStateListOf
    import androidx.compose.runtime.remember
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import com.zenlauncher.data.models.AppInfo
    import com.zenlauncher.helpers.AppUtils
    import com.zenlauncher.helpers.Constants


    @Composable
    fun FavoritesList(
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current
        val selectedApps = remember { mutableStateListOf<AppInfo>() }

        LaunchedEffect(Unit) {
            val apps = AppUtils.loadFavorites(context)
            val sortedApps = apps.sortedBy { it.label }
            if (selectedApps != sortedApps) {
                selectedApps.clear()
                selectedApps.addAll(sortedApps)
            }
        }

        Column(modifier = modifier) {
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