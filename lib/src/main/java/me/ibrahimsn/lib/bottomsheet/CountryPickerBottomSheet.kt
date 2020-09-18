package me.ibrahimsn.lib.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_country_picker.*
import me.ibrahimsn.lib.Countries
import me.ibrahimsn.lib.Country
import me.ibrahimsn.lib.R

class CountryPickerBottomSheet: BottomSheetDialogFragment() {

    private var itemAdapter: CountryAdapter? = null

    var onCountrySelectedListener: ((Country?) -> Unit)? = null

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
        recyclerView.apply {
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            layoutManager = LinearLayoutManager(context)
            adapter = itemAdapter
        }
        imageButtonClose.setOnClickListener {
            dismiss()
        }
    }

    fun setup(@LayoutRes itemLayout: Int) {
        itemAdapter = CountryAdapter(itemLayout).apply {
            setup(Countries.list)
            onItemClickListener = {
                onCountrySelectedListener?.invoke(it)
                dismiss()
            }
        }
    }

    companion object {
        fun newInstance() = CountryPickerBottomSheet()
        const val TAG = "tag-country-picker"
    }
}
