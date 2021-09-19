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
import me.ibrahimsn.lib.internal.core.Proxy
import me.ibrahimsn.lib.internal.ext.*
import me.ibrahimsn.lib.internal.io.FileReader
import me.ibrahimsn.lib.internal.pattern.CountryPattern
import me.ibrahimsn.lib.internal.ui.CountryPickerArguments
import me.ibrahimsn.lib.internal.ui.CountryPickerBottomSheet
import me.ibrahimsn.lib.internal.util.PhoneNumberTextWatcher
import java.util.*

class PhoneNumberKit private constructor(
    private val context: Context,
    private val excludedCountries: List<String>,
    private val admittedCountries: List<String>
) {

    private val supervisorJob = SupervisorJob()

    private val scope = CoroutineScope(supervisorJob + Dispatchers.Main)

    private val proxy: Proxy by lazy { Proxy(context) }

    private val state: MutableStateFlow<State> = MutableStateFlow(State.Ready)

    private var input: TextInputLayout? = null

    private var rawInput: CharSequence?
        get() = input?.editText?.text
        set(value) {
            input?.editText?.removeTextChangedListener(textWatcher)
            input?.editText?.clear()
            input?.editText?.append(value)
            input?.editText?.addTextChangedListener(textWatcher)
        }

    val isValid: Boolean get() = validate(rawInput)

    private val textWatcher = object : PhoneNumberTextWatcher() {
        override fun onTextChanged(text: String, isDeleting: Boolean) {
            scope.launch {
                val state = this@PhoneNumberKit.state.value
                if (state is State.Attached) {
                    val parsedNumber = proxy.parsePhoneNumber(
                        rawInput?.toString().orEmpty().clearSpaces(),
                        state.country.iso2
                    )

                    if (state.country.code != parsedNumber?.countryCode) {
                        val country = default {
                            getCountries().findCountry(parsedNumber?.countryCode)
                        }

                        if (country != null) {
                            val pattern = CountryPattern.create(
                                proxy.formatPhoneNumber(
                                    proxy.getExampleNumber(country.iso2)
                                )
                            )

                            this@PhoneNumberKit.state.value = State.Attached(
                                country = country,
                                pattern = pattern
                            )
                        }
                    }

                    if (text.isNotEmpty() && isDeleting.not()) {
                        rawInput = state.pattern.apply(text)
                    }
                }
            }
        }
    }

    private fun setCountry(countryIso2: String) = scope.launch {
        val country = default {
            getCountries().findCountry(countryIso2.trim().lowercase(Locale.ENGLISH))
        } ?: return@launch

        val pattern = CountryPattern.create(
            proxy.formatPhoneNumber(
                proxy.getExampleNumber(country.iso2)
            )
        )

        state.value = State.Attached(
            country = country,
            pattern = pattern
        )
    }

    fun attachToInput(input: TextInputLayout, defaultCountry: Int) {
        this.input = input
        scope.launch {
            val country = default {
                getCountries().findCountry(defaultCountry)
            }
            if (country != null) attachToInput(input, country)
        }
    }

    fun attachToInput(input: TextInputLayout, countryIso2: String) {
        this.input = input
        scope.launch {
            val country = default {
                getCountries().findCountry(countryIso2.trim().lowercase(Locale.ENGLISH))
            }
            if (country != null) attachToInput(input, country)
        }
    }

    private fun collectState() = scope.launch {
        state.collect { state ->
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
                }
            }
        }
    }

    private suspend fun getCountries() = io {
        FileReader.readAssetFile(context, ASSET_FILE_NAME)
            .toCountryList()
    }

    private fun attachToInput(input: TextInputLayout, country: Country) {
        input.editText?.inputType = InputType.TYPE_CLASS_PHONE
        input.editText?.addTextChangedListener(textWatcher)

        input.isStartIconVisible = true
        input.setStartIconTintList(null)

        collectState()
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
            CountryPickerBottomSheet.newInstance(
                CountryPickerArguments(
                    itemLayout,
                    searchEnabled,
                    excludedCountries,
                    admittedCountries
                )
            ).apply {
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

    private fun List<Country>.findCountry(
        countryCode: Int?
    ) = this.filter {
        admittedCountries.isEmpty() || admittedCountries.contains(it.iso2)
    }.filterNot {
        excludedCountries.contains(it.iso2)
    }.firstOrNull {
        it.code == countryCode
    }

    private fun List<Country>.findCountry(
        countryIso2: String?
    ) = this.filter {
        admittedCountries.isEmpty() || admittedCountries.contains(it.iso2)
    }.filterNot {
        excludedCountries.contains(it.iso2)
    }.firstOrNull {
        it.iso2 == countryIso2
    }

    private fun validate(number: CharSequence?): Boolean {
        if (number == null) return false
        return state.value.doIfAttached {
            proxy.validateNumber(number.toString(), country.iso2)
        } ?: false
    }

    companion object {
        const val ASSET_FILE_NAME = "countries.json"
    }

    class Builder(private val context: Context) {

        private var excludedCountries: List<String>? = null

        private var admittedCountries: List<String>? = null

        fun excludeCountries(countries: List<String>): Builder {
            this.excludedCountries = countries
            return this
        }

        fun admitCountries(countries: List<String>): Builder {
            this.admittedCountries = countries
            return this
        }

        fun build(): PhoneNumberKit {
            return PhoneNumberKit(
                context,
                excludedCountries.orEmpty(),
                admittedCountries.orEmpty()
            )
        }
    }
}
