package bav.onecell.heroscreen

import bav.onecell.common.router.Router

class HeroScreenPresenter(
        private val router: Router) : HeroScreen.Presenter {

    override fun openMainMenu() {
        router.goToMain()
    }
}
