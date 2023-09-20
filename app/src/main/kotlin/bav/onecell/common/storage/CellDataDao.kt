package bav.onecell.common.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import bav.onecell.model.cell.Data

@Dao
interface CellDataDao {
    @Query("SELECT * FROM cellData")
    fun getAll(): List<Data>

    @Insert(onConflict = REPLACE)
    fun insert(data: Data)

    @Query("DELETE FROM cellData")
    fun deleteAll()
}
