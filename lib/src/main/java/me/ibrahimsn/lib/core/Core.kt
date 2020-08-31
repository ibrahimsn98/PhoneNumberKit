package me.ibrahimsn.lib.core

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import java.util.*

class Core {

    private var phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    fun parsePhoneNumber(
        number: String?,
        defaultRegion: String?
    ): Phonenumber.PhoneNumber? {
        return try {
            phoneUtil.parseAndKeepRawInput(
                number,
                defaultRegion?.toUpperCase(Locale.ROOT)
            )
        } catch(e: NumberParseException) {
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
                iso2?.toUpperCase(Locale.ROOT),
                PhoneNumberUtil.PhoneNumberType.MOBILE
            )
        } catch (e: Exception) {
            null
        }
    }

}