package me.ibrahimsn.lib.bottomsheet

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.text.isDigitsOnly
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_country_picker.*
import me.ibrahimsn.lib.Countries
import me.ibrahimsn.lib.Country
import me.ibrahimsn.lib.R

class CountryPickerBottomSheet : BottomSheetDialogFragment() {

    private val countryAdapter = CountryAdapter()

    var onCountrySelectListener: ((Country?) -> Unit)? = null

    private var searchTimer: CountDownTimer? = null

    private val config by lazy { arguments?.getParcelable<CountryPickerConfig>(COUNTRY_PICKER_CONFIG) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.ThemeOverlay_PhoneNumberKit_BottomSheetDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.bottom_sheet_country_picker,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (config?.enableSearch == true) {
            searchTimer = object : CountDownTimer(350, 50) {
                override fun onFinish() {
                    val countries = findCountries(editTextSearch.text.toString())
                    countryAdapter.setup(countries)
                }

                override fun onTick(p0: Long) {
                }
            }
            editTextSearch.visibility = View.VISIBLE

            editTextSearch.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(query: Editable?) {
                    searchTimer?.cancel()
                    searchTimer?.start()
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            })
        }

        recyclerView.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(context)
            adapter = countryAdapter
        }
        countryAdapter.apply {
            setup(Countries.list)
            onItemClickListener = {
                onCountrySelectListener?.invoke(it)
                dismiss()
            }
        }
        imageButtonClose.setOnClickListener {
            dismiss()
        }
    }

    private fun findCountries(query: String): List<Country> {
        if (query.isEmpty())
            return Countries.list
        else
            return when (config?.searchType) {
                SearchType.FULL_SEARCH -> {
                    Countries.list.filter {
                        it.name.contains(
                            query,
                            ignoreCase = true
                        ) || it.countryCode.toString().contains(query, ignoreCase = true)
                    }
                }
                SearchType.COUNTRY_CODE_ONLY -> {
                    return if (query.isDigitsOnly())
                        Countries.list.filter {
                            it.countryCode.toString().contains(query, ignoreCase = true)
                        }
                    else
                        Countries.list
                }
                SearchType.COUNTRY_NAME_ONLY -> {
                    return if (query.isDigitsOnly().not())
                        Countries.list.filter { it.name.contains(query, ignoreCase = true) }
                    else
                        Countries.list
                }
                else -> Countries.list
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchTimer?.cancel()
        searchTimer = null
    }

    companion object {
        const val TAG = "tag-country-picker"
        const val COUNTRY_PICKER_CONFIG = "country-picker-config"

        fun newInstance(config: CountryPickerConfig) = CountryPickerBottomSheet().apply {
            arguments = bundleOf(COUNTRY_PICKER_CONFIG to config)
        }
    }
}
