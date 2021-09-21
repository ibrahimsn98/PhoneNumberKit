package me.ibrahimsn.lib.internal.util

import android.text.Editable
import android.text.TextWatcher

abstract class PhoneNumberTextWatcher : TextWatcher {

    override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
        // no-op
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        // no-op
    }

    override fun afterTextChanged(text: Editable?) {
        // no-op
    }
}
