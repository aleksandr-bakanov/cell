package bav.onecell.common.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import bav.onecell.R

class HexPicker(context: Context, attributeSet: AttributeSet): ConstraintLayout(context, attributeSet) {
    init {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater)
                ?.inflate(R.layout.view_hex_picker, this)
    }

    val buttonHex: AppCompatImageButton get() = findViewById(R.id.buttonHex)
    val textViewHexCount: TextView get() = findViewById(R.id.textViewHexCount)
    val selection: View get() = findViewById<View>(R.id.selection)

    fun setButtonClickListener(listener: (v: View) -> Unit) {
        val button = findViewById<AppCompatImageButton>(R.id.buttonHex)
        button.setOnClickListener { listener.invoke(this) }
    }

    fun setButtonLongClickListener(listener: (v: View) -> Unit) {
        val button = findViewById<AppCompatImageButton>(R.id.buttonHex)
        button.setOnLongClickListener {
            listener.invoke(this)
            true
        }
    }

    fun setHexCount(value: Int) {
        val textField = findViewById<TextView>(R.id.textViewHexCount)
        textField.text = value.toString()
    }
}
