package bav.onecell.main

import bav.onecell.common.router.Router

class MainPresenter(
        private val router: Router) : Main.Presenter {

    companion object {
        private const val TAG = "MainPresenter"
    }

    override fun openPreBattleView() {
        router.goToCellsForBattleSelection()
    }

    override fun openCellsListView() {
        router.goToCellsList()
    }
    //endregion
}