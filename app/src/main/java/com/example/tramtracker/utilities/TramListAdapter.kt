package com.example.tramtracker.utilities

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.tramtracker.R
import androidx.cardview.widget.CardView
import android.widget.Filter
import android.widget.Filterable
import java.util.Locale

/*
TramListAdapter è responsabile della creazione e del binding delle View per la RecyclerView.
Gestisce il filtraggio della lista delle fermate del tram e l'interazione con gli elementi della lista.
Include un ViewHolder per gestire ogni elemento della lista e un listener per gestire i click sugli elementi.
 */
class TramListAdapter(private var tramList: ArrayList<String>, private val listener: OnItemClickListener) :
    RecyclerView.Adapter<TramListAdapter.TramListViewHolder>(), Filterable {

    private var tramListFull: ArrayList<String> = ArrayList(tramList)

    class TramListViewHolder(itemView: View, private val listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val tramStopTextView: TextView = itemView.findViewById(R.id.nome_fermata)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(word: String) {
            tramStopTextView.text = word
        }

        override fun onClick(v: View?) {
            listener.onItemClick(adapterPosition, tramStopTextView.text.toString())
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TramListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fermata_item, parent, false) as CardView
        return TramListViewHolder(view, listener)
    }

    override fun getItemCount(): Int {
        return tramList.size
    }

    override fun onBindViewHolder(holder: TramListViewHolder, position: Int) {
        holder.bind(tramList[position])
    }

    override fun getFilter(): Filter {
        return tramFilter
    }

    private val tramFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList = ArrayList<String>()

            if (constraint.isNullOrEmpty()) {
                filteredList.addAll(tramListFull)
            } else {
                val filterPattern = constraint.toString().lowercase(Locale.ROOT).trim { it <= ' ' }

                for (item in tramListFull) {
                    if (item.lowercase(Locale.ROOT).contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }

            val results = FilterResults()
            results.values = filteredList
            return results
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            results?.let {
                tramList.clear()
                @Suppress("UNCHECKED_CAST")
                tramList.addAll(it.values as List<String>)
                notifyDataSetChanged()
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, itemName: String)
    }
}