package bav.onecell.model

import bav.onecell.common.storage.Storage
import bav.onecell.model.hexes.HexMath
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ModelModule {
    @Provides
    @Singleton
    fun provideCellRepository(hexMath: HexMath, storage: Storage): RepositoryContract.CellRepo {
        return CellRepository(hexMath, storage)
    }
}