package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AdaptedMotivationBundle
import com.example.data.ExpandedRecoveryContent
import com.example.data.MethodsAndScienceCatalog
import com.example.data.RelapseRiskReport
import com.example.data.local.EmotionalState
import com.example.data.local.TherapyToneMode
import com.example.ui.AppTab
import com.example.ui.UserProfileState
import kotlinx.coroutines.delay
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeAndTrackerScreen(
    profile: UserProfileState,
    diaryEntriesCount: Int,
    adaptedMotivation: AdaptedMotivationBundle,
    relapseReport: RelapseRiskReport?,
    isAnalyzingRisk: Boolean,
    onSelectEmotion: (EmotionalState) -> Unit,
    onSelectToneMode: (TherapyToneMode) -> Unit,
    onUpdateTrackerParams: (Int, Int, Int) -> Unit,
    onOpenEmergencyHelp: () -> Unit,
    onNavigateToTab: (AppTab) -> Unit,
    onSendInstantMotivationNotification: () -> Unit,
    onRunDeepAnalysis: (String) -> Unit
) {
    var showEditTrackerModal by remember { mutableStateOf(false) }
    var customAnalysisQuestion by remember { mutableStateOf("") }
    var currentHour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    LaunchedEffect(Unit) {
        while (true) {
            currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            delay(60_000L)
        }
    }
    val daypartSupport = remember(currentHour) { ExpandedRecoveryContent.daypartForHour(currentHour) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Hero Sobriety Counter Card with Illustration
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_sobriety_card"),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_serenity),
                        contentDescription = "Рассвет трезвости",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(215.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(215.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x99102419),
                                        Color(0xE6102419)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0x33FFFFFF),
                                shape = RoundedCornerShape(50)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "AES-256",
                                        tint = Color(0xFFD7EFE0),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Оффлайн • Шифрование AES-256",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                }
                            }
                            TextButton(
                                onClick = { showEditTrackerModal = true },
                                modifier = Modifier.testTag("edit_tracker_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Изменить срок",
                                    tint = Color(0xFFFCE8CE),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Срок",
                                    color = Color(0xFFFCE8CE),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ВАШ ЛИЧНЫЙ ПЛАН",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFD7EFE0)
                        )
                        Text(
                            text = "${profile.soberDays} дн. ${profile.remainingHoursToday} ч.",
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                color = Color(0x33FFFFFF),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Savings,
                                        contentDescription = null,
                                        tint = Color(0xFFF2B66D)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "${profile.moneySavedRub} ₽",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (profile.dailySpendRub > 0) "Оценка по вашим данным" else "Добавьте свои траты",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color(0xFFD7EFE0)
                                        )
                                    }
                                }
                            }
                            Surface(
                                color = Color(0x33FFFFFF),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = Color(0xFF8CD6A9)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (profile.dailyPureEthanolMl > 0) "${profile.pureEthanolAvoidedMl} мл" else "—",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (profile.dailyPureEthanolMl > 0) "Оценка по вашим данным" else "Данные не введены",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color(0xFFD7EFE0)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().testTag("daypart_support_card"),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Text("${daypartSupport.label} · ${daypartSupport.heading}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(daypartSupport.message, style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { onNavigateToTab(AppTab.METHODS) }) {
                        Text("${daypartSupport.actionTitle}: ${daypartSupport.action}")
                    }
                }
            }
        }

        // Quick access to support; this is not emergency monitoring.
        item {
            Button(
                onClick = onOpenEmergencyHelp,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .testTag("sos_emergency_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Экстренная помощь",
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "НУЖНА ПОДДЕРЖКА СЕЙЧАС?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Откройте варианты помощи. При опасных симптомах вызовите экстренную службу.",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // Emotional State Selector (Adapts Motivational System in Real Time)
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Как вы себя чувствуете сегодня?",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Выберите состояние для подбора поддержки. Это не диагноз и не оценка медицинского риска.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        maxItemsInEachRow = 2
                    ) {
                        EmotionalState.entries.forEach { emotion ->
                            val isSelected = profile.currentEmotion == emotion
                            val bg = if (isSelected) {
                                if (emotion == EmotionalState.CRISIS) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                            val borderColor = if (isSelected) {
                                if (emotion == EmotionalState.CRISIS) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            } else {
                                Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 58.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(bg)
                                    .border(2.dp, borderColor, RoundedCornerShape(14.dp))
                                    .clickable { onSelectEmotion(emotion) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .testTag("emotion_chip_${emotion.name}"),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column {
                                    Text(
                                        text = emotion.titleRu,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = emotion.subtitleRu,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Adapted Motivational Message, Quote & Story Preview Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("adapted_motivation_home_card"),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ПОДДЕРЖКА НА СЕЙЧАС",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = adaptedMotivation.adaptationReason,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = adaptedMotivation.dailyMessage.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = adaptedMotivation.dailyMessage.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    if (profile.toneMode != TherapyToneMode.GENTLE && adaptedMotivation.dailyMessage.toughModeMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚡ Трезвая правда без иллюзий:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = adaptedMotivation.dailyMessage.toughModeMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Famous Quote of the Day
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = adaptedMotivation.famousQuote.content,
                                style = MaterialTheme.typography.bodyLarge,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "— ${adaptedMotivation.famousQuote.authorOrHero} (${adaptedMotivation.famousQuote.authorRoleOrAge})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onNavigateToTab(AppTab.MOTIVATION) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 52.dp)
                                .testTag("open_all_motivation_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "Больше поддержки",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                        OutlinedButton(
                            onClick = onSendInstantMotivationNotification,
                            modifier = Modifier
                                .heightIn(min = 52.dp)
                                .testTag("send_motivation_notif_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Отправить послание сейчас"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Сейчас")
                        }
                    }
                }
            }
        }

        // Therapy Tone Switcher Card (Gentle vs Adaptive vs Tough Demotivation)
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Режим психологического воздействия",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TherapyToneMode.entries.forEach { mode ->
                        val selected = profile.toneMode == mode
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectToneMode(mode) }
                                .testTag("tone_mode_${mode.name}"),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (selected) Icons.Default.CheckCircle else Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mode.titleRu,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = mode.descriptionRu,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Analytical Relapse Prevention Card
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("relapse_analytics_card"),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Самопроверка и план поддержки",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Обновить анализ",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onRunDeepAnalysis(customAnalysisQuestion) }
                        )
                    }

                    if (isAnalyzingRisk) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Подбираем локальные подсказки по вашим заметкам…",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else if (relapseReport != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = relapseReport.safetyNote,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = relapseReport.attentionLevel,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = relapseReport.supportSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Рекомендованный план действий:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        relapseReport.immediateActionPlan.forEach { step ->
                            Text(
                                text = "• $step",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customAnalysisQuestion,
                        onValueChange = { customAnalysisQuestion = it },
                        label = { Text("Заметка для себя (локальная подсказка)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_trigger_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onRunDeepAnalysis(customAnalysisQuestion) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("run_relapse_analysis_button")
                    ) {
                        Text("Показать варианты поддержки")
                    }
                }
            }
        }

        // Achievements & Virtual Rewards System ("Система достижений и наград")
        item {
            var showAllAchievements by remember { mutableStateOf(false) }
            val achievements = remember(
                profile.soberDays,
                profile.completedMeditationsCount,
                profile.studiedMaterialIds.size,
                profile.userCommunityActionsCount,
                diaryEntriesCount
            ) {
                com.example.data.EducationAndAchievementsCatalog.buildAchievementsList(
                    soberDays = profile.soberDays,
                    completedMeditationsCount = profile.completedMeditationsCount,
                    studiedMaterialsCount = profile.studiedMaterialIds.size,
                    communityInteractionsCount = profile.userCommunityActionsCount,
                    diaryAndVitalsCount = diaryEntriesCount
                )
            }
            val unlockedCount = achievements.count { it.isUnlocked }
            val visibleAchievements = if (showAllAchievements) achievements else achievements.take(4)

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("achievements_rewards_card"),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🏆 Система достижений и наград",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Открыто наград: $unlockedCount из ${achievements.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        TextButton(
                            onClick = { onNavigateToTab(AppTab.METHODS) },
                            modifier = Modifier.testTag("open_education_from_achievements")
                        ) {
                            Text("К тренажерам")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    visibleAchievements.forEach { ach ->
                        val bg = if (ach.isUnlocked) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant
                        val contentColor = if (ach.isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant

                        Surface(
                            color = bg,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                                .testTag("achievement_badge_${ach.id}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
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
                                            imageVector = if (ach.isUnlocked) Icons.Default.CheckCircle else Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = if (ach.isUnlocked) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = ach.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = contentColor
                                            )
                                            Text(
                                                text = ach.rewardTitle,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (ach.isUnlocked) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                    }
                                    Surface(
                                        color = if (ach.isUnlocked) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = if (ach.isUnlocked) "ПОЛУЧЕНО" else "${ach.currentValue}/${ach.targetValue}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (ach.isUnlocked) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = ach.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = contentColor
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { ach.progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showAllAchievements = !showAllAchievements },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("toggle_all_achievements_button")
                    ) {
                        Text(
                            if (showAllAchievements) "Свернуть список наград"
                            else "Показать все награды (${achievements.size})"
                        )
                    }
                }
            }
        }

        // General health reminders; not a biological recovery timer
        item {
            var showAllMilestones by remember { mutableStateOf(false) }
            val visibleMilestones = if (showAllMilestones) {
                MethodsAndScienceCatalog.organMilestones
            } else {
                MethodsAndScienceCatalog.organMilestones.take(4)
            }

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Заметки о заботе о здоровье", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Счётчик показывает только время, а не состояние органов. Сроки восстановления индивидуальны.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    visibleMilestones.forEachIndexed { index, milestone ->
                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Text("${index + 1}. ${milestone.title}", style = MaterialTheme.typography.titleMedium)
                            Text(milestone.organSystem, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(milestone.physiologicalProcess, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    OutlinedButton(
                        onClick = { showAllMilestones = !showAllMilestones },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("toggle_all_organ_milestones_button")
                    ) {
                        Text(if (showAllMilestones) "Свернуть заметки" else "Показать все заметки (${MethodsAndScienceCatalog.organMilestones.size})")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showEditTrackerModal) {
        var daysInput by remember { mutableStateOf(profile.soberDays.toString()) }
        var spendInput by remember { mutableStateOf(profile.dailySpendRub.toString()) }
        var ethanolInput by remember { mutableStateOf(profile.dailyPureEthanolMl.toString()) }

        AlertDialog(
            onDismissRequest = { showEditTrackerModal = false },
            title = { Text("Настройка счетчика трезвости") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = daysInput,
                        onValueChange = { daysInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Сколько полных дней вы не пьете?") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = spendInput,
                        onValueChange = { spendInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Средние траты на алкоголь в день в прошлом (₽)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = ethanolInput,
                        onValueChange = { ethanolInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Примерный чистый спирт в день в прошлом (мл)") },
                        supportingText = { Text("Рюмка водки ≈ 20 мл, бокал вина ≈ 18 мл, бутылка пива ≈ 20 мл. Можно оставить пустым.") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val d = daysInput.toIntOrNull() ?: profile.soberDays
                        val s = spendInput.toIntOrNull() ?: profile.dailySpendRub
                        val e = ethanolInput.toIntOrNull() ?: profile.dailyPureEthanolMl
                        onUpdateTrackerParams(d, s, e)
                        showEditTrackerModal = false
                    }
                ) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditTrackerModal = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}
