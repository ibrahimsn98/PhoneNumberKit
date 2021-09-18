package me.ibrahimsn.lib

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
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
import me.ibrahimsn.lib.internal.pattern.Pattern
import me.ibrahimsn.lib.internal.util.PhoneNumberTextWatcher
import java.util.*

class PhoneNumberKit private constructor(
    private val context: Context,
    private val excludedCountries: List<String>?
) {

    private val supervisorJob = SupervisorJob()

    private val scope = CoroutineScope(supervisorJob + Dispatchers.Main)

    private val proxy: Proxy by lazy { Proxy(context) }

    private var input: TextInputLayout? = null

    private val state: MutableStateFlow<State> = MutableStateFlow(State.Ready)

    private var rawInput: CharSequence?
        get() = input?.editText?.text
        set(value) {
            input?.tag = Constants.VIEW_TAG
            input?.editText?.clear()
            input?.editText?.append(value)
            input?.tag = null
        }

    val isValid: Boolean get() = validate(rawInput)

    private fun collectState() = scope.launch {
        state.collect {
            renderState(it)
        }
    }

    private val textWatcher = object : PhoneNumberTextWatcher() {
        override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
            scope.launch {
                if (input?.tag != Constants.VIEW_TAG) {
                    val state = this@PhoneNumberKit.state.value
                    if (state is State.Attached) {
                        val parsedNumber = proxy.parsePhoneNumber(
                            rawInput.toString().clearSpaces(),
                            state.country.iso2
                        )

                        // Update country flag and mask if detected as a different one
                        if (state.country.code != parsedNumber?.countryCode) {
                            val country = getCountries().getCountry(parsedNumber?.countryCode)

                            val pattern = createNumberFormat(
                                proxy.formatPhoneNumber(
                                    proxy.getExampleNumber(country.iso2)
                                )
                            )

                            this@PhoneNumberKit.state.value = State.Attached(
                                country = country,
                                pattern = Pattern(pattern),
                                shouldFormat = count != 0
                            )
                        }
                    }
                }
            }
        }
    }

    private fun applyFormat(pattern: Pattern) {
        rawInput?.let { raw ->
            // Clear all of the non-digit characters from the phone number
            val pureNumber = raw.filter { i -> i.isDigit() }.toMutableList()

            // Add plus to beginning of the number
            pureNumber.add(0, CHAR_PLUS)

            for (i in pattern.indices) {
                if (pureNumber.size > i) {
                    // Put required format spaces
                    if (pattern.get(i) == KEY_SPACE && pureNumber[i] != CHAR_SPACE) {
                        pureNumber.add(i, CHAR_SPACE)
                        continue
                    }

                    // Put required format dashes
                    if (pattern.get(i)  == KEY_DASH && pureNumber[i] != CHAR_DASH) {
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

    private fun renderState(state: State) {
        when (state) {
            is State.Ready -> {}
            is State.Attached -> {
                getFlagIcon(state.country.iso2)?.let { icon ->
                    input?.startIconDrawable = icon
                }
                if (rawInput.isNullOrEmpty()) {
                    rawInput = state.country.code.prependPlus()
                }
                input?.editText?.filters = arrayOf(
                    InputFilter.LengthFilter(state.pattern.length)
                )
                if (state.shouldFormat) applyFormat(state.pattern)
            }
        }
    }

    private fun setCountry(countryIso2: String) {
        scope.launch {
            val countries = getCountries()
            val country = countries.getCountry(countryIso2.trim().lowercase(Locale.ENGLISH))

            val pattern = createNumberFormat(
                proxy.formatPhoneNumber(
                    proxy.getExampleNumber(country.iso2)
                )
            )

            state.value = State.Attached(
                country = country,
                pattern = Pattern(pattern)
            )
        }
    }

    private suspend fun getCountries() = withContext(Dispatchers.IO) {
        FileReader.readAssetFile(context, ASSET_FILE_NAME)
            .toCountryList()
    }

    private fun createNumberFormat(number: String?): CharArray {
        return number.orEmpty()
            .replace("(\\d)".toRegex(), KEY_DIGIT.toString())
            .replace("(\\s)".toRegex(), KEY_SPACE.toString())
            .toCharArray()
    }

    fun attachToInput(input: TextInputLayout, defaultCountry: Int) {
        this.input = input
        scope.launch {
            val countries = getCountries()
            val country = countries.getCountry(defaultCountry)
            launch(Dispatchers.Main) {
                attachToInput(input, country)
            }
        }
    }

    fun attachToInput(input: TextInputLayout, countryIso2: String) {
        this.input = input
        scope.launch {
            val countries = getCountries()
            val country = countries.getCountry(countryIso2.trim().lowercase(Locale.ENGLISH))
            launch(Dispatchers.Main) {
                attachToInput(input, country)
            }
        }
    }

    private fun attachToInput(input: TextInputLayout, country: Country) {
        input.editText?.inputType = InputType.TYPE_CLASS_PHONE
        input.editText?.addTextChangedListener(textWatcher)

        input.isStartIconVisible = true
        input.setStartIconTintList(null)

        setCountry(country.iso2)
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
                    setCountry(country?.iso2.orEmpty())
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

    private fun List<Country>.getCountry(
        countryCode: Int?
    ) = this.first {
        it.code == countryCode
    }

    private fun List<Country>.getCountry(
        countryIso2: String?
    ) = this.first {
        it.iso2 == countryIso2
    }

    private fun validate(number: CharSequence?): Boolean {
        if (number == null) return false
        return true //proxy.validateNumber(number.toString(), country?.iso2)
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
