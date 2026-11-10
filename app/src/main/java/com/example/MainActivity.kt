package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppTab
import com.example.ui.SobrietyViewModel
import com.example.ui.screens.DiaryAndCommunityScreen
import com.example.ui.screens.HealthAndDoctorExportScreen
import com.example.ui.screens.HomeAndTrackerScreen
import com.example.ui.screens.MethodsAndScienceScreen
import com.example.ui.screens.MotivationScreen
import com.example.ui.theme.YasnyBeregTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YasnyBeregAppRoot()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YasnyBeregAppRoot() {
    val context = LocalContext.current
    val viewModel: SobrietyViewModel = viewModel(
        factory = SobrietyViewModel.provideFactory(context)
    )

    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val adaptedMotivation by viewModel.adaptedMotivation.collectAsStateWithLifecycle()
    val diaryEntries by viewModel.diaryEntries.collectAsStateWithLifecycle()
    val vitalsReadings by viewModel.vitalsReadings.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val communityPosts by viewModel.communityPosts.collectAsStateWithLifecycle()
    val wearableState by viewModel.wearableState.collectAsStateWithLifecycle()
    val relapseReport by viewModel.relapseReport.collectAsStateWithLifecycle()
    val isAnalyzingRisk by viewModel.isAnalyzingRisk.collectAsStateWithLifecycle()
    val showEmergencyDialog by viewModel.showEmergencyDialog.collectAsStateWithLifecycle()
    val criticalAlertBanner by viewModel.criticalAlertBanner.collectAsStateWithLifecycle()
    val statusToast by viewModel.statusToast.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusToast) {
        val msg = statusToast
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusToast()
        }
    }

    YasnyBeregTheme(elderlyLargeFont = profile.elderlyLargeFont) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Ясный Берег",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Трезвость: ${profile.soberDays} дн. • ${profile.toneMode.titleRu}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        FilterChip(
                            selected = profile.elderlyLargeFont,
                            onClick = { viewModel.toggleElderlyLargeFont() },
                            label = {
                                Text(
                                    text = if (profile.elderlyLargeFont) "Крупно ✓" else "Крупно",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FormatSize,
                                    contentDescription = "Крупный шрифт для пожилых"
                                )
                            },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("elderly_font_toggle_chip")
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    AppTab.entries.forEach { tab ->
                        val icon = when (tab) {
                            AppTab.HOME -> Icons.Default.Home
                            AppTab.MOTIVATION -> Icons.Default.Star
                            AppTab.METHODS -> Icons.Default.Psychology
                            AppTab.DIARY -> Icons.Default.MenuBook
                            AppTab.HEALTH -> Icons.Default.Favorite
                        }
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { viewModel.selectTab(tab) },
                            icon = { Icon(imageVector = icon, contentDescription = tab.titleRu) },
                            label = { Text(text = tab.titleRu) },
                            modifier = Modifier.testTag("bottom_nav_${tab.name}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Critical Physiological or Craving State Alert Banner
                AnimatedVisibility(visible = !criticalAlertBanner.isNullOrBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .testTag("critical_alert_banner"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = criticalAlertBanner.orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.dismissCriticalAlertBanner() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Закрыть"
                                )
                            }
                        }
                    }
                }

                when (selectedTab) {
                    AppTab.HOME -> HomeAndTrackerScreen(
                        profile = profile,
                        adaptedMotivation = adaptedMotivation,
                        relapseReport = relapseReport,
                        isAnalyzingRisk = isAnalyzingRisk,
                        onSelectEmotion = { viewModel.updateEmotionalState(it) },
                        onSelectToneMode = { viewModel.updateTherapyToneMode(it) },
                        onUpdateTrackerParams = { days, spend ->
                            viewModel.updateSobrietyDaysCount(days, spend)
                        },
                        onOpenEmergencyHelp = { viewModel.setEmergencyDialogVisible(true) },
                        onNavigateToTab = { viewModel.selectTab(it) },
                        onSendInstantMotivationNotification = {
                            viewModel.sendAdaptiveMotivationalNotificationNow()
                        },
                        onRunDeepAnalysis = { query ->
                            viewModel.refreshRelapseAnalytics(query)
                        }
                    )

                    AppTab.MOTIVATION -> MotivationScreen(
                        profile = profile,
                        adaptedMotivation = adaptedMotivation,
                        bookmarks = bookmarks,
                        onToggleBookmark = { viewModel.toggleMotivationalBookmark(it) },
                        onUpdateNotificationSchedule = { hour, min, enabled ->
                            viewModel.updateDailyNotificationSchedule(hour, min, enabled)
                        },
                        onSendNotificationNow = {
                            viewModel.sendAdaptiveMotivationalNotificationNow()
                        },
                        onBackToHome = { viewModel.selectTab(AppTab.HOME) }
                    )

                    AppTab.METHODS -> MethodsAndScienceScreen(
                        profile = profile,
                        onToggleSupplement = { viewModel.toggleSupplementChecked(it) },
                        onRecordMeditationCompleted = { viewModel.recordMeditationOrPracticeCompleted(it) },
                        onToggleMaterialStudied = { viewModel.toggleMaterialStudied(it) },
                        onBackToHome = { viewModel.selectTab(AppTab.HOME) }
                    )

                    AppTab.DIARY -> DiaryAndCommunityScreen(
                        profile = profile,
                        diaryEntries = diaryEntries,
                        communityPosts = communityPosts,
                        onAddDiaryEntry = { emo, craving, sleep, symp, comm, photo, supps ->
                            viewModel.addWellnessDiaryEntry(
                                emo, craving, sleep, symp, comm, photo, supps
                            )
                        },
                        onDeleteDiaryEntry = { viewModel.deleteDiaryEntry(it) },
                        onAddCommunityPost = { name, age, tag, msg ->
                            viewModel.addCommunitySupportPost(name, age, tag, msg)
                        },
                        onToggleSupportPost = { viewModel.toggleSupportCommunityPost(it) },
                        onBackToHome = { viewModel.selectTab(AppTab.HOME) }
                    )

                    AppTab.HEALTH -> HealthAndDoctorExportScreen(
                        profile = profile,
                        wearableState = wearableState,
                        vitalsReadings = vitalsReadings,
                        diaryEntries = diaryEntries,
                        onStartBleScan = { viewModel.bleManager.startScan() },
                        onStopBleScan = { viewModel.bleManager.stopScan() },
                        onConnectBleDevice = { viewModel.bleManager.connectToDevice(it) },
                        onSaveBleReading = { viewModel.saveCurrentBleReadingToVault() },
                        onAddManualVitals = { bpm, sys, dia, src, note ->
                            viewModel.addPhysiologicalReading(bpm, sys, dia, src, note)
                        },
                        onBackToHome = { viewModel.selectTab(AppTab.HOME) }
                    )
                }
            }
        }

        // Emergency Anti-Relapse SOS Dialog ("СТОП-СРЫВ")
        if (showEmergencyDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.setEmergencyDialogVisible(false) },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Протокол «СТОП-СРЫВ»")
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "1. Остановитесь на 15 минут. Физиологический пик острой тяги длится не более 15 минут — не покупайте спиртное и не наливайте прямо сейчас.",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "2. Выпейте 2 больших стакана воды и съешьте плотную пищу: падение сахара в крови усиливает тягу втрое.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚡ Жесткая правда о первой рюмке:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = "Удовольствие продлится 20 минут, а расплата — скачок давления до 180, тахикардия в 4 утра, риск инсульта, стыд перед семьей и обнуление ваших ${profile.soberDays} дней чистоты.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                viewModel.setEmergencyDialogVisible(false)
                                viewModel.selectTab(AppTab.METHODS)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Включить дыхание 4-7-8 и НЛП «Взмах»")
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.setEmergencyDialogVisible(false)
                                viewModel.selectTab(AppTab.MOTIVATION)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                        ) {
                            Text("Читать истории тех, кто выстоял")
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.setEmergencyDialogVisible(false) }) {
                        Text("Я остаюсь трезвым сегодня")
                    }
                }
            )
        }
    }
}
