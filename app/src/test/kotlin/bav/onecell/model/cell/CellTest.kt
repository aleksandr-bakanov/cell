package bav.onecell.model.cell

import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CellTest {
    private val hexMath = HexMath()
    private val cell = Cell(hexMath)

    @Before
    fun setup() {
        cell.data.hexes.clear()
        cell.updateOutlineHexes()
        cell.data.direction = Cell.Direction.N
    }

    @Test
    fun defaultConstructorShouldSaveOriginAndDirection() {
        val origin = Hex(1, 2, -3)
        val direction = Cell.Direction.SW
        val cell = Cell(hexMath, Data(origin = origin, direction = direction))
        assertEquals(origin, cell.data.origin)
        assertEquals(direction, cell.data.direction)
    }

    @Test
    fun cloneShouldMakeDeepCopyOfCell() {
        val origin = Hex(1, 2, -3)
        val direction = Cell.Direction.SW
        val hexes = arrayOf(Hex(1, 2, -3), Hex(0, 5, -5))
        val map = mutableMapOf<Pair<Int, Int>, Hex>()
        hexes.forEach { map[it.mapKey] = it }
        val cell = Cell(hexMath, Data(hexes = map, origin = origin, direction = direction))

        val copy = cell.clone()
        assertEquals(cell.data.origin, copy.data.origin)
        assertEquals(cell.data.direction, copy.data.direction)
        assertEquals(cell.data.hexes, copy.data.hexes)
    }

    @Test
    fun sizeShouldReturnValidValue() {
        assertEquals(0, cell.size())
        for (hex in arrayOf(
                Hex(0, 0, 0).withType(Hex.Type.LIFE),
                Hex(1, -1, 0).withType(Hex.Type.ENERGY),
                Hex(2, -2, 0).withType(Hex.Type.ATTACK))
        ) cell.addHex(hex)
        assertEquals(3, cell.size())
    }

    @Test
    fun addHexShouldNotAllowToAddSameHexTwice() {
        val hex = Hex(0, 0, 0)
        cell.addHex(hex)
        cell.addHex(hex)
        assertEquals(1, cell.data.hexes.size)
    }

    @Test
    fun removeHexShouldRemoveExistingHex() {
        val hex = Hex(0, 0, 0)
        cell.addHex(hex)
        assertEquals(1, cell.data.hexes.size)
        cell.removeHex(hex)
        assertEquals(0, cell.data.hexes.size)
    }

    @Test
    fun removeHexShouldNotRemoveNonExistingHex() {
        val hex = Hex(0, 0, 0)
        cell.addHex(hex)
        assertEquals(1, cell.data.hexes.size)
        cell.removeHex(Hex(1, 0, -1))
        assertEquals(1, cell.data.hexes.size)
    }

    @Test
    fun containsShouldReturnValidValue() {
        val hex = Hex(0, 0,0)
        assertFalse(cell.contains(hex))
        cell.addHex(hex)
        assertTrue(cell.contains(hex))
        cell.removeHex(hex)
        assertFalse(cell.contains(hex))
    }

    @Test
    fun evaluateCellHexesPowerShouldProvideValidValues() {
        val lifeHex = Hex(0, 0, 0).withType(Hex.Type.LIFE)
        val energyHex = Hex(1, -1, 0).withType(Hex.Type.ENERGY)
        val nearAttackHex = Hex(1, 0, -1).withType(Hex.Type.ATTACK)
        val farAttackHex = Hex(-1, 1, 0).withType(Hex.Type.ATTACK)
        val veryFarAttackHex = Hex(2, 0, -2).withType(Hex.Type.ATTACK)
        val mostFarAttackHex = Hex(-2, 2, 0).withType(Hex.Type.ATTACK)
        for (hex in arrayOf(lifeHex, energyHex, nearAttackHex, farAttackHex, veryFarAttackHex, mostFarAttackHex)) cell.addHex(hex)
        cell.evaluateCellHexesPower()
        assertEquals(Hex.Power.LIFE_SELF.value, cell.data.hexes[lifeHex.mapKey]?.power)
        assertEquals(Hex.Power.ENERGY_SELF.value, cell.data.hexes[energyHex.mapKey]?.power)
        assertEquals(Hex.Power.LIFE_TO_NEIGHBOR.value + Hex.Power.ENERGY_TO_NEIGHBOR.value, cell.data.hexes[nearAttackHex.mapKey]?.power)
        assertEquals(Hex.Power.LIFE_TO_NEIGHBOR.value + Hex.Power.ENERGY_TO_FAR_NEIGHBOR.value, cell.data.hexes[farAttackHex.mapKey]?.power)
        assertEquals(Hex.Power.ENERGY_TO_FAR_NEIGHBOR.value, cell.data.hexes[veryFarAttackHex.mapKey]?.power)
        assertEquals(0, cell.data.hexes[mostFarAttackHex.mapKey]?.power)
    }

    @Test
    fun getOutlineHexesShouldReturnEmptyCollectionInCaseOfEmptyCell() {
        assertEquals(0, cell.getOutlineHexes().size)
    }

    @Test
    fun getOutlineHexesShouldReturnValidHexes() {
        for (hex in arrayOf(
                Hex(0, 0, 0),
                Hex(1, -1, 0))
        ) cell.addHex(hex)
        cell.updateOutlineHexes()
        val outline = cell.getOutlineHexes()
        val expected = arrayListOf(
                Hex(2, -2, 0),
                Hex(2, -1, -1),
                Hex(1, 0, -1),
                Hex(0, 1, -1),
                Hex(-1, 1, 0),
                Hex(-1, 0, 1),
                Hex(0, -1, 1),
                Hex(1, -2, 1))
        assertEquals(expected.size, outline.size)
        assertTrue(outline.containsAll(expected))
    }

    @Test
    fun rotateLeftShouldWorksAsPlanned() {
        cell.addHex(Hex(1, -1, 0))
        cell.rotateLeft()
        assertTrue(cell.contains(Hex(0, -1, 1)))
        assertEquals(1, cell.data.hexes.size)
        cell.rotateLeft()
        assertTrue(cell.contains(Hex(-1, 0, 1)))
        assertEquals(1, cell.data.hexes.size)
        cell.rotateLeft()
        assertTrue(cell.contains(Hex(-1, 1, 0)))
        assertEquals(1, cell.data.hexes.size)
        cell.rotateLeft()
        assertTrue(cell.contains(Hex(0, 1, -1)))
        assertEquals(1, cell.data.hexes.size)
        cell.rotateLeft()
        assertTrue(cell.contains(Hex(1, 0, -1)))
        assertEquals(1, cell.data.hexes.size)
        cell.rotateLeft()
        assertTrue(cell.contains(Hex(1, -1, 0)))
        assertEquals(1, cell.data.hexes.size)
    }

    @Test
    fun rotateRightShouldWorksAsPlanned() {
        cell.addHex(Hex(1, -1, 0))
        cell.rotateRight()
        assertTrue(cell.contains(Hex(1, 0, -1)))
        assertEquals(1, cell.data.hexes.size)
        cell.rotateRight()
        assertTrue(cell.contains(Hex(0, 1, -1)))
        assertEquals(1, cell.data.hexes.size)
        cell.rotateRight()
        assertTrue(cell.contains(Hex(-1, 1, 0)))
        assertEquals(1, cell.data.hexes.size)
        cell.rotateRight()
        assertTrue(cell.contains(Hex(-1, 0, 1)))
        assertEquals(1, cell.data.hexes.size)
        cell.rotateRight()
        assertTrue(cell.contains(Hex(0, -1, 1)))
        assertEquals(1, cell.data.hexes.size)
        cell.rotateRight()
        assertTrue(cell.contains(Hex(1, -1, 0)))
        assertEquals(1, cell.data.hexes.size)
    }

    @Test
    fun rotateShouldDoNothingInCaseOfSameDirectionPassed() {
        cell.addHex(Hex(1, -1, 0))
        cell.rotate(Cell.Direction.N)
        assertTrue(cell.contains(Hex(1, -1, 0)))
    }

    @Test
    fun rotateShouldFlipInCaseOfOppositeDirectionPassed() {
        cell.addHex(Hex(1, -1, 0))
        cell.rotate(Cell.Direction.S)
        assertTrue(cell.contains(Hex(-1, 1, 0)))
    }

    @Test
    fun rotateShouldWorksFineFromNorthDirection() {
        cell.addHex(Hex(1, -1, 0))
        cell.rotate(Cell.Direction.NE)
        assertTrue(cell.contains(Hex(1, 0, -1)))
        cell.rotate(Cell.Direction.N)
        cell.rotate(Cell.Direction.SE)
        assertTrue(cell.contains(Hex(0, 1, -1)))
        cell.rotate(Cell.Direction.N)
        cell.rotate(Cell.Direction.NW)
        assertTrue(cell.contains(Hex(0, -1, 1)))
        cell.rotate(Cell.Direction.N)
        cell.rotate(Cell.Direction.SW)
        assertTrue(cell.contains(Hex(-1, 0, 1)))
    }

    @Test
    fun rotateShouldWorksFineFromNorthEastDirection() {
        cell.rotate(Cell.Direction.NE)
        cell.addHex(Hex(1, -1, 0))
        cell.rotate(Cell.Direction.SE)
        assertTrue(cell.contains(Hex(1, 0, -1)))
        cell.rotate(Cell.Direction.NE)
        cell.rotate(Cell.Direction.S)
        assertTrue(cell.contains(Hex(0, 1, -1)))
        cell.rotate(Cell.Direction.NE)
        cell.rotate(Cell.Direction.N)
        assertTrue(cell.contains(Hex(0, -1, 1)))
        cell.rotate(Cell.Direction.NE)
        cell.rotate(Cell.Direction.NW)
        assertTrue(cell.contains(Hex(-1, 0, 1)))
    }

    @Test
    fun rotateShouldWorksFineFromSouthEastDirection() {
        cell.rotate(Cell.Direction.SE)
        cell.addHex(Hex(1, -1, 0))
        cell.rotate(Cell.Direction.S)
        assertTrue(cell.contains(Hex(1, 0, -1)))
        cell.rotate(Cell.Direction.SE)
        cell.rotate(Cell.Direction.SW)
        assertTrue(cell.contains(Hex(0, 1, -1)))
        cell.rotate(Cell.Direction.SE)
        cell.rotate(Cell.Direction.NE)
        assertTrue(cell.contains(Hex(0, -1, 1)))
        cell.rotate(Cell.Direction.SE)
        cell.rotate(Cell.Direction.N)
        assertTrue(cell.contains(Hex(-1, 0, 1)))
    }

    @Test
    fun rotateShouldWorksFineFromSouthDirection() {
        cell.rotate(Cell.Direction.S)
        cell.addHex(Hex(1, -1, 0))
        cell.rotate(Cell.Direction.SW)
        assertTrue(cell.contains(Hex(1, 0, -1)))
        cell.rotate(Cell.Direction.S)
        cell.rotate(Cell.Direction.NW)
        assertTrue(cell.contains(Hex(0, 1, -1)))
        cell.rotate(Cell.Direction.S)
        cell.rotate(Cell.Direction.SE)
        assertTrue(cell.contains(Hex(0, -1, 1)))
        cell.rotate(Cell.Direction.S)
        cell.rotate(Cell.Direction.NE)
        assertTrue(cell.contains(Hex(-1, 0, 1)))
    }

    @Test
    fun rotateShouldWorksFineFromSouthWestDirection() {
        cell.rotate(Cell.Direction.SW)
        cell.addHex(Hex(1, -1, 0))
        cell.rotate(Cell.Direction.NW)
        assertTrue(cell.contains(Hex(1, 0, -1)))
        cell.rotate(Cell.Direction.SW)
        cell.rotate(Cell.Direction.N)
        assertTrue(cell.contains(Hex(0, 1, -1)))
        cell.rotate(Cell.Direction.SW)
        cell.rotate(Cell.Direction.S)
        assertTrue(cell.contains(Hex(0, -1, 1)))
        cell.rotate(Cell.Direction.SW)
        cell.rotate(Cell.Direction.SE)
        assertTrue(cell.contains(Hex(-1, 0, 1)))
    }

    @Test
    fun rotateShouldWorksFineFromNorthWestDirection() {
        cell.rotate(Cell.Direction.NW)
        cell.addHex(Hex(1, -1, 0))
        cell.rotate(Cell.Direction.N)
        assertTrue(cell.contains(Hex(1, 0, -1)))
        cell.rotate(Cell.Direction.NW)
        cell.rotate(Cell.Direction.NE)
        assertTrue(cell.contains(Hex(0, 1, -1)))
        cell.rotate(Cell.Direction.NW)
        cell.rotate(Cell.Direction.SW)
        assertTrue(cell.contains(Hex(0, -1, 1)))
        cell.rotate(Cell.Direction.NW)
        cell.rotate(Cell.Direction.S)
        assertTrue(cell.contains(Hex(-1, 0, 1)))
    }
}
