package bav.onecell.common.storage

import android.content.Context
import android.os.Environment
import android.util.Log
import bav.onecell.R
import bav.onecell.common.Common
import bav.onecell.model.cell.Cell
import bav.onecell.model.RepositoryContract
import bav.onecell.model.cell.Data
import bav.onecell.model.hexes.HexMath
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class StorageImpl(
        private val context: Context,
        private val dataBase: CellDataBase,
        private val hexMath: HexMath,
        private val gameState: Common.GameState): Storage {

    override fun storeCellRepository(repo: RepositoryContract.CellRepo) {
        GlobalScope.launch {
            val dao = dataBase.cellDataDao()
            // TODO: optimise persistence
            for (i in 0 until repo.cellsCount()) {
                //Log.d(TAG, "${repo.getCell(i)?.data?.toJson()}")
                repo.getCell(i)?.let { dao.insert(it.data) }
            }
        }
    }

    override fun storeCell(cellData: Data) {
        if (isExternalStorageWritable()) {
            val file = File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "${cellData.name}.txt")
            file.createNewFile()
            FileOutputStream(file).use {
                it.write(cellData.toJson().toByteArray(Charsets.UTF_8))
                it.close()
            }
        }

        GlobalScope.launch { dataBase.cellDataDao().insert(cellData) }
    }

    override fun restoreCellRepository(): List<Cell> {
        val dao = dataBase.cellDataDao()
        if (gameState.isFirstLaunch()) {
            // Fill storage from JSON descriptions
            val cellJsons = context.resources.getStringArray(R.array.cell_descriptions)
            dao.deleteAll()
            for (json in cellJsons) {
                //Log.d(TAG, "restore: $json")
                dao.insert(Data.fromJson(json))
            }
        }

        val cells = mutableListOf<Cell>()
        for (d in dao.getAll()) {
            cells.add(Cell(hexMath, d))
        }
        return cells
    }

    //region private
    /* Checks if external storage is available for read and write */
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    //endregion

    companion object {
        private const val TAG = "StorageImpl"
    }
}
