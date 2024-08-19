package com.example.tramtracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.tramtracker.MainActivity
import com.example.tramtracker.R
import com.example.tramtracker.database.Directions
import com.example.tramtracker.utilities.TramListAdapter

/*
TramListFragment imposta la RecyclerView per visualizzare una lista delle fermate del tram e configura
la SearchView per consentire la ricerca all'interno della lista delle fermate.
Gestisce inoltre la navigazione verso moreDetailFragment quando un elemento della lista viene cliccato.
*/

class TramListFragment : Fragment(), TramListAdapter.OnItemClickListener {

    private lateinit var tramList : ArrayList<String>
    private lateinit var adapter : TramListAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val stopList = MainActivity.getDatabaseInstance(requireContext()).getPartialStopsList(Directions.ANDATA)

        tramList = arrayListOf()
        for (stop in stopList)
            tramList.add(stop.name)

        val view = inflater.inflate(R.layout.fragment_tram_list, container, false)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.lista_fermate)


        val recyclerView: RecyclerView = view.findViewById(R.id.tramstop_list)
        adapter = TramListAdapter(tramList, this)
        recyclerView.adapter = adapter

        val searchView: SearchView = view.findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return false
            }
        })

        return view

    }

    override fun onItemClick(position: Int, itemName: String) {
        val bundle = Bundle()
        bundle.putString("TRAM_STOP_NAME", itemName)
        findNavController().navigate(R.id.action_tramListFragment_to_moreDetailFragment, bundle)
    }


}
