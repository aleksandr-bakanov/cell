package bav.onecell.celllogic.picker

import io.reactivex.Observable

interface Picker {
    interface View {

    }

    interface Presenter {
        fun pickerOptionsCount(): Int
        fun pickerOptionOnClick(position: Int)
        fun getPickerOptionTitle(position: Int): Int
        fun optionsUpdateNotifier(): Observable<Unit>
    }
}