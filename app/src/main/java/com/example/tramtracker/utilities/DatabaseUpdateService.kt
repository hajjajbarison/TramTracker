package com.example.tramtracker.utilities

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.tramtracker.MainActivity
import com.example.tramtracker.R
import java.util.Timer
import java.util.TimerTask


/*
DatabaseUpdateService è un Service che svolge aggiornamenti del database periodicamente attraverso la funzione
updateDatabase della classe DatabaseHelper.
Attraverso un timer, ogni minuto viene eseguito un controllo; se il precedente update è terminato, allora
può essere svolto un nuovo update.
 */


class DatabaseUpdateService : Service() {

    private val CHANNEL_ID = "DBUpdateChannel"
    private val timer = Timer()
    private var isUpdating = false

    override fun onCreate() {
        super.onCreate()
        createNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Thread {
            timer.scheduleAtFixedRate(CheckUpdateTask(), 0, 60000)
        }.start()
        return START_STICKY
    }

    inner class CheckUpdateTask : TimerTask() {
        override fun run() {
            // controllo se sono presenti aggiornamenti in corso
            if (!isUpdating) {
                isUpdating = true
                updateDatabase()
            }
        }
    }

    private fun updateDatabase() {
        Log.d("DatabaseUpdateService", "Aggiornamento del database in corso...")
        // operazioni di update
        val database = MainActivity.getDatabaseInstance(applicationContext)
        val stopsList = database.getStopsList()
        val timetableList = database.getTimetableList()
        database.updateDatabase(stopsList, timetableList)

        // update completato
        isUpdating = false
    }


    private fun createNotification() : Notification{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = getString(R.string.channel_name_db)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance)

        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Notifica di aggiornamento")
            .setContentText("Aggiornamento del database in corso")
            .setSmallIcon(R.drawable.ic_tram_notification)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)

        return notificationBuilder.build()
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

}