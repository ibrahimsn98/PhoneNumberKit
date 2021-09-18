package me.ibrahimsn.lib.internal.pattern

class Pattern(private val data: CharArray) {

    val length: Int
        get() = data.size

    val indices: IntRange
        get() = data.indices

    fun get(i: Int) = data[i]
}
