package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AdaptedMotivationBundle
import com.example.data.AdaptiveMotivationEngine
import com.example.data.OfflineSampleRelapseAnalysisRepository
import com.example.data.RelapseAnalysisRepository
import com.example.data.RelapseRiskReport
import com.example.data.local.CommunityPostEntity
import com.example.data.local.EmotionalState
import com.example.data.local.MotivationalBookmarkEntity
import com.example.data.local.PhysiologicalReadingEntity
import com.example.data.local.SobrietyDao
import com.example.data.local.SobrietyDatabase
import com.example.data.local.TherapyToneMode
import com.example.data.local.WellnessDiaryEntity
import com.example.notifications.NotificationHelper
import com.example.security.CryptoVault
import com.example.sensors.BleHeartRateManager
import com.example.sensors.WearableTelemetryState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

enum class AppTab(val titleRu: String) {
    HOME("Главная"),
    MOTIVATION("Мотивация"),
    METHODS("Методы"),
    DIARY("Дневник"),
    HEALTH("Здоровье")
}

data class UserProfileState(
    val userName: String = "Вы",
    val sobrietyStartMillis: Long = System.currentTimeMillis(),
    val dailySpendRub: Int = 0,
    val dailyPureEthanolMl: Int = 0,
    val currentEmotion: EmotionalState = EmotionalState.CALM,
    val toneMode: TherapyToneMode = TherapyToneMode.ADAPTIVE,
    val elderlyLargeFont: Boolean = true,
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0,
    val notificationTimes: Set<String> = setOf("08:00", "12:30", "20:00"),
    val dailyNotificationsEnabled: Boolean = false,
    val checkedSupplementIds: Set<String> = emptySet(),
    val completedMeditationsCount: Int = 0,
    val studiedMaterialIds: Set<String> = emptySet(),
    val userCommunityActionsCount: Int = 0
) {
    val totalSoberHours: Int
        get() = ((System.currentTimeMillis() - sobrietyStartMillis).coerceAtLeast(0L) / 3_600_000L).toInt()

    val soberDays: Int
        get() = totalSoberHours / 24

    val remainingHoursToday: Int
        get() = totalSoberHours % 24

    val moneySavedRub: Int
        get() = (totalSoberHours * dailySpendRub) / 24

    val pureEthanolAvoidedMl: Int
        get() = (totalSoberHours * dailyPureEthanolMl) / 24
}

