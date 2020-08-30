package me.ibrahimsn.lib

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import me.ibrahimsn.lib.bottomsheet.CountryPickerBottomSheet
import me.ibrahimsn.lib.util.PhoneNumberTextWatcher
import me.ibrahimsn.lib.util.PhoneNumberValidator
import me.ibrahimsn.lib.util.prependPlus
import me.ibrahimsn.lib.util.startsWithPlus
import java.util.*

class PhoneNumberKit(private val context: Context) {

    private var phoneUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    private var input: TextInputLayout? = null

    private var country: Country? = null

    private var rawInput: String?
        get() = input?.editText?.text?.toString()
        set(value) {
            input?.tag = Constants.VIEW_TAG
            input?.editText?.setText(value)
            input?.tag = null
            value?.let {
                input?.editText?.setSelection(value.length)
            }
        }

    val isValid: Boolean
        get() = PhoneNumberValidator.validate(rawInput)

    private val textWatcher = object: PhoneNumberTextWatcher() {
        override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (input?.tag != Constants.VIEW_TAG) {
                if (!text.isNullOrEmpty() && !text.startsWithPlus()) {
                    rawInput = text.prependPlus()
                }

                val parsedNumber = parsePhoneNumber(rawInput)

                formatPhoneNumber(parsedNumber)?.let { number ->
                    rawInput = number
                }

                if (country == null || country?.countryCode != parsedNumber?.countryCode) {
                    setCountry(getCountry(parsedNumber?.countryCode))
                }
            }
        }
    }

    private fun setCountry(country: Country?, isManual: Boolean = false) {
        country?.let {
            this.country = country

            if (isManual) {
                rawInput = country.countryCode.prependPlus()
            }

            // Setup country icon
            getFlagIcon(country.iso2)?.let { icon ->
                input?.startIconDrawable = icon
            }

            // Set text length limit according to the example phone number
            formatPhoneNumber(getExampleNumber(country.iso2))?.let { number ->
                input?.editText?.filters = arrayOf(InputFilter.LengthFilter(number.length))
            }
        }
    }

    /**
     * Attaches to textInputLayout
     */
    fun attachToInput(input: TextInputLayout, defaultCountry: Int) {
        this.input = input
        input.editText?.inputType = InputType.TYPE_CLASS_PHONE
        input.editText?.addTextChangedListener(textWatcher)

        input.isStartIconVisible = true
        input.isStartIconCheckable = true
        input.setStartIconTintList(null)

        setCountry(getCountry(defaultCountry) ?: Countries.list[0], true)
    }

    /**
     * Sets up country code picker bottomSheet
     */
    fun setupCountryPicker(activity: AppCompatActivity) {
        input?.setStartIconOnClickListener {
            CountryPickerBottomSheet.newInstance().apply {
                onCountrySelectListener = { country ->
                    setCountry(country, true)
                }
                show(activity.supportFragmentManager, CountryPickerBottomSheet.TAG)
            }
        }
    }

    /**
     * Provides an example phone number according to country iso2
     */
    fun getExampleNumber(iso2: String?): PhoneNumber? {
        return try {
            phoneUtil.getExampleNumberForType(
                iso2?.toUpperCase(Locale.ROOT),
                PhoneNumberUtil.PhoneNumberType.MOBILE
            )
        } catch (e: Exception) {
            null
        }
    }

    fun parsePhoneNumber(number: String?): PhoneNumber? {
        val defaultRegion = if (country != null) country?.iso2?.toUpperCase(Locale.ROOT) else ""
        return try {
            phoneUtil.parseAndKeepRawInput(number, defaultRegion)
        } catch(e: NumberParseException) {
            null
        }
    }

    fun formatPhoneNumber(phoneNumber: PhoneNumber?): String? {
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
     * Provides country flag icon for given country iso2
     */
    fun getFlagIcon(iso2: String?): Drawable? {
        return try {
            ContextCompat.getDrawable(context, context.resources.getIdentifier(
                "country_flag_$iso2",
                "drawable",
                context.packageName
            ))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Provides country for given country code
     */
    fun getCountry(countryCode: Int?): Country? {
        for (country in Countries.list) {
            if (country.countryCode == countryCode) {
                return country
            }
        }
        return null
    }
}
