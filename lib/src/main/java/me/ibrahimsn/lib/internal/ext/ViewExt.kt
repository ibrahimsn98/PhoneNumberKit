package me.ibrahimsn.lib.internal.ext

import android.view.View
import android.widget.EditText

internal fun EditText.clear() {
    this.setText("")
}

internal fun View.showIf(statement: Boolean) {
    visibility = if (statement) View.VISIBLE else View.GONE
}
