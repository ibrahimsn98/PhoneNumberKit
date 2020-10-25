package me.ibrahimsn.lib.bottomsheet

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @param enableSearch Show/hide search feature.
 * @param searchType default = [SearchType.FULL_SEARCH]
 */

@Parcelize
class CountryPickerConfig(
    val enableSearch: Boolean,
    val searchType: SearchType? = SearchType.FULL_SEARCH,
) : Parcelable

enum class SearchType {
    COUNTRY_NAME_ONLY, COUNTRY_CODE_ONLY, FULL_SEARCH
}