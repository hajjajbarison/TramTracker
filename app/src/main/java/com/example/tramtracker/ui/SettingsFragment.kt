package com.example.tramtracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.tramtracker.R

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.impostazioni)

        val livelliBtn = view.findViewById<CardView>(R.id.livelli_btn)
        val dettagliRisparmioBtn = view.findViewById<CardView>(R.id.dettagli_risparmio_btn)
        val monitoraggioBtn = view.findViewById<CardView>(R.id.monitoraggio_btn)

        livelliBtn.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_settingsFragment_to_levelsFragment)
        }
        dettagliRisparmioBtn.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_settingsFragment_to_savingDetailsFragment)
        }
        monitoraggioBtn.setOnClickListener {
            view.findNavController()
                .navigate(R.id.action_settingsFragment_to_statisticsFragment)
        }

        return view
    }
}