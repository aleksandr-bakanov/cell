package bav.onecell.common.storage

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
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
