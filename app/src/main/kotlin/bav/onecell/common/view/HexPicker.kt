package bav.onecell.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import bav.onecell.R

class HexPicker(context: Context, attributeSet: AttributeSet): ConstraintLayout(context, attributeSet) {
    init {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater)
                ?.inflate(R.layout.view_hex_picker, this)
    }

    fun setButtonClickListener(listener: (v: View) -> Unit) {
        val button = findViewById<AppCompatImageButton>(R.id.buttonHex)
        button.setOnClickListener { listener.invoke(this) }
    }
}
