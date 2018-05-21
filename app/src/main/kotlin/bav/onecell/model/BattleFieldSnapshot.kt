package bav.onecell.model

import bav.onecell.model.cell.Cell

data class BattleFieldSnapshot(val cells: MutableList<Cell> = mutableListOf(),
                               val corpses: MutableList<Cell> = mutableListOf())