package me.ibrahimsn.lib.bottomsheet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_country_picker.view.*
import me.ibrahimsn.lib.Country
import me.ibrahimsn.lib.R

class CountryAdapter : RecyclerView.Adapter<CountryAdapter.ItemViewHolder>() {

    private val items = mutableListOf<Country>()

    var onItemClickListener: ((Country?) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CountryAdapter.ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_country_picker,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CountryAdapter.ItemViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun setup(items: List<Country>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }

    inner class ItemViewHolder(view: View): RecyclerView.ViewHolder(view) {

        private var boundItem: Country? = null

        init {
            itemView.setOnClickListener {
                onItemClickListener?.invoke(boundItem)
            }
        }

        fun bind(country: Country) {
            this.boundItem = country
            itemView.imageViewFlag.setImageResource(getFlagResource(country.iso2))
            itemView.textViewName.text = country.name
            itemView.textViewCode.text = country.countryCode.toString()
        }

        private fun getFlagResource(iso2: String?): Int {
            return itemView.context.resources.getIdentifier(
                "country_flag_$iso2",
                "drawable",
                itemView.context.packageName
            )
        }
    }
}