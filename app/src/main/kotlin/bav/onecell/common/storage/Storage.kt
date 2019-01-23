package bav.onecell.common.storage

import bav.onecell.model.cell.Cell
import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Data

interface Storage {
    /**
     * Stores cells in database
     *
     * @param repo
     */
    fun storeCellRepository(repo: RepositoryContract.CellRepo)
    fun storeCell(cellData: Data)
    fun restoreCellRepository(): List<Cell>
}