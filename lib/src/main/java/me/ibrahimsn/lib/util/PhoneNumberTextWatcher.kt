package me.ibrahimsn.lib.util

import android.text.Editable
import android.text.TextWatcher

abstract class PhoneNumberTextWatcher : TextWatcher {

    override fun afterTextChanged(text: Editable?) {
        // no-op
    }

    override fun beforeTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
        // no-op
    }
}
