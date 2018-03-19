package bav.onecell.model

import android.util.Log
import bav.onecell.model.hexes.Hex

/**
 * This class contains functions to check possibility to add hexes into cell
 */
class Rules {
    companion object {
        private const val TAG = "Rules"

        fun isAllowedToAddHexIntoCell(cell: Cell, hex: Hex): Boolean {
            return when (hex.type) {
                Hex.Type.LIFE -> checkLifeCell(cell.hexes, hex)
                Hex.Type.ENERGY -> checkEnergyCell(cell.hexes, hex)
                Hex.Type.ATTACK -> checkAttackCell(cell.hexes, hex)
                else -> false
            }
        }

        private fun checkLifeCell(hexes: MutableSet<Hex>, hex: Hex): Boolean {
            // First of all we need to check whether this hex is a first one in cell,
            // then it will be allowed only if it is life hex.
            if (hexes.size == 0) {
                Log.d(TAG, "checkLifeCell hexes.size == 0")
                return true
            }
            // Life hex should be linked with others life hexes directly or through energy cell.
            // Also life hex can adjoin with no more than one energy hex.
            val neighbors = getNeighborCountByType(hexes, hex)
            Log.d(TAG, "checkLifeCell neighbors = $neighbors")
            if (hexHasNoNeighbors(neighbors)) return false
            if (neighbors[1] > 1) return false
            if (neighbors[0] == 0 && neighbors[1] == 1) return true
            if (neighbors[0] == 0) return false
            return true
        }

        private fun checkEnergyCell(hexes: MutableSet<Hex>, hex: Hex): Boolean {
            if (hexes.size == 0) {
                Log.d(TAG, "checkEnergyCell hexes.size == 0")
                return false
            }
            // Energy hex can't adjoin with other energy hex
            val neighborsCount = getNeighborCountByType(hexes, hex)
            if (hexHasNoNeighbors(neighborsCount)) return false
            if (neighborsCount[1] > 0) return false
            if (neighborsCount[0] == 0) return false
            // Check all life neighbors's energy neighbors
            val lifeNeighbors = getNeighborsByType(hexes, hex, Hex.Type.LIFE)
            lifeNeighbors.forEach {
                if (getNeighborCountByType(hexes, it)[1] > 0)
                    return false
            }
            return true
        }

        private fun checkAttackCell(hexes: MutableSet<Hex>, hex: Hex): Boolean {
            if (hexes.size == 0) {
                Log.d(TAG, "checkAttackCell hexes.size == 0")
                return false
            }
            // Energy hex can't adjoin with other energy hex
            val neighbors = getNeighborCountByType(hexes, hex)
            if (hexHasNoNeighbors(neighbors)) return false
            return true
        }

        /**
         * Returns count of neighboring hexes by hex types
         *
         * @return [0] - count of life hexes; [1] - energy hexes; [2] - attack hexes
         */
        private fun getNeighborCountByType(hexes: MutableSet<Hex>, hex: Hex): Array<Int> {
            val neighbors = hexes.intersect(Hex.hexNeighbors(hex))
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

        private fun getNeighborsByType(hexes: MutableSet<Hex>, hex: Hex, type: Hex.Type): List<Hex> {
            return hexes.intersect(Hex.hexNeighbors(hex)).filter { it.type == type }
        }
    }
}