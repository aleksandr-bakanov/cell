package bav.onecell.common.router

interface Router {
    /**
     * Go to cell editor view.
     *
     * @param cellIndex Index of cell within cell repository
     */
    fun goToCellConstructor(cellIndex: Int)

    /**
     * Go to battle view.
     *
     * @param cellIndexes Indexes of cells within cell repository
     */
    fun goToBattleView(cellIndexes: List<Int>)
}