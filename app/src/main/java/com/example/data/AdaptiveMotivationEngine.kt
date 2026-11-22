package com.example.data

import com.example.data.local.EmotionalState
import com.example.data.local.MotivationCategory
import com.example.data.local.TherapyToneMode
import java.util.Calendar


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

    /** How many of the best-matching cards participate in the daily rotation. */
    private const val ROTATION_POOL_SIZE = 7

    private fun currentDayOfYear(): Int = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)

    /**
     * Picks the [dayIndex]-th element of the pool so the content rotates once per day
     * instead of freezing on a single top-scored card until the user's state changes.
     */
    private fun rotate(pool: List<MotivationalItem>, dayOfYear: Int, offset: Int): MotivationalItem {
        if (pool.isEmpty()) {
            return allItems.firstOrNull()
                ?: MotivationalItem(
                    id = "fallback_empty",
                    category = MotivationCategory.DAILY_MESSAGE,
                    title = "Поддержка рядом",
                    authorOrHero = "Редакция «Ясный Берег»",
                    authorRoleOrAge = "Авторское послание поддержки",
                    content = "Выберите один безопасный следующий шаг и, если нужно, попросите о поддержке.",
                    practicalTakeaway = "Напишите надёжному человеку или запишите вопрос для специалиста.",
                    minSoberDays = 0,
                    maxSoberDays = 3650,
                    targetEmotions = emptySet()
                )
        }
        return pool[Math.floorMod(dayOfYear + offset, pool.size)]
    }

    fun selectAdaptedBundle(
        soberDays: Int,
        currentEmotion: EmotionalState,
        cravingLevel: Int,
        stressIndex: Int,
        toneMode: TherapyToneMode,
        dayOfYear: Int = -1
    ): AdaptedMotivationBundle {
        val rotationDay = if (dayOfYear in 1..366) dayOfYear else currentDayOfYear()

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
        val reason = "День $soberDays • этап «$stage» • состояние «${effectiveEmotion.titleRu}» • $cravingNote"

        return AdaptedMotivationBundle(
            dailyMessage = rotate(daily.take(ROTATION_POOL_SIZE), rotationDay, 0),
            famousQuote = rotate(thoughts.take(ROTATION_POOL_SIZE), rotationDay, 1),
            realStory = rotate(examples.take(ROTATION_POOL_SIZE), rotationDay, 2),
            adaptationReason = reason,
            allMatchedItems = (daily + thoughts + examples).sortedByDescending(::score)
        )
    }
}
