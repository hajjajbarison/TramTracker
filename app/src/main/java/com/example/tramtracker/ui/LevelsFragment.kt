package com.example.tramtracker.ui

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tramtracker.MainActivity
import com.example.tramtracker.R
import com.example.tramtracker.utilities.DatabaseUpdateService
import com.example.tramtracker.utilities.GeofencingService
import com.example.tramtracker.utilities.LocationUtilities
import com.example.tramtracker.utilities.PreferencesHelper
import com.example.tramtracker.utilities.WakeLockManager

/* Fragment contenuto all'interno di SettingsActivity. Fornisce la possibilità di attivare/disattivare
* la modalità di risparmio energetico (tramite Switch) che prevede l'esecuzione di livelli energetici. Contiene una seekbar
* che riporta il livello attualmente eseguito (livello 0 senza risparmio energetico) e una descrizione
* su quando i livelli vengono eseguiti in caso di modalità risparmio (range di percentuale di batteria).
* Attivazione/disattivazione modalità risparmio energetico posticipata di 300 ms dall'input dello
* Switch per rendere l'interfaccia grafica più fluida */
@Suppress("DEPRECATION")
class LevelsFragment : Fragment() {

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_levels, container, false)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.livelli_risparmio_energetico)

        val seekBar = view.findViewById<SeekBar>(R.id.seekBar)
        seekBar.isEnabled = false

        val switch = view.findViewById<SwitchCompat>(R.id.switch_livelli)
        switch.isChecked = PreferencesHelper.getBoolean("SWITCH", false)

        if(switch.isChecked)
            seekBar.progress = PreferencesHelper.getInt("LEVEL", 0)
        else
            seekBar.progress = 0

        switch.setOnCheckedChangeListener{ _, isChecked ->
            runnable?.let { handler.removeCallbacks(it) }

            runnable = Runnable {
                if (isChecked) {
                    MainActivity.registerBatteryBroadcastReceiver(requireContext())
                    seekBar.progress = PreferencesHelper.getInt("LEVEL", 0)
                } else {
                    MainActivity.unregisterBatteryBroadcastReceiver(requireContext())
                    seekBar.progress = 0

                    // LEVEL 0
                    Log.d("LevelsFragment_onCreate", "Level 0")
                    stopAllServices(requireContext())

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ContextCompat.startForegroundService(
                            requireContext(),
                            Intent(requireContext(), LocationUtilities::class.java)
                        )
                        Log.d("LevelsFragment", "locationService started as foreground")
                    } else {
                        requireContext().startService(
                            Intent(
                                requireContext(),
                                LocationUtilities::class.java
                            )
                        )
                        Log.d("LevelsFragment", "locationService started")
                    }
                    // Aggiornamento in background del database
                    val dBService = Intent(requireContext(), DatabaseUpdateService::class.java)
                    requireContext().startService(dBService)
                    Log.d("LevelsFragment", "DBService started")
                    // Geofencing
                    val geoService = Intent(requireContext(), GeofencingService::class.java)
                    requireContext().startService(geoService)
                    Log.d("LevelsFragment", "GeoService started")
                    WakeLockManager.acquireWakeLock(requireContext())

                }
                PreferencesHelper.putBoolean("SWITCH", isChecked)
            }
            handler.postDelayed(runnable!!, 300)
        }

        return view
    }

    private fun stopAllServices(context: Context) {

        Log.d("LevelsFragment_stopAllServices", "stopAllServices() called")
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = manager.getRunningServices(Integer.MAX_VALUE)
        for (serviceInfo in runningServices) {
            if (serviceInfo.service.packageName == context.packageName) {
                val serviceName = serviceInfo.service.className
                val serviceIntent = Intent(context, Class.forName(serviceName))
                context.stopService(serviceIntent)
                Log.d("LevelsFragment_stopAllServices", "$serviceName stopped")
            }
        }
    }

}