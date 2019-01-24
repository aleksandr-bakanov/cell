package bav.onecell.main

import bav.onecell.model.RepositoryContract

class MainPresenter(private val cellRepo: RepositoryContract.CellRepo) : Main.Presenter {
    companion object {
        private const val TAG = "MainPresenter"
    }
    //endregion
}