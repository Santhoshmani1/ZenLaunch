package com.zenlauncher.ui.screens

import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenlauncher.data.models.AppInfo
import com.zenlauncher.AppList
import com.zenlauncher.helpers.AppUtils
import com.zenlauncher.helpers.Constants
import com.zenlauncher.reciever.PackageAddedReceiver
import com.zenlauncher.reciever.PackageRemovedReceiver
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var originalApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    val selectedApps =
        remember { mutableStateListOf<AppInfo>().apply { addAll(AppUtils.loadFavorites(context)) } }
    var overlayLetter by remember { mutableStateOf<String?>(null) }
    val isIndexDragging = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
        val launchableApps = pm.queryIntentActivities(intent, 0)
            .map {
                AppInfo(
                    it.loadLabel(pm).toString(),
                    it.activityInfo.packageName,
                    it.activityInfo.name
                )
            }
            .sortedBy { it.label.lowercase() }
        originalApps = launchableApps
        apps = launchableApps
    }

    DisposableEffect(Unit) {
        val packageAddedReceiver = PackageAddedReceiver { newApp ->
            apps = (apps + newApp).sortedBy { it.label.lowercase() }
            originalApps = (originalApps + newApp).sortedBy { it.label.lowercase() }
        }
        val addedFilter = IntentFilter(Intent.ACTION_PACKAGE_ADDED).apply { addDataScheme("package") }
        context.registerReceiver(packageAddedReceiver, addedFilter)

        val packageRemovedReceiver = PackageRemovedReceiver(
            onAppRemoved = { pkg ->
                apps = apps.filter { it.packageName != pkg }
                originalApps = originalApps.filter { it.packageName != pkg }
                selectedApps.removeAll { it.packageName == pkg }
            },
            onFavoritesUpdated = {
                AppUtils.saveFavorites(context, selectedApps)
            }
        )
        val removedFilter = IntentFilter(Intent.ACTION_PACKAGE_REMOVED).apply { addDataScheme("package") }
        context.registerReceiver(packageRemovedReceiver, removedFilter)

        onDispose {
            context.unregisterReceiver(packageAddedReceiver)
            context.unregisterReceiver(packageRemovedReceiver)
        }
    }


    val letters = ('A'..'Z').toList()
    val letterIndexMap = remember(apps) {
        buildMap {
            apps.forEachIndexed { index, app ->
                app.label.firstOrNull()?.uppercaseChar()?.let { c ->
                    if (c in 'A'..'Z') putIfAbsent(c, index)
                }
            }
        }
    }

    var indexBarTop by remember { mutableFloatStateOf(0f) }
    var indexBarHeight by remember { mutableFloatStateOf(0f) }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)) {
        Column(modifier = Modifier.fillMaxSize()) {

            UnderlineSearchBar(
                query = searchQuery,
                onQueryChange = { query ->
                    searchQuery = query
                    apps = if (query.isBlank()) originalApps else originalApps.filter { app ->
                        app.label.contains(query, ignoreCase = true)
                    }
                }
            )

            Box(modifier = Modifier.fillMaxSize()) {
                AppList(
                    apps = apps,
                    selectedApps = selectedApps,
                    highlightLetter = overlayLetter?.firstOrNull(),
                    fadeOthers = isIndexDragging.value,
                    listState = listState,
                    onUpdated = { AppUtils.saveFavorites(context, selectedApps) }
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        0.1.dp,
                        Alignment.CenterVertically
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(40.dp)
                        .padding(vertical = 8.dp)
                        .onGloballyPositioned { coordinates ->
                            val bounds = coordinates.boundsInRoot()
                            indexBarTop = bounds.top
                            indexBarHeight = bounds.height
                        }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragStart = { isIndexDragging.value = true },
                                onVerticalDrag = { change, _ ->
                                    val absoluteY = change.position.y + indexBarTop
                                    val relativeY =
                                        (absoluteY - indexBarTop).coerceIn(0f, indexBarHeight)
                                    val letterHeight = indexBarHeight / letters.size
                                    val index = (relativeY / letterHeight).toInt()
                                        .coerceIn(0, letters.size - 1)
                                    val letter = letters[index]
                                    overlayLetter = letter.toString()
                                    letterIndexMap[letter]?.let { targetIndex ->
                                        coroutineScope.launch {
                                            listState.scrollToItem(targetIndex)
                                        }
                                    }
                                },
                                onDragEnd = {
                                    isIndexDragging.value = false
                                    overlayLetter = null
                                },
                                onDragCancel = {
                                    isIndexDragging.value = false
                                    overlayLetter = null
                                }
                            )
                        }
                ) {
                    IconButton(
                        onClick = {
                            context.startActivity(
                                Intent(
                                    context,
                                    SettingsActivity::class.java
                                )
                            )
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    letters.forEach { letter ->
                        Text(
                            letter.toString(),
                            color = Color.Transparent.copy(alpha = 0.85f),
                            fontSize = 8.sp,
                        )
                    }
                }

                overlayLetter?.let { letter ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = (-48).dp)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(letter, color = Color.White, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun UnderlineSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search apps..."
) {
    val interactionSource = remember { MutableInteractionSource() }

    TextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        placeholder = {
            Text(
                text = placeholder,
                color = Color.Gray,
                fontSize = Constants.Sizes.APP_LABEL_TEXT_SIZE.sp
            )
        },
        textStyle = LocalTextStyle.current.copy(
            color = Color.White,
            fontSize = Constants.Sizes.APP_LABEL_TEXT_SIZE.sp
        ),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.White,
            unfocusedIndicatorColor = Color.Gray,
            cursorColor = Color.White,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.White.copy(alpha = 0.8f)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        },
        interactionSource = interactionSource
    )
}
