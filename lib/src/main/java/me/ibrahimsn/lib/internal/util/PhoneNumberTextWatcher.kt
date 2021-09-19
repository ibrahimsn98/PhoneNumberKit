package me.ibrahimsn.lib.internal.util

import android.text.Editable
import android.text.TextWatcher

internal abstract class PhoneNumberTextWatcher : TextWatcher {

    abstract fun onTextChanged(text: String, isDeleting: Boolean)

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
        onTextChanged(text?.toString().orEmpty(), count == 0)
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        // no-op
    }

    override fun afterTextChanged(text: Editable?) {
        // no-op
    }
}
