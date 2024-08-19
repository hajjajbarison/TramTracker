@file:Suppress("DEPRECATION")

package com.example.tramtracker.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.tramtracker.MainActivity
import com.example.tramtracker.R
import com.example.tramtracker.database.DayType
import com.example.tramtracker.database.Directions
import com.example.tramtracker.database.Stop
import com.example.tramtracker.database.Timetable
import com.example.tramtracker.utilities.LocationUtilities
import com.example.tramtracker.utilities.PositionConstants
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.osmdroid.config.Configuration.*
import java.io.File
import java.util.Calendar


/*
La classe DetailActivity implementa la gestione delle informazioni sulle fermate cliccate all'interno
di MainActivity (via Marker o via Fermate più vicine).
 */

@Suppress("DEPRECATION")
class DetailActivity : AppCompatActivity() {

    private lateinit var bottomNavigationViewDetail: BottomNavigationView
    private lateinit var maggioriDettagli : CardView

    private lateinit var nomeFermata : String

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val latitude = intent!!.getStringExtra(PositionConstants.EXTRA_LATITUDE)
            val longitude = intent.getStringExtra(PositionConstants.EXTRA_LONGITUDE)
            if (latitude != null && longitude != null) {

                val stopId : Int = when (nomeFermata){
                    "Capolinea Pontevigodarzere", "Assunta" -> MainActivity.getDatabaseInstance(applicationContext).getStop(nomeFermata, Directions.RITORNO)!!.id
                    "Capolinea Guizza"-> MainActivity.getDatabaseInstance(applicationContext).getStop(nomeFermata, Directions.ANDATA)!!.id
                    else -> MainActivity.getDatabaseInstance(applicationContext).getStop(nomeFermata, Directions.ANDATA)!!.id
                }
                val stop = MainActivity.getDatabaseInstance(applicationContext).getStop(stopId)

                try {
                    val distanza = getString(R.string.distanza_fermata, String.format("%.3f", (LocationUtilities.calculateDistance(stop!!.latitude, latitude.toDouble(), stop.longitude, longitude.toDouble()) / 1000)))
                    findViewById<TextView>(R.id.distanza_fermata).text = distanza
                }
                catch (e : Exception){
                    Log.e("DISTANCE_MAP", "Errore nel calcolo della distanza ($latitude - $longitude)")
                }


                val cal = Calendar.getInstance()
                var nextDeparture : List<Timetable>
                val dayType : DayType = if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) DayType.FESTIVO else DayType.FERIALE
                val nextDepartureLblA = findViewById<TextView>(R.id.prossima_corsa_andata)
                val nextDepartureLblR = findViewById<TextView>(R.id.prossima_corsa_ritorno)
                lateinit var prossimaPartenza : String      // concatenazione di stringhe
                when (nomeFermata){
                    "Capolinea Pontevigodarzere", "Assunta" -> {
                        nextDeparture = MainActivity.getDatabaseInstance(applicationContext).getNextTimetableTimes(
                            cal.get(Calendar.HOUR_OF_DAY).toString(),
                            cal.get(Calendar.MINUTE).toString(),
                            dayType,
                            nomeFermata,
                            Directions.RITORNO,
                            1
                            )

                        nextDepartureLblR.visibility = View.INVISIBLE
                        prossimaPartenza = getString(R.string.prossima_corsa_ritorno, "${nextDeparture[0].hour}:${nextDeparture[0].minute}")
                        nextDepartureLblA.text = prossimaPartenza
                    }
                    "Capolinea Guizza"-> {
                        nextDeparture = MainActivity.getDatabaseInstance(applicationContext).getNextTimetableTimes(
                            cal.get(Calendar.HOUR_OF_DAY).toString(),
                            cal.get(Calendar.MINUTE).toString(),
                            dayType,
                            nomeFermata,
                            Directions.ANDATA,
                            1
                        )

                        nextDepartureLblR.visibility = View.INVISIBLE
                        prossimaPartenza = getString(R.string.prossima_corsa_andata, "${nextDeparture[0].hour}:${nextDeparture[0].minute}")
                        nextDepartureLblA.text = prossimaPartenza
                    }
                    else -> {
                        nextDeparture = MainActivity.getDatabaseInstance(applicationContext).getNextTimetableTimes(
                            cal.get(Calendar.HOUR_OF_DAY).toString(),
                            cal.get(Calendar.MINUTE).toString(),
                            dayType,
                            nomeFermata,
                            Directions.ANDATA,
                            1
                        )

                        prossimaPartenza = getString(R.string.prossima_corsa_andata, "${nextDeparture[0].hour}:${nextDeparture[0].minute}")
                        nextDepartureLblA.text = prossimaPartenza

                        nextDeparture = MainActivity.getDatabaseInstance(applicationContext).getNextTimetableTimes(
                            cal.get(Calendar.HOUR_OF_DAY).toString(),
                            cal.get(Calendar.MINUTE).toString(),
                            dayType,
                            nomeFermata,
                            Directions.RITORNO,
                            1
                        )

                        prossimaPartenza = getString(R.string.prossima_corsa_ritorno, "${nextDeparture[0].hour}:${nextDeparture[0].minute}")
                        nextDepartureLblR.text = prossimaPartenza
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(receiver, IntentFilter(PositionConstants.ACTION_LOCATION_UPDATE))
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(receiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load/initialize the osmdroid configuration.
        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        // Inflate and create the map
        setContentView(R.layout.detail_activity)

        //map = findViewById(R.id.map)
        //MainActivity.initializeMap(this, map, MainActivity.getDatabaseInstance(this))

        /*val localOverlay = MyLocationNewOverlay(MainActivity.getLocationProvider(), map)
        localOverlay.disableFollowLocation()
        map.overlays.add(localOverlay)*/

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = getString(R.string.dettagli_fermata_toolbar)

        val db = MainActivity.getDatabaseInstance(this)


        // ---------------------------


        val fermataTV : TextView = findViewById(R.id.fermata)
        val prossimaCorsaRitornoTV : TextView = findViewById(R.id.prossima_corsa_ritorno)
        val fermataAndata: Stop?
        val fermataRitorno: Stop?

        val intent = intent
        lateinit var fermata : String       // concatenazione di stringhe
        // RECUPERO INFORMAZIONI DAL MARKER CLICCATA

        loadImageFromFile(intent.getStringExtra("IMAGE_PATH")!!,findViewById(R.id.mapView))

        when {
            intent.hasExtra("INTENT_MARKER") -> {
                nomeFermata = intent.getStringExtra("INTENT_MARKER").toString()

                when (nomeFermata) {
                    "Capolinea Pontevigodarzere", "Assunta" -> {
                        fermataRitorno = db.getStop(nomeFermata, Directions.RITORNO)
                        //setupMapForStop(mapController, fermataRitorno!!.latitude, fermataRitorno.longitude)

                        prossimaCorsaRitornoTV.visibility = View.INVISIBLE
                        fermata = getString(R.string.fermata, fermataRitorno!!.name)
                        fermataTV.text = fermata
                    }
                    "Capolinea Guizza", "Sacchetti" -> {
                        fermataAndata = db.getStop(nomeFermata, Directions.ANDATA)
                        //setupMapForStop(mapController, fermataAndata!!.latitude, fermataAndata.longitude)

                        prossimaCorsaRitornoTV.visibility = View.INVISIBLE
                        fermata = getString(R.string.fermata, fermataAndata!!.name)
                        fermataTV.text = fermata
                    }
                    else -> {
                        fermataAndata = db.getStop(nomeFermata, Directions.ANDATA)
                        //setupMapForStop(mapController, fermataAndata!!.latitude, fermataAndata.longitude)

                        fermata = getString(R.string.fermata, fermataAndata!!.name)
                        fermataTV.text = fermata
                    }
                }

            }
            intent.hasExtra("INTENT_FERMATA_VICINA1") -> {
                nomeFermata = intent.getStringExtra("INTENT_FERMATA_VICINA1").toString()

                when (nomeFermata) {
                    "Capolinea Pontevigodarzere", "Assunta" -> {
                        fermataRitorno = db.getStop(nomeFermata, Directions.RITORNO)
                        //setupMapForStop(mapController, fermataRitorno!!.latitude, fermataRitorno.longitude)

                        prossimaCorsaRitornoTV.visibility = View.INVISIBLE
                        fermata = getString(R.string.fermata, fermataRitorno!!.name)
                        fermataTV.text = fermata

                    }
                    "Capolinea Guizza", "Sacchetti" -> {
                        fermataAndata = db.getStop(nomeFermata, Directions.ANDATA)
                        //setupMapForStop(mapController, fermataAndata!!.latitude, fermataAndata.longitude)

                        prossimaCorsaRitornoTV.visibility = View.INVISIBLE
                        fermata = getString(R.string.fermata, fermataAndata!!.name)
                        fermataTV.text = fermata
                    }
                    else -> {
                        fermataAndata = db.getStop(nomeFermata, Directions.ANDATA)
                        //setupMapForStop(mapController, fermataAndata!!.latitude, fermataAndata.longitude)

                        fermata = getString(R.string.fermata, fermataAndata!!.name)
                        fermataTV.text = fermata
                    }
                }
            }
            intent.hasExtra("INTENT_FERMATA_VICINA2") -> {
                nomeFermata = intent.getStringExtra("INTENT_FERMATA_VICINA2").toString()

                when (nomeFermata) {
                    "Capolinea Pontevigodarzere", "Assunta" -> {
                        fermataRitorno = db.getStop(nomeFermata, Directions.RITORNO)
                        //setupMapForStop(mapController, fermataRitorno!!.latitude, fermataRitorno.longitude)

                        prossimaCorsaRitornoTV.visibility = View.INVISIBLE
                        fermata = getString(R.string.fermata, fermataRitorno!!.name)
                        fermataTV.text = fermata

                    }
                    "Capolinea Guizza", "Sacchetti" -> {
                        fermataAndata = db.getStop(nomeFermata, Directions.ANDATA)
                        //setupMapForStop(mapController, fermataAndata!!.latitude, fermataAndata.longitude)

                        prossimaCorsaRitornoTV.visibility = View.INVISIBLE
                        fermata = getString(R.string.fermata, fermataAndata!!.name)
                        fermataTV.text = fermata

                    }
                    else -> {
                        fermataAndata = db.getStop(nomeFermata, Directions.ANDATA)
                        //setupMapForStop(mapController, fermataAndata!!.latitude, fermataAndata.longitude)

                        fermata = getString(R.string.fermata, fermataAndata!!.name)
                        fermataTV.text = fermata
                    }
                }
            }
        }


        maggioriDettagli = findViewById(R.id.maggiori_dettagli)
        maggioriDettagli.setOnClickListener {
            val maggioriDettagliToLista = Intent(this, ListActivity::class.java).apply {
                when {
                    intent.hasExtra("INTENT_MARKER") -> {
                        putExtra("INTENT_MAGGIORI_DETTAGLI", intent.getStringExtra("INTENT_MARKER").toString())
                    }
                    intent.hasExtra("INTENT_FERMATA_VICINA1") -> {
                        putExtra("INTENT_MAGGIORI_DETTAGLI", intent.getStringExtra("INTENT_FERMATA_VICINA1").toString())
                    }
                    intent.hasExtra("INTENT_FERMATA_VICINA2") -> {
                        putExtra("INTENT_MAGGIORI_DETTAGLI", intent.getStringExtra("INTENT_FERMATA_VICINA2").toString())
                    }
                    else -> {
                        // Nessuno dei tre intent ha avviato l'Activity
                    }
                }
            }
            startActivity(maggioriDettagliToLista)
        }


        // GESTIONE BOTTOM APP BAR
        bottomNavigationViewDetail = findViewById(R.id.bottom_navigation_view_detail)
        bottomNavigationViewDetail.selectedItemId = R.id.navigation_mappa
        bottomNavigationViewDetail.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.navigation_mappa -> {
                    val detailToMap = Intent(this@DetailActivity, MainActivity::class.java)
                    startActivity(detailToMap)
                    true
                }
                R.id.navigation_lista -> {
                    val detailToList = Intent(this@DetailActivity, ListActivity::class.java)
                    startActivity(detailToList)
                    true
                }
                R.id.navigation_impostazioni -> {
                    val detailToSettings = Intent(this@DetailActivity, SettingsActivity::class.java)
                    startActivity(detailToSettings)
                    true
                }
                else -> false
            }
        }
    }

    private fun loadImageFromFile(imagePath : String, imgView : ImageView){
        val imgFile = File(imagePath)
        if (imgFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
            imgView.setImageBitmap(myBitmap)
        }

    }
}