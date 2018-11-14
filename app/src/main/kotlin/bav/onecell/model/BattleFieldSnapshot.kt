package bav.onecell.model

import bav.onecell.model.battle.Bullet
import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.logic.Action
import bav.onecell.model.hexes.Hex

data class BattleFieldSnapshot(
        // Life cells
        val cells: MutableList<Cell> = mutableListOf(),
        // Dead cells
        val corpses: MutableList<Cell> = mutableListOf(),
        // Actions to be performed by living cells. Action can be null if no action has been performed.
        // Indexing is the same as in `cells` list.
        val cellsActions: MutableList<Action?> = mutableListOf(),
        // Cell's moving direction
        val movingDirections: MutableList<Int> = mutableListOf(),
        // Hexes to be removed (hashes of hexes), list can be empty
        val hexesToRemove: MutableList<MutableList<Pair<Int, Int>>> = mutableListOf(),
        // Death rays, pairs of start and end points
        val deathRays: MutableList<Pair<Hex, Hex>> = mutableListOf(),
        // Bullets
        val bullets: MutableList<Bullet> = mutableListOf()) {

    fun duration(): Int = movementDuration() + hexRemovalDuration() + actionsDuration() + deathRaysDuration()

    fun movementDuration(): Int = if (movingDirections.isNotEmpty()) CELL_MOVING_DURATION_MS else 0
    fun hexRemovalDuration(): Int = if (hexesToRemove.isNotEmpty()) HEX_FADING_DURATION_MS else 0
    fun actionsDuration(): Int = if (cellsActions.filter { it != null }.isNotEmpty()) ACTION_PERFORM_DURATION_MS else 0
    fun deathRaysDuration(): Int = if (deathRays.isNotEmpty()) DEATH_RAY_DURATION_MS else 0

    companion object {
        const val CELL_MOVING_DURATION_MS: Int = 500
        const val HEX_FADING_DURATION_MS: Int = 500
        const val ACTION_PERFORM_DURATION_MS: Int = 500
        const val DEATH_RAY_DURATION_MS: Int = 300
    }
}
