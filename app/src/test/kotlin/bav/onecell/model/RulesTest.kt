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
        cell.hexes[hex.hashCode()] = hex
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, hex))
    }

    @Test
    fun addHexSeparatedFromCellShouldReturnFalse() {
        val hex = Hex(0, 0, 0).withType(Hex.Type.LIFE)
        cell.hexes[hex.hashCode()] = hex
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, Hex(2, 2, 2).withType(Hex.Type.LIFE)))
    }

    @Test
    fun addLifeHexBetweenTwoEnergyHexesShouldReturnFalse() {
        for (hex in arrayOf(
                Hex(-1, 1, 0).withType(Hex.Type.LIFE),
                Hex(-1, 0, 1).withType(Hex.Type.LIFE),
                Hex(0, 1, -1).withType(Hex.Type.LIFE),
                Hex(0, -1, 1).withType(Hex.Type.ENERGY),
                Hex(1, 0, -1).withType(Hex.Type.ENERGY))
        ) cell.hexes[hex.hashCode()] = hex
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, Hex(1, -1, 0).withType(Hex.Type.LIFE)))
    }

    @Test
    fun addLifeHexToEnergyNeighborShouldReturnTrue() {
        for (hex in arrayOf(
                Hex(0, 0, 0).withType(Hex.Type.LIFE),
                Hex(1, -1, 0).withType(Hex.Type.ENERGY))
        ) cell.hexes[hex.hashCode()] = hex
        assertTrue(rules.isAllowedToAddHexIntoCell(cell, Hex(2, -2, 0).withType(Hex.Type.LIFE)))
    }

    @Test
    fun addLifeHexWithoutLifeOrEnergyNeighborsShouldReturnFalse() {
        for (hex in arrayOf(
                Hex(0, 0, 0).withType(Hex.Type.LIFE),
                Hex(1, -1, 0).withType(Hex.Type.ENERGY),
                Hex(2, -2, 0).withType(Hex.Type.ATTACK))
        ) cell.hexes[hex.hashCode()] = hex
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, Hex(3, -3, 0).withType(Hex.Type.LIFE)))
    }
}
