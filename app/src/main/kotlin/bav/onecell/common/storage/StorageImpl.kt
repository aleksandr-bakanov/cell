package bav.onecell.common.storage

import bav.onecell.model.cell.Cell
import bav.onecell.model.RepositoryContract
import bav.onecell.model.hexes.HexMath
import kotlinx.coroutines.experimental.launch

class StorageImpl(private val dataBase: CellDataBase,
                  private val hexMath: HexMath): Storage {
    override fun storeCellRepository(repo: RepositoryContract.CellRepo) {
        launch {
            val dao = dataBase.cellDataDao()
            for (i in 0..(repo.cellsCount() - 1)) {
                repo.getCell(i)?.let { dao.insert(it.data) }
            }
        }
    }

    override fun loadCellsForRepository(): List<Cell> {
        val cells = mutableListOf<Cell>()
        for (d in dataBase.cellDataDao().getAll()) {
            cells.add(Cell(hexMath, d))
        }
        return cells
    }
}
