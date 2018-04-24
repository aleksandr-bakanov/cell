package bav.onecell.model

import bav.onecell.model.cell.Cell
import io.reactivex.subjects.BehaviorSubject

interface RepositoryContract {

    interface CellRepo {

        fun loadFromStore(): BehaviorSubject<Unit>

        fun cellsCount(): Int

        fun addCell(cell: Cell)

        fun removeCell(index: Int)

        fun createNewCell()

        fun getCell(index: Int): Cell?

        fun storeCells()
    }
}