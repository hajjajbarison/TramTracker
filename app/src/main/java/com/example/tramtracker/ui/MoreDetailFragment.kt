package com.example.tramtracker.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.tramtracker.MainActivity
import com.example.tramtracker.R
import com.example.tramtracker.database.DayType
import com.example.tramtracker.database.Directions
import com.example.tramtracker.database.Stop
import com.example.tramtracker.database.Timetable
import com.example.tramtracker.utilities.LocationUtilities
import com.example.tramtracker.utilities.PositionConstants
import java.io.File
import java.util.Calendar

class MoreDetailFragment : Fragment() {

    private lateinit var nomeFermata : String
    private lateinit var stopImagePath : String

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val latitude = intent!!.getStringExtra(PositionConstants.EXTRA_LATITUDE)
            val longitude = intent.getStringExtra(PositionConstants.EXTRA_LONGITUDE)
            if (latitude != null && longitude != null) {

                val stopId : Int = when (nomeFermata){
                    "Capolinea Pontevigodarzere", "Assunta" -> MainActivity.getDatabaseInstance(requireContext()).getStop(nomeFermata, Directions.RITORNO)!!.id
                    "Capolinea Guizza"-> MainActivity.getDatabaseInstance(requireContext()).getStop(nomeFermata, Directions.ANDATA)!!.id
                    else -> MainActivity.getDatabaseInstance(requireContext()).getStop(nomeFermata, Directions.ANDATA)!!.id
                }
                val stop = MainActivity.getDatabaseInstance(requireContext()).getStop(stopId)

                try {
                    val distanza = getString(R.string.distanza_fermata, String.format("%.3f", (LocationUtilities.calculateDistance(stop!!.latitude, latitude.toDouble(), stop.longitude, longitude.toDouble()) / 1000)))
                    view?.findViewById<TextView>(R.id.distanza_fermata)?.text = distanza
                }
                catch (e : Exception){
                    Log.e("DISTANCE_MAP", "Errore nel calcolo della distanza ($latitude - $longitude)")
                }

                val cal = Calendar.getInstance()
                var nextDeparture : List<Timetable>
                val dayType : DayType = if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) DayType.FESTIVO else DayType.FERIALE

                val departuresAnd = view?.findViewById<TextView>(R.id.prossima_corsa_andata)
                val departuresRit = view?.findViewById<TextView>(R.id.prossima_corsa_ritorno)

                val dep1And = view?.findViewById<TextView>(R.id.corsa1_and)
                val dep2And = view?.findViewById<TextView>(R.id.corsa2_and)
                val dep3And = view?.findViewById<TextView>(R.id.corsa3_and)
                val dep1Rit = view?.findViewById<TextView>(R.id.corsa1_rit)
                val dep2Rit = view?.findViewById<TextView>(R.id.corsa2_rit)
                val dep3Rit = view?.findViewById<TextView>(R.id.corsa3_rit)

                when (nomeFermata){
                    "Capolinea Pontevigodarzere", "Assunta" -> {
                        nextDeparture = MainActivity.getDatabaseInstance(requireContext()).getNextTimetableTimes(
                            cal.get(Calendar.HOUR_OF_DAY).toString(),
                            cal.get(Calendar.MINUTE).toString(),
                            dayType,
                            nomeFermata,
                            Directions.RITORNO,
                            3
                        )

                        dep1Rit?.visibility = View.INVISIBLE
                        dep2Rit?.visibility = View.INVISIBLE
                        dep3Rit?.visibility = View.INVISIBLE

                        departuresAnd?.text = getString(R.string.prossima_corsa_ritorno, "")

                        dep1And?.text = if (nextDeparture.isNotEmpty())  "${nextDeparture[0].hour}:${nextDeparture[0].minute}" else  ""
                        dep2And?.text = if (nextDeparture.size >= 2)  "${nextDeparture[1].hour}:${nextDeparture[1].minute}" else  ""
                        dep3And?.text = if (nextDeparture.size >= 3)  "${nextDeparture[2].hour}:${nextDeparture[2].minute}" else  ""
                    }
                    "Capolinea Guizza"-> {
                        nextDeparture = MainActivity.getDatabaseInstance(requireContext()).getNextTimetableTimes(
                            cal.get(Calendar.HOUR_OF_DAY).toString(),
                            cal.get(Calendar.MINUTE).toString(),
                            dayType,
                            nomeFermata,
                            Directions.ANDATA,
                            3
                        )

                        dep1Rit?.visibility = View.INVISIBLE
                        dep2Rit?.visibility = View.INVISIBLE
                        dep3Rit?.visibility = View.INVISIBLE

                        departuresAnd?.text = getString(R.string.prossima_corsa_andata, "")

                        dep1And?.text = if (nextDeparture.isNotEmpty())  "${nextDeparture[0].hour}:${nextDeparture[0].minute}" else  ""
                        dep2And?.text = if (nextDeparture.size >= 2)  "${nextDeparture[1].hour}:${nextDeparture[1].minute}" else  ""
                        dep3And?.text = if (nextDeparture.size >= 3)  "${nextDeparture[2].hour}:${nextDeparture[2].minute}" else  ""
                    }
                    else -> {
                        nextDeparture = MainActivity.getDatabaseInstance(requireContext()).getNextTimetableTimes(
                            cal.get(Calendar.HOUR_OF_DAY).toString(),
                            cal.get(Calendar.MINUTE).toString(),
                            dayType,
                            nomeFermata,
                            Directions.ANDATA,
                            3
                        )

                        departuresAnd?.text = getString(R.string.prossima_corsa_andata, "")

                        dep1And?.text = if (nextDeparture.isNotEmpty())  "${nextDeparture[0].hour}:${nextDeparture[0].minute}" else  ""
                        dep2And?.text = if (nextDeparture.size >= 2)  "${nextDeparture[1].hour}:${nextDeparture[1].minute}" else  ""
                        dep3And?.text = if (nextDeparture.size >= 3)  "${nextDeparture[2].hour}:${nextDeparture[2].minute}" else  ""

                        nextDeparture = MainActivity.getDatabaseInstance(requireContext()).getNextTimetableTimes(
                            cal.get(Calendar.HOUR_OF_DAY).toString(),
                            cal.get(Calendar.MINUTE).toString(),
                            dayType,
                            nomeFermata,
                            Directions.RITORNO,
                            3
                        )

                        departuresRit?.text = getString(R.string.prossima_corsa_ritorno, "")

                        dep1Rit?.text = if (nextDeparture.isNotEmpty())  "${nextDeparture[0].hour}:${nextDeparture[0].minute}" else  ""
                        dep2Rit?.text = if (nextDeparture.size >= 2)  "${nextDeparture[1].hour}:${nextDeparture[1].minute}" else  ""
                        dep3Rit?.text = if (nextDeparture.size >= 3)  "${nextDeparture[2].hour}:${nextDeparture[2].minute}" else  ""
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_more_detail, container, false)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.ulteriori_dettagli_fermata)

        val db = ListActivity.getDatabaseInstance(requireContext())
        val fermataAndata : Stop?
        val fermataRitorno : Stop?

        val fermataTV : TextView = view.findViewById(R.id.fermata)
        val prossimaCorsaAndataTV : TextView = view.findViewById(R.id.prossima_corsa_andata)
        val prossimaCorsaRitornoTV : TextView = view.findViewById(R.id.prossima_corsa_ritorno)
        val descrizioneTV : TextView = view.findViewById(R.id.descrizione)


        arguments?.let {
            nomeFermata = it.getString("INTENT_MAGGIORI_DETTAGLI") ?: it.getString("TRAM_STOP_NAME") ?: ""
            when (nomeFermata) {
                "Capolinea Pontevigodarzere", "Assunta" -> {
                    fermataRitorno = db.getStop(nomeFermata, Directions.RITORNO)
                    fermataTV.text = getString(R.string.fermata, fermataRitorno!!.name)
                    descrizioneTV.text = getString(R.string.descrizione, fermataRitorno.description)
                    prossimaCorsaRitornoTV.visibility = View.INVISIBLE
                    prossimaCorsaAndataTV.text = getString(R.string.prossima_corsa_ritorno_senza_parametro)
                    stopImagePath = fermataRitorno.imagePath
                }
                "Capolinea Guizza", "Sacchetti" -> {
                    fermataAndata = db.getStop(nomeFermata, Directions.ANDATA)
                    fermataTV.text = getString(R.string.fermata, fermataAndata!!.name)
                    descrizioneTV.text = getString(R.string.descrizione, fermataAndata.description)
                    prossimaCorsaRitornoTV.visibility = View.INVISIBLE
                    prossimaCorsaAndataTV.text = getString(R.string.prossima_corsa_andata_senza_parametro)
                    stopImagePath = fermataAndata.imagePath
                }
                else -> {
                    fermataAndata = db.getStop(nomeFermata, Directions.ANDATA)
                    fermataRitorno = db.getStop(nomeFermata, Directions.RITORNO)
                    fermataTV.text = getString(R.string.fermata, fermataAndata!!.name)
                    descrizioneTV.text = getString(R.string.descrizione, fermataAndata.description)
                    prossimaCorsaAndataTV.text = getString(R.string.prossima_corsa_andata_senza_parametro)
                    prossimaCorsaRitornoTV.text = getString(R.string.prossima_corsa_ritorno_senza_parametro)
                    stopImagePath = fermataRitorno!!.imagePath
                }
            }
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, IntentFilter(PositionConstants.ACTION_LOCATION_UPDATE))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadImageFromFile(stopImagePath, requireView().findViewById(R.id.immagine_fermata))
    }

    private fun loadImageFromFile(imagePath : String, imgView : ImageView){
        val imgFile = File(imagePath)
        if (imgFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            imgView.setImageBitmap(myBitmap)
        }

    }

}