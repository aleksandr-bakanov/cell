package bav.onecell.common.storage

import android.content.Context
import bav.onecell.R
import bav.onecell.model.Cell
import bav.onecell.model.RepositoryContract

class StorageImpl(private val context: Context): Storage {
    override fun storeCellRepository(repo: RepositoryContract.CellRepo) {
        val prefs = context.getSharedPreferences(
                context.getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE)
        with (prefs.edit()) {
            val count = repo.cellsCount()
            for (i in 0..(count - 1)) {

            }
            apply()
        }
    }

    override fun loadCellsForRepository(): List<Cell> {
        return listOf()
    }
}