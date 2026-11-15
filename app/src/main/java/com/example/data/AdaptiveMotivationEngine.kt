package com.example.data

import com.example.data.local.EmotionalState
import com.example.data.local.MotivationCategory
import com.example.data.local.TherapyToneMode


data class MotivationalItem(
    val id: String,
    val category: MotivationCategory,
    val title: String,
    val authorOrHero: String,
    val authorRoleOrAge: String,
    val content: String,
    val practicalTakeaway: String,
    val minSoberDays: Int,
    val maxSoberDays: Int,
    val targetEmotions: Set<EmotionalState>,
    val toughModeMessage: String? = null,
    val subCategoryTag: String = "Общее"
)

data class AdaptedMotivationBundle(
    val dailyMessage: MotivationalItem,
    val famousQuote: MotivationalItem,
    val realStory: MotivationalItem,
    val adaptationReason: String,
    val allMatchedItems: List<MotivationalItem>
)

object AdaptiveMotivationEngine {
    /** All displayed cards are original copy or explicitly labeled composite examples. */
    val allItems: List<MotivationalItem> = ExpandedRecoveryContent.motivationalItems

    fun selectAdaptedBundle(
        soberDays: Int,
        currentEmotion: EmotionalState,
        cravingLevel: Int,
        stressIndex: Int,
        toneMode: TherapyToneMode
    ): AdaptedMotivationBundle {
        val effectiveEmotion = when {
            cravingLevel >= 7 || stressIndex >= 75 -> EmotionalState.CRISIS
            cravingLevel >= 5 || stressIndex >= 60 -> EmotionalState.ANXIOUS
            else -> currentEmotion
        }

        fun score(item: MotivationalItem): Int {
            var value = if (soberDays in item.minSoberDays..item.maxSoberDays) 50 else 0
            if (effectiveEmotion in item.targetEmotions) value += 45
            if (toneMode != TherapyToneMode.GENTLE && item.subCategoryTag == "Трудный момент") value += 10
            return value
        }

        val daily = allItems.filter { it.category == MotivationCategory.DAILY_MESSAGE }.sortedByDescending(::score)
        val thoughts = allItems.filter { it.category == MotivationCategory.FAMOUS_QUOTE }.sortedByDescending(::score)
        val examples = allItems.filter { it.category == MotivationCategory.REAL_STORY }.sortedByDescending(::score)
        val stage = when {
            soberDays < 7 -> "первые шаги"
            soberDays < 30 -> "личный план поддержки"
            soberDays < 90 -> "укрепление выбранных привычек"
            else -> "долгосрочная поддержка"
        }
        val cravingNote = if (cravingLevel in 0..10) "тяга $cravingLevel/10 по вашей записи" else "оценка тяги не внесена"
        val reason = "Подборка для этапа «$stage» • состояние: «${effectiveEmotion.titleRu}» • $cravingNote"

        return AdaptedMotivationBundle(
            dailyMessage = daily.first(),
            famousQuote = thoughts.first(),
            realStory = examples.first(),
            adaptationReason = reason,
            allMatchedItems = (daily + thoughts + examples).sortedByDescending(::score)
        )
    }
}
