package com.zenlauncher.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.zenlauncher.data.models.AppInfo
import com.zenlauncher.helpers.AppUtils
import com.zenlauncher.ui.components.AppList
import com.zenlauncher.ui.components.SearchBar
import com.zenlauncher.ui.handlers.PackageChangeHandler
import kotlinx.coroutines.launch

@Composable
fun AppListScreen(onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var searchQuery by remember { mutableStateOf("") }
    val allApps = remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    val apps = remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    val selectedApps = remember { mutableStateListOf<AppInfo>() }

    var overlayLetter by remember { mutableStateOf<String?>(null) }
    val isIndexDragging = remember { mutableStateOf(false) }

    // Load apps
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

        val renames = AppUtils.loadRenames(context)
        val renamedApps = launchableApps.map { app ->
            val key = "${app.packageName}/${app.className}"
            app.copy(label = renames[key] ?: app.label)
        }

        allApps.value = renamedApps
        apps.value = renamedApps

        selectedApps.clear()
        selectedApps.addAll(AppUtils.loadFavourites(context))
    }

    PackageChangeHandler(
        context = context,
        searchQuery = searchQuery,
        allApps = allApps,
        apps = apps,
        selectedApps = selectedApps
    )

    val letters = ('A'..'Z').toList()
    val letterIndexMap = remember(apps.value) {
        buildMap {
            apps.value.forEachIndexed { index, app ->
                app.label.firstOrNull()?.uppercaseChar()?.let {
                    if (it in 'A'..'Z') putIfAbsent(it, index)
                }
            }
        }
    }

    var indexBarHeight by remember { mutableStateOf(0f) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus(force = true)
                    keyboardController?.hide()
                }
            }
    ) {
        Column {
            // Search bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { query ->
                    searchQuery = query
                    val normalized = query.trim().replace("\\s+".toRegex(), " ")
                    apps.value =
                        if (normalized.isBlank()) allApps.value
                        else allApps.value.filter { it.label.contains(normalized, ignoreCase = true) }
                },
                onSearch = {
                    if (apps.value.isNotEmpty()) {
                        AppUtils.launchApp(context, apps.value.first())
                    } else if (searchQuery.isNotBlank()) {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                "https://www.google.com/search?q=$searchQuery".toUri()
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }
                },
                onSettingsClick = onOpenSettings
            )

            // No results message
            if (searchQuery.isNotBlank() && apps.value.isEmpty()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(0.5f))
                    Spacer(Modifier.width(8.dp))
                    Text("Press Enter to search on web", color = Color.White.copy(0.6f), fontSize = 13.sp)
                }
            }

            // App list with A-Z index
            Box(Modifier.fillMaxSize()) {
                AppList(
                    apps = apps.value,
                    selectedApps = selectedApps,
                    highlightLetter = overlayLetter?.firstOrNull(),
                    fadeOthers = isIndexDragging.value,
                    listState = listState,
                    onFavouritesChanged = {
                        coroutineScope.launch { AppUtils.saveFavourites(context, selectedApps) }
                    },
                    onAppUpdated = {
                        coroutineScope.launch { AppUtils.saveRename(context, it) }
                    }
                )

                // Right index bar
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(40.dp)
                        .padding(vertical = 8.dp)
                        .onGloballyPositioned { indexBarHeight = it.boundsInRoot().height }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragStart = { isIndexDragging.value = true },
                                onVerticalDrag = { change, _ ->
                                    val index = ((change.position.y / (indexBarHeight / letters.size)))
                                        .toInt()
                                        .coerceIn(0, letters.size - 1)
                                    val letter = letters[index]
                                    overlayLetter = letter.toString()
                                    letterIndexMap[letter]?.let {
                                        coroutineScope.launch { listState.scrollToItem(it) }
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
                    Spacer(Modifier.height(44.dp))
                    letters.forEach { Text(it.toString(), color = Color.Transparent, fontSize = 8.sp) }
                }

                // Overlay letter
                overlayLetter?.let {
                    Box(
                        Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = (-48).dp)
                    ) {
                        Text(it, color = Color.White, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}
