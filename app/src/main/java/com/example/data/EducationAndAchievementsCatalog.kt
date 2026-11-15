package com.example.data

enum class EducationalFormat(val labelRu: String) {
    ARTICLE("Статья"),
    VIDEO("Видео-лекция"),
    INFOGRAPHIC("Инфографика")
}

enum class EducationalTopic(val titleRu: String) {
    TRIGGERS("Триггеры"),
    MECHANISMS("Понимание зависимости"),
    CRAVING_STRATEGIES("Навыки и поддержка"),
    SOCIAL_SUPPORT("Поддержка"),
    HEALTHY_HABITS("Здоровые привычки"),
    RELAPSE_RECOVERY("Восстановление"),
    WITHDRAWAL_SAFETY("Безопасность при отмене"),
    TREATMENT_OPTIONS("Варианты помощи"),
    MEDICATIONS("Лекарства и взаимодействия"),
    SLEEP("Сон"),
    NUTRITION("Питание")
}

data class InfographicMetric(
    val label: String,
    val valueText: String,
    val fraction: Float,
    val commentary: String
)

data class VideoChapter(
    val timeCode: String,
    val chapterTitle: String,
    val slideSummary: String
)

data class EducationalMaterial(
    val id: String,
    val format: EducationalFormat,
    val topic: EducationalTopic,
    val title: String,
    val durationOrReadTime: String,
    val summary: String,
    val bodyParagraphs: List<String>,
    val keyScientificFacts: List<String>,
    val infographicMetrics: List<InfographicMetric> = emptyList(),
    val videoChapters: List<VideoChapter> = emptyList(),
    val sources: List<ContentSource> = emptyList()
)

data class AchievementMilestone(
    val id: String,
    val title: String,
    val description: String,
    val rewardTitle: String,
    val currentValue: Int,
    val targetValue: Int,
    val categoryLabel: String
) {
    val isUnlocked: Boolean
        get() = currentValue >= targetValue

    val progressFraction: Float
        get() = if (targetValue <= 0) 1f else (currentValue.toFloat() / targetValue.toFloat()).coerceIn(0f, 1f)
}

object EducationAndAchievementsCatalog {
    /** Thirty-five clinically cautious, source-linked articles replace six misleading legacy materials. */
    val educationalMaterials: List<EducationalMaterial> = ExpandedRecoveryContent.educationalMaterials

    fun buildAchievementsList(
        soberDays: Int,
        completedMeditationsCount: Int,
        studiedMaterialsCount: Int,
        communityInteractionsCount: Int,
        diaryAndVitalsCount: Int
    ): List<AchievementMilestone> = listOf(
        AchievementMilestone(
            id = "ach_24_hours",
            title = "Первый день в личном плане",
            description = "Отметить выбранный вами безопасный шаг и поддержку",
            rewardTitle = "Знак «Решимость»",
            currentValue = soberDays.coerceAtLeast(0),
            targetValue = 1,
            categoryLabel = "Личный путь"
        ),
        AchievementMilestone(
            id = "ach_7_days",
            title = "Неделя заботы о себе",
            description = "Продолжать пользоваться подходящей вам поддержкой",
            rewardTitle = "Знак «Первый этап»",
            currentValue = soberDays.coerceAtLeast(0),
            targetValue = 7,
            categoryLabel = "Личный путь"
        ),
        AchievementMilestone(
            id = "ach_14_days",
            title = "Две недели выбранного плана",
            description = "Замечать полезные шаги без обещаний медицинского результата по срокам",
            rewardTitle = "Знак «Ясный горизонт»",
            currentValue = soberDays.coerceAtLeast(0),
            targetValue = 14,
            categoryLabel = "Личный путь"
        ),
        AchievementMilestone(
            id = "ach_30_days",
            title = "Месяц последовательных шагов",
            description = "Отмечать поддержку и обсуждать дальнейший план со специалистом",
            rewardTitle = "Знак «Продолжение»",
            currentValue = soberDays.coerceAtLeast(0),
            targetValue = 30,
            categoryLabel = "Личный путь"
        ),
        AchievementMilestone(
            id = "ach_first_community_post",
            title = "Первый шаг к поддержке",
            description = "Попросить о помощи или поддержать человека, если это безопасно для вас",
            rewardTitle = "Знак «Связь»",
            currentValue = communityInteractionsCount,
            targetValue = 1,
            categoryLabel = "Социальная поддержка"
        ),
        AchievementMilestone(
            id = "ach_5_meditations",
            title = "Пять выполненных практик",
            description = "Попробовать навыки самоподдержки по собственному выбору",
            rewardTitle = "Знак «Практика»",
            currentValue = completedMeditationsCount,
            targetValue = 5,
            categoryLabel = "Практики"
        ),
        AchievementMilestone(
            id = "ach_3_education_studied",
            title = "Три полезных материала",
            description = "Изучить информацию и задать специалисту вопросы, если они возникли",
            rewardTitle = "Знак «Знание»",
            currentValue = studiedMaterialsCount,
            targetValue = 3,
            categoryLabel = "Образование"
        ),
        AchievementMilestone(
            id = "ach_diary_keeper",
            title = "Пять заметок о самочувствии",
            description = "Собирать наблюдения для личного дневника или разговора с врачом",
            rewardTitle = "Знак «Наблюдение»",
            currentValue = diaryAndVitalsCount,
            targetValue = 5,
            categoryLabel = "Самонаблюдение"
        )
    )
}
