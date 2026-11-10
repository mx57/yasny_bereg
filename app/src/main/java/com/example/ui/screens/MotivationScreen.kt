package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AdaptedMotivationBundle
import com.example.data.MotivationalItem
import com.example.data.local.MotivationCategory
import com.example.data.local.MotivationalBookmarkEntity
import com.example.data.local.TherapyToneMode
import com.example.ui.UserProfileState

private enum class MotivationFilter(val labelRu: String) {
    ALL_ADAPTED("Адаптировано для вас"),
    DAILY_MESSAGES("Послания дня"),
    FAMOUS_QUOTES("Цитаты великих"),
    REAL_STORIES("Истории успеха"),
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

    val displayedItems = remember(selectedFilter, adaptedMotivation, bookmarkedIds) {
        when (selectedFilter) {
            MotivationFilter.ALL_ADAPTED -> adaptedMotivation.allMatchedItems
            MotivationFilter.DAILY_MESSAGES -> adaptedMotivation.allMatchedItems.filter {
                it.category == MotivationCategory.DAILY_MESSAGE
            }
            MotivationFilter.FAMOUS_QUOTES -> adaptedMotivation.allMatchedItems.filter {
                it.category == MotivationCategory.FAMOUS_QUOTE
            }
            MotivationFilter.REAL_STORIES -> adaptedMotivation.allMatchedItems.filter {
                it.category == MotivationCategory.REAL_STORY
            }
            MotivationFilter.BOOKMARKS -> adaptedMotivation.allMatchedItems.filter {
                bookmarkedIds.contains(it.id)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.img_recovery_path),
                        contentDescription = "Путь выздоровления и мотивации",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(155.dp)
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Система адаптивной мотивации",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = adaptedMotivation.adaptationReason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Ежедневные ободряющие сообщения, цитаты великих личностей о трезвости и реальные истории выздоровления автоматически подбираются под ваш стаж (${profile.soberDays} дн.) и текущее самочувствие («${profile.currentEmotion.titleRu}»).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Daily Notification Schedule & Instant Send Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_notification_settings_card"),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Ежедневные уведомления",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Утреннее послание, цитата и поддержка",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { enabled ->
                                notificationsEnabled = enabled
                                onUpdateNotificationSchedule(selectedHour, 0, enabled)
                            },
                            modifier = Modifier.testTag("toggle_daily_notifications")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Выберите удобное время ежедневной отправки:",
                        style = MaterialTheme.typography.labelLarge
                    )
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

                    Spacer(modifier = Modifier.height(12.dp))
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
                            .heightIn(min = 54.dp)
                            .testTag("trigger_motivational_notification_now")
                    ) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Отправить сегодняшнее сообщение сейчас",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Category Filter Row
        item {
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
                            text = "В избранном пока нет сохраненных карточек",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Нажмите на значок закладки на любом послании, цитате или истории успеха, чтобы сохранить её для трудной минуты.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(displayedItems, key = { it.id }) { item ->
                MotivationalItemCard(
                    item = item,
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
    isBookmarked: Boolean,
    showToughNote: Boolean,
    onToggleBookmark: () -> Unit,
    onShareItem: () -> Unit
) {
    val badgeColor = when (item.category) {
        MotivationCategory.DAILY_MESSAGE -> MaterialTheme.colorScheme.primaryContainer
        MotivationCategory.FAMOUS_QUOTE -> MaterialTheme.colorScheme.secondaryContainer
        MotivationCategory.REAL_STORY -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val badgeTextColor = when (item.category) {
        MotivationCategory.DAILY_MESSAGE -> MaterialTheme.colorScheme.onPrimaryContainer
        MotivationCategory.FAMOUS_QUOTE -> MaterialTheme.colorScheme.onSecondaryContainer
        MotivationCategory.REAL_STORY -> MaterialTheme.colorScheme.onTertiaryContainer
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("motivation_card_${item.id}"),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
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
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (item.category) {
                                MotivationCategory.DAILY_MESSAGE -> Icons.Default.Star
                                MotivationCategory.FAMOUS_QUOTE -> Icons.Default.FormatQuote
                                MotivationCategory.REAL_STORY -> Icons.Default.Bookmark
                            },
                            contentDescription = null,
                            tint = badgeTextColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.category.labelRu,
                            style = MaterialTheme.typography.labelLarge,
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

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${item.authorOrHero} • ${item.authorRoleOrAge}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.content,
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = if (item.category == MotivationCategory.FAMOUS_QUOTE) FontStyle.Italic else FontStyle.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (showToughNote && !item.toughModeMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
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
                        Text(
                            text = item.toughModeMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
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
}
