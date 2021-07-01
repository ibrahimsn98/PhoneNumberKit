package me.ibrahimsn.lib.util

import android.text.Editable
import android.text.TextWatcher

internal abstract class PhoneNumberTextWatcher : TextWatcher {

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        // no-op
    }

    override fun afterTextChanged(text: Editable?) {
        // no-op
    }
}
