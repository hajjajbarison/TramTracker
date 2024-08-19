package com.example.tramtracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.tramtracker.R

class SavingDetailsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_saving_details, container, false)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.dettagli_risparmio_energetico_toolbar)

        return view
    }
}