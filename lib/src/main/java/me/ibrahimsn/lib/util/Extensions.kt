package me.ibrahimsn.lib.util

import android.view.View
import android.widget.EditText
import me.ibrahimsn.lib.Constants.CHAR_PLUS

internal fun CharSequence?.prependPlus(): String {
    return StringBuilder()
        .append(CHAR_PLUS)
        .append(this)
        .toString()
}

internal fun Int.prependPlus(): String {
    return StringBuilder()
        .append(CHAR_PLUS)
        .append(this)
        .toString()
}

internal fun CharSequence?.startsWithPlus(): Boolean {
    return this?.startsWith(CHAR_PLUS) == true
}

internal fun String?.clearSpaces(): String? {
    return this?.replace("\\s+", "")
}

internal fun <T> Collection<T>.toRawString(): String {
    return this.joinToString("")
}

internal fun EditText.clear() {
    this.setText("")
}

internal fun View.showIf(statement: Boolean) {
    visibility = if (statement) View.VISIBLE else View.GONE
}
