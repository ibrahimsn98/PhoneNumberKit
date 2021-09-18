package me.ibrahimsn.lib

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import me.ibrahimsn.lib.api.Country
import me.ibrahimsn.lib.api.Phone
import me.ibrahimsn.lib.internal.Constants.CHAR_DASH
import me.ibrahimsn.lib.internal.Constants.CHAR_PLUS
import me.ibrahimsn.lib.internal.Constants.CHAR_SPACE
import me.ibrahimsn.lib.internal.Constants.KEY_DASH
import me.ibrahimsn.lib.internal.Constants.KEY_DIGIT
import me.ibrahimsn.lib.internal.Constants.KEY_SPACE
import me.ibrahimsn.lib.internal.ui.CountryPickerBottomSheet
import me.ibrahimsn.lib.internal.Constants
import me.ibrahimsn.lib.internal.core.Proxy
import me.ibrahimsn.lib.internal.ext.*
import me.ibrahimsn.lib.internal.ext.clear
import me.ibrahimsn.lib.internal.ext.clearSpaces
import me.ibrahimsn.lib.internal.ext.toCountryList
import me.ibrahimsn.lib.internal.ext.toRawString
import me.ibrahimsn.lib.internal.io.FileReader
import me.ibrahimsn.lib.internal.util.PhoneNumberTextWatcher
import java.util.*

class PhoneNumberKit private constructor(
    private val context: Context,
    private val excludedCountries: List<String>?
) {

    private val supervisorJob = SupervisorJob()

    private val scope = CoroutineScope(supervisorJob + Dispatchers.Default)

    private val proxy: Proxy by lazy { Proxy(context) }

    private var input: TextInputLayout? = null

    private var state: State = State()

    private var rawInput: CharSequence?
        get() = input?.editText?.text
        set(value) {
            input?.tag = Constants.VIEW_TAG
            input?.editText?.clear()
            input?.editText?.append(value)
            input?.tag = null
        }

    val isValid: Boolean get() = validate(rawInput)

    private val textWatcher = object : PhoneNumberTextWatcher() {
        override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
            scope.launch {
                if (input?.tag != Constants.VIEW_TAG) {
                    val parsedNumber = proxy.parsePhoneNumber(
                        rawInput.toString().clearSpaces(),
                        state?.country?.iso2
                    )

                    // Update country flag and mask if detected as a different one
                    if (state?.country == null || state?.country?.code != parsedNumber?.countryCode) {
                        val country = getCountries().findCountry(parsedNumber?.countryCode)

                        launch(Dispatchers.Main) {
                            setCountry(country)
                        }
                    }

                    if (count != 0) {
                        launch(Dispatchers.Main) {
                            applyFormat()
                        }
                    }

                    validate(rawInput)
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

            if (pureNumber.size > 1) {
                rawInput = pureNumber.toRawString()
            }
        }
    }

    private fun setCountry(country: Country?, prefill: Boolean = false) {
        country?.let {
            this.country = country

            // Setup country icon
            getFlagIcon(country.iso2)?.let { icon ->
                input?.startIconDrawable = icon
            }

            // Set text length limit according to the example phone number
            proxy.getExampleNumber(country.iso2)?.let { example ->
                if (prefill) {
                    rawInput = if (country.code != example.countryCode) {
                        example.countryCode.prependPlus() + country.code
                    } else {
                        country.code.prependPlus()
                    }
                }
            }

            proxy.formatPhoneNumber(proxy.getExampleNumber(country.iso2))?.let { number ->
                input?.editText?.filters = arrayOf(InputFilter.LengthFilter(number.length))
                format = createNumberFormat(number)
                applyFormat()
            }
        }
    }

    private fun setCountry(countryIso2: String) {
        scope.launch {
            val countries = getCountries()
            val country = countries.findCountry(countryIso2.trim().lowercase(Locale.ENGLISH))
            launch(Dispatchers.Main) {
                setCountry(
                    country = country ?: countries[0],
                    prefill = true
                )
            }
        }
    }

    private suspend fun getCountries() = withContext(Dispatchers.IO) {
        FileReader.readAssetFile(context, ASSET_FILE_NAME)
            .toCountryList()
    }

    // Creates a pattern like +90 506 555 55 55 -> +0010001000100100
    private fun createNumberFormat(number: String): String {
        var format = number.replace("(\\d)".toRegex(), KEY_DIGIT.toString())
        format = format.replace("(\\s)".toRegex(), KEY_SPACE.toString())
        return format
    }

    fun attachToInput(input: TextInputLayout, defaultCountry: Int) {
        this.input = input
        scope.launch {
            val countries = getCountries()
            val country = countries.findCountry(defaultCountry)
            launch(Dispatchers.Main) {
                attachToInput(input, country ?: countries.first())
            }
        }
    }

    fun attachToInput(input: TextInputLayout, countryIso2: String) {
        this.input = input
        scope.launch {
            val countries = getCountries()
            val country = countries.findCountry(countryIso2.trim().lowercase(Locale.ENGLISH))
            launch(Dispatchers.Main) {
                attachToInput(input, country ?: countries.first())
            }
        }
    }

    private fun attachToInput(input: TextInputLayout, country: Country) {
        input.editText?.inputType = InputType.TYPE_CLASS_PHONE
        input.editText?.addTextChangedListener(textWatcher)

        input.isStartIconVisible = true
        input.setStartIconTintList(null)

        setCountry(country = country, prefill = true)
    }

    /**
     * Sets up country code picker bottomSheet
     */
    fun setupCountryPicker(
        activity: AppCompatActivity,
        itemLayout: Int = R.layout.item_country_picker,
        searchEnabled: Boolean = false
    ) {
        input?.isStartIconCheckable = true
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
        proxy.parsePhoneNumber(number, defaultRegion)?.let { phone ->
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
        return proxy.formatPhoneNumber(proxy.parsePhoneNumber(number, defaultRegion))
    }

    /**
     * Provides an example phone number according to country iso2 code
     */
    fun getExampleNumber(iso2: String?): Phone? {
        proxy.getExampleNumber(iso2)?.let { phone ->
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

    private fun List<Country>.findCountry(
        countryCode: Int?
    ) = this.firstOrNull {
        it.code == countryCode
    }

    private fun List<Country>.findCountry(
        countryIso2: String?
    ) = this.firstOrNull {
        it.iso2 == countryIso2
    }

    private fun validate(number: CharSequence?): Boolean {
        if (number == null) return false
        return proxy.validateNumber(number.toString(), country?.iso2)
    }

    companion object {
        private const val ASSET_FILE_NAME = "countries.json"
    }

    class Builder(private val context: Context) {

        private var excludedCountries: List<String>? = null

        fun excludeCountries(countries: List<String>): Builder {
            this.excludedCountries = countries
            return this
        }

        fun build(): PhoneNumberKit {
            return PhoneNumberKit(context, excludedCountries)
        }
    }
}
