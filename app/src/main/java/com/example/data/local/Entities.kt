package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class EmotionalState(
    val titleRu: String,
    val subtitleRu: String,
    val riskWeight: Int
) {
    CRISIS("Острая тяга / Кризис", "Сильное желание выпить прямо сейчас", 9),
    ANXIOUS("Тревога и напряжение", "Раздражительность, внутреннее беспокойство", 7),
    LONELY("Одиночество и тоска", "Чувство изоляции, жалость к себе", 6),
    TIRED("Усталость и истощение", "Упадок сил, плохой сон, апатия", 5),
    CALM("Спокойствие и равновесие", "Ровное состояние, контроль над днем", 2),
    PROUD("Уверенность и подъем", "Гордость за трезвость, ясность ума", 1)
}

enum class MotivationCategory(val labelRu: String) {
    DAILY_MESSAGE("Послание дня"),
    FAMOUS_QUOTE("Цитаты великих"),
    REAL_STORY("Истории людей")
}

enum class TherapyToneMode(
    val titleRu: String,
    val descriptionRu: String
) {
    GENTLE(
        "Мягкая поддержка",
        "Бережная когнитивно-поведенческая терапия, сочувствие и пошаговое восстановление"
    ),
    ADAPTIVE(
        "Адаптивный баланс",
        "Автоматическое переключение от теплого участия к твердой дисциплине при росте тяги"
    ),
    TOUGH(
        "Жесткая правда (Демотивация)",
        "Прямые клинические, финансовые и социальные последствия алкоголя без самообмана"
    )
}

@Entity(tableName = "wellness_diary_entries")
data class WellnessDiaryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long = System.currentTimeMillis(),
    val emotionalStateName: String = EmotionalState.CALM.name,
    val cravingLevel: Int = 2, // 0..10
    val sleepHours: Float = 7.5f,
    val encryptedSymptoms: String = "",
    val encryptedComment: String = "",
    val photoUriString: String? = null,
    val tookSupplements: Boolean = false
)

@Entity(tableName = "physiological_readings")
data class PhysiologicalReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMillis: Long = System.currentTimeMillis(),
    val heartRateBpm: Int,
    val systolicBp: Int = 120,
    val diastolicBp: Int = 80,
    val stressIndex: Int, // 0..100
    val hrvRmssdMs: Int, // Heart rate variability estimate in ms
    val sourceDevice: String, // e.g., "BLE Пульсометр Polar H10" or "Ручной замер"
    val isCriticalAlert: Boolean = false,
    val encryptedNote: String = ""
)

@Entity(tableName = "motivational_bookmarks")
data class MotivationalBookmarkEntity(
    @PrimaryKey val itemId: String,
    val bookmarkedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_support_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorName: String,
    val authorAge: Int,
    val soberDays: Int,
    val categoryTag: String,
    val messageText: String,
    val supportCount: Int = 0,
    val isSupportedByMe: Boolean = false,
    val timestampMillis: Long = System.currentTimeMillis()
)
