package bav.onecell.common.storage

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import bav.onecell.R
import bav.onecell.model.cell.Cell
import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Data
import bav.onecell.model.hexes.HexMath
import kotlinx.coroutines.experimental.launch

class StorageImpl(
        private val context: Context,
        private val dataBase: CellDataBase,
        private val hexMath: HexMath): Storage {

    override fun storeCellRepository(repo: RepositoryContract.CellRepo) {
        launch {
            val dao = dataBase.cellDataDao()
            // TODO: optimise persistence
            for (i in 0 until repo.cellsCount()) {
                Log.d(TAG, "${repo.getCell(i)?.data?.toJson()}")
                repo.getCell(i)?.let { dao.insert(it.data) }
            }
        }
    }

    override fun storeCell(cell: Cell) {
        Log.d(TAG, cell.data.toJson())
        launch { dataBase.cellDataDao().insert(cell.data) }
    }

    override fun restoreCellRepository(): List<Cell> {
        val dao = dataBase.cellDataDao()
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (sharedPreferences.getBoolean(FIRST_TIME_APP_LAUNCH, true)) {
            sharedPreferences.edit().putBoolean(FIRST_TIME_APP_LAUNCH, false).apply()
            // Fill storage from JSON descriptions
            val cellJsons = context.resources.getStringArray(R.array.cell_descriptions)
            dao.deleteAll()
            for (json in cellJsons) {
                Log.d(TAG, "restore: $json")
                dao.insert(Data.fromJson(json))
            }
        }

        val cells = mutableListOf<Cell>()
        for (d in dao.getAll()) {
            cells.add(Cell(hexMath, d))
        }
        return cells
    }

    companion object {
        private const val TAG = "StorageImpl"
        private const val FIRST_TIME_APP_LAUNCH = "first_time_app_launch"
    }
}
