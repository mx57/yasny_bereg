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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

enum class AppTab(val titleRu: String) {
    HOME("Главная"),
    MOTIVATION("Мотивация"),
    METHODS("Методы"),
    DIARY("Дневник"),
    HEALTH("Здоровье")
}

data class UserProfileState(
    val userName: String = "Павел Николаевич",
    val sobrietyStartMillis: Long = System.currentTimeMillis() - 12L * 24L * 3600_000L, // Default 12 days sober
    val dailySpendRub: Int = 650,
    val dailyPureEthanolMl: Int = 95,
    val currentEmotion: EmotionalState = EmotionalState.CALM,
    val toneMode: TherapyToneMode = TherapyToneMode.ADAPTIVE,
    val elderlyLargeFont: Boolean = true,
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0,
    val dailyNotificationsEnabled: Boolean = true,
    val checkedSupplementIds: Set<String> = setOf("supp_b1_thiamine", "supp_magnesium"),
    val completedMeditationsCount: Int = 3,
    val studiedMaterialIds: Set<String> = setOf("edu_triggers_infographic"),
    val userCommunityActionsCount: Int = 1
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

    val adaptedMotivation: StateFlow<AdaptedMotivationBundle> = combine(
        _profile,
        diaryEntries,
        vitalsReadings
    ) { prof, diary, vitals ->
        val latestCraving = diary.firstOrNull()?.cravingLevel ?: 2
        val latestStress = vitals.firstOrNull()?.stressIndex ?: 35
        AdaptiveMotivationEngine.selectAdaptedBundle(
            soberDays = prof.soberDays,
            currentEmotion = prof.currentEmotion,
            cravingLevel = latestCraving,
            stressIndex = latestStress,
            toneMode = prof.toneMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = AdaptiveMotivationEngine.selectAdaptedBundle(
            soberDays = _profile.value.soberDays,
            currentEmotion = _profile.value.currentEmotion,
            cravingLevel = 2,
            stressIndex = 35,
            toneMode = _profile.value.toneMode
        )
    )

    init {
        NotificationHelper.ensureChannelsCreated(appContext)
        seedInitialDataIfNeeded()
        refreshRelapseAnalytics()
    }

    private fun loadProfileFromPrefs(): UserProfileState {
        val defaultStart = System.currentTimeMillis() - 12L * 24L * 3600_000L
        val startMillis = prefs.getLong("sobriety_start_millis", defaultStart)
        val spend = prefs.getInt("daily_spend_rub", 650)
        val ethanol = prefs.getInt("daily_ethanol_ml", 95)
        val emotionName = prefs.getString("current_emotion", EmotionalState.CALM.name) ?: EmotionalState.CALM.name
        val toneName = prefs.getString("tone_mode", TherapyToneMode.ADAPTIVE.name) ?: TherapyToneMode.ADAPTIVE.name
        val largeFont = prefs.getBoolean("elderly_large_font", true)
        val notifHour = prefs.getInt("notif_hour", 9)
        val notifMin = prefs.getInt("notif_minute", 0)
        val notifEnabled = prefs.getBoolean("notif_enabled", true)
        val supps = prefs.getStringSet("checked_supplements", null)?.toSet()
            ?: setOf("supp_b1_thiamine", "supp_magnesium")
        val meditations = prefs.getInt("completed_meditations_count", 3)
        val studied = prefs.getStringSet("studied_materials", null)?.toSet()
            ?: setOf("edu_triggers_infographic")
        val commActions = prefs.getInt("user_community_actions", 1)

        return UserProfileState(
            sobrietyStartMillis = startMillis,
            dailySpendRub = spend,
            dailyPureEthanolMl = ethanol,
            currentEmotion = runCatching { EmotionalState.valueOf(emotionName) }.getOrDefault(EmotionalState.CALM),
            toneMode = runCatching { TherapyToneMode.valueOf(toneName) }.getOrDefault(TherapyToneMode.ADAPTIVE),
            elderlyLargeFont = largeFont,
            notificationHour = notifHour,
            notificationMinute = notifMin,
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
            .putBoolean("notif_enabled", state.dailyNotificationsEnabled)
            .putStringSet("checked_supplements", state.checkedSupplementIds)
            .putInt("completed_meditations_count", state.completedMeditationsCount)
            .putStringSet("studied_materials", state.studiedMaterialIds)
            .putInt("user_community_actions", state.userCommunityActionsCount)
            .apply()
    }

    private fun seedInitialDataIfNeeded() {
        viewModelScope.launch {
            if (dao.countCommunityPosts() == 0) {
                val now = System.currentTimeMillis()
                dao.insertCommunityPosts(
                    listOf(
                        CommunityPostEntity(
                            authorName = "Михаил Федорович",
                            authorAge = 66,
                            soberDays = 184,
                            categoryTag = "Поддержка старшего поколения",
                            messageText = "Ребята и ровесники, держитесь! Мне 66 лет, пил почти сорок лет с перерывами. Сегодня ровно полгода полной трезвости. Утром прошел 5 километров по парку, давление 126 на 78 без таблеток. Главное — пережить первые две недели и не слушать уговоры старых собутыльников.",
                            supportCount = 42,
                            timestampMillis = now - 3_600_000L * 3
                        ),
                        CommunityPostEntity(
                            authorName = "Анна Викторовна",
                            authorAge = 54,
                            soberDays = 31,
                            categoryTag = "Месяц без алкоголя",
                            messageText = "Сегодня мой 30-й трезвый день! Очень помог дневник самочувствия и вечерний магний с травяным сбором. Когда на 10-й день накрыла тоска, прочитала здесь историю Виктора Павловича и расплакалась — не пошла в магазин. Спасибо каждому из вас!",
                            supportCount = 29,
                            timestampMillis = now - 3_600_000L * 8
                        ),
                        CommunityPostEntity(
                            authorName = "Дмитрий",
                            authorAge = 44,
                            soberDays = 7,
                            categoryTag = "Первая неделя",
                            messageText = "Седьмой день позади. Первые трое суток руки ходили ходуном, пульс зашкаливал. Дышал по квадрату 4-7-8 и включал жесткую демотивацию каждый раз, когда тянуло к ларьку. Сегодня впервые нормально поспал 7 часов!",
                            supportCount = 35,
                            timestampMillis = now - 3_600_000L * 15
                        )
                    )
                )

                // Seed 2 initial encrypted diary & physiological readings so charts & doctor report have immediate context
                dao.insertDiaryEntry(
                    WellnessDiaryEntity(
                        timestampMillis = now - 86_400_000L,
                        emotionalStateName = EmotionalState.CALM.name,
                        cravingLevel = 2,
                        sleepHours = 7.5f,
                        encryptedSymptoms = CryptoVault.encrypt("Утренняя ясность, тремора нет, аппетит хороший"),
                        encryptedComment = CryptoVault.encrypt("Вечером гулял в парке, принял витамин B1 и магний. Тяги почти не было."),
                        tookSupplements = true
                    )
                )
                dao.insertPhysiologicalReading(
                    PhysiologicalReadingEntity(
                        timestampMillis = now - 43_200_000L,
                        heartRateBpm = 68,
                        systolicBp = 124,
                        diastolicBp = 78,
                        stressIndex = 32,
                        hrvRmssdMs = 46,
                        sourceDevice = "Утренний контроль в покое",
                        isCriticalAlert = false,
                        encryptedNote = CryptoVault.encrypt("Спокойный пульс после полноценного сна")
                    )
                )
            }
        }
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
        if (emotion == EmotionalState.CRISIS) {
            triggerCriticalStateNotification(
                "Зафиксирована острая тяга к алкоголю",
                "Включен протокол защиты от срыва. Выпейте 2 стакана воды и откройте экстренную технику НЛП или дыхания."
            )
        }
        refreshRelapseAnalytics()
    }

    fun updateTherapyToneMode(mode: TherapyToneMode) {
        val updated = _profile.value.copy(toneMode = mode)
        _profile.value = updated
        saveProfileToPrefs(updated)
        refreshRelapseAnalytics()
    }

    fun updateSobrietyDaysCount(daysAgo: Int, dailySpendRub: Int) {
        val newStart = System.currentTimeMillis() - daysAgo.coerceAtLeast(0).toLong() * 24L * 3600_000L
        val updated = _profile.value.copy(
            sobrietyStartMillis = newStart,
            dailySpendRub = dailySpendRub.coerceAtLeast(100)
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

    fun updateDailyNotificationSchedule(hour: Int, minute: Int, enabled: Boolean) {
        val updated = _profile.value.copy(
            notificationHour = hour.coerceIn(0, 23),
            notificationMinute = minute.coerceIn(0, 59),
            dailyNotificationsEnabled = enabled
        )
        _profile.value = updated
        saveProfileToPrefs(updated)
        if (enabled) {
            val currentMsg = adaptedMotivation.value.dailyMessage
            NotificationHelper.scheduleDailyMotivation(
                context = appContext,
                hourOfDay = updated.notificationHour,
                minute = updated.notificationMinute,
                title = "Ясный Берег • ${currentMsg.title}",
                body = currentMsg.content
            )
            _statusToast.value = String.format(
                "Ежедневные уведомления настроены на %02d:%02d",
                updated.notificationHour,
                updated.notificationMinute
            )
        } else {
            _statusToast.value = "Ежедневные уведомления отключены"
        }
    }

    fun sendAdaptiveMotivationalNotificationNow() {
        val bundle = adaptedMotivation.value
        val sent = NotificationHelper.sendNotificationNow(
            context = appContext,
            title = "Послание дня (${_profile.value.soberDays} дн. трезвости): ${bundle.dailyMessage.title}",
            body = "${bundle.dailyMessage.content}\n\nЦитата дня: ${bundle.famousQuote.authorOrHero} — ${bundle.famousQuote.content}",
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

            if (cravingLevel >= 7 || emotion == EmotionalState.CRISIS) {
                triggerCriticalStateNotification(
                    "Высокий риск срыва (Тяга $cravingLevel/10)",
                    "Система зафиксировала опасный уровень тяги в дневнике. Немедленно выполните технику «Серфинг тяги» или включите «Жесткий режим»."
                )
            } else {
                _statusToast.value = "Запись зашифрована (AES-256) и сохранена в оффлайн-дневник"
            }
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
            val cleanBpm = heartRateBpm.coerceIn(40, 220)
            val cleanSys = systolicBp.coerceIn(70, 250)
            val cleanDia = diastolicBp.coerceIn(40, 150)
            val (stress, hrv) = BleHeartRateManager.calculateStressAndHrvFromBpm(cleanBpm)
            val isCritical = cleanBpm >= 100 || cleanSys >= 155 || stress >= 80

            val entity = PhysiologicalReadingEntity(
                timestampMillis = System.currentTimeMillis(),
                heartRateBpm = cleanBpm,
                systolicBp = cleanSys,
                diastolicBp = cleanDia,
                stressIndex = stress,
                hrvRmssdMs = hrv,
                sourceDevice = sourceDevice,
                isCriticalAlert = isCritical,
                encryptedNote = CryptoVault.encrypt(notePlain.trim())
            )
            dao.insertPhysiologicalReading(entity)

            if (isCritical) {
                triggerCriticalStateNotification(
                    "Критический физиологический стресс (ЧСС $cleanBpm, АД $cleanSys/$cleanDia, Стресс $stress%)",
                    "Обнаружена вегетативная перегрузка! Высокий пульс и давление часто провоцируют срыв. Сядьте, выпейте воды и выполните медленное дыхание."
                )
            } else {
                _statusToast.value = "Показатели сохранены и зашифрованы (Индекс стресса: $stress%)"
            }
            refreshRelapseAnalytics()
        }
    }

    fun saveCurrentBleReadingToVault() {
        val w = wearableState.value
        if (w.currentHeartRateBpm > 0) {
            addPhysiologicalReading(
                heartRateBpm = w.currentHeartRateBpm,
                systolicBp = 125,
                diastolicBp = 80,
                sourceDevice = w.connectedDeviceName ?: "BLE Носимый пульсометр",
                notePlain = "Замер с носимого датчика по Bluetooth LE"
            )
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
