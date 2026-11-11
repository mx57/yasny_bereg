package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    INTERACTIVE_TRAINERS("Тренажеры (5)"),
    PSYCHOLOGY_NLP("КПТ, Шичко и НЛП"),
    TOUGH_DEMOTIVATION("Жесткая демотивация"),
    EDUCATION("Обучение"),
    SUPPLEMENTS("Биодобавки")
}

private enum class InteractiveTrainerTab(val labelRu: String) {
    GROUNDING_54321("Заземление 5-4-3-2-1"),
    SHICHKO_MYTHS("Разбор мифов (Шичко/КПТ)"),
    JACOBSON_PMR("Релаксация Джекобсона"),
    BREATHING_478("Дыхание 4-7-8"),
    NLP_SWISH("НЛП «Взмах»")
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
    var selectedTrainerTab by remember { mutableStateOf(InteractiveTrainerTab.GROUNDING_54321) }
    var selectedEduFormat by remember { mutableStateOf<EducationalFormat?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // 1. Interactive 4-7-8 Breathing Anti-Craving Coach state
    var isBreathingActive by remember { mutableStateOf(false) }
    var breathingPhase by remember { mutableStateOf("Готов к старту (Цикл 4-7-8)") }
    var phaseSecondsLeft by remember { mutableIntStateOf(0) }
    var targetCircleScale by remember { mutableStateOf(1.0f) }

    // 2. Interactive NLP Swish ("Взмах") state
    var swishRepsCount by remember { mutableIntStateOf(0) }
    var swishActiveState by remember { mutableStateOf(false) }

    // 3. Interactive 5-4-3-2-1 Grounding state
    val completedGroundingSteps = remember { mutableStateListOf<Int>() }

    // 4. Interactive Jacobson Progressive Muscle Relaxation state
    var activeJacobsonIndex by remember { mutableIntStateOf(0) }
    var isJacobsonTimerRunning by remember { mutableStateOf(false) }
    var jacobsonPhaseText by remember { mutableStateOf("Нажмите «Старт цикла», чтобы пройти 5 сек напряжения и 10 сек расслабления") }
    var jacobsonSecondsLeft by remember { mutableIntStateOf(0) }

    // 5. Interactive Shichko & CBT Myth Busters state
    var activeMythIndex by remember { mutableIntStateOf(0) }
    var isMythRefutationRevealed by remember { mutableStateOf(true) }

    LaunchedEffect(isBreathingActive) {
        while (isBreathingActive) {
            targetCircleScale = 1.25f
            for (s in 4 downTo 1) {
                breathingPhase = "Глубокий вдох носом..."
                phaseSecondsLeft = s
                delay(1000L)
            }
            targetCircleScale = 1.25f
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

    LaunchedEffect(isJacobsonTimerRunning, activeJacobsonIndex) {
        if (isJacobsonTimerRunning) {
            val group = MethodsAndScienceCatalog.jacobsonMuscleGroups[activeJacobsonIndex]
            for (s in group.tensionSeconds downTo 1) {
                jacobsonPhaseText = "НАПРЯЖЕНИЕ (${group.muscleZoneTitle}): держите 5 сек..."
                jacobsonSecondsLeft = s
                delay(1000L)
            }
            for (s in group.relaxSeconds downTo 1) {
                jacobsonPhaseText = "РАССЛАБЛЕНИЕ И ТЕПЛО: выдохните и отпустите зажим..."
                jacobsonSecondsLeft = s
                delay(1000L)
            }
            isJacobsonTimerRunning = false
            jacobsonSecondsLeft = 0
            jacobsonPhaseText = "Зона «${group.muscleZoneTitle}» полностью расслаблена ✓"
        }
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetCircleScale,
        animationSpec = tween(durationMillis = 900),
        label = "breathing_scale"
    )

    val filteredTechniques = remember(selectedSection, searchQuery) {
        val base = when (selectedSection) {
            MethodsSection.ALL -> MethodsAndScienceCatalog.therapyTechniques
            MethodsSection.PSYCHOLOGY_NLP -> MethodsAndScienceCatalog.therapyTechniques.filter { !it.isToughDemotivation }
            MethodsSection.TOUGH_DEMOTIVATION -> MethodsAndScienceCatalog.therapyTechniques.filter { it.isToughDemotivation }
            MethodsSection.INTERACTIVE_TRAINERS,
            MethodsSection.EDUCATION,
            MethodsSection.SUPPLEMENTS -> emptyList()
        }
        val q = searchQuery.trim()
        if (q.isEmpty()) base else base.filter {
            it.title.contains(q, ignoreCase = true) ||
                it.summary.contains(q, ignoreCase = true) ||
                it.categoryTitle.contains(q, ignoreCase = true) ||
                it.stepByStepGuide.any { s -> s.contains(q, ignoreCase = true) }
        }
    }

    val filteredEducation = remember(selectedSection, selectedEduFormat, searchQuery) {
        if (selectedSection != MethodsSection.ALL && selectedSection != MethodsSection.EDUCATION) {
            emptyList()
        } else {
            val all = EducationAndAchievementsCatalog.educationalMaterials
            val byFmt = if (selectedEduFormat == null) all else all.filter { it.format == selectedEduFormat }
            val q = searchQuery.trim()
            if (q.isEmpty()) byFmt else byFmt.filter {
                it.title.contains(q, ignoreCase = true) ||
                    it.summary.contains(q, ignoreCase = true) ||
                    it.topic.titleRu.contains(q, ignoreCase = true)
            }
        }
    }

    val filteredSupplements = remember(selectedSection, searchQuery) {
        if (selectedSection != MethodsSection.ALL && selectedSection != MethodsSection.SUPPLEMENTS) {
            emptyList()
        } else {
            val all = MethodsAndScienceCatalog.supplementsGuide
            val q = searchQuery.trim()
            if (q.isEmpty()) all else all.filter {
                it.name.contains(q, ignoreCase = true) ||
                    it.primaryBenefit.contains(q, ignoreCase = true) ||
                    it.mechanismOfAction.contains(q, ignoreCase = true)
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
            Text(
                text = "Методы, Тренажеры и Наука",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("methods_search_input"),
                placeholder = {
                    Text("Поиск упражнения (Шичко, HALT, НЛП, Магний, Сон)...")
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

        // ==================== INTERACTIVE TRAINERS HUB (5 TRAINERS) ====================
        if (selectedSection == MethodsSection.ALL ||
            selectedSection == MethodsSection.INTERACTIVE_TRAINERS ||
            selectedSection == MethodsSection.PSYCHOLOGY_NLP
        ) {
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .testTag("interactive_trainers_hub_card"),
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SelfImprovement,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Интерактивные тренажеры",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "Практик: ${profile.completedMeditationsCount}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Выберите тренажер для мгновенного снятия тяги, паники, мышечного зажима или разбора алкогольных мифов:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InteractiveTrainerTab.entries.forEach { tab ->
                                FilterChip(
                                    selected = selectedTrainerTab == tab,
                                    onClick = { selectedTrainerTab = tab },
                                    label = { Text(tab.labelRu) },
                                    modifier = Modifier.testTag("trainer_tab_${tab.name}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                        Spacer(modifier = Modifier.height(12.dp))

                        when (selectedTrainerTab) {
                            InteractiveTrainerTab.GROUNDING_54321 -> {
                                Column(
                                    modifier = Modifier.testTag("grounding_54321_trainer"),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.TouchApp,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Тренажер заземления «5-4-3-2-1» при панике и тяге",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = "Переключает внимание мозга из лимбического шторма в реальность за 2 минуты. Отмечайте шаги по мере выполнения:",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    MethodsAndScienceCatalog.groundingSteps54321.forEach { step ->
                                        val isDone = completedGroundingSteps.contains(step.countNumber)
                                        Surface(
                                            color = if (isDone) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (isDone) {
                                                        completedGroundingSteps.remove(step.countNumber)
                                                    } else {
                                                        completedGroundingSteps.add(step.countNumber)
                                                        if (completedGroundingSteps.size == 5) {
                                                            onRecordMeditationCompleted("Заземление 5-4-3-2-1")
                                                        }
                                                    }
                                                }
                                                .testTag("grounding_step_${step.countNumber}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (isDone) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(26.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Шаг ${step.countNumber} — ${step.senseTitle}",
                                                        style = MaterialTheme.typography.titleMedium
                                                    )
                                                    Text(
                                                        text = step.instruction,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    Text(
                                                        text = step.examplesHint,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                completedGroundingSteps.clear()
                                                onRecordMeditationCompleted("Заземление 5-4-3-2-1")
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 50.dp)
                                                .testTag("complete_grounding_button")
                                        ) {
                                            Text("Завершить практику (+1 к награде)")
                                        }
                                        if (completedGroundingSteps.isNotEmpty()) {
                                            OutlinedButton(
                                                onClick = { completedGroundingSteps.clear() },
                                                modifier = Modifier.heightIn(min = 50.dp)
                                            ) {
                                                Text("Сброс")
                                            }
                                        }
                                    }
                                }
                            }

                            InteractiveTrainerTab.SHICHKO_MYTHS -> {
                                val myths = MethodsAndScienceCatalog.shichkoMythBusters
                                val currentMyth = myths[activeMythIndex]
                                Column(
                                    modifier = Modifier.testTag("shichko_myth_buster_trainer"),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.FactCheck,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Разбор ложных убеждений по Г. А. Шичко и КПТ (${activeMythIndex + 1}/${myths.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        myths.forEachIndexed { idx, _ ->
                                            FilterChip(
                                                selected = activeMythIndex == idx,
                                                onClick = {
                                                    activeMythIndex = idx
                                                    isMythRefutationRevealed = true
                                                },
                                                label = { Text("Миф №${idx + 1}") }
                                            )
                                        }
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.errorContainer,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = currentMyth.mythTitle,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Ложная программа: ${currentMyth.falseProgramQuote}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontStyle = FontStyle.Italic,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }

                                    AnimatedVisibility(visible = isMythRefutationRevealed) {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(14.dp)) {
                                                    Text(
                                                        text = "🔬 Научное и клиническое опровержение:",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = currentMyth.scientificRefutation,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                                    )
                                                }
                                            }
                                            Surface(
                                                color = MaterialTheme.colorScheme.secondaryContainer,
                                                shape = RoundedCornerShape(14.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(14.dp)) {
                                                    Text(
                                                        text = "✍️ Формула сознательной трезвости перед сном (по Г. А. Шичко):",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = currentMyth.eveningDiaryFormula,
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                activeMythIndex = (activeMythIndex + 1) % myths.size
                                                isMythRefutationRevealed = true
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 50.dp)
                                        ) {
                                            Text("Следующий миф →")
                                        }
                                        Button(
                                            onClick = {
                                                onRecordMeditationCompleted("Разбор убеждений по методу Шичко")
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 50.dp)
                                                .testTag("complete_shichko_button")
                                        ) {
                                            Text("Усвоено (+1)")
                                        }
                                    }
                                }
                            }

                            InteractiveTrainerTab.JACOBSON_PMR -> {
                                val groups = MethodsAndScienceCatalog.jacobsonMuscleGroups
                                val currentGroup = groups[activeJacobsonIndex]
                                Column(
                                    modifier = Modifier.testTag("jacobson_pmr_trainer"),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Bedtime,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Мышечная релаксация по Э. Джекобсону (Для сна и снятия зажимов)",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        groups.forEachIndexed { idx, g ->
                                            FilterChip(
                                                selected = activeJacobsonIndex == idx,
                                                onClick = {
                                                    isJacobsonTimerRunning = false
                                                    activeJacobsonIndex = idx
                                                    jacobsonPhaseText = "Готово к циклу: ${g.muscleZoneTitle}"
                                                },
                                                label = { Text("${g.stepNumber}. ${g.muscleZoneTitle.substringBefore(" ")}") }
                                            )
                                        }
                                    }

                                    Surface(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = "Зона ${currentGroup.stepNumber} из ${groups.size}: ${currentGroup.muscleZoneTitle}",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "1. Фаза напряжения (5 сек): ${currentGroup.tensionInstruction}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "2. Фаза расслабления (10 сек): ${currentGroup.releaseSensation}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = if (jacobsonSecondsLeft > 0) "$jacobsonPhaseText ($jacobsonSecondsLeft сек)"
                                                else jacobsonPhaseText,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = { isJacobsonTimerRunning = !isJacobsonTimerRunning },
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 50.dp)
                                                .testTag("start_jacobson_timer_button")
                                        ) {
                                            Text(if (isJacobsonTimerRunning) "Стоп таймер" else "Старт цикла (15с)")
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                isJacobsonTimerRunning = false
                                                if (activeJacobsonIndex < groups.lastIndex) {
                                                    activeJacobsonIndex += 1
                                                } else {
                                                    activeJacobsonIndex = 0
                                                    onRecordMeditationCompleted("Релаксация по Джекобсону")
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 50.dp)
                                                .testTag("next_jacobson_zone_button")
                                        ) {
                                            Text(
                                                if (activeJacobsonIndex < groups.lastIndex) "Следующая зона →"
                                                else "Завершить (+1)"
                                            )
                                        }
                                    }
                                }
                            }

                            InteractiveTrainerTab.BREATHING_478 -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("breathing_coach_card"),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Air,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Дыхательная медитация 4-7-8",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Снижает частоту пульса и гасит выброс адреналина при острой тяге за 90 секунд",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(110.dp)
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

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = breathingPhase,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
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
                                                .heightIn(min = 52.dp)
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
                                                .heightIn(min = 52.dp)
                                                .testTag("complete_meditation_button")
                                        ) {
                                            Text("Завершить (+1)")
                                        }
                                    }
                                }
                            }

                            InteractiveTrainerTab.NLP_SWISH -> {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("nlp_swish_interactive_card")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AutoFixHigh,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Интерактивный тренажер НЛП «ВЗМАХ»",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = if (!swishActiveState) {
                                            "Представьте навязчивый образ спиртного и нажмите кнопку ниже, чтобы мгновенно сжать и вышвырнуть его, заменив образом здорового и сильного себя (Сделано взмахов: $swishRepsCount из 7)."
                                        } else {
                                            "ВЗМАХ ВЫПОЛНЕН ($swishRepsCount/7)! Образ алкоголя сжат в черную точку и стерт. На всем экране — вы с ясным взглядом, ровным пульсом и гордостью за себя!"
                                        },
                                        style = MaterialTheme.typography.bodyLarge
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
                                                .heightIn(min = 52.dp)
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
                                                modifier = Modifier.heightIn(min = 52.dp)
                                            ) {
                                                Text("Сброс")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==================== THERAPY TECHNIQUES (CBT, SHICHKO, NLP, TOUGH DEMOTIVATION) ====================
        if (filteredTechniques.isNotEmpty()) {
            item {
                Text(
                    text = "Пошаговые методики и упражнения (${filteredTechniques.size})",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            itemsIndexed(filteredTechniques, key = { _, it -> it.id }) { idx, tech ->
                TherapyTechniqueCard(
                    tech = tech,
                    initiallyExpanded = idx == 0,
                    onMarkCompleted = { onRecordMeditationCompleted(tech.title) }
                )
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
                                text = "Образовательный центр (${profile.studiedMaterialIds.size}/${EducationAndAchievementsCatalog.educationalMaterials.size} изучено)",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
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

            itemsIndexed(filteredEducation, key = { _, it -> it.id }) { idx, eduItem ->
                EducationalMaterialCard(
                    item = eduItem,
                    initiallyExpanded = idx == 0,
                    isStudied = profile.studiedMaterialIds.contains(eduItem.id),
                    onToggleStudied = { onToggleMaterialStudied(eduItem.id) }
                )
            }
        }

        // ==================== SUPPLEMENTS SECTION ====================
        if (filteredSupplements.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalPharmacy,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Справочник биодобавок (${filteredSupplements.size})",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Text(
                    text = "Отметьте принимаемые препараты для включения в отчет лечащему врачу.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            itemsIndexed(filteredSupplements, key = { _, it -> it.id }) { idx, supp ->
                SupplementCard(
                    item = supp,
                    initiallyExpanded = idx == 0,
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
    initiallyExpanded: Boolean,
    isStudied: Boolean,
    onToggleStudied: () -> Unit
) {
    var expanded by remember(item.id) { mutableStateOf(initiallyExpanded) }
    var activeChapterIndex by remember { mutableIntStateOf(0) }
    var isVideoPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(isVideoPlaying) {
        while (isVideoPlaying && item.videoChapters.isNotEmpty()) {
            delay(4000L)
            activeChapterIndex = (activeChapterIndex + 1) % item.videoChapters.size
        }
    }

    val formatIcon = when (item.format) {
        EducationalFormat.ARTICLE -> Icons.AutoMirrored.Filled.Article
        EducationalFormat.VIDEO -> Icons.Default.PlayCircle
        EducationalFormat.INFOGRAPHIC -> Icons.Default.BarChart
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded }
            .testTag("edu_card_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
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
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
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

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    if (item.format == EducationalFormat.VIDEO && item.videoChapters.isNotEmpty()) {
                        val currentChapter = item.videoChapters[activeChapterIndex]
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
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
                                        Text(if (isVideoPlaying) "Пауза" else "Смотреть")
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = currentChapter.chapterTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentChapter.slideSummary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    item.videoChapters.forEachIndexed { idx, _ ->
                                        FilterChip(
                                            selected = activeChapterIndex == idx,
                                            onClick = { activeChapterIndex = idx },
                                            label = { Text("Эпизод ${idx + 1}") }
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    if (item.infographicMetrics.isNotEmpty()) {
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
                                Spacer(modifier = Modifier.height(6.dp))
                                item.infographicMetrics.forEach { metric ->
                                    Column(modifier = Modifier.padding(vertical = 5.dp)) {
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
                        Spacer(modifier = Modifier.height(10.dp))
                    }

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

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onToggleStudied,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "Свернуть материал" else "Открыть материал полностью")
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

@Composable
private fun TherapyTechniqueCard(
    tech: TherapyTechnique,
    initiallyExpanded: Boolean,
    onMarkCompleted: () -> Unit
) {
    var expanded by remember(tech.id) { mutableStateOf(initiallyExpanded) }
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded }
            .testTag("technique_card_${tech.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = 1.dp,
            color = if (tech.isToughDemotivation) MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
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
                    color = if (tech.isToughDemotivation) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
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
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
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

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tech.title,
                style = MaterialTheme.typography.titleLarge,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tech.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    tech.stepByStepGuide.forEachIndexed { idx, step ->
                        Text(
                            text = "${idx + 1}. $step",
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = contentColor.copy(alpha = 0.25f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Научное обоснование: ${tech.scientificBasis}",
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor.copy(alpha = 0.88f)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(
                        text = if (expanded) "Свернуть шаги" else "Пошаговая инструкция (${tech.stepByStepGuide.size} шагов)",
                        color = if (tech.isToughDemotivation) contentColor else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = if (tech.isToughDemotivation) contentColor else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun SupplementCard(
    item: SupplementGuideItem,
    initiallyExpanded: Boolean,
    isChecked: Boolean,
    onToggle: () -> Unit
) {
    var expanded by remember(item.id) { mutableStateOf(initiallyExpanded) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded }
            .testTag("supplement_card_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Подробнее"
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
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
    }
}
