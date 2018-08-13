package bav.onecell.model

import bav.onecell.model.cell.Cell
import bav.onecell.model.cell.logic.Action

data class BattleFieldSnapshot(
        // Life cells
        val cells: MutableList<Cell> = mutableListOf(),
        // Dead cells
        val corpses: MutableList<Cell> = mutableListOf(),
        // Actions to be performed by life cells. Action can be null if no action has been performed.
        val cellsActions: MutableList<Action?> = mutableListOf(),
        // Cell's moving direction
        val movingDirections: MutableList<Int> = mutableListOf(),
        // Hexes to be removed (hashes of hexes)
        val hexesToRemove: MutableList<MutableList<Int>> = mutableListOf())