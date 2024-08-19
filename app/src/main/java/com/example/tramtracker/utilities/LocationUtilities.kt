package com.example.tramtracker.utilities

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.location.LocationListener
import android.location.LocationManager
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.tramtracker.database.Stop
import android.app.Service
import android.location.GnssStatus
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt



private const val CHANNEL_ID = "GPSStatusChannel"
private const val GNSS_UPDATE_INTERVAL = 3000
private var UPDATE_INTERVAL : Long = 1000

//Classe di utilities che implementa sia funzioni per il calcolo della distanza tra l'utente e le fermate che metodi per ottenere aggionamenti della posizione in tempo reale
class LocationUtilities : Service(){


    private var gpsLocationManager: LocationManager? = null
    private var internetLocationManager: LocationManager? = null

    private var gpsLocationListener: LocationListener? = null
    private var internetLocationListener: LocationListener? = null

    private var gnssStatusCallback: GnssStatus.Callback? = null
    private lateinit var notificationManager : NotificationManager

    private var lastGnssUpdateTime : Long = 0
    private var isCoarseLocationRunning = false
    private var isFineLocationRunning = false


    private var isInternetProviderRunning = false

    private lateinit var serviceHandler: Handler
    private lateinit var handlerThread: HandlerThread

    //Metodo che viene eseguito all'avviamento del servizio per la posizione
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceHandler.post {
            try {
                startGpsLocationUpdates(applicationContext)
                gpsLocationManager!!.registerGnssStatusCallback(gnssStatusCallback!!, null)
                PreferencesHelper.init(this)
                if(intent != null) {
                    val updateTime = intent.getLongExtra("UPDATE_TIME", UPDATE_INTERVAL)
                    val accuracy = intent.getIntExtra("ACCURACY", 0)
                    setUpdateInterval(updateTime, accuracy, applicationContext)
                    PreferencesHelper.putLong("UPDATE_TIME", updateTime)
                    PreferencesHelper.putInt("ACCURACY", accuracy)
                }else{
                    val updateTime = PreferencesHelper.getLong("UPDATE_TIME", UPDATE_INTERVAL)
                    val accuracy = PreferencesHelper.getInt("ACCURACY", 0)
                    setUpdateInterval(updateTime, accuracy, applicationContext)
                }
            } catch (e: SecurityException) {
                Log.e("GPS_LOCATION", e.printStackTrace().toString())
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        handlerThread = HandlerThread("LocationServiceThread")
        handlerThread.start()
        serviceHandler = Handler(handlerThread.looper)

        initializeGPSInfos()
        startForeground(1, createNotification())
    }

    override fun stopService(name: Intent?): Boolean {
        super.stopService(name)

        stopGpsLocationUpdates()
        var returnValue = false
        if (gnssStatusCallback != null) {
            gpsLocationManager!!.unregisterGnssStatusCallback(gnssStatusCallback!!)
            returnValue = true
        }
        handlerThread.quitSafely()
        return returnValue
    }

    //Metodo che serve per creare il canale di notifica per il service
    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "GPSConnectionStatusChannel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(com.example.tramtracker.R.string.LocationUtilitiesNotificationtitle))
            .setContentText(getString(com.example.tramtracker.R.string.LocationUtilitiesNotificationText))
            .setSmallIcon(com.example.tramtracker.R.drawable.ic_tram_notification)
            .setOngoing(true)
            .build()
    }

    //Funzione per aggiornare il contenuto della notifica
    private fun updateNotification(contentText: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(com.example.tramtracker.R.string.LocationUtilitiesNotificationtitle))
            .setContentText(getString(com.example.tramtracker.R.string.GPSStatusText) + " $contentText")
            .setSmallIcon(com.example.tramtracker.R.drawable.ic_tram_notification)
            .setOngoing(true)
            .build()

