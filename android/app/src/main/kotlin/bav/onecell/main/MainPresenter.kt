package bav.onecell.main

import bav.onecell.model.Cell
import bav.onecell.model.CellRepository

class MainPresenter(
        private val view: Main.View,
        private val cellRepository: CellRepository) : Main.Presenter {

    private val cellListAdapter = CellListAdapter(cellRepository)

    companion object {
        private const val TAG = "MainPresenter"
    }

    //region Overriden methods
    override fun createNewCell() {
        val cell = Cell()
        cellRepository.cells.add(cell)
        cellListAdapter.notifyItemInserted(cellRepository.cells.size - 1)
    }

    override fun provideCellListAdapter(): CellListAdapter {
        return cellListAdapter
    }
    //endregion
}