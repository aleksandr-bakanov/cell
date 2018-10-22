package bav.onecell.model

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
        val hexesToRemove: MutableList<MutableList<Int>> = mutableListOf(),
        // Death rays, pairs of start and end points
        val deathRays: MutableList<Pair<Hex, Hex>> = mutableListOf())