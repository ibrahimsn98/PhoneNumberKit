package me.ibrahimsn.lib.util

import android.util.Patterns

object PhoneNumberValidator {

    fun validate(input: CharSequence?): Boolean {
        return !input.isNullOrEmpty() && Patterns.PHONE.matcher(input).find()
    }
}
