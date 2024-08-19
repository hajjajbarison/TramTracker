package com.example.tramtracker.utilities

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.tramtracker.MainActivity
import com.example.tramtracker.R
import com.example.tramtracker.database.Directions

/*
GeofencingService è un Service che si occupa di eseguire la funzione di Geofence.
Avvicinandosi ad una fermata, con distanza inferiore a 500 metri, l'utente sarà avvisato
tramite una notifica della sua vicinanza alla fermata in questione.
*/


class GeofencingService : Service() {

    private val CHANNEL_ID = "GeofencingServiceChannel"
    private val notificationId = 16
    private var currentStopName: String? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val latitude = intent!!.getStringExtra(PositionConstants.EXTRA_LATITUDE)
            val longitude = intent.getStringExtra(PositionConstants.EXTRA_LONGITUDE)
            if (latitude != null && longitude != null) {
                try {
                    val distanceMap = LocationUtilities.getDistancesMap(MainActivity.getDatabaseInstance(applicationContext)
                        .getPartialStopsList(Directions.ANDATA), latitude.toDouble(), longitude.toDouble())

                    // Prendo la prima fermata della lista
                    val distance = distanceMap.entries.first().key
                    val stopName = MainActivity.getDatabaseInstance(applicationContext)
                        .getStop(distanceMap[distance]!!)!!.name
                    if (distance <= 500.0 && stopName != currentStopName) {
                        currentStopName = stopName
                        notifyCloseStop(stopName)
                    }
                }
                catch (e: Exception) {
                    Log.e("MAP_DISTANCE", "Errore nel calcolo delle distanze")
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LocalBroadcastManager.getInstance(applicationContext).registerReceiver(receiver, IntentFilter(PositionConstants.ACTION_LOCATION_UPDATE))
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun notifyCloseStop(stopName: String) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Fermata tram vicina!")
            .setContentText("Ti trovi vicino alla fermata del tram: $stopName")
            .setSmallIcon(R.drawable.ic_tram_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(false)
            .setOnlyAlertOnce(false)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(applicationContext).unregisterReceiver(receiver)
    }
}