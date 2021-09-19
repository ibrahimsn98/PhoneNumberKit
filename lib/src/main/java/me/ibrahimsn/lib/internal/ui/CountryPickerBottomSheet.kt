package me.ibrahimsn.lib.internal.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_country_picker.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import me.ibrahimsn.lib.PhoneNumberKit
import me.ibrahimsn.lib.R
import me.ibrahimsn.lib.api.Country
import me.ibrahimsn.lib.internal.ext.showIf
import me.ibrahimsn.lib.internal.ext.toCountryList
import me.ibrahimsn.lib.internal.io.FileReader
import java.util.*

class CountryPickerBottomSheet : BottomSheetDialogFragment() {

    private val supervisorJob = SupervisorJob()

    private val scope = CoroutineScope(supervisorJob + Dispatchers.Main)

    var onCountrySelectedListener: ((Country?) -> Unit)? = null

    private val viewState: MutableStateFlow<CountryPickerViewState> = MutableStateFlow(
        CountryPickerViewState(emptyList())
    )

    private val args: CountryPickerArguments = requireNotNull(
        arguments?.getParcelable(BUNDLE_ARGS)
    )

    private val itemAdapter: CountryAdapter by lazy {
        CountryAdapter(args.itemLayout)
    }

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
        initView()
        collectViewState()
        fetchData()
    }

    private fun initView() {
        searchView.showIf(args.isSearchEnabled)
        recyclerView.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(context)
            adapter = itemAdapter
        }
        imageButtonClose.setOnClickListener {
            dismiss()
        }
        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchCountries(newText)
                return true
            }
        })
        itemAdapter.onItemClickListener = {
            onCountrySelectedListener?.invoke(it)
            dismiss()
        }
    }

    private fun collectViewState() = scope.launch {
        viewState.collect {
            itemAdapter.setup(it.countries)
        }
    }

    private fun fetchData() {
        viewState.value = CountryPickerViewState(
            FileReader.readAssetFile(requireContext(), PhoneNumberKit.ASSET_FILE_NAME)
            .toCountryList()
        )
    }

    private fun searchCountries(query: String?) {
        scope.launch {
            query?.let {
                val countries = viewState.value.countries
                val filtered = countries.filter {
                    it.code.toString().startsWith(query) ||
                            it.name.lowercase(Locale.ROOT).contains(query.lowercase(Locale.ROOT))
                }
                recyclerView.post {
                    itemAdapter.setup(filtered)
                }
            }
        }
    }

    companion object {
        const val TAG = "tagCountryPickerBottomSheet"
        private const val BUNDLE_ARGS = "bundleArgs"

        fun newInstance(args: CountryPickerArguments) = CountryPickerBottomSheet().apply {
            arguments = bundleOf(BUNDLE_ARGS to args)
        }
    }
}
