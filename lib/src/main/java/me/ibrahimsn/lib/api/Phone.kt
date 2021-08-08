package me.ibrahimsn.lib.api

data class Phone(
    val nationalNumber: Long?,
    val countryCode: Int?,
    val rawInput: String?,
    val numberOfLeadingZeros: Int?
)
