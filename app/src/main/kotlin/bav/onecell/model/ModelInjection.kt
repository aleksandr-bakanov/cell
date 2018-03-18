package bav.onecell.model

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ModelModule {
    @Provides
    @Singleton
    fun provideCellRepository(): RepositoryContract.CellRepo {
        return CellRepository()
    }
}