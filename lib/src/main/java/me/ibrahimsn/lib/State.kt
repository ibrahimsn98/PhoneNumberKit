package me.ibrahimsn.lib

import me.ibrahimsn.lib.api.Country
import me.ibrahimsn.lib.internal.pattern.Pattern

data class State(
    val country: Country,
    val pattern: Pattern
)
