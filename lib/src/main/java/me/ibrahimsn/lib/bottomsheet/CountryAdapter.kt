package me.ibrahimsn.lib.bottomsheet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import me.ibrahimsn.lib.Country
import me.ibrahimsn.lib.R

class CountryAdapter(
    @LayoutRes private var itemLayout: Int
) : RecyclerView.Adapter<CountryAdapter.ItemViewHolder>() {

    private val items = mutableListOf<Country>()

    var onItemClickListener: ((Country?) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CountryAdapter.ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context).inflate(itemLayout, parent, false)
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

        private val imageViewFlag = view.findViewById<ImageView>(R.id.imageViewFlag)
        private val textViewName = view.findViewById<TextView>(R.id.textViewName)
        private val textViewCode = view.findViewById<TextView>(R.id.textViewCode)

        private var boundItem: Country? = null

        init {
            itemView.setOnClickListener {
                onItemClickListener?.invoke(boundItem)
            }
        }

        fun bind(country: Country) {
            this.boundItem = country
            imageViewFlag.setImageResource(getFlagResource(country.iso2))
            textViewName.text = country.name
            textViewCode.text = country.countryCode.toString()
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