package bav.onecell.common.router

import android.content.Context

interface Router {
    /**
     * Go to cell logic editor view.
     *
     * @param cellIndex Index of cell within cell repository
     */
    fun goToCellLogicEditor(context: Context, cellIndex: Int)

    /**
     * Go to battle view.
     *
     * @param cellIndexes Indexes of cells within cell repository
     */
    fun goToBattleView(context: Context, cellIndexes: List<Int>)
}