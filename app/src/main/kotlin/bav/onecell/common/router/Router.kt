package bav.onecell.common.router

import android.os.Bundle
import io.reactivex.Observable

interface Router {

    data class Window(val type: WindowType, val args: Bundle? = null)

    enum class WindowType {
        MAIN, CELLS_LIST, BATTLE_CELLS_SELECTION, BATTLE, CELL_EDITOR, RULES_EDITOR, CONDITIONS_EDITOR
    }

    fun windowChange(): Observable<Window>

    fun goToMain()

    fun goToBattle()

    fun goToCellsList()

    fun goToCellEditor(index: Int)
}