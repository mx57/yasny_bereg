package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AdaptedMotivationBundle
import com.example.data.MotivationalItem
import com.example.data.local.MotivationCategory
import com.example.data.local.MotivationalBookmarkEntity
import com.example.data.local.TherapyToneMode
import com.example.ui.UserProfileState

private enum class MotivationFilter(val labelRu: String) {
    ALL_ADAPTED("Все (${com.example.data.AdaptiveMotivationEngine.allItems.size})"),
    MEDICAL_QUOTES("Врачи и хирурги"),
    TOUGH_QUOTES("Жесткая правда"),
    CLASSICS_QUOTES("Классики и философы"),
    REAL_STORIES("Истории успеха"),
    DAILY_MESSAGES("Послания дня"),
    BOOKMARKS("Избранное")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MotivationScreen(
    profile: UserProfileState,
    adaptedMotivation: AdaptedMotivationBundle,
    bookmarks: List<MotivationalBookmarkEntity>,
    onToggleBookmark: (String) -> Unit,
    onUpdateNotificationSchedule: (Int, Int, Boolean) -> Unit,
    onSendNotificationNow: () -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler(onBack = onBackToHome)
    val context = LocalContext.current

    var selectedFilter by remember { mutableStateOf(MotivationFilter.ALL_ADAPTED) }
    var searchQuery by remember { mutableStateOf("") }
    var showNotificationSettings by remember { mutableStateOf(false) }

    var selectedHour by remember(profile.notificationHour) { mutableIntStateOf(profile.notificationHour) }
    var notificationsEnabled by remember(profile.dailyNotificationsEnabled) {
        mutableStateOf(profile.dailyNotificationsEnabled)
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        onSendNotificationNow()
    }

    val bookmarkedIds = remember(bookmarks) { bookmarks.map { it.itemId }.toSet() }

    val displayedItems = remember(selectedFilter, adaptedMotivation, bookmarkedIds, searchQuery) {
        val byCategory = when (selectedFilter) {
            MotivationFilter.ALL_ADAPTED -> adaptedMotivation.allMatchedItems
            MotivationFilter.MEDICAL_QUOTES -> adaptedMotivation.allMatchedItems.filter {
                it.subCategoryTag == "Врачи и наука"
            }
            MotivationFilter.TOUGH_QUOTES -> adaptedMotivation.allMatchedItems.filter {
                it.subCategoryTag == "Жесткая правда" || !it.toughModeMessage.isNullOrBlank()
            }
            MotivationFilter.CLASSICS_QUOTES -> adaptedMotivation.allMatchedItems.filter {
                it.subCategoryTag == "Классики и философы"
            }
            MotivationFilter.DAILY_MESSAGES -> adaptedMotivation.allMatchedItems.filter {
                it.category == MotivationCategory.DAILY_MESSAGE
            }
            MotivationFilter.REAL_STORIES -> adaptedMotivation.allMatchedItems.filter {
                it.category == MotivationCategory.REAL_STORY
            }
            MotivationFilter.BOOKMARKS -> adaptedMotivation.allMatchedItems.filter {
                bookmarkedIds.contains(it.id)
            }
        }
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            byCategory
        } else {
            byCategory.filter { item ->
                item.title.contains(q, ignoreCase = true) ||
                    item.authorOrHero.contains(q, ignoreCase = true) ||
                    item.content.contains(q, ignoreCase = true) ||
                    item.practicalTakeaway.contains(q, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
            ) {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.img_recovery_path),
                        contentDescription = "Путь выздоровления и мотивации",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(135.dp)
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Библиотека мотивации и мудрости",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = adaptedMotivation.adaptationReason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Compact & Expandable Daily Notification Schedule Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .testTag("daily_notification_settings_card"),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showNotificationSettings = !showNotificationSettings },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Ежедневные уведомления (${String.format("%02d:00", selectedHour)})",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = if (notificationsEnabled) "Включены • Нажмите для настройки времени"
                                    else "Отключены • Нажмите для включения",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = if (showNotificationSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Развернуть настройки уведомлений"
                        )
                    }

                    AnimatedVisibility(visible = showNotificationSettings) {
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Автоматическая отправка каждый день:",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Switch(
                                    checked = notificationsEnabled,
                                    onCheckedChange = { enabled ->
                                        notificationsEnabled = enabled
                                        onUpdateNotificationSchedule(selectedHour, 0, enabled)
                                    },
                                    modifier = Modifier.testTag("toggle_daily_notifications")
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(7, 8, 9, 12, 18, 20, 21).forEach { hour ->
                                    FilterChip(
                                        selected = selectedHour == hour,
                                        onClick = {
                                            selectedHour = hour
                                            onUpdateNotificationSchedule(hour, 0, notificationsEnabled)
                                        },
                                        label = { Text(String.format("%02d:00", hour)) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                onSendNotificationNow()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("trigger_motivational_notification_now")
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Отправить сегодняшнее послание в шторку",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Search and Filter Controls
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("motivation_search_input"),
                    placeholder = {
                        Text("Поиск по автору (Углов, Павлов, Шичко, Чехов) или слову...")
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Поиск")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Очистить поиск")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MotivationFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = {
                                Text(
                                    text = filter.labelRu,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            modifier = Modifier.testTag("motivation_filter_${filter.name}")
                        )
                    }
                }

                Text(
                    text = "Найдено материалов: ${displayedItems.size} • Нажмите на карточку для полного чтения",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (displayedItems.isEmpty()) {
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedFilter == MotivationFilter.BOOKMARKS) {
                                "В избранном пока нет сохраненных карточек"
                            } else {
                                "По вашему запросу ничего не найдено"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (selectedFilter == MotivationFilter.BOOKMARKS) {
                                "Нажмите на значок закладки на любой цитате врачей, классиков или истории успеха, чтобы сохранить её для трудной минуты."
                            } else {
                                "Попробуйте сбросить фильтр или изменить текст в строке поиска."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (searchQuery.isNotEmpty() || selectedFilter != MotivationFilter.ALL_ADAPTED) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedFilter = MotivationFilter.ALL_ADAPTED
                                }
                            ) {
                                Text("Показать все материалы")
                            }
                        }
                    }
                }
            }
        } else {
            itemsIndexed(displayedItems, key = { _, it -> it.id }) { index, item ->
                MotivationalItemCard(
                    item = item,
                    initiallyExpanded = index == 0,
                    isBookmarked = bookmarkedIds.contains(item.id),
                    showToughNote = profile.toneMode != TherapyToneMode.GENTLE,
                    onToggleBookmark = { onToggleBookmark(item.id) },
                    onShareItem = {
                        val shareText = buildString {
                            appendLine("«${item.title}»")
                            appendLine("${item.authorOrHero} (${item.authorRoleOrAge})")
                            appendLine()
                            appendLine(item.content)
                            appendLine()
                            appendLine("Практический вывод: ${item.practicalTakeaway}")
                            appendLine("— Приложение «Ясный Берег»")
                        }
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Поделиться мотивацией"))
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MotivationalItemCard(
    item: MotivationalItem,
    initiallyExpanded: Boolean,
    isBookmarked: Boolean,
    showToughNote: Boolean,
    onToggleBookmark: () -> Unit,
    onShareItem: () -> Unit
) {
    var expanded by remember(item.id) { mutableStateOf(initiallyExpanded) }
    val isToughItem = item.subCategoryTag == "Жесткая правда"

    val badgeColor = when {
        isToughItem -> MaterialTheme.colorScheme.errorContainer
        item.subCategoryTag == "Врачи и наука" -> MaterialTheme.colorScheme.primaryContainer
        item.category == MotivationCategory.FAMOUS_QUOTE -> MaterialTheme.colorScheme.secondaryContainer
        item.category == MotivationCategory.REAL_STORY -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val badgeTextColor = when {
        isToughItem -> MaterialTheme.colorScheme.onErrorContainer
        item.subCategoryTag == "Врачи и наука" -> MaterialTheme.colorScheme.onPrimaryContainer
        item.category == MotivationCategory.FAMOUS_QUOTE -> MaterialTheme.colorScheme.onSecondaryContainer
        item.category == MotivationCategory.REAL_STORY -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded }
            .testTag("motivation_card_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isToughItem) 1.5.dp else 1.dp,
            color = if (isToughItem) MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                isToughItem -> Icons.Default.Warning
                                item.subCategoryTag == "Врачи и наука" -> Icons.Default.MedicalServices
                                item.category == MotivationCategory.FAMOUS_QUOTE -> Icons.Default.FormatQuote
                                item.category == MotivationCategory.REAL_STORY -> Icons.Default.Bookmark
                                else -> Icons.Default.Star
                            },
                            contentDescription = null,
                            tint = badgeTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${item.subCategoryTag} • ${item.category.labelRu}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.testTag("bookmark_btn_${item.id}")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Сохранить в избранное",
                            tint = if (isBookmarked) MaterialTheme.colorScheme.secondary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onShareItem,
                        modifier = Modifier.testTag("share_btn_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Поделиться",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${item.authorOrHero} • ${item.authorRoleOrAge}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.content,
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = if (item.category == MotivationCategory.FAMOUS_QUOTE) FontStyle.Italic else FontStyle.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    if (showToughNote && !item.toughModeMessage.isNullOrBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚡ Прямое предупреждение (Жесткий режим):",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.toughModeMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Практический шаг сегодня:",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.practicalTakeaway,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.testTag("expand_motivation_${item.id}")
                ) {
                    Text(
                        text = if (expanded) "Свернуть карточку" else "Читать полностью и практический вывод",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null
                    )
                }
            }
        }
    }
}
