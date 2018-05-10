package bav.onecell.common.storage

import bav.onecell.model.cell.Cell
import bav.onecell.model.RepositoryContract

interface Storage {
    /**
     * Stores cells in database
     *
     * @param repo
     */
    fun storeCellRepository(repo: RepositoryContract.CellRepo)
    fun restoreCellRepository(): List<Cell>
}