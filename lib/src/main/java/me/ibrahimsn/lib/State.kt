package me.ibrahimsn.lib

import me.ibrahimsn.lib.api.Country
import me.ibrahimsn.lib.internal.pattern.CountryPattern

sealed class State {

    object Ready : State()

    data class Attached(
        val country: Country,
        val pattern: CountryPattern
    ) : State()
}
