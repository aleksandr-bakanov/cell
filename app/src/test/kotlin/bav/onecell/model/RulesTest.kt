package bav.onecell.model

import bav.onecell.model.hexes.Hex
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RulesTest {
    private val cell = Cell()
    private val rules = Rules()

    @Before
    fun setup() {
        cell.hexes.clear()
    }

    @Test
    fun addLifeHexToEmptyCellShouldReturnTrue() {
        val hex = Hex(0, 0, 0).withType(Hex.Type.LIFE)
        assertTrue(rules.isAllowedToAddHexIntoCell(cell, hex))
    }

    @Test
    fun addEnergyHexToEmptyCellShouldReturnFalse() {
        val hex = Hex(0, 0, 0).withType(Hex.Type.ENERGY)
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, hex))
    }

    @Test
    fun addAttackHexToEmptyCellShouldReturnFalse() {
        val hex = Hex(0, 0, 0).withType(Hex.Type.ATTACK)
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, hex))
    }

    @Test
    fun addHexWithWrongTypeShouldReturnFalse() {
        val hex = Hex(0, 0, 0).withType(Hex.Type.REMOVE)
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, hex))
    }

    @Test
    fun addExistingHexShouldReturnFalse() {
        val hex = Hex(0, 0, 0).withType(Hex.Type.LIFE)
        cell.hexes.add(hex)
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, hex))
    }

    @Test
    fun addHexSeparatedFromCellShouldReturnFalse() {
        cell.hexes.add(Hex(0, 0, 0).withType(Hex.Type.LIFE))
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, Hex(2, 2, 2).withType(Hex.Type.LIFE)))
    }

    @Test
    fun addLifeHexBetweenTwoEnergyHexesShouldReturnFalse() {
        cell.hexes.add(Hex(-1, 1, 0).withType(Hex.Type.LIFE))
        cell.hexes.add(Hex(-1, 0, 1).withType(Hex.Type.LIFE))
        cell.hexes.add(Hex(0, 1, -1).withType(Hex.Type.LIFE))
        cell.hexes.add(Hex(0, -1, 1).withType(Hex.Type.ENERGY))
        cell.hexes.add(Hex(1, 0, -1).withType(Hex.Type.ENERGY))
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, Hex(1, -1, 0).withType(Hex.Type.LIFE)))
    }

    @Test
    fun addLifeHexToEnergyNeighborShouldReturnTrue() {
        cell.hexes.add(Hex(0, 0, 0).withType(Hex.Type.LIFE))
        cell.hexes.add(Hex(1, -1, 0).withType(Hex.Type.ENERGY))
        assertTrue(rules.isAllowedToAddHexIntoCell(cell, Hex(2, -2, 0).withType(Hex.Type.LIFE)))
    }

    @Test
    fun addLifeHexWithoutLifeOrEnergyNeighborsShouldReturnFalse() {
        cell.hexes.add(Hex(0, 0, 0).withType(Hex.Type.LIFE))
        cell.hexes.add(Hex(1, -1, 0).withType(Hex.Type.ENERGY))
        cell.hexes.add(Hex(2, -2, 0).withType(Hex.Type.ATTACK))
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, Hex(3, -3, 0).withType(Hex.Type.LIFE)))
    }
}
