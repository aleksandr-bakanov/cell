package bav.onecell.common.storage

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import bav.onecell.model.cell.Data

@Database(entities = [Data::class], version = 1)
@TypeConverters(Converters::class)
abstract class CellDataBase: RoomDatabase() {

    abstract fun cellDataDao(): CellDataDao
}