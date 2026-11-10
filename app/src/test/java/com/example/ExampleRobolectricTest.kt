package com.example

import com.example.data.MethodsAndScienceCatalog
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

    @Test
    fun `methods and supplements catalogs are populated`() {
        assertTrue(MethodsAndScienceCatalog.therapyTechniques.isNotEmpty())
        assertTrue(MethodsAndScienceCatalog.supplementsGuide.isNotEmpty())
        assertTrue(MethodsAndScienceCatalog.organMilestones.isNotEmpty())
    }
}
