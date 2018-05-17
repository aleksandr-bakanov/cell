package bav.onecell.model

import bav.onecell.model.cell.Cell
import io.reactivex.subjects.ReplaySubject

interface RepositoryContract {

    interface CellRepo {

        fun loadFromStore(): ReplaySubject<Unit>

        fun cellsCount(): Int

        fun addCell(cell: Cell)

        fun removeCell(index: Int)

        fun createNewCell()

        fun getCell(index: Int): Cell?

        fun storeCells()
    }
}