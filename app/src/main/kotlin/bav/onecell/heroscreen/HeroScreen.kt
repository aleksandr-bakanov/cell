package bav.onecell.heroscreen

import bav.onecell.celllogic.conditions.Conditions
import bav.onecell.celllogic.picker.Picker
import bav.onecell.celllogic.rules.Rules
import bav.onecell.editor.Editor

interface HeroScreen {
    interface View: Editor.View {
        fun setPickerBackground(colorId: Int)
    }
    interface Presenter: Editor.Presenter, Rules.Presenter, Conditions.Presenter, Picker.Presenter {
        fun openMainMenu()
    }
}
