package me.ibrahimsn.lib

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import me.ibrahimsn.lib.Constants.CHAR_DASH
import me.ibrahimsn.lib.Constants.CHAR_PLUS
import me.ibrahimsn.lib.Constants.CHAR_SPACE
import me.ibrahimsn.lib.Constants.KEY_DASH
import me.ibrahimsn.lib.Constants.KEY_DIGIT
import me.ibrahimsn.lib.Constants.KEY_SPACE
import me.ibrahimsn.lib.bottomsheet.CountryPickerBottomSheet
import me.ibrahimsn.lib.core.Core
import me.ibrahimsn.lib.util.*
import java.util.*

class PhoneNumberKit(private val context: Context) {

    private val core = Core(context)

    private var input: TextInputLayout? = null

    private var country: Country? = null

    private var format: String = ""

    private var hasManualCountry = false

    private var rawInput: CharSequence?
        get() = input?.editText?.text
        set(value) {
            input?.tag = Constants.VIEW_TAG
            input?.editText?.clear()
            input?.editText?.append(value)
            input?.tag = null
        }

    private val textWatcher = object : PhoneNumberTextWatcher() {
        override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
            if (input?.tag != Constants.VIEW_TAG) {
                val parsedNumber = core.parsePhoneNumber(
                    rawInput.toString().clearSpaces(),
                    country?.iso2
                )

                // Update country flag and mask if detected as a different one
                if (country == null || country?.countryCode != parsedNumber?.countryCode) {
                    if (!hasManualCountry) {
                        setCountry(getCountry(parsedNumber?.countryCode))
                    }
                }

                if (count != 0) {
                    applyFormat()
                }
            }
        }
    }

    private fun applyFormat() {
        rawInput?.let { raw ->
            // Clear all of the non-digit characters from the phone number
            val pureNumber = raw.filter { i -> i.isDigit() }.toMutableList()

            // Add plus to beginning of the number
            pureNumber.add(0, CHAR_PLUS)

            for (i in format.indices) {
                if (pureNumber.size > i) {
                    // Put required format spaces
                    if (format[i] == KEY_SPACE && pureNumber[i] != CHAR_SPACE) {
                        pureNumber.add(i, CHAR_SPACE)
                        continue
                    }

                    // Put required format dashes
                    if (format[i] == KEY_DASH && pureNumber[i] != CHAR_DASH) {
                        pureNumber.add(i, CHAR_DASH)
                        continue
                    }
                }
            }

            rawInput = pureNumber.toRawString()
        }
    }

    private fun setCountry(country: Country?, isManual: Boolean = false) {
        country?.let {
            this.country = country

            // Setup country icon
            getFlagIcon(country.iso2)?.let { icon ->
                input?.startIconDrawable = icon
            }

            // Set text length limit according to the example phone number
            core.getExampleNumber(country.iso2)?.let { example ->

                if (isManual) {
                    hasManualCountry = true
                    rawInput = if (country.countryCode != example.countryCode) {
                        example.countryCode.prependPlus() + country.countryCode
                    } else {
                        country.countryCode.prependPlus()
                    }
                }
            }

            core.formatPhoneNumber(core.getExampleNumber(country.iso2))?.let { number ->
                input?.editText?.filters = arrayOf(InputFilter.LengthFilter(number.length))
                format = createNumberFormat(number)
                applyFormat()
            }
        }
    }

    // Creates a pattern like +90 506 555 55 55 -> +0010001000100100
    private fun createNumberFormat(number: String): String {
        var format = number.replace("(\\d)".toRegex(), KEY_DIGIT.toString())
        format = format.replace("(\\s)".toRegex(), KEY_SPACE.toString())
        return format
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
        setCountry(getCountry(defaultCountry) ?: Countries.list[0])
        rawInput = country?.countryCode?.prependPlus()
    }

    /**
     * Attaches to textInputLayout
     */
    fun attachToInput(input: TextInputLayout, countryIso2: String) {
        this.input = input
        input.editText?.inputType = InputType.TYPE_CLASS_PHONE
        input.editText?.addTextChangedListener(textWatcher)

        input.isStartIconVisible = true
        input.isStartIconCheckable = true
        input.setStartIconTintList(null)

        // Set initial country
        setCountry(getCountry(countryIso2.trim().toLowerCase(Locale.ENGLISH)) ?: Countries.list[0])
        rawInput = country?.countryCode?.prependPlus()
    }

    /**
     * Sets up country code picker bottomSheet
     */
    fun setupCountryPicker(
        activity: AppCompatActivity,
        itemLayout: Int = R.layout.item_country_picker,
        searchEnabled: Boolean = false
    ) {
        input?.setStartIconOnClickListener {
            CountryPickerBottomSheet.newInstance().apply {
                setup(itemLayout, searchEnabled)
                onCountrySelectedListener = { country ->
                    setCountry(country, true)
                }
                show(
                    activity.supportFragmentManager,
                    CountryPickerBottomSheet.TAG
                )
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
            ContextCompat.getDrawable(
                context, context.resources.getIdentifier(
                    "country_flag_$iso2",
                    "drawable",
                    context.packageName
                )
            )
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

    /**
     * Provides country for given country iso2
     */
    private fun getCountry(countryIso2: String?): Country? {
        for (country in Countries.list) {
            if (country.iso2 == countryIso2) {
                return country
            }
        }
        return null
    }
}
