package org.usvm.dataflow.jvm.impl

import org.jacodb.api.jvm.ext.methods
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.usvm.dataflow.jvm.equals.EqualsProcessor
import kotlin.test.assertNotNull

// What does it mean?
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EqualsTest : BaseAnalysisTest() {
    @Test
    fun `check equals is structural in person class`() {
        val personClass = cp.findClassOrNull("Person")
        // TODO: process correctly overrides.
        val equalsMethod = personClass?.methods?.find { it.name == "equals" }
        assertNotNull(equalsMethod)

        EqualsProcessor.isEqualsStructural(cp, equalsMethod)
    }

    @Test
    fun `check equals is structural in team class`() {
        val teamClass = cp.findClassOrNull("Team")
        // TODO: process correctly overrides.
        val equalsMethod = teamClass?.methods?.find { it.name == "equals" }
        assertNotNull(equalsMethod)

        EqualsProcessor.isEqualsStructural(cp, equalsMethod)
    }
}