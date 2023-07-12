package org.usvm.samples.codegen.deepequals


import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.usvm.samples.JavaMethodTestRunner
import org.usvm.test.util.checkers.eq

class DeepEqualsTest : JavaMethodTestRunner() {
    @Test
    @Disabled("Unexpected lvalue org.usvm.machine.JcStaticFieldRef@34136bd1")
    fun testReturnList() {
        checkDiscoveredProperties(
            DeepEqualsTestingClass::returnList,
            eq(1),
        )
    }

    @Test
    @Disabled("An operation is not implemented: Not yet implemented")
    fun testReturnSet() {
        checkDiscoveredProperties(
            DeepEqualsTestingClass::returnSet,
            eq(1),
        )
    }

    @Test
    @Disabled("Unexpected lvalue org.usvm.machine.JcStaticFieldRef@6a55594b")
    fun testReturnMap() {
        checkDiscoveredProperties(
            DeepEqualsTestingClass::returnMap,
            eq(1),
        )
    }

    @Test
    @Disabled("Wrong type resolving")
    fun testReturnArray() {
        checkDiscoveredProperties(
            DeepEqualsTestingClass::returnArray,
            eq(1),
            { _, r ->
                val first = DeepEqualsTestingClass()
                val second = DeepEqualsTestingClass()

                r!!.size == 2 && r.first() == first && r.last() == second
            }
        )
    }

    @Test
    @Disabled("Unexpected lvalue org.usvm.machine.JcStaticFieldRef@5927f2b1")
    fun testReturn2DList() {
        checkDiscoveredProperties(
            DeepEqualsTestingClass::return2DList,
            eq(1),
        )
    }

    @Test
    fun testReturn2DSet() {
        checkDiscoveredProperties(
            DeepEqualsTestingClass::return2DSet,
            eq(1),
        )
    }

    @Test
    @Disabled("Unexpected lvalue org.usvm.machine.JcStaticFieldRef@5927f2b1")
    fun testReturn2DMap() {
        checkDiscoveredProperties(
            DeepEqualsTestingClass::return2DMap,
            eq(1),
        )
    }

    @Test
    @Disabled("Unexpected lvalue org.usvm.machine.JcStaticFieldRef@5927f2b1")
    fun testIntegers2DList() {
        checkDiscoveredProperties(
            DeepEqualsTestingClass::returnIntegers2DList,
            eq(1),
        )
    }

    @Test
    @Disabled("Wrong type resolving")
    fun testReturn2DArray() {
        checkDiscoveredProperties(
            DeepEqualsTestingClass::return2DArray,
            eq(1),
            { _, r ->
                val firstValue = DeepEqualsTestingClass()
                val secondValue = DeepEqualsTestingClass()

                val array = arrayOf(firstValue, secondValue)

                r!!.size == 2 && r.first().contentEquals(array) && r.last().contentEquals(array)
            }
        )
    }

    @Test
    fun testReturnCommonClass() {
        checkDiscoveredProperties(
            DeepEqualsTestingClass::returnCommonClass,
            eq(1),
            { _, r ->
                val (defaultX, defaultY) = DeepEqualsTestingClass().let { it.x to it.y }

                r!!.x == defaultX && r.y == defaultY
            }
        )
    }

    @Test
    fun testTriangle() {
        checkDiscoveredProperties(
            DeepEqualsTestingClass::returnTriangle,
            eq(1),
            { _, firstValue, secondValue, first ->
                val second = first!!.next
                val third = second.next
                val thirdNext = third.next

                val sum = firstValue + secondValue

                first.value == firstValue &&second.value == secondValue && third.value == sum && first === thirdNext
            }
        )
    }

    @Test
    @Disabled("Not implemented: virtual calls with abstract methods")
    fun testQuadrilateral() {
        checkDiscoveredProperties(
            DeepEqualsTestingClass::returnQuadrilateralFromNode,
            eq(1),
        )
    }

    @Test
    @Disabled("Support multidimensional arrays initialization")
    fun testIntMultiArray() {
        checkDiscoveredProperties(
            DeepEqualsTestingClass::fillIntMultiArrayWithConstValue,
            eq(3),
            { _, length, _, r ->
                length <= 0 && r == null
            },
            { _, length, value, r ->
                val changedValue = r!![1][0][1]
                r[1][0][1] = value

                length > 0 && value == 10 && changedValue == 12 && r.flatten().all { it == value }
            },
            { _, length, value, r ->
                length > 0 && value != 10 && r!!.flatten().all { it == value }
            },
        )
    }

    @Test
    @Disabled("Support multidimensional arrays initialization")
    fun testDoubleMultiArray() {
        checkDiscoveredProperties(
            DeepEqualsTestingClass::fillDoubleMultiArrayWithConstValue,
            eq(3),
        )
    }

    @Test
    @Disabled("Support multidimensional arrays initialization")
    fun testIntegerWrapperMultiArray() {
        checkDiscoveredProperties(
            DeepEqualsTestingClass::fillIntegerWrapperMultiArrayWithConstValue,
            eq(3),
        )
    }

    @Test
    @Disabled("Support multidimensional arrays initialization")
    fun testDoubleWrapperMultiArray() {
        checkDiscoveredProperties(
            DeepEqualsTestingClass::fillDoubleWrapperMultiArrayWithConstValue,
            eq(3),
        )
    }
}

private fun Array<Array<IntArray>>.flatten(): List<Int> = flatMap {
    row -> row.flatMap { it.toList() }
}
