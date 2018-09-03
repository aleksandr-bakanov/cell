package bav.onecell.common.storage

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import bav.onecell.model.cell.Cell
import bav.onecell.model.RepositoryContract
import bav.onecell.model.hexes.HexMath
import kotlinx.coroutines.experimental.launch

class StorageImpl(
        private val context: Context,
        private val dataBase: CellDataBase,
        private val hexMath: HexMath): Storage {

    init {
        initializeStorage()
    }

    override fun storeCellRepository(repo: RepositoryContract.CellRepo) {
        launch {
            val dao = dataBase.cellDataDao()
            // TODO: optimise persistence
            dao.deleteAll()
            for (i in 0..(repo.cellsCount() - 1)) {
                repo.getCell(i)?.let { dao.insert(it.data) }
            }
        }
    }

    override fun restoreCellRepository(): List<Cell> {
        val cells = mutableListOf<Cell>()
        for (d in dataBase.cellDataDao().getAll()) {
            cells.add(Cell(hexMath, d))
            Log.d(TAG, d.toJson())
        }
        return cells
    }

    //region Private methods
    private fun initializeStorage() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (sharedPreferences.getBoolean(FIRST_TIME_APP_LAUNCH, true)) {
            sharedPreferences.edit().putBoolean(FIRST_TIME_APP_LAUNCH, false).apply()
            // Fill storage from JSON descriptions

        }
    }
    //endregion

    companion object {
        private const val TAG = "StorageImpl"
        private const val FIRST_TIME_APP_LAUNCH = "first_time_app_launch"
    }
}
