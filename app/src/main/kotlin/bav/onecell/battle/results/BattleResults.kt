package bav.onecell.battle.results

import bav.onecell.model.cell.Cell

interface BattleResults {
    interface View {

    }
    interface Presenter {
        fun initialize(dealtDamage: Map<Int, Int>, deadOrAliveCells: Map<Int, Boolean>)
        fun cellsCount(): Int
        fun getCell(position: Int): Cell?
        fun getDealtDamage(position: Int): Int
        fun getDeadOrAlive(position: Int): Boolean
        fun goToHeroesScreen()
        fun getCellName(resourceId: String): String
    }
}
