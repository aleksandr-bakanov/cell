package bav.onecell.heroscreen

import bav.onecell.celllogic.conditions.Conditions
import bav.onecell.celllogic.rules.Rules
import bav.onecell.editor.Editor

interface HeroScreen {
    interface View: Editor.View {

    }
    interface Presenter: Editor.Presenter, Rules.Presenter, Conditions.Presenter {
        fun openMainMenu()
    }
}
