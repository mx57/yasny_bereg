package com.example

import com.example.data.AdaptiveMotivationEngine
import com.example.data.EducationAndAchievementsCatalog
import com.example.data.MethodsAndScienceCatalog
import com.example.sensors.BleHeartRateManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `methods and supplements catalogs are populated with expanded trainers`() {
        assertTrue(MethodsAndScienceCatalog.therapyTechniques.size >= 100)
        assertTrue(MethodsAndScienceCatalog.supplementsGuide.size >= 20)
        assertTrue(MethodsAndScienceCatalog.organMilestones.isNotEmpty())
        assertEquals(5, MethodsAndScienceCatalog.groundingSteps54321.size)
        assertTrue(
            "Jacobson PMR should cover the full body, at least 6 groups",
            MethodsAndScienceCatalog.jacobsonMuscleGroups.size >= 6
        )
        assertTrue(
            "Shichko myth-buster deck should keep growing",
            MethodsAndScienceCatalog.shichkoMythBusters.size >= 8
        )
        assertTrue(AdaptiveMotivationEngine.allItems.size >= 150)
        assertTrue(EducationAndAchievementsCatalog.educationalMaterials.size >= 40)
    }

    @Test
    fun `jacobson muscle groups follow a continuous step order`() {
        val steps = MethodsAndScienceCatalog.jacobsonMuscleGroups.map { it.stepNumber }
        assertEquals(steps.sorted(), steps)
        assertEquals("Step numbers must be unique", steps.size, steps.distinct().size)
    }

    @Test
    fun `rmssd calculation handles rr intervals correctly`() {
        // Identical intervals have zero variability -> RMSSD 0.
        assertEquals(0, BleHeartRateManager.calculateRmssdFromRrIntervals(listOf(800, 800, 800, 800)))
        // Alternating intervals 800/1000: diff = 200ms each -> RMSSD 200.
        assertEquals(200, BleHeartRateManager.calculateRmssdFromRrIntervals(listOf(800, 1000, 800, 1000)))
        // Fewer than three intervals are not enough.
        assertEquals(0, BleHeartRateManager.calculateRmssdFromRrIntervals(listOf(800, 900)))
    }
}
