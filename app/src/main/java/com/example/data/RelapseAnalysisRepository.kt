package com.example.data

import com.example.data.local.EmotionalState
import com.example.data.local.PhysiologicalReadingEntity
import com.example.data.local.TherapyToneMode
import com.example.data.local.WellnessDiaryEntity
import kotlinx.coroutines.delay

data class RelapseRiskReport(
    val attentionLevel: String,
    val primaryTriggersDetected: List<String>,
    val supportSummary: String,
    val immediateActionPlan: List<String>,
    val safetyNote: String,
    val isClinicalAssessment: Boolean = false
)

/** Local reflection only; the interface never claims to calculate clinical relapse probability. */
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

class OfflineSampleRelapseAnalysisRepository : RelapseAnalysisRepository {
    override suspend fun analyzeEmotionalStateAndRelapseRisk(
        soberDays: Int,
        currentEmotion: EmotionalState,
        diaryEntries: List<WellnessDiaryEntity>,
        vitalsReadings: List<PhysiologicalReadingEntity>,
        toneMode: TherapyToneMode,
        customUserQuery: String
    ): RelapseRiskReport {
        delay(120L)
        val latestDiary = diaryEntries.firstOrNull()
        val craving = latestDiary?.cravingLevel
        val triggers = buildList {
            if (craving != null) add("В дневнике отмечена тяга: $craving/10 — ваша личная оценка")
            if (currentEmotion in setOf(EmotionalState.CRISIS, EmotionalState.ANXIOUS, EmotionalState.LONELY, EmotionalState.TIRED)) {
                add("Вы отметили состояние «${currentEmotion.titleRu}»")
            }
            if (customUserQuery.isNotBlank()) add("Вы указали ситуацию для личного разбора")
            if (vitalsReadings.isNotEmpty()) add("В дневнике есть измерения; приложение не оценивает их клиническую значимость")
            if (isEmpty()) add("Подробных заметок пока нет — это не означает отсутствия риска")
        }
        val attentionLevel = when {
            craving != null && craving >= 7 -> "Вы отметили сильную тягу"
            currentEmotion == EmotionalState.CRISIS -> "Вы попросили о поддержке"
            triggers.any { it.contains("тревож") || it.contains("одиноч") } -> "Можно запланировать поддержку"
            else -> "Заметки для разговора"
        }
        val summary = when {
            customUserQuery.isNotBlank() -> "Ваш текст остаётся на устройстве. Используйте его как заметку или вопрос специалисту; приложение не ставит диагноз и не прогнозирует употребление."
            else -> "Это подборка по вашим отметкам, а не процент вероятности, медицинская оценка или прогноз. Если данные кажутся тревожными, обсудите их со специалистом."
        }
        val actionPlan = listOf(
            "Выберите один безопасный шаг: связаться с надёжным человеком, врачом или службой поддержки.",
            "Если регулярно употребляете много алкоголя или ранее были симптомы отмены, до прекращения обсудите план с медицинским специалистом.",
            "При судорогах, галлюцинациях, спутанности сознания, потере сознания или проблемах с дыханием вызывайте местную экстренную помощь."
        )
        return RelapseRiskReport(
            attentionLevel = attentionLevel,
            primaryTriggersDetected = triggers,
            supportSummary = summary,
            immediateActionPlan = actionPlan,
            safetyNote = "Добавки не назначаются по этому анализу. Не заменяйте им консультацию и безопасный медицинский план отмены."
        )
    }
}
