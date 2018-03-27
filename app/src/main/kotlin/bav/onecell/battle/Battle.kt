package bav.onecell.battle

interface Battle {

    interface View {
        fun setBackgroundFieldRadius(radius: Int)
        fun updateBattleView()
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