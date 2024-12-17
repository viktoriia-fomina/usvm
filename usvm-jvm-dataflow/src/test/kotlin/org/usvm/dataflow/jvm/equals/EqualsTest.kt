package org.usvm.dataflow.jvm.equals

import org.jacodb.api.jvm.ext.methods
import org.jacodb.impl.features.classpaths.JcUnknownClass
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.usvm.dataflow.jvm.impl.BaseAnalysisTest
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EqualsTest : BaseAnalysisTest() {
    @Test
    fun `check equals is structural in person class`() {
        val personClass = cp.findClassOrNull("equals.Person")
        assertTrue(personClass !is JcUnknownClass) { "Person class is not loaded correctly" }
        // TODO: process correctly overrides.
        val equalsMethod = personClass?.methods?.find {
            it.name == "equals" && it.parameters.size == 1 && it.parameters.first().type.typeName == "java.lang.Object"
        }
        assertNotNull(equalsMethod)
        assertTrue(EqualsProcessor.isEqualsStructural(cp, equalsMethod))
    }

    @Test
    fun `check equals is structural in team class`() {
        val teamClass = cp.findClassOrNull("equals.Team")
        assertTrue(teamClass !is JcUnknownClass) { "Team class is not loaded correctly" }
        // TODO: process correctly overrides.
        val equalsMethod = teamClass?.methods?.find { it.name == "equals" }
        assertNotNull(equalsMethod)
        assertFalse(EqualsProcessor.isEqualsStructural(cp, equalsMethod))
    }
}