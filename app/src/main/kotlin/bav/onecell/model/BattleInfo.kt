package bav.onecell.model

/**
 * Class represents full info about battle, including snapshots and battle results.
 */
data class BattleInfo(
        // Battle snapshots
        val snapshots: MutableList<BattleFieldSnapshot> = mutableListOf(),
        // Damage dealt by cells during the battle. Keys are indexes in cellRepository.
        val damageDealtByCells: MutableMap<Int, Int> = mutableMapOf(),
        // Info about life status of cells at the end of the battle
        val deadOrAliveCells: MutableMap<Int, Boolean> = mutableMapOf(),
        // Is battlefield fogged
        val isFog: Boolean = false,
        // Winner group id - means which party has won
        val winnerGroupId: Int = 0) {

    fun clear() {
        snapshots.forEach { it.clear() }

        snapshots.clear()
        damageDealtByCells.clear()
        deadOrAliveCells.clear()
    }
}
