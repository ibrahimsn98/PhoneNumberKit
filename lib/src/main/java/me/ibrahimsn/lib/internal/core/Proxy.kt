package me.ibrahimsn.lib.internal.core

import android.content.Context
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber
import me.ibrahimsn.lib.internal.ext.prependPlus
import me.ibrahimsn.lib.internal.ext.startsWithPlus
import java.util.*

internal class Proxy(context: Context) {

    private var phoneUtil: PhoneNumberUtil = PhoneNumberUtil.createInstance(context)

    fun parsePhoneNumber(
        number: String?,
        defaultRegion: String?
    ): Phonenumber.PhoneNumber? {
        return try {
            phoneUtil.parseAndKeepRawInput(
                if (number.startsWithPlus()) number else number.prependPlus(),
                defaultRegion?.uppercase(Locale.ROOT)
            )
        } catch (e: NumberParseException) {
            null
        }
    }

    fun formatPhoneNumber(phoneNumber: Phonenumber.PhoneNumber?): String? {
        return try {
            phoneUtil.format(
                phoneNumber,
                PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Provides an example phone number according to country iso2 code
     */
    fun getExampleNumber(iso2: String?): Phonenumber.PhoneNumber? {
        return try {
            phoneUtil.getExampleNumberForType(
                iso2?.uppercase(Locale.ROOT),
                PhoneNumberUtil.PhoneNumberType.MOBILE
            )
        } catch (e: Exception) {
            null
        }
    }

    fun validateNumber(number: String?, countryCode: String?): Boolean {
        return try {
            val p = parsePhoneNumber(number, countryCode)
            return phoneUtil.isValidNumber(p)
        } catch (e: Exception) {
            false
        }
    }
}
