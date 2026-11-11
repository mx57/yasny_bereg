package com.example

import com.example.data.AdaptiveMotivationEngine
import com.example.data.MethodsAndScienceCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `methods and supplements catalogs are populated with expanded trainers`() {
        assertTrue(MethodsAndScienceCatalog.therapyTechniques.size >= 14)
        assertTrue(MethodsAndScienceCatalog.supplementsGuide.size >= 8)
        assertTrue(MethodsAndScienceCatalog.organMilestones.isNotEmpty())
        assertEquals(5, MethodsAndScienceCatalog.groundingSteps54321.size)
        assertEquals(6, MethodsAndScienceCatalog.jacobsonMuscleGroups.size)
        assertEquals(6, MethodsAndScienceCatalog.shichkoMythBusters.size)
        assertTrue(AdaptiveMotivationEngine.allItems.size >= 25)
    }
}
