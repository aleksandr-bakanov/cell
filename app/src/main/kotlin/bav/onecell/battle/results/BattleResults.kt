package bav.onecell.battle.results

import bav.onecell.model.cell.Cell

interface BattleResults {
    interface View {

    }
    interface Presenter {
        fun initialize(dealtDamage: Map<Int, Int>, deadOrAliveCells: Map<Int, Boolean>)
        fun cellsCount(groupId: Int): Int
        fun getCell(groupId: Int, position: Int): Cell?
        fun getDealtDamage(groupId: Int, position: Int): Int
        fun getDeadOrAlive(groupId: Int, position: Int): Boolean
        fun goToHeroesScreen()
        fun getCellName(resourceId: String): String
    }
}
