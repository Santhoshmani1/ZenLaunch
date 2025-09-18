package com.zenlauncher.ui.screens

import android.content.Intent
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
import com.zenlauncher.helpers.AppUtils
import com.zenlauncher.helpers.Constants
import com.zenlauncher.ui.components.AppList
import com.zenlauncher.ui.handlers.PackageChangeHandler
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf("") }
    val allApps = remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    val apps = remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    val selectedApps = remember { mutableStateListOf<AppInfo>() }

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

        val renames = AppUtils.loadRenames(context)
        val renamedApps = launchableApps.map { app ->
            val key = "${app.packageName}/${app.className}"
            app.copy(label = renames[key] ?: app.label)
        }

        allApps.value = renamedApps
        apps.value = renamedApps

        val favorites = AppUtils.loadFavorites(context)
        selectedApps.clear()
        selectedApps.addAll(favorites.map { fav ->
            val key = "${fav.packageName}/${fav.className}"
            fav.copy(label = renames[key] ?: fav.label)
        })
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
                app.label.firstOrNull()?.uppercaseChar()?.let { c ->
                    if (c in 'A'..'Z') putIfAbsent(c, index)
                }
            }
        }
    }

    var indexBarTop by remember { mutableFloatStateOf(0f) }
    var indexBarHeight by remember { mutableFloatStateOf(0f) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Column(Modifier.fillMaxSize()) {

            UnderlineSearchBar(
                query = searchQuery,
                onQueryChange = { query ->
                    searchQuery = query
                    val normalized = query.trim().replace("\\s+".toRegex(), " ")
                    apps.value = if (normalized.isBlank()) allApps.value else allApps.value.filter {
                        it.label.contains(normalized, ignoreCase = true)
                    }
                }
            )

            Box(Modifier.fillMaxSize()) {
                AppList(
                    apps = apps.value,
                    selectedApps = selectedApps,
                    highlightLetter = overlayLetter?.firstOrNull(),
                    fadeOthers = isIndexDragging.value,
                    listState = listState,
                    onFavoritesChanged = {
                        coroutineScope.launch { AppUtils.saveFavorites(context, selectedApps) }
                    },
                    onAppUpdated = { updatedApp ->
                        coroutineScope.launch {
                            AppUtils.saveRename(context, updatedApp)
                            val renames = AppUtils.loadRenames(context)
                            allApps.value = allApps.value.map { app ->
                                val key = "${app.packageName}/${app.className}"
                                app.copy(label = renames[key] ?: app.label)
                            }
                            val normalized = searchQuery.trim().replace("\\s+".toRegex(), " ")
                            apps.value = if (normalized.isBlank()) allApps.value else allApps.value.filter {
                                it.label.contains(normalized, ignoreCase = true)
                            }
                            selectedApps.replaceAll { fav ->
                                val key = "${fav.packageName}/${fav.className}"
                                fav.copy(label = renames[key] ?: fav.label)
                            }
                        }
                    }
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.1.dp, Alignment.CenterVertically),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(40.dp)
                        .padding(vertical = 8.dp)
                        .onGloballyPositioned {
                            val bounds = it.boundsInRoot()
                            indexBarTop = bounds.top
                            indexBarHeight = bounds.height
                        }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragStart = { isIndexDragging.value = true },
                                onVerticalDrag = { change, _ ->
                                    val relativeY =
                                        (change.position.y).coerceIn(0f, indexBarHeight)
                                    val letterHeight = indexBarHeight / letters.size
                                    val index = (relativeY / letterHeight).toInt()
                                        .coerceIn(0, letters.size - 1)
                                    val letter = letters[index]
                                    overlayLetter = letter.toString()
                                    letterIndexMap[letter]?.let { idx ->
                                        coroutineScope.launch { listState.scrollToItem(idx) }
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
                            context.startActivity(Intent(context, SettingsActivity::class.java))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                    Spacer(Modifier.height(8.dp))
                    letters.forEach {
                        Text(it.toString(), color = Color.Transparent, fontSize = 8.sp)
                    }
                }

                overlayLetter?.let { letter ->
                    Box(
                        Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = (-48).dp)
                            .padding(12.dp)
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
                placeholder,
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
            Icon(Icons.Default.Search,
                contentDescription = "Search", tint = Color.White.copy(alpha = 0.8f))
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close,
                        contentDescription = "Clear", tint = Color.White.copy(alpha = 0.8f))
                }
            }
        },
        interactionSource = interactionSource
    )
}
