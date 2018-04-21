package bav.onecell.common.storage

import bav.onecell.model.cell.Cell
import bav.onecell.model.RepositoryContract

interface Storage {
    fun storeCellRepository(repo: RepositoryContract.CellRepo)
    fun loadCellsForRepository(): List<Cell>
}