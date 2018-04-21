package bav.onecell.model

import bav.onecell.model.cell.Cell

interface RepositoryContract {

    interface CellRepo {

        fun cellsCount(): Int

        fun addCell(cell: Cell)

        fun createNewCell()

        fun getCell(index: Int): Cell?

        fun storeCells()
    }
}