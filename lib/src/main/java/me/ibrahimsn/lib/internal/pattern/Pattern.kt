package me.ibrahimsn.lib.internal.pattern

import me.ibrahimsn.lib.internal.model.CaretString

interface Pattern {

    fun apply(number: CaretString, before: Int, count: Int): PatternResult
}
