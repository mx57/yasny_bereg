package com.example

import com.example.data.AdaptiveMotivationEngine
import com.example.data.EducationAndAchievementsCatalog
import com.example.data.EducationalTopic
import com.example.data.local.EmotionalState
import com.example.data.local.TherapyToneMode
import com.example.security.CryptoVault
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `adaptive motivation engine selects messages quotes and stories by progress and emotion`() {
        val bundle = AdaptiveMotivationEngine.selectAdaptedBundle(
            soberDays = 7,
            currentEmotion = EmotionalState.ANXIOUS,
            cravingLevel = 6,
            stressIndex = 65,
            toneMode = TherapyToneMode.ADAPTIVE
        )
        assertNotNull(bundle.dailyMessage)
        assertNotNull(bundle.famousQuote)
        assertNotNull(bundle.realStory)
        assertTrue(bundle.adaptationReason.contains("7 дн."))
    }

    @Test
    fun `education catalog covers all 6 scientific recovery topics`() {
        val topicsCovered = EducationAndAchievementsCatalog.educationalMaterials.map { it.topic }.toSet()
        assertEquals(EducationalTopic.entries.toSet(), topicsCovered)
    }

    @Test
    fun `achievements system unlocks milestones accurately`() {
        val achievements = EducationAndAchievementsCatalog.buildAchievementsList(
            soberDays = 12,
            completedMeditationsCount = 5,
            studiedMaterialsCount = 3,
            communityInteractionsCount = 2,
            diaryAndVitalsCount = 4
        )
        val sevenDays = achievements.first { it.id == "ach_7_days" }
        val fiveMeditations = achievements.first { it.id == "ach_5_meditations" }
        val firstCommunityPost = achievements.first { it.id == "ach_first_community_post" }
        assertTrue(sevenDays.isUnlocked)
        assertTrue(fiveMeditations.isUnlocked)
        assertTrue(firstCommunityPost.isUnlocked)
    }

    @Test
    fun `crypto vault encrypts and decrypts medical diary notes`() {
        val original = "Пульс 68 уд/мин, сон крепкий, тяги к алкоголю нет"
        val encrypted = CryptoVault.encrypt(original)
        val decrypted = CryptoVault.decrypt(encrypted)
        assertEquals(original, decrypted)
    }
}
