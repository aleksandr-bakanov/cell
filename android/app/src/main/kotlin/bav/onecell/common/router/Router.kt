package bav.onecell.common.router

interface Router {
    /**
     * Go to cell editor view.
     *
     * @param cellIndex index of cell within cell repository
     */
    fun goToCellConstructor(cellIndex: Int)
}