package bav.onecell.heroscreen

import bav.onecell.editor.Editor

interface HeroScreen {
    interface View: Editor.View {

    }
    interface Presenter: Editor.Presenter {
        fun openMainMenu()
    }
}
