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
        hexMath.radToNeighborDirection((-2 * PI).toFloat())
    }

    @Test(expected = IllegalArgumentException::class)
    fun radToDirShouldThrowInCaseOfAngleMoreThanPi() {
        hexMath.radToNeighborDirection((2 * PI).toFloat())
    }

    @Test
    fun radToDirShouldReturnValidDirections() {
        assertEquals(5, hexMath.radToNeighborDirection((-PI * 2 / 3).toFloat()))
        assertEquals(0, hexMath.radToNeighborDirection((-PI * 1 / 3).toFloat()))
        assertEquals(1, hexMath.radToNeighborDirection(0f))
        assertEquals(2, hexMath.radToNeighborDirection((PI * 1 / 3).toFloat()))
        assertEquals(3, hexMath.radToNeighborDirection((PI * 2 / 3).toFloat()))
        assertEquals(4, hexMath.radToNeighborDirection(PI.toFloat()))
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

    @Test
    fun addShouldReturnValidHex() {
        assertEquals(Hex(-1, 0, 1), hexMath.add(Hex(0, 1, -1), Hex(-1, -1, 2)))
    }

    @Test
    fun subtractShouldReturnValidHex() {
        assertEquals(Hex(-1, -4, 5), hexMath.subtract(Hex(0, -1, 1), Hex(1, 3, -4)))
    }

    @Test
    fun multiplyShouldReturnValidHex() {
        assertEquals(Hex(-3, -6, 9), hexMath.multiply(Hex(-1, -2, 3), 3))
    }

    @Test
    fun lengthShouldReturnValidValue() {
        assertEquals(3, hexMath.length(Hex(-1, -2, 3)))
    }

    @Test
    fun distanceShouldReturnValidValue() {
        assertEquals(5, hexMath.distance(Hex(0, -1, 1), Hex(1, 3, -4)))
    }

    @Test
    fun rotateRightShouldReturnValidHex() {
        assertEquals(Hex(2, -3, 1), hexMath.rotateRight(Hex(-1, -2, 3)))
    }

    @Test
    fun rotateRightTwiceShouldReturnValidHex() {
        assertEquals(Hex(3, -1, -2), hexMath.rotateRightTwice(Hex(-1, -2, 3)))
    }

    @Test
    fun rotateLeftShouldReturnValidHex() {
        assertEquals(Hex(-3, 1, 2), hexMath.rotateLeft(Hex(-1, -2, 3)))
    }

    @Test
    fun rotateLeftTwiceShouldReturnValidHex() {
        assertEquals(Hex(-2, 3, -1), hexMath.rotateLeftTwice(Hex(-1, -2, 3)))
    }

    @Test
    fun rotateFlipShouldReturnValidHex() {
        assertEquals(Hex(1, 2, -3), hexMath.rotateFlip(Hex(-1, -2, 3)))
    }
}