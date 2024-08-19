package com.example.tramtracker.utilities

import android.content.Context
import android.os.PowerManager

/*
Singleton object che permette la creazione di una singola istanza del wakelock che può essere gestito
(acquisito o rilasciato) in diverse classi (MainActivity, BatteryBroadcastReceiver, LevelsFragment).
Il wakelock viene acquisito solo se non è già stato acquisito e può essere rilasciato solo se è stato
acquisito.
*/

object WakeLockManager {
    private var wakeLock: PowerManager.WakeLock? = null

    fun acquireWakeLock(context: Context) {
        if (wakeLock == null) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "TramTracker::WakeLockTag")
            // Il wakelock viene preso per 10 minuti
            wakeLock?.acquire(10*60*1000L)
        }
    }

    fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }
}