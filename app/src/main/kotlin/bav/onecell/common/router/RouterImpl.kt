package bav.onecell.common.router

import android.os.Bundle
import bav.onecell.editor.EditorFragment
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

class RouterImpl : Router {

    private val windowChanger = BehaviorSubject.create<Router.Window>()

    override fun windowChange(): Observable<Router.Window> = windowChanger

    override fun goToMain() {
        windowChanger.onNext(Router.Window(Router.WindowType.MAIN))
    }

    override fun goToBattle() {

    }

    override fun goToCellsList() {
        windowChanger.onNext(Router.Window(Router.WindowType.CELLS_LIST))
    }

    override fun goToCellEditor(index: Int) {
        val bundle = Bundle()
        bundle.putInt(EditorFragment.CELL_INDEX, index)
        windowChanger.onNext(Router.Window(Router.WindowType.CELL_EDITOR, bundle))
    }
}
