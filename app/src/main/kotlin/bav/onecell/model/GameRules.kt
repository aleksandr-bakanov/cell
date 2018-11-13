package bav.onecell.model

import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.Hex
import bav.onecell.model.hexes.HexMath

/**
 * This class contains functions to check possibility to add/remove hexes in cell
 */
class GameRules(private val hexMath: HexMath) {

    fun isAllowedToAddHexIntoCell(cell: Cell, hex: Hex): Boolean {
        if (cell.data.hexes.values.contains(hex)) return false
        return when (hex.type) {
            Hex.Type.LIFE -> checkLifeCell(cell.data.hexes.values, hex)
            Hex.Type.ENERGY -> checkEnergyCell(cell.data.hexes.values, hex)
            Hex.Type.ATTACK -> checkAttackCell(cell.data.hexes.values, hex)
            Hex.Type.DEATH_RAY -> checkAttackCell(cell.data.hexes.values, hex)
            Hex.Type.OMNI_BULLET -> checkAttackCell(cell.data.hexes.values, hex)
            else -> false
        }
    }

    fun isAllowedToRemoveHexFromCell(cell: Cell, hex: Hex): Boolean {
        if (cell.data.hexes.isEmpty()) return false
        // If cell doesn't contain such hex then it obviously can't be removed
        if (!cell.data.hexes.contains(hex.mapKey)) return false
        // The only one life hex left in cell is allowed to be removed
        if (cell.data.hexes.size == 1) return true
        // Cell can't exist without life hexes
        val allHexesButOne = cell.data.hexes.filter { it.value != hex }.values
        if (allHexesButOne.count { it.type == Hex.Type.LIFE } == 0) return false
        // If we got here that means cell contain at least 2 hexes and removing desired hex
        // will not remove last life hex. So we may check remain hexes connectivity.
        // First let's check connectivity of life and energy hexes only
        if (!checkHexesConnectivity(allHexesButOne.filter {
                    it.type == Hex.Type.LIFE || it.type == Hex.Type.ENERGY
                })) return false
        // After that we may check connectivity of the whole cell
        if (!checkHexesConnectivity(allHexesButOne)) return false
        return true
    }

    private fun checkLifeCell(hexes: Collection<Hex>, hex: Hex): Boolean {
        // First of all we need to check whether this hex is a first one in cell,
        // then it will be allowed only if it is life hex.
        if (hexes.isEmpty()) return true
        // Life hex should be linked with others life hexes directly or through energy cell.
        // Also life hex can adjoin with no more than one energy hex.
        val neighbors = getNeighborCountByType(hexes, hex)
        if (hexHasNoNeighbors(neighbors)) return false
        // There are more than one energy neighbor
        if (neighbors[1] > 1) return false
        // One energy neighbor, zero life neighbors
        if (neighbors[0] == 0 && neighbors[1] == 1) return true
        // No life neighbors
        if (neighbors[0] == 0) return false
        // All other cases are valid
        return true
    }

    private fun checkEnergyCell(hexes: Collection<Hex>, hex: Hex): Boolean {
        if (hexes.isEmpty()) return false
        // Energy hex can't adjoin with other energy hex
        val neighborsCount = getNeighborCountByType(hexes, hex)
        if (hexHasNoNeighbors(neighborsCount)) return false
        // There is at least one energy neighbor
        if (neighborsCount[1] > 0) return false
        // There is no life neighbors
        if (neighborsCount[0] == 0) return false
        // Check all life neighbors's energy neighbors
        val lifeNeighbors = getNeighborsByType(hexes, hex, Hex.Type.LIFE)
        lifeNeighbors.forEach {
            if (getNeighborCountByType(hexes, it)[1] > 0)
                return false
        }
        return true
    }

    private fun checkAttackCell(hexes: Collection<Hex>, hex: Hex): Boolean {
        if (hexes.isEmpty()) return false
        val neighbors = getNeighborCountByType(hexes, hex)
        if (hexHasNoNeighbors(neighbors)) return false
        return true
    }

    /**
     * Returns count of neighboring hexes by hex types
     *
     * @return [0] - count of life hexes; [1] - energy hexes; [2] - attack hexes
     */
    private fun getNeighborCountByType(hexes: Collection<Hex>, hex: Hex): Array<Int> {
        val neighbors = hexes.intersect(hexMath.hexNeighbors(hex))
        var life = 0
        var energy = 0
        var attack = 0
        for (neighbor in neighbors) {
            when (neighbor.type) {
                Hex.Type.LIFE -> life++
                Hex.Type.ENERGY -> energy++
                Hex.Type.ATTACK -> attack++
                else -> Unit
            }
        }
        return arrayOf(life, energy, attack)
    }

    private fun hexHasNoNeighbors(n: Array<Int>): Boolean {
        return n[0] == 0 && n[1] == 0 && n[2] == 0
    }

    private fun getNeighborsByType(hexes: Collection<Hex>, hex: Hex, type: Hex.Type): List<Hex> {
        return hexes.intersect(hexMath.hexNeighbors(hex)).filter { it.type == type }
    }

    fun checkHexesConnectivity(allHexes: Collection<Hex>): Boolean {
        if (allHexes.isEmpty()) return false
        val connectedHexes = mutableSetOf<Hex>()
        checkConnectivityRecursively(allHexes, allHexes.first(), connectedHexes)
        return allHexes.toSet() == connectedHexes
    }

    private fun checkConnectivityRecursively(allHexes: Collection<Hex>, hex: Hex, connectedHexes: MutableSet<Hex>) {
        if (connectedHexes.contains(hex)) return
        connectedHexes.add(hex)
        val notCheckedNeighbors = allHexes.intersect(hexMath.hexNeighbors(hex)).subtract(connectedHexes)
        notCheckedNeighbors.forEach { checkConnectivityRecursively(allHexes, it, connectedHexes) }
    }
}
