package bav.onecell.model

/**
 * Class represents full info about battle, including snapshots and battle results.
 */
data class BattleInfo(
        // Battle snapshots
        val snapshots: MutableList<BattleFieldSnapshot> = mutableListOf(),
        // Damage dealt by cells during the battle. Keys are indexes in cellRepository.
        val damageDealtByCells: Map<Int, Int> = mutableMapOf(),
        // Info about life status of cells at the end of the battle
        val deadOrAliveCells: Map<Int, Boolean> = mutableMapOf(),
        // Is battlefield fogged
        val isFog: Boolean = false)
