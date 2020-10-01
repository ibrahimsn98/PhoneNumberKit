package me.ibrahimsn.lib.util

import android.widget.EditText
import me.ibrahimsn.lib.Constants.CHAR_PLUS

fun CharSequence?.prependPlus(): String {
    return StringBuilder()
        .append(CHAR_PLUS)
        .append(this)
        .toString()
}

fun Int.prependPlus(): String {
    return StringBuilder()
        .append(CHAR_PLUS)
        .append(this)
        .toString()
}

fun CharSequence?.startsWithPlus(): Boolean {
    return this?.startsWith(CHAR_PLUS) == true
}

fun String?.clearSpaces(): String? {
    return this?.replace("\\s+", "")
}

fun <T> Collection<T>.toRawString(): String {
    return this.joinToString("")
}

fun EditText.clear() {
    this.setText("")
}