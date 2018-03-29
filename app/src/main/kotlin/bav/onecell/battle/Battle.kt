package bav.onecell.battle

import bav.onecell.model.Cell
import bav.onecell.model.hexes.Hex

interface Battle {

    interface View {
        fun setBackgroundFieldRadius(radius: Int)
        fun setCells(cells: List<Cell>)
        fun updateBattleView()
        fun setRing(ring: List<Hex>)
    }

    interface Presenter {
        /**
         * Presenter initializer
         *
         * @param cellIndexes Indexes of cells within cell repository
         */
        fun initialize(cellIndexes: List<Int>)

        /**
         * Initialize next battle step calculation
         */
        fun doNextStep()
    }
}