package bav.onecell.common.router

import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class RouterImpl : Router {

    private val windowChanger = BehaviorSubject.create<Router.Window>()

    override fun windowChange(): Observable<Router.Window> = windowChanger

    override fun goToMain() {
        windowChanger.onNext(Router.Window.MAIN)
    }

    override fun goToBattle() {

    }

    override fun goToCellsList() {
        windowChanger.onNext(Router.Window.CELLS_LIST)
    }
}
