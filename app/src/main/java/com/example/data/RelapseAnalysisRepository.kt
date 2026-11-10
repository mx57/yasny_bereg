package com.example.data

import com.example.data.local.EmotionalState
import com.example.data.local.PhysiologicalReadingEntity
import com.example.data.local.TherapyToneMode
import com.example.data.local.WellnessDiaryEntity
import com.example.security.CryptoVault
import kotlinx.coroutines.delay

data class RelapseRiskReport(
    val riskScorePercent: Int, // 0..100
    val riskLevelLabel: String, // "Низкий", "Умеренный", "Высокий", "Критический"
    val primaryTriggersDetected: List<String>,
    val clinicalSummary: String,
    val immediateActionPlan: List<String>,
    val recommendedSupplementFocus: String,
    val isOfflineSampleResponse: Boolean = true,
    val firebaseNotice: String = "Примечание: Облачная генерация через Firebase AI Logic (Gemini) отключена, так как настройка Firebase была отклонена. Показан демонстрационный клинический разбор на основе ваших локальных показателей."
)

/**
 * Single interface for all emotional state and relapse-prevention AI analytics calls.
 */
interface RelapseAnalysisRepository {
    suspend fun analyzeEmotionalStateAndRelapseRisk(
        soberDays: Int,
        currentEmotion: EmotionalState,
        diaryEntries: List<WellnessDiaryEntity>,
        vitalsReadings: List<PhysiologicalReadingEntity>,
        toneMode: TherapyToneMode,
        customUserQuery: String = ""
    ): RelapseRiskReport
}

/**
 * Offline placeholder & local clinical scoring implementation used when Firebase setup is declined.
 */
class OfflineSampleRelapseAnalysisRepository : RelapseAnalysisRepository {

    override suspend fun analyzeEmotionalStateAndRelapseRisk(
        soberDays: Int,
        currentEmotion: EmotionalState,
        diaryEntries: List<WellnessDiaryEntity>,
        vitalsReadings: List<PhysiologicalReadingEntity>,
        toneMode: TherapyToneMode,
        customUserQuery: String
    ): RelapseRiskReport {
        delay(450L)

        val latestDiary = diaryEntries.firstOrNull()
        val latestVital = vitalsReadings.firstOrNull()

        val craving = latestDiary?.cravingLevel ?: 2
        val sleepHours = latestDiary?.sleepHours ?: 7.5f
        val heartRate = latestVital?.heartRateBpm ?: 72
        val stressIndex = latestVital?.stressIndex ?: 35

        // Compute composite risk score (0..100)
        var risk = currentEmotion.riskWeight * 5
        risk += craving * 4
        if (sleepHours < 6.0f) risk += 15
        if (heartRate >= 95) risk += 12
        if (stressIndex >= 70) risk += 15
        if (soberDays in 0..7) risk += 10
        risk = risk.coerceIn(5, 98)

        val riskLabel = when {
            risk >= 75 -> "Критический (Требуется экстренная защита от срыва)"
            risk >= 50 -> "Повышенный (Внимание к триггерам)"
            risk >= 30 -> "Умеренный (Стабильная адаптация)"
            else -> "Низкий (Устойчивое равновесие)"
        }

        val triggers = mutableListOf<String>()
        if (craving >= 6) triggers.add("Повышенная субъективная тяга ($craving/10 баллов)")
        if (sleepHours < 6.5f) triggers.add("Дефицит сна (${String.format("%.1f", sleepHours)} ч) — фактор истощения HALT")
        if (heartRate >= 90 || stressIndex >= 65) {
            triggers.add("Вегетативное напряжение (Пульс $heartRate уд/мин, стресс $stressIndex%)")
        }
        if (currentEmotion == EmotionalState.LONELY || currentEmotion == EmotionalState.ANXIOUS || currentEmotion == EmotionalState.CRISIS) {
            triggers.add("Эмоциональный фактор: ${currentEmotion.titleRu}")
        }
        if (triggers.isEmpty()) {
            triggers.add("Острых триггеров не выявлено — ровный фон восстановления")
        }

        val latestNote = latestDiary?.let { CryptoVault.decrypt(it.encryptedComment) }.orEmpty()
        val queryContext = if (customUserQuery.isNotBlank()) {
            " Ваш запрос («$customUserQuery») учтен в плане стабилизации."
        } else if (latestNote.isNotBlank()) {
            " Запись дневника («${latestNote.take(80)}») указывает на важность соблюдения режима отдыха."
        } else {
            ""
        }

        val summary = when (toneMode) {
            TherapyToneMode.TOUGH ->
                "[Образец аналитического заключения] Стаж трезвости: $soberDays дн. Индекс риска срыва: $risk%. " +
                    "Внимание: вегетативная нервная система находится под нагрузкой (ЧСС $heartRate уд/мин, стресс $stressIndex%). " +
                    "Любая попытка «снять напряжение» алкоголем сейчас вызовет резкий выброс ацетальдегида, скачок давления и гарантированный запой.$queryContext"
            else ->
                "[Образец аналитического заключения] Стаж трезвости: $soberDays дн. Расчетный индекс риска: $risk%. " +
                    "Ваше текущее состояние («${currentEmotion.titleRu}») закономерно для данного этапа нейрохимической перестройки. " +
                    "Показатели пульса ($heartRate уд/мин) и сна (${String.format("%.1f", sleepHours)} ч) требуют бережного режима и исключения перегрузок.$queryContext"
        }

        val actionPlan = buildList {
            if (risk >= 60) {
                add("Немедленно выполните дыхательный цикл 4-7-8 (вкладка «Методы») в течение 3 минут для снижения ЧСС.")
                add("Выпейте 400 мл теплой воды и плотно поешьте (повышение глюкозы снижает тягу на 40%).")
                add("Примените технику НЛП «Взмах» или включите «Жесткий режим», чтобы заблокировать торг с зависимостью.")
            } else {
                add("Продолжайте вести зашифрованный дневник самочувствия утром и вечером.")
                add("Совершите спокойную 30-минутную прогулку на свежем воздухе перед сном.")
                add("Прочитайте сегодняшнюю историю успеха в разделе «Мотивация» и поддержите участников сообщества.")
            }
        }

        val supplementFocus = when {
            stressIndex >= 65 || currentEmotion == EmotionalState.ANXIOUS || currentEmotion == EmotionalState.CRISIS ->
                "Фокус нутрицевтиков: Магний бисглицинат (300–400 мг вечером) + L-Теанин (200 мг) для снятия вегетативной тревоги (по согласованию с врачом)."
            craving >= 5 ->
                "Фокус нутрицевтиков: N-Ацетилцистеин (NAC 600 мг) и экстракт Кудзу для выравнивания обмена глутамата и снижения навязчивой тяги."
            else ->
                "Фокус нутрицевтиков: Витамин B1 (Тиамин) + Омега-3 ПНЖК и Силимарин для плановой регенерации нервной ткани и печени."
        }

        return RelapseRiskReport(
            riskScorePercent = risk,
            riskLevelLabel = riskLabel,
            primaryTriggersDetected = triggers,
            clinicalSummary = summary,
            immediateActionPlan = actionPlan,
            recommendedSupplementFocus = supplementFocus,
            isOfflineSampleResponse = true
        )
    }
}
