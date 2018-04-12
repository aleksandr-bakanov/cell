package bav.onecell.model.hexes

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import kotlin.math.PI

class HexMathTest {
    private val hexMath = HexMath()

    @Test(expected = IllegalArgumentException::class)
    fun getHexByDirectionShouldThrowIfNegativeDirectionPassed() {
        hexMath.getHexByDirection(-1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun getHexByDirectionShouldThrowIfDirectionIsOutOfRange() {
        hexMath.getHexByDirection(6)
    }

    @Test
    fun getHexByDirectionShouldReturnValidHexes() {
        assertEquals(hexMath.getHexByDirection(0), Hex(1, -1, 0))
        assertEquals(hexMath.getHexByDirection(1), Hex(1, 0, -1))
        assertEquals(hexMath.getHexByDirection(2), Hex(0, 1, -1))
        assertEquals(hexMath.getHexByDirection(3), Hex(-1, 1, 0))
        assertEquals(hexMath.getHexByDirection(4), Hex(-1, 0, 1))
        assertEquals(hexMath.getHexByDirection(5), Hex(0, -1, 1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun getNeighborsWithinRadiusWithZeroRadiusShouldThrow() {
        assertEquals(0, hexMath.getNeighborsWithinRadius(Hex(0, 0, 0), 0).size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun getNeighborsWithinRadiusWithNegativeRadiusShouldThrow() {
        assertEquals(-1, hexMath.getNeighborsWithinRadius(Hex(0, 0, 0), 0).size)
    }

    @Test
    fun getNeighborsWithinRadiusWithOneRadiusShouldReturnSixHexes() {
        val neighbors = hexMath.getNeighborsWithinRadius(Hex(0, 0, 0), 1)
        assertEquals(6, neighbors.size)
        for (hex in arrayOf(
                Hex(1, -1, 0),
                Hex(1, 0, -1),
                Hex(0, 1, -1),
                Hex(-1, 1, 0),
                Hex(-1, 0, 1),
                Hex(0, -1, 1))
        ) assertTrue(neighbors.contains(hex))
    }

    @Test
    fun getNeighborsWithinRadiusWithTwoRadiusShouldReturnTwelveHexes() {
        val neighbors = hexMath.getNeighborsWithinRadius(Hex(0, 0, 0), 2)
        assertEquals(18, neighbors.size)
    }

    @Test(expected = IllegalArgumentException::class)
    fun radToDirShouldThrowInCaseOfAngleLessThanMinusPi() {
        hexMath.radToDir((-2 * PI).toFloat())
    }

    @Test(expected = IllegalArgumentException::class)
    fun radToDirShouldThrowInCaseOfAngleMoreThanPi() {
        hexMath.radToDir((2 * PI).toFloat())
    }

    @Test
    fun radToDirShouldReturnValidDirections() {
        assertEquals(5, hexMath.radToDir((-PI * 2 / 3).toFloat()))
        assertEquals(0, hexMath.radToDir((-PI * 1 / 3).toFloat()))
        assertEquals(1, hexMath.radToDir(0f))
        assertEquals(2, hexMath.radToDir((PI * 1 / 3).toFloat()))
        assertEquals(3, hexMath.radToDir((PI * 2 / 3).toFloat()))
        assertEquals(4, hexMath.radToDir(PI.toFloat()))
    }

    @Test
    fun getHexNeighborShouldReturnValidHexes() {
        val neighbors = arrayOf(
                Hex(1, -2, 1),
                Hex(1, -1, 0),
                Hex(0, 0, 0),
                Hex(-1, 0, 1),
                Hex(-1, -1, 2),
                Hex(0, -2, 2))
        neighbors.forEachIndexed { index, hex ->
            assertEquals(hexMath.getHexNeighbor(Hex(0, -1, 1), index), hex)
        }
    }

    @Test
    fun hexNeighborsShouldReturnValidHexes() {
        val expected = arrayOf(
                Hex(1, -2, 1),
                Hex(1, -1, 0),
                Hex(0, 0, 0),
                Hex(-1, 0, 1),
                Hex(-1, -1, 2),
                Hex(0, -2, 2))
        val real = hexMath.hexNeighbors(Hex(0, -1, 1))
        for (hex in expected) {
            assertTrue(real.contains(hex))
        }
    }
}