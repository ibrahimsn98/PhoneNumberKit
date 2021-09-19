package me.ibrahimsn.lib.internal.ui

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class CountryPickerArguments(
    val itemLayout: Int,
    val isSearchEnabled: Boolean,
    val excludedCountries: List<String>,
    val admittedCountries: List<String>
) : Parcelable
