package bav.onecell.model

/**
 * Class represents full info about battle, including snapshots and battle results.
 */
data class BattleInfo(val snapshots: MutableList<BattleFieldSnapshot> = mutableListOf())
