package bav.onecell.main

import bav.onecell.common.router.Router

class MainPresenter(
        private val router: Router) : Main.Presenter {

    companion object {
        private const val TAG = "MainPresenter"
    }

    override fun openBattleView() {
        router.goToBattle()
    }

    override fun openCellsListView() {
        router.goToCellsList()
    }
    //endregion
}