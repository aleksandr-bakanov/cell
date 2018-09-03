package bav.onecell.common.storage

import android.arch.persistence.room.Room
import android.content.Context
import bav.onecell.model.hexes.HexMath
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class StorageModule {

    @Provides
    @Singleton
    fun provideStorage(@Named("app_context") context: Context, dataBase: CellDataBase, hexMath: HexMath): Storage {
        return StorageImpl(context, dataBase, hexMath)
    }

    @Provides
    @Singleton
    fun provideCellDataBase(@Named("app_context") context: Context): CellDataBase {
        return Room.databaseBuilder(context, CellDataBase::class.java, "cells.db").build()
    }
}