class SobrietyViewModel(
    private val appContext: Context,
    private val dao: SobrietyDao,
    private val analysisRepository: RelapseAnalysisRepository = OfflineSampleRelapseAnalysisRepository()
) : ViewModel() {

    private val prefs = appContext.getSharedPreferences("yasny_bereg_prefs", Context.MODE_PRIVATE)
    val bleManager = BleHeartRateManager(appContext)

    private val _selectedTab = MutableStateFlow(AppTab.HOME)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    private val _showEmergencyDialog = MutableStateFlow(false)
    val showEmergencyDialog: StateFlow<Boolean> = _showEmergencyDialog.asStateFlow()

    private val _criticalAlertBanner = MutableStateFlow<String?>(null)
    val criticalAlertBanner: StateFlow<String?> = _criticalAlertBanner.asStateFlow()

    private val _statusToast = MutableStateFlow<String?>(null)
    val statusToast: StateFlow<String?> = _statusToast.asStateFlow()

    private val _profile = MutableStateFlow(loadProfileFromPrefs())
    val profile: StateFlow<UserProfileState> = _profile.asStateFlow()

    val diaryEntries: StateFlow<List<WellnessDiaryEntity>> = dao.observeDiaryEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val vitalsReadings: StateFlow<List<PhysiologicalReadingEntity>> = dao.observePhysiologicalReadings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val bookmarks: StateFlow<List<MotivationalBookmarkEntity>> = dao.observeBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val communityPosts: StateFlow<List<CommunityPostEntity>> = dao.observeCommunityPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val wearableState: StateFlow<WearableTelemetryState> = bleManager.state

    private val _relapseReport = MutableStateFlow<RelapseRiskReport?>(null)
    val relapseReport: StateFlow<RelapseRiskReport?> = _relapseReport.asStateFlow()

    private val _isAnalyzingRisk = MutableStateFlow(false)
    val isAnalyzingRisk: StateFlow<Boolean> = _isAnalyzingRisk.asStateFlow()

    /** Re-emits the day of year so the daily motivation rotation refreshes around midnight. */
    private val dayOfYearFlow = flow {
        while (true) {
            emit(Calendar.getInstance().get(Calendar.DAY_OF_YEAR))
            delay(30 * 60_000L)
        }
    }

    val adaptedMotivation: StateFlow<AdaptedMotivationBundle> = combine(
        _profile,
        diaryEntries,
        dayOfYearFlow
    ) { prof, diary, dayOfYear ->
        val latestCraving = diary.firstOrNull()?.cravingLevel ?: -1
        AdaptiveMotivationEngine.selectAdaptedBundle(
            soberDays = prof.soberDays,
            currentEmotion = prof.currentEmotion,
            cravingLevel = latestCraving,
            stressIndex = 0,
            toneMode = prof.toneMode,
            dayOfYear = dayOfYear
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = AdaptiveMotivationEngine.selectAdaptedBundle(
            soberDays = _profile.value.soberDays,
            currentEmotion = _profile.value.currentEmotion,
            cravingLevel = -1,
            stressIndex = 0,
            toneMode = _profile.value.toneMode
        )
    )

    init {
        NotificationHelper.ensureChannelsCreated(appContext)
        NotificationHelper.restoreSavedSchedule(appContext)
        seedInitialDataIfNeeded()
        refreshRelapseAnalytics()
    }

    private fun loadProfileFromPrefs(): UserProfileState {
        val defaultStart = System.currentTimeMillis()
        val startMillis = prefs.getLong("sobriety_start_millis", defaultStart)
        val spend = prefs.getInt("daily_spend_rub", 0)
        val ethanol = prefs.getInt("daily_ethanol_ml", 0).coerceAtLeast(0)
        val emotionName = prefs.getString("current_emotion", EmotionalState.CALM.name) ?: EmotionalState.CALM.name
        val toneName = prefs.getString("tone_mode", TherapyToneMode.ADAPTIVE.name) ?: TherapyToneMode.ADAPTIVE.name
        val largeFont = prefs.getBoolean("elderly_large_font", true)
        val notifHour = prefs.getInt("notif_hour", 9)
        val notifMin = prefs.getInt("notif_minute", 0)
        val notifEnabled = prefs.getBoolean("notif_enabled", false)
        val storedTimes = prefs.getStringSet("notif_times", null)?.toSet()
        val notificationTimes = storedTimes?.intersect(NotificationHelper.reminderTimes.toSet())
            ?: if (notifEnabled) {
                val legacyTarget = prefs.getInt("notif_hour", 9) * 60 + prefs.getInt("notif_minute", 0)
                val legacySlot = NotificationHelper.reminderTimes.minByOrNull { time ->
                    val parts = time.split(":")
                    kotlin.math.abs((parts[0].toInt() * 60 + parts[1].toInt()) - legacyTarget)
                } ?: "08:00"
                setOf(legacySlot)
            } else setOf("08:00", "12:30", "20:00")
        val supps = prefs.getStringSet("checked_supplements", null)?.toSet()
            ?: emptySet()
        val meditations = prefs.getInt("completed_meditations_count", 0)
        val studied = prefs.getStringSet("studied_materials", null)?.toSet()
            ?: emptySet()
        val commActions = prefs.getInt("user_community_actions", 0)

        return UserProfileState(
            sobrietyStartMillis = startMillis,
            dailySpendRub = spend,
            dailyPureEthanolMl = ethanol,
            currentEmotion = runCatching { EmotionalState.valueOf(emotionName) }.getOrDefault(EmotionalState.CALM),
            toneMode = runCatching { TherapyToneMode.valueOf(toneName) }.getOrDefault(TherapyToneMode.ADAPTIVE),
            elderlyLargeFont = largeFont,
            notificationHour = notifHour,
            notificationMinute = notifMin,
            notificationTimes = notificationTimes,
            dailyNotificationsEnabled = notifEnabled,
            checkedSupplementIds = supps,
            completedMeditationsCount = meditations,
            studiedMaterialIds = studied,
            userCommunityActionsCount = commActions
        )
    }

    private fun saveProfileToPrefs(state: UserProfileState) {
        prefs.edit()
            .putLong("sobriety_start_millis", state.sobrietyStartMillis)
            .putInt("daily_spend_rub", state.dailySpendRub)
            .putInt("daily_ethanol_ml", state.dailyPureEthanolMl)
            .putString("current_emotion", state.currentEmotion.name)
            .putString("tone_mode", state.toneMode.name)
            .putBoolean("elderly_large_font", state.elderlyLargeFont)
            .putInt("notif_hour", state.notificationHour)
            .putInt("notif_minute", state.notificationMinute)
            .putStringSet("notif_times", state.notificationTimes)
            .putBoolean("notif_enabled", state.dailyNotificationsEnabled)
            .putStringSet("checked_supplements", state.checkedSupplementIds)
            .putInt("completed_meditations_count", state.completedMeditationsCount)
            .putStringSet("studied_materials", state.studiedMaterialIds)
            .putInt("user_community_actions", state.userCommunityActionsCount)
            .apply()
    }

    private fun seedInitialDataIfNeeded() {
        // Never create sample testimonials or health measurements that could be mistaken for real data.
    }

    fun selectTab(tab: AppTab) {
        _selectedTab.value = tab
    }

    fun setEmergencyDialogVisible(visible: Boolean) {
        _showEmergencyDialog.value = visible
    }

    fun dismissCriticalAlertBanner() {
        _criticalAlertBanner.value = null
    }

    fun clearStatusToast() {
        _statusToast.value = null
    }

    fun toggleElderlyLargeFont() {
        val updated = _profile.value.copy(elderlyLargeFont = !_profile.value.elderlyLargeFont)
        _profile.value = updated
        saveProfileToPrefs(updated)
    }

    fun updateEmotionalState(emotion: EmotionalState) {
        val updated = _profile.value.copy(currentEmotion = emotion)
        _profile.value = updated
        saveProfileToPrefs(updated)
        refreshRelapseAnalytics()
    }

    fun updateTherapyToneMode(mode: TherapyToneMode) {
        val updated = _profile.value.copy(toneMode = mode)
        _profile.value = updated
        saveProfileToPrefs(updated)
        refreshRelapseAnalytics()
    }

    fun updateSobrietyDaysCount(daysAgo: Int, dailySpendRub: Int, dailyPureEthanolMl: Int = 0) {
        val newStart = System.currentTimeMillis() - daysAgo.coerceAtLeast(0).toLong() * 24L * 3600_000L
        val updated = _profile.value.copy(
            sobrietyStartMillis = newStart,
            dailySpendRub = dailySpendRub.coerceAtLeast(0),
            dailyPureEthanolMl = dailyPureEthanolMl.coerceAtLeast(0)
        )
        _profile.value = updated
        saveProfileToPrefs(updated)
        _statusToast.value = "Параметры трекера обновлены: $daysAgo дн. трезвости"
        refreshRelapseAnalytics()
    }

    fun toggleSupplementChecked(supplementId: String) {
        val current = _profile.value.checkedSupplementIds.toMutableSet()
        if (current.contains(supplementId)) {
            current.remove(supplementId)
        } else {
            current.add(supplementId)
        }
        val updated = _profile.value.copy(checkedSupplementIds = current)
        _profile.value = updated
        saveProfileToPrefs(updated)
    }

    fun toggleMotivationalBookmark(itemId: String) {
        viewModelScope.launch {
            val isBookmarked = bookmarks.value.any { it.itemId == itemId }
            if (isBookmarked) {
                dao.removeBookmark(itemId)
                _statusToast.value = "Удалено из избранного"
            } else {
                dao.addBookmark(MotivationalBookmarkEntity(itemId = itemId))
                _statusToast.value = "Сохранено в личную подборку мотивации"
            }
        }
    }

    fun updateDailyNotificationTimes(times: Set<String>, enabled: Boolean) {
        val selected = times.intersect(NotificationHelper.reminderTimes.toSet())
        val active = enabled && selected.isNotEmpty()
        val firstTime = selected.sorted().firstOrNull()?.split(":")
        val hour = firstTime?.getOrNull(0)?.toIntOrNull() ?: _profile.value.notificationHour
        val minute = firstTime?.getOrNull(1)?.toIntOrNull() ?: _profile.value.notificationMinute
        val updated = _profile.value.copy(
            notificationHour = hour,
            notificationMinute = minute,
            notificationTimes = selected,
            dailyNotificationsEnabled = active
        )
        _profile.value = updated
        saveProfileToPrefs(updated)
        if (active) {
            NotificationHelper.scheduleDailyMotivations(appContext, selected)
            _statusToast.value = "Поддерживающие напоминания: ${selected.sorted().joinToString(", ")}"
        } else {
            NotificationHelper.cancelAllDailyMotivations(appContext)
            _statusToast.value = if (enabled) "Выберите хотя бы одно время напоминания" else "Напоминания отключены"
        }
    }

    fun sendAdaptiveMotivationalNotificationNow() {
        val bundle = adaptedMotivation.value
        val sent = NotificationHelper.sendNotificationNow(
            context = appContext,
            title = "Послание дня (${_profile.value.soberDays} дн. трезвости): ${bundle.dailyMessage.title}",
            body = "${bundle.dailyMessage.content}\n\nМысль для поддержки: ${bundle.famousQuote.content}",
            isCritical = false
        )
        _statusToast.value = if (sent) {
            "Мотивационное уведомление отправлено в шторку Android!"
        } else {
            "Разрешите показ уведомлений для получения ежедневных сообщений"
        }
    }

    private fun triggerCriticalStateNotification(title: String, body: String) {
        _criticalAlertBanner.value = "$title: $body"
        NotificationHelper.sendNotificationNow(
            context = appContext,
            title = "⚠️ КРИТИЧЕСКОЕ ОПОВЕЩЕНИЕ: $title",
            body = body,
            isCritical = true
        )
    }

    fun copyPhotoToInternalVault(sourceUri: Uri): String? {
        return try {
            val photosDir = File(appContext.filesDir, "diary_photos").apply { mkdirs() }
            val destFile = File(photosDir, "diary_${System.currentTimeMillis()}.jpg")
            appContext.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destFile).toString()
        } catch (_: Exception) {
            sourceUri.toString()
        }
    }

    fun addWellnessDiaryEntry(
        emotion: EmotionalState,
        cravingLevel: Int,
        sleepHours: Float,
        symptomsPlain: String,
        commentPlain: String,
        photoUriString: String?,
        tookSupplements: Boolean
    ) {
        viewModelScope.launch {
            val internalPhotoUri = photoUriString?.let { Uri.parse(it) }?.let { copyPhotoToInternalVault(it) }
            val entry = WellnessDiaryEntity(
                timestampMillis = System.currentTimeMillis(),
                emotionalStateName = emotion.name,
                cravingLevel = cravingLevel.coerceIn(0, 10),
                sleepHours = sleepHours.coerceIn(0f, 16f),
                encryptedSymptoms = CryptoVault.encrypt(symptomsPlain.trim()),
                encryptedComment = CryptoVault.encrypt(commentPlain.trim()),
                photoUriString = internalPhotoUri,
                tookSupplements = tookSupplements
            )
            dao.insertDiaryEntry(entry)

            // Sync current emotion into profile so motivational messages adapt immediately
            val updatedProf = _profile.value.copy(currentEmotion = emotion)
            _profile.value = updatedProf
            saveProfileToPrefs(updatedProf)

            _statusToast.value = "Запись сохранена в офлайн-дневник. Оценка тяги — ваша заметка, не медицинский диагноз."
            refreshRelapseAnalytics()
        }
    }

    fun deleteDiaryEntry(id: Long) {
        viewModelScope.launch {
            dao.deleteDiaryEntry(id)
            _statusToast.value = "Запись удалена из дневника"
        }
    }

    fun addPhysiologicalReading(
        heartRateBpm: Int,
        systolicBp: Int,
        diastolicBp: Int,
        sourceDevice: String,
        notePlain: String
    ) {
        viewModelScope.launch {
            val cleanBpm = if (heartRateBpm <= 0) 0 else heartRateBpm.coerceIn(40, 220)
            // 0 means "not measured" and must stay 0 — the app never fabricates approximate values.
            val cleanSys = if (systolicBp <= 0) 0 else systolicBp.coerceIn(70, 250)
            val cleanDia = if (diastolicBp <= 0) 0 else diastolicBp.coerceIn(40, 150)

            val entity = PhysiologicalReadingEntity(
                timestampMillis = System.currentTimeMillis(),
                heartRateBpm = cleanBpm,
                systolicBp = cleanSys,
                diastolicBp = cleanDia,
                stressIndex = 0,
                hrvRmssdMs = 0,
                sourceDevice = sourceDevice,
                isCriticalAlert = false,
                encryptedNote = CryptoVault.encrypt(notePlain.trim())
            )
            dao.insertPhysiologicalReading(entity)
            _statusToast.value = "Замер сохранён. Расчёт медицинского риска, HRV и стресса по нему не выполняется."
            refreshRelapseAnalytics()
        }
    }

    fun saveCurrentBleReadingToVault() {
        val w = wearableState.value
        val bpm = w.currentHeartRateBpm
        if (bpm <= 0) {
            _statusToast.value = "Сначала подключите Bluetooth-датчик и дождитесь измерения пульса"
            return
        }
        viewModelScope.launch {
            dao.insertPhysiologicalReading(
                PhysiologicalReadingEntity(
                    timestampMillis = System.currentTimeMillis(),
                    heartRateBpm = bpm.coerceIn(40, 220),
                    systolicBp = 0,
                    diastolicBp = 0,
                    stressIndex = 0,
                    hrvRmssdMs = w.currentHrvRmssdMs.coerceAtLeast(0),
                    sourceDevice = "${w.connectedDeviceName ?: "Bluetooth-датчик"} (пульс)",
                    isCriticalAlert = false,
                    encryptedNote = CryptoVault.encrypt("Замер с носимого датчика. Давление не измерялось; стресс-индекс не рассчитывается.")
                )
            )
            _statusToast.value = "Пульс ${bpm} уд/мин сохранён в журнал здоровья (давление не измерялось)"
            refreshRelapseAnalytics()
        }
    }

    fun refreshRelapseAnalytics(customQuery: String = "") {
        viewModelScope.launch {
            _isAnalyzingRisk.value = true
            val prof = _profile.value
            val report = analysisRepository.analyzeEmotionalStateAndRelapseRisk(
                soberDays = prof.soberDays,
                currentEmotion = prof.currentEmotion,
                diaryEntries = diaryEntries.value,
                vitalsReadings = vitalsReadings.value,
                toneMode = prof.toneMode,
                customUserQuery = customQuery
            )
            _relapseReport.value = report
            _isAnalyzingRisk.value = false
        }
    }

    fun recordMeditationOrPracticeCompleted(practiceName: String) {
        val nextCount = _profile.value.completedMeditationsCount + 1
        val updated = _profile.value.copy(completedMeditationsCount = nextCount)
        _profile.value = updated
        saveProfileToPrefs(updated)
        _statusToast.value = if (nextCount == 5) {
            "🏆 Достижение открыто: «Завершено 5 медитаций и практик»!"
        } else {
            "Засчитана практика «$practiceName» (Всего: $nextCount)"
        }
    }

    fun toggleMaterialStudied(materialId: String) {
        val current = _profile.value.studiedMaterialIds.toMutableSet()
        val added = if (current.contains(materialId)) {
            current.remove(materialId)
            false
        } else {
            current.add(materialId)
            true
        }
        val updated = _profile.value.copy(studiedMaterialIds = current)
        _profile.value = updated
        saveProfileToPrefs(updated)
        if (added) {
            _statusToast.value = if (current.size == 3) {
                "🏆 Достижение открыто: «Вооружен наукой (3 материала)»!"
            } else {
                "Материал отмечен изученным (${current.size} из ${ com.example.data.EducationAndAchievementsCatalog.educationalMaterials.size })"
            }
        }
    }

    fun addCommunitySupportPost(authorName: String, authorAge: Int, categoryTag: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            dao.insertCommunityPost(
                CommunityPostEntity(
                    authorName = authorName.ifBlank { _profile.value.userName },
                    authorAge = authorAge.coerceIn(18, 105),
                    soberDays = _profile.value.soberDays,
                    categoryTag = categoryTag,
                    messageText = text.trim(),
                    supportCount = 1,
                    isSupportedByMe = true
                )
            )
            val nextComm = _profile.value.userCommunityActionsCount + 1
            val updatedProf = _profile.value.copy(userCommunityActionsCount = nextComm)
            _profile.value = updatedProf
            saveProfileToPrefs(updatedProf)
            _statusToast.value = "Ваше сообщение опубликовано в ленте поддержки (Достижение «Первое сообщение в сообществе» активно!)"
        }
    }

    fun toggleSupportCommunityPost(post: CommunityPostEntity) {
        viewModelScope.launch {
            val updated = if (post.isSupportedByMe) {
                post.copy(
                    supportCount = (post.supportCount - 1).coerceAtLeast(0),
                    isSupportedByMe = false
                )
            } else {
                val nextComm = _profile.value.userCommunityActionsCount + 1
                val updatedProf = _profile.value.copy(userCommunityActionsCount = nextComm)
                _profile.value = updatedProf
                saveProfileToPrefs(updatedProf)
                post.copy(
                    supportCount = post.supportCount + 1,
                    isSupportedByMe = true
                )
            }
            dao.updateCommunityPost(updated)
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val appContext = context.applicationContext
                    val db = SobrietyDatabase.getInstance(appContext)
                    return SobrietyViewModel(
                        appContext = appContext,
                        dao = db.sobrietyDao(),
                        analysisRepository = OfflineSampleRelapseAnalysisRepository()
                    ) as T
                }
            }
    }
}
