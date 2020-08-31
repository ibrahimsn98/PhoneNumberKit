package me.ibrahimsn.lib.util

fun CharSequence.prependPlus(): String {
    return StringBuilder()
        .append("+")
        .append(this)
        .toString()
}

fun Int.prependPlus(): String {
    return StringBuilder()
        .append("+")
        .append(this)
        .toString()
}

fun CharSequence.startsWithPlus(): Boolean {
    return this.startsWith("+")
}
