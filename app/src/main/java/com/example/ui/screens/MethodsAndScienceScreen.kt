package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.EducationAndAchievementsCatalog
import com.example.data.EducationalFormat
import com.example.data.EducationalMaterial
import com.example.data.MethodsAndScienceCatalog
import com.example.data.SupplementGuideItem
import com.example.data.TherapyTechnique
import com.example.ui.UserProfileState
import kotlinx.coroutines.delay

private enum class MethodsSection(val labelRu: String) {
    ALL("Все разделы"),
    EDUCATION("Обучение (Статьи, Видео, Инфографика)"),
    PSYCHOLOGY_NLP("Медитации, КПТ и НЛП"),
    TOUGH_DEMOTIVATION("Жесткая демотивация"),
    SUPPLEMENTS("Биодобавки и Наука")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MethodsAndScienceScreen(
    profile: UserProfileState,
    onToggleSupplement: (String) -> Unit,
    onRecordMeditationCompleted: (String) -> Unit,
    onToggleMaterialStudied: (String) -> Unit,
    onBackToHome: () -> Unit
) {
    BackHandler(onBack = onBackToHome)

    var selectedSection by remember { mutableStateOf(MethodsSection.ALL) }
    var selectedEduFormat by remember { mutableStateOf<EducationalFormat?>(null) }

    // Interactive 4-7-8 Breathing Anti-Craving Coach state
    var isBreathingActive by remember { mutableStateOf(false) }
    var breathingPhase by remember { mutableStateOf("Готов к старту (Цикл 4-7-8)") }
    var phaseSecondsLeft by remember { mutableIntStateOf(0) }
    var targetCircleScale by remember { mutableStateOf(1.0f) }

    // Interactive NLP Swish ("Взмах") state
    var swishRepsCount by remember { mutableIntStateOf(0) }
    var swishActiveState by remember { mutableStateOf(false) }

    LaunchedEffect(isBreathingActive) {
        while (isBreathingActive) {
            targetCircleScale = 1.28f
            for (s in 4 downTo 1) {
                breathingPhase = "Глубокий вдох носом..."
                phaseSecondsLeft = s
                delay(1000L)
            }
            targetCircleScale = 1.28f
            for (s in 7 downTo 1) {
                breathingPhase = "Спокойная задержка дыхания..."
                phaseSecondsLeft = s
                delay(1000L)
            }
            targetCircleScale = 0.88f
            for (s in 8 downTo 1) {
                breathingPhase = "Медленный выдох через губы..."
                phaseSecondsLeft = s
                delay(1000L)
            }
        }
        targetCircleScale = 1.0f
        breathingPhase = "Готов к старту (Цикл 4-7-8)"
        phaseSecondsLeft = 0
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetCircleScale,
        animationSpec = tween(durationMillis = 900),
        label = "breathing_scale"
    )

    val filteredTechniques = remember(selectedSection) {
        when (selectedSection) {
            MethodsSection.ALL -> MethodsAndScienceCatalog.therapyTechniques
            MethodsSection.PSYCHOLOGY_NLP -> MethodsAndScienceCatalog.therapyTechniques.filter { !it.isToughDemotivation }
            MethodsSection.TOUGH_DEMOTIVATION -> MethodsAndScienceCatalog.therapyTechniques.filter { it.isToughDemotivation }
            MethodsSection.EDUCATION, MethodsSection.SUPPLEMENTS -> emptyList()
        }
    }

    val filteredEducation = remember(selectedSection, selectedEduFormat) {
        if (selectedSection != MethodsSection.ALL && selectedSection != MethodsSection.EDUCATION) {
            emptyList()
        } else {
            val all = EducationAndAchievementsCatalog.educationalMaterials
            if (selectedEduFormat == null) all else all.filter { it.format == selectedEduFormat }
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
            Text(
                text = "Методы, Образование и Биохимия",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MethodsSection.entries.forEach { sec ->
                    FilterChip(
                        selected = selectedSection == sec,
                        onClick = { selectedSection = sec },
                        label = { Text(sec.labelRu, style = MaterialTheme.typography.labelLarge) },
                        modifier = Modifier.testTag("methods_section_${sec.name}")
                    )
                }
            }
        }

        // ==================== EDUCATIONAL MATERIALS SECTION ====================
        if (selectedSection == MethodsSection.ALL || selectedSection == MethodsSection.EDUCATION) {
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("education_header_card"),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Образовательный центр выздоровления",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Научные статьи, интерактивные видео-лекции и наглядная инфографика: триггеры, механизмы зависимости, стратегии преодоления тяги, социальная поддержка, здоровые привычки и выход из срыва. Изучено: ${profile.studiedMaterialIds.size} из ${EducationAndAchievementsCatalog.educationalMaterials.size}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedEduFormat == null,
                                onClick = { selectedEduFormat = null },
                                label = { Text("Все форматы") }
                            )
                            EducationalFormat.entries.forEach { fmt ->
                                FilterChip(
                                    selected = selectedEduFormat == fmt,
                                    onClick = { selectedEduFormat = fmt },
                                    label = { Text(fmt.labelRu) },
                                    modifier = Modifier.testTag("edu_format_filter_${fmt.name}")
                                )
                            }
                        }
                    }
                }
            }

            items(filteredEducation, key = { it.id }) { eduItem ->
                EducationalMaterialCard(
                    item = eduItem,
                    isStudied = profile.studiedMaterialIds.contains(eduItem.id),
                    onToggleStudied = { onToggleMaterialStudied(eduItem.id) }
                )
            }
        }

        // ==================== BREATHING MEDITATION & NLP SWISH ====================
        if (selectedSection == MethodsSection.ALL || selectedSection == MethodsSection.PSYCHOLOGY_NLP) {
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("breathing_coach_card"),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Air,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Дыхательная медитация 4-7-8 (Выполнено: ${profile.completedMeditationsCount})",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Снижает частоту пульса и гасит выброс адреналина при острой тяге за 90 секунд",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        Box(
                            modifier = Modifier
                                .size(116.dp)
                                .scale(animatedScale)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isBreathingActive) "${phaseSecondsLeft}с" else "4-7-8",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = breathingPhase,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { isBreathingActive = !isBreathingActive },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 54.dp)
                                    .testTag("toggle_breathing_coach_button")
                            ) {
                                Text(
                                    text = if (isBreathingActive) "Пауза" else "Старт дыхания",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    isBreathingActive = false
                                    onRecordMeditationCompleted("Дыхательная медитация 4-7-8")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 54.dp)
                                    .testTag("complete_meditation_button")
                            ) {
                                Text("Завершить (+1 к награде)")
                            }
                        }
                    }
                }
            }

            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nlp_swish_interactive_card"),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Интерактивный тренажер НЛП «ВЗМАХ»",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (!swishActiveState) {
                                "Представьте навязчивый образ спиртного и нажмите кнопку ниже, чтобы мгновенно сжать и вышвырнуть его, заменив образом здорового и сильного себя (Сделано взмахов: $swishRepsCount из 7)."
                            } else {
                                "ВЗМАХ ВЫПОЛНЕН ($swishRepsCount/7)! Образ алкоголя сжат в черную точку и стерт. На всем экране — вы с ясным взглядом, ровным пульсом и гордостью за себя!"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    val next = (swishRepsCount + 1).coerceAtMost(7)
                                    swishRepsCount = next
                                    swishActiveState = true
                                    if (next == 7) {
                                        onRecordMeditationCompleted("Сессия НЛП «Взмах»")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 54.dp)
                                    .testTag("nlp_swish_action_button")
                            ) {
                                Text("СДЕЛАТЬ «ВЗМАХ!» ($swishRepsCount/7)")
                            }
                            if (swishRepsCount > 0) {
                                OutlinedButton(
                                    onClick = {
                                        swishRepsCount = 0
                                        swishActiveState = false
                                    },
                                    modifier = Modifier.heightIn(min = 54.dp)
                                ) {
                                    Text("Сброс")
                                }
                            }
                        }
                    }
                }
            }
        }

        if (filteredTechniques.isNotEmpty()) {
            items(filteredTechniques, key = { it.id }) { tech ->
                TherapyTechniqueCard(
                    tech = tech,
                    onMarkCompleted = { onRecordMeditationCompleted(tech.title) }
                )
            }
        }

        if (selectedSection == MethodsSection.ALL || selectedSection == MethodsSection.SUPPLEMENTS) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalPharmacy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Научный справочник биодобавок и нутрицевтиков",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Отметьте галочкой препараты, которые вы принимаете по согласованию с врачом — они будут автоматически включены в медицинскую выписку для вашего лечащего врача.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(MethodsAndScienceCatalog.supplementsGuide, key = { it.id }) { supp ->
                SupplementCard(
                    item = supp,
                    isChecked = profile.checkedSupplementIds.contains(supp.id),
                    onToggle = { onToggleSupplement(supp.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun EducationalMaterialCard(
    item: EducationalMaterial,
    isStudied: Boolean,
    onToggleStudied: () -> Unit
) {
    var activeChapterIndex by remember { mutableIntStateOf(0) }
    var isVideoPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(isVideoPlaying) {
        while (isVideoPlaying && item.videoChapters.isNotEmpty()) {
            delay(4000L)
            activeChapterIndex = (activeChapterIndex + 1) % item.videoChapters.size
        }
    }

    val formatIcon = when (item.format) {
        EducationalFormat.ARTICLE -> Icons.Default.Article
        EducationalFormat.VIDEO -> Icons.Default.PlayCircle
        EducationalFormat.INFOGRAPHIC -> Icons.Default.BarChart
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("edu_card_${item.id}"),
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
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = formatIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${item.format.labelRu} • ${item.topic.titleRu}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Text(
                    text = item.durationOrReadTime,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.summary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            // Interactive Offline Video Lecture Player
            if (item.format == EducationalFormat.VIDEO && item.videoChapters.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                val currentChapter = item.videoChapters[activeChapterIndex]
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎬 Оффлайн видео-слайд (${currentChapter.timeCode})",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Button(
                                onClick = { isVideoPlaying = !isVideoPlaying },
                                modifier = Modifier.testTag("play_video_${item.id}")
                            ) {
                                Icon(
                                    imageVector = if (isVideoPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isVideoPlaying) "Пауза" else "Смотреть урок")
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = currentChapter.chapterTitle,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentChapter.slideSummary,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item.videoChapters.forEachIndexed { idx, ch ->
                                FilterChip(
                                    selected = activeChapterIndex == idx,
                                    onClick = { activeChapterIndex = idx },
                                    label = { Text("Эпизод ${idx + 1}") }
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Infographic Bars
            if (item.infographicMetrics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "📊 Наглядная инфографика по данным исследований:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        item.infographicMetrics.forEach { metric ->
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = metric.label,
                                        style = MaterialTheme.typography.labelLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = metric.valueText,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { metric.fraction },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = metric.commentary,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            item.bodyParagraphs.forEach { paragraph ->
                Text(
                    text = paragraph,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ключевые научные факты:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            item.keyScientificFacts.forEach { fact ->
                Text(
                    text = "• $fact",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onToggleStudied,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
                    .testTag("mark_studied_${item.id}")
            ) {
                Icon(
                    imageVector = if (isStudied) Icons.Default.CheckCircle else Icons.Default.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isStudied) "Изучено ✓ (Засчитано в достижения)" else "Отметить изученным (+1 к награде)")
            }
        }
    }
}

@Composable
private fun TherapyTechniqueCard(
    tech: TherapyTechnique,
    onMarkCompleted: () -> Unit
) {
    val containerColor = if (tech.isToughDemotivation) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val contentColor = if (tech.isToughDemotivation) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("technique_card_${tech.id}"),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (tech.isToughDemotivation) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (tech.isToughDemotivation) Icons.Default.Warning else Icons.Default.Psychology,
                            contentDescription = null,
                            tint = if (tech.isToughDemotivation) MaterialTheme.colorScheme.onError
                            else MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tech.categoryTitle,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (tech.isToughDemotivation) MaterialTheme.colorScheme.onError
                            else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Text(
                    text = "${tech.durationMinutes} мин",
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = tech.title,
                style = MaterialTheme.typography.headlineMedium,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = tech.summary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )

            Spacer(modifier = Modifier.height(12.dp))
            tech.stepByStepGuide.forEachIndexed { idx, step ->
                Text(
                    text = "${idx + 1}. $step",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentColor,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = contentColor.copy(alpha = 0.25f))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Научное обоснование: ${tech.scientificBasis}",
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.85f)
            )
            if (!tech.isToughDemotivation) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onMarkCompleted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("complete_practice_${tech.id}")
                ) {
                    Text("Выполнил практику (+1 к достижению)")
                }
            }
        }
    }
}

@Composable
private fun SupplementCard(
    item: SupplementGuideItem,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("supplement_card_${item.id}"),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier.testTag("supplement_check_${item.id}")
                ) {
                    Icon(
                        imageVector = if (isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = "Отметить прием",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = item.primaryBenefit,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Форма и дозировка: ${item.dosageAndForm}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.mechanismOfAction,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Режим приема: ${item.timingAdvice}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚕️ Важно: ${item.cautionNote}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }
}
