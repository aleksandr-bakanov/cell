package bav.onecell.heroscreen

import bav.onecell.celllogic.conditions.Conditions
import bav.onecell.celllogic.picker.Picker
import bav.onecell.celllogic.rules.Rules
import bav.onecell.editor.Editor
import bav.onecell.model.hexes.Hex

interface HeroScreen {
    interface View: Editor.View {
        fun setPickerBackground(colorId: Int)
        fun setCellName(name: String)
        fun updateAvatars()
        fun updateHexesInBucket(type: Hex.Type, count: Int)
    }
    interface Presenter: Editor.Presenter, Rules.Presenter, Conditions.Presenter, Picker.Presenter {
        override fun initialize(cellIndex: Int)
        fun openMainMenu()
        fun increaseSelectedRulePriority()
        fun decreaseSelectedRulePriority()
        fun getCellCount(): Int
    }
}
