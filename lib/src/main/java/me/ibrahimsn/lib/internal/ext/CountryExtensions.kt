package me.ibrahimsn.lib.internal.ext

import me.ibrahimsn.lib.api.Country
import org.json.JSONArray

internal fun String?.toCountryList(): List<Country> {
    val countries = mutableListOf<Country>()
    try {
        val json = JSONArray(this)
        for (i in 0 until json.length()) {
            val country = json.getJSONObject(i)
            countries.add(
                Country(
                    iso2 = country.getString("iso2"),
                    name = country.getString("name"),
                    code = country.getInt("code")
                )
            )
        }
    } catch (e: Exception) {
        // ignored
    }
    return countries
}
