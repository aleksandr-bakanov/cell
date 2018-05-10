package bav.onecell.model

import bav.onecell.common.storage.Storage
import bav.onecell.model.cell.Cell
import bav.onecell.model.hexes.HexMath
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch

class CellRepository(
        private val hexMath: HexMath,
        private val storage: Storage,
        private val cells: ArrayList<Cell> = arrayListOf()) : RepositoryContract.CellRepo {

    private val loadFromStoreStatus: BehaviorSubject<Unit> = BehaviorSubject.create()

    init {
        launch {
            val cellsFromStorage = async { storage.restoreCellRepository() }.await()
            for (cell in cellsFromStorage) {
                cells.add(cell)
            }
            loadFromStoreStatus.onComplete()
        }
    }

    override fun loadFromStore(): BehaviorSubject<Unit> = loadFromStoreStatus

    override fun cellsCount(): Int = cells.size

    override fun addCell(cell: Cell) {
        cells.add(cell)
    }

    override fun removeCell(index: Int) {
        if (index in 0 until cells.size) cells.removeAt(index)
    }

    override fun createNewCell() {
        val cell = Cell(hexMath)
        addCell(cell)
    }

    override fun getCell(index: Int): Cell? {
        return if (index in 0..(cells.size - 1)) cells[index] else null
    }

    override fun storeCells() {
        storage.storeCellRepository(this)
    }
}