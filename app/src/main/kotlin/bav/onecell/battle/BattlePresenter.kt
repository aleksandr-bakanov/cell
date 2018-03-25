package bav.onecell.battle

import bav.onecell.common.router.Router
import bav.onecell.model.RepositoryContract

class BattlePresenter(
        private val view: Battle.View,
        private val cellRepository: RepositoryContract.CellRepo,
        private val router: Router) : Battle.Presenter {
}