package me.ibrahimsn.lib

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import me.ibrahimsn.lib.bottomsheet.CountryPickerBottomSheet
import me.ibrahimsn.lib.core.Core
import me.ibrahimsn.lib.util.PhoneNumberTextWatcher
import me.ibrahimsn.lib.util.PhoneNumberValidator
import me.ibrahimsn.lib.util.prependPlus
import me.ibrahimsn.lib.util.startsWithPlus

class PhoneNumberKit(private val context: Context) {

    private val core = Core()

    private var input: TextInputLayout? = null

    private var country: Country? = null

    private var rawInput: String?
        get() = input?.editText?.text?.toString()
        set(value) {
            input?.tag = Constants.VIEW_TAG
            input?.editText?.setText(value)
            value?.let {
                input?.editText?.setSelection(value.length)
            }
            input?.tag = null
        }

    val isValid: Boolean
        get() = PhoneNumberValidator.validate(rawInput)

    private val textWatcher = object: PhoneNumberTextWatcher() {
        override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (input?.tag != Constants.VIEW_TAG) {
                if (!text.isNullOrEmpty() && !text.startsWithPlus()) {
                    rawInput = text.prependPlus()
                }

                val parsedNumber = core.parsePhoneNumber(rawInput, country?.iso2)

                // Update country flag and mask if detected as a different one
                if (country == null || country?.countryCode != parsedNumber?.countryCode) {
                    setCountry(getCountry(parsedNumber?.countryCode))
                }

                // Update input text as formatted phone number
                core.formatPhoneNumber(parsedNumber)?.let { number ->
                    rawInput = number
                }
            }
        }
    }

    private fun setCountry(country: Country?, isManual: Boolean = false) {
        country?.let {
            this.country = country

            // Clear input if a country code selected manually
            if (isManual) {
                rawInput = country.countryCode.prependPlus()
            }

            // Setup country icon
            getFlagIcon(country.iso2)?.let { icon ->
                input?.startIconDrawable = icon
            }

            // Set text length limit according to the example phone number
            core.formatPhoneNumber(core.getExampleNumber(country.iso2))?.let { number ->
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

        // Set initial country
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
     * Parses raw phone number into phone object
     */
    fun parsePhoneNumber(number: String?, defaultRegion: String?): Phone? {
        core.parsePhoneNumber(number, defaultRegion)?.let { phone ->
            return Phone(
                nationalNumber = phone.nationalNumber,
                countryCode = phone.countryCode,
                rawInput = phone.rawInput,
                numberOfLeadingZeros = phone.numberOfLeadingZeros
            )
        }
        return null
    }

    /**
     * Formats raw phone number into international phone
     */
    fun formatPhoneNumber(number: String?, defaultRegion: String?): String? {
        return core.formatPhoneNumber(core.parsePhoneNumber(number, defaultRegion))
    }

    /**
     * Provides an example phone number according to country iso2 code
     */
    fun getExampleNumber(iso2: String?): Phone? {
        core.getExampleNumber(iso2)?.let { phone ->
            return Phone(
                nationalNumber = phone.nationalNumber,
                countryCode = phone.countryCode,
                rawInput = phone.rawInput,
                numberOfLeadingZeros = phone.numberOfLeadingZeros
            )
        }
        return null
    }

    /**
     * Provides country flag icon for given country iso2 code
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
