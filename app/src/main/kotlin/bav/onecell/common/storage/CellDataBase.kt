package bav.onecell.common.storage

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import android.content.Context
import bav.onecell.model.cell.Data

@Database(entities = [Data::class], version = 1)
@TypeConverters(Converters::class)
abstract class CellDataBase: RoomDatabase() {

    abstract fun cellDataDao(): CellDataDao
}