        notificationManager.notify(1, notification)
    }

    /*Funzione che avvia il servizio GPS per la posizione, invia inizialmente l'ultima posizione conosciuta, se disponibile,
    * altrimenti avvia il listener per la posizione in tempo reale*/
    private fun startGpsLocationUpdates(context : Context) {
        try {
            Log.d("GPS_LOCATION", "Avviato il servizio GPS per la posizione")
            gpsLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            val lastKnownPosition = gpsLocationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            LocalBroadcastManager.getInstance(context)
                .sendBroadcast(Intent(PositionConstants.ACTION_LOCATION_UPDATE).apply {
                    putExtra(PositionConstants.EXTRA_LATITUDE, lastKnownPosition?.latitude.toString())
                    putExtra(PositionConstants.EXTRA_LONGITUDE, lastKnownPosition?.longitude.toString())
                    Log.d("GPS_LOCATION", "Inviata ultima posizione conosciuta GPS")
                })

            gpsLocationListener = LocationListener { location ->
                val intent = Intent(PositionConstants.ACTION_LOCATION_UPDATE).apply {
                    putExtra(PositionConstants.EXTRA_LATITUDE, location.latitude.toString())
                    putExtra(PositionConstants.EXTRA_LONGITUDE, location.longitude.toString())
                }
                Log.d("GPS_LOCATION", "Inviata posizione GPS")
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }

            gpsLocationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL, 0f, gpsLocationListener!!)
        }
        catch (e: SecurityException) {
            Log.e("GPS_LOCATION", e.printStackTrace().toString())
            startGpsLocationUpdates(context)
        }
    }

    //Metodo che fornisce informazioni sul numero di satelliti connessi, utile sia per debug che per informare l'utente
    private fun initializeGPSInfos(){
        gnssStatusCallback = object : GnssStatus.Callback() {
            override fun onSatelliteStatusChanged(status: GnssStatus) {
                var usedInFixCount = 0

                val currentTime = System.currentTimeMillis()
                if (currentTime - lastGnssUpdateTime < GNSS_UPDATE_INTERVAL) {
                    return
                }
                lastGnssUpdateTime = currentTime

                for (i in 0 until status.satelliteCount) {
                    if (status.usedInFix(i)) {
                        usedInFixCount++
                    }
                }

                var accuracyMessage = ""

                if(isCoarseLocationRunning)
                    accuracyMessage = getString(com.example.tramtracker.R.string.GPSCoarseLocationUsage)

                if(isFineLocationRunning){
                    accuracyMessage = when (usedInFixCount) {
                        0 -> getString(com.example.tramtracker.R.string.GPSSignalAbsent)
                        in 1..3 -> getString(com.example.tramtracker.R.string.GPSSignalVeryWeak)
                        in 4..7 -> getString(com.example.tramtracker.R.string.GPSSignalWeak)
                        in 8..12 -> getString(com.example.tramtracker.R.string.GPSSignalGood)
                        else -> getString(com.example.tramtracker.R.string.GPSSignalExcelent)
                    }

                    // Mostra un messaggio all'utente
                    if (usedInFixCount == 0 && !isInternetProviderRunning) {
                        startInternetLocationUpdates(applicationContext)
                        isInternetProviderRunning = true
                        accuracyMessage = getString(com.example.tramtracker.R.string.InternetCoarseLocationUsage)
                    }
                    //Può essere null
                    else if(usedInFixCount > 0  && isInternetProviderRunning) {
                        stopInternetLocationUpdates()
                        isInternetProviderRunning = false
                    }
                    if (isInternetProviderRunning)
                        accuracyMessage = getString(com.example.tramtracker.R.string.InternetCoarseLocationUsage)
                }
                Log.d("SATELLITE", "Satelliti rilevati: $usedInFixCount")
                updateNotification(accuracyMessage)
            }
        }
    }

    //Metodo per avviare il servizio internet per la posizione, è analogo a quello GPS solo che come provider usa la rete
    private fun startInternetLocationUpdates(context : Context){
        try {
            Log.d("INTERNET_LOCATION", "Avviato il servizio Internet per la posizione")
            internetLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            internetLocationListener = LocationListener { location ->
                val intent = Intent(PositionConstants.ACTION_LOCATION_UPDATE).apply {
                    putExtra(PositionConstants.EXTRA_LATITUDE, location.latitude.toString())
                    putExtra(PositionConstants.EXTRA_LONGITUDE, location.longitude.toString())
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }

            gpsLocationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_INTERVAL, 0f, internetLocationListener!!)
        }
        catch (e: SecurityException) {
            Log.e("GPS_LOCATION", e.printStackTrace().toString())
        }
    }

    // Metodo per fermare gli aggiornamenti della posizione GPS
    private fun stopGpsLocationUpdates() {
        Log.d("GPS_LOCATION", "Fermato il servizio GPS per la posizione")
        gpsLocationManager?.removeUpdates(gpsLocationListener!!)
    }

    // Metodo per fermare gli aggiornamenti della posizione internet
    private fun stopInternetLocationUpdates() {
        Log.d("INTERNET_LOCATION", "Fermato il servizio internet per la posizione")
        internetLocationManager?.removeUpdates(internetLocationListener!!)
    }

    //Metodo per aggiornare l'intervallo di aggiornamento della posizione
    private fun setUpdateInterval(time : Long, accuracy : Int, context : Context){
        UPDATE_INTERVAL = time

        if (gpsLocationManager != null && gpsLocationListener != null) {
            gpsLocationManager!!.removeUpdates(gpsLocationListener!!)
        }

        if (internetLocationManager != null && internetLocationListener != null) {
            internetLocationManager!!.removeUpdates(internetLocationListener!!)
        }

        try {
            val localLocationListener = LocationListener { location -> // Chiama la funzione di callback quando la posizione viene aggiornata
                val intent = Intent(PositionConstants.ACTION_LOCATION_UPDATE).apply {
                    putExtra(PositionConstants.EXTRA_LATITUDE, location.latitude.toString())
                    putExtra(PositionConstants.EXTRA_LONGITUDE, location.longitude.toString())
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
            }

            if (accuracy == 0) {
                isCoarseLocationRunning = false
                isFineLocationRunning = true
                gpsLocationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL, 0f, localLocationListener)
            } else {
                isCoarseLocationRunning = true
                isFineLocationRunning = false
                internetLocationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_INTERVAL, 0f, localLocationListener)
            }
        }
        catch (_: SecurityException) {}
    }

    //Parte companion object che garantisce di accedere in modo statico ai metodi che forniscono le distanze dalle fermate
    companion object {
                //Raggio della terra approssimativo in metri
        private const val R = 6371000

        fun getDistancesMap(stops: List<Stop>, usrLatitude : Double, usrLongitude : Double): Map<Double, Int> {
            val resultMap = mutableMapOf<Double, Int>()
            for (stop in stops) {
                resultMap[calculateDistance(stop.latitude, usrLatitude, stop.longitude, usrLongitude)] = stop.id
            }

            return resultMap.toSortedMap()
        }

        //Calcolo della distanza tramite Distanza di Haversine (per distanze sulla superficie terrestre)
        fun calculateDistance(latA: Double, latB: Double, longA: Double, longB: Double): Double {
            // Conversione delle coordinate da gradi a radianti
            val lat1Rad = Math.toRadians(latA)
            val lon1Rad = Math.toRadians(longA)
            val lat2Rad = Math.toRadians(latB)
            val lon2Rad = Math.toRadians(longB)

            // Differenze delle coordinate
            val deltaLat = abs(lat2Rad - lat1Rad)
            val deltaLon = abs(lon2Rad - lon1Rad)

            // Formula di Haversine
            val a = sin(deltaLat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))

            // Distanza in metri
            return R * c
        }
    }
}