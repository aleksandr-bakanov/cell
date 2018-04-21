package bav.onecell.model

import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RulesTest {
    private val hexMath = HexMath()
    private val cell = Cell(hexMath)
    private val rules = Rules(hexMath)

    @Before
    fun setup() {
        cell.data.hexes.clear()
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
        cell.addHex(hex)
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, hex))
    }

    @Test
    fun addHexSeparatedFromCellShouldReturnFalse() {
        cell.addHex(Hex(0, 0, 0).withType(Hex.Type.LIFE))
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, Hex(2, -2, 0).withType(Hex.Type.LIFE)))
    }

    @Test
    fun addLifeHexBetweenTwoEnergyHexesShouldReturnFalse() {
        for (hex in arrayOf(
                Hex(-1, 1, 0).withType(Hex.Type.LIFE),
                Hex(-1, 0, 1).withType(Hex.Type.LIFE),
                Hex(0, 1, -1).withType(Hex.Type.LIFE),
                Hex(0, -1, 1).withType(Hex.Type.ENERGY),
                Hex(1, 0, -1).withType(Hex.Type.ENERGY))
        ) cell.addHex(hex)
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, Hex(1, -1, 0).withType(Hex.Type.LIFE)))
    }

    @Test
    fun addLifeHexToEnergyNeighborShouldReturnTrue() {
        for (hex in arrayOf(
                Hex(0, 0, 0).withType(Hex.Type.LIFE),
                Hex(1, -1, 0).withType(Hex.Type.ENERGY))
        ) cell.addHex(hex)
        assertTrue(rules.isAllowedToAddHexIntoCell(cell, Hex(2, -2, 0).withType(Hex.Type.LIFE)))
    }

    @Test
    fun addLifeHexWithoutLifeOrEnergyNeighborsShouldReturnFalse() {
        for (hex in arrayOf(
                Hex(0, 0, 0).withType(Hex.Type.LIFE),
                Hex(1, -1, 0).withType(Hex.Type.ENERGY),
                Hex(2, -2, 0).withType(Hex.Type.ATTACK))
        ) cell.addHex(hex)
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, Hex(3, -3, 0).withType(Hex.Type.LIFE)))
    }

    @Test
    fun addLifeHexNearAnotherLifeHexShouldReturnTrue() {
        for (hex in arrayOf(
                Hex(0, 0, 0).withType(Hex.Type.LIFE))
        ) cell.addHex(hex)
        assertTrue(rules.isAllowedToAddHexIntoCell(cell, Hex(1, -1, 0).withType(Hex.Type.LIFE)))
    }

    @Test
    fun addEnergyHexWithoutNeighborsShouldReturnFalse() {
        for (hex in arrayOf(
                Hex(0, 0, 0).withType(Hex.Type.LIFE))
        ) cell.addHex(hex)
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, Hex(2, -2, 0).withType(Hex.Type.ENERGY)))
    }

    @Test
    fun addEnergyHexNearAnotherEnergyHexShouldReturnFalse() {
        for (hex in arrayOf(
                Hex(0, 0, 0).withType(Hex.Type.LIFE),
                Hex(1, -1, 0).withType(Hex.Type.ENERGY))
        ) cell.addHex(hex)
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, Hex(2, -2, 0).withType(Hex.Type.ENERGY)))
    }

    @Test
    fun addEnergyHexWithoutLifeNeighborsShouldReturnFalse() {
        for (hex in arrayOf(
                Hex(0, 0, 0).withType(Hex.Type.LIFE),
                Hex(1, -1, 0).withType(Hex.Type.ATTACK))
        ) cell.addHex(hex)
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, Hex(2, -2, 0).withType(Hex.Type.ENERGY)))
    }

    @Test
    fun addEnergyHexNearLifeHexWithExistingEnergyNeighborShouldReturnFalse() {
        for (hex in arrayOf(
                Hex(0, 0, 0).withType(Hex.Type.LIFE),
                Hex(-1, 1, 0).withType(Hex.Type.ENERGY))
        ) cell.addHex(hex)
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, Hex(1, -1, 0).withType(Hex.Type.ENERGY)))
    }

    @Test
    fun addEnergyHexNearLifeHexShouldReturnTrue() {
        for (hex in arrayOf(
                Hex(0, 0, 0).withType(Hex.Type.LIFE))
        ) cell.addHex(hex)
        assertTrue(rules.isAllowedToAddHexIntoCell(cell, Hex(1, -1, 0).withType(Hex.Type.ENERGY)))
    }

    @Test
    fun addAttackHexWithoutNeighborsShouldReturnFalse() {
        for (hex in arrayOf(
                Hex(0, 0, 0).withType(Hex.Type.LIFE))
        ) cell.addHex(hex)
        assertFalse(rules.isAllowedToAddHexIntoCell(cell, Hex(2, -2, 0).withType(Hex.Type.ATTACK)))
    }

    @Test
    fun addAttackHexNearLifeHexShouldReturnTrue() {
        for (hex in arrayOf(
                Hex(0, 0, 0).withType(Hex.Type.LIFE))
        ) cell.addHex(hex)
        assertTrue(rules.isAllowedToAddHexIntoCell(cell, Hex(1, -1, 0).withType(Hex.Type.ATTACK)))
    }

    @Test
    fun removeAnyHexFromEmptyCellShouldNotBeAllowed() {
        assertFalse(rules.isAllowedToRemoveHexFromCell(cell, Hex(0, 0, 0).withType(Hex.Type.LIFE)))
        assertFalse(rules.isAllowedToRemoveHexFromCell(cell, Hex(0, 0, 0).withType(Hex.Type.ENERGY)))
        assertFalse(rules.isAllowedToRemoveHexFromCell(cell, Hex(0, 0, 0).withType(Hex.Type.ATTACK)))
    }

    @Test
    fun removeLastHexFromCellShouldBeAllowed() {
        for (hex in arrayOf(
                Hex(0, 0, 0))
        ) cell.addHex(hex)
        assertTrue(rules.isAllowedToRemoveHexFromCell(cell, Hex(0, 0, 0)))
    }

    @Test
    fun removeHexLeadingToBreakOfLifeAndEnergyHexesConnectivityShouldNotBeAllowed() {
        for (hex in arrayOf(
                Hex(0, 0, 0).withType(Hex.Type.LIFE),
                Hex(-1, 1, 0).withType(Hex.Type.LIFE),
                Hex(1, -1, 0).withType(Hex.Type.ENERGY))
        ) cell.addHex(hex)
        assertFalse(rules.isAllowedToRemoveHexFromCell(cell, Hex(0, 0, 0)))
    }

    @Test
    fun removeHexLeadingToBreakOfHexesConnectivityShouldNotBeAllowed() {
        for (hex in arrayOf(
                Hex(0, 0, 0).withType(Hex.Type.LIFE),
                Hex(-1, 1, 0).withType(Hex.Type.LIFE),
                Hex(1, -1, 0).withType(Hex.Type.ENERGY),
                Hex(2, -2, 0).withType(Hex.Type.ATTACK))
        ) cell.addHex(hex)
        assertFalse(rules.isAllowedToRemoveHexFromCell(cell, Hex(1, -1, 0)))
    }

    @Test
    fun removeHexNotLeadingToBreakOfHexesConnectivityShouldBeAllowed() {
        for (hex in arrayOf(
                Hex(0, 0, 0).withType(Hex.Type.LIFE),
                Hex(-1, 1, 0).withType(Hex.Type.LIFE),
                Hex(1, -1, 0).withType(Hex.Type.ENERGY),
                Hex(2, -2, 0).withType(Hex.Type.ATTACK))
        ) cell.addHex(hex)
        assertTrue(rules.isAllowedToRemoveHexFromCell(cell, Hex(2, -2, 0)))
        assertTrue(rules.isAllowedToRemoveHexFromCell(cell, Hex(-1, 1, 0)))
    }
}
