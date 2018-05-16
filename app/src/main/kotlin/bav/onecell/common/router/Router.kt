package bav.onecell.common.router

import io.reactivex.Observable

interface Router {

    enum class Window {
        MAIN, CELLS_LIST
    }

    fun windowChange(): Observable<Window>

    fun goToMain()

    fun goToBattle()

    fun goToCellsList()
}