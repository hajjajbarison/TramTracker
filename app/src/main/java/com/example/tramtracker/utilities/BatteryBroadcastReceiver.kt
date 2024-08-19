package com.example.tramtracker.utilities

import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getString
import com.example.tramtracker.R

/* Broadcast Receiver che riceve avvisi riguardo al cambio di stato della batteria del dispositivo
* (Intent.ACTION_BATTERY_CHANGED). Viene utilizzato per ricavare la percentuale di batteria ed
* effettuare esecuzioni condizionate ad essa (livelli energetici). Ogni livello viene eseguito
* una sola volta dal momento in cui il dispositivo entra nel range di batteria associato al
* livello stesso. Registrato dal momento in cui l'utente decide di attivare la modalità di risparmio
* energetico in LevelsFragment. */
@Suppress("DEPRECATION")
class BatteryBroadcastReceiver : BroadcastReceiver(){

    private var lastExecutedLevel = -1

    private val CHANNEL_BRIGHTNESS = "BrightnessNotificationChannel"
    private val CHANNEL_DARKMODE = "DarkModeNotificationChannel"

    override fun onReceive(context: Context, intent: Intent) {

        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val percentage = (level * 100)/(scale.toFloat())

        val currentLevel = onCurrentLevel(percentage)
        if(currentLevel != lastExecutedLevel){
            lastExecutedLevel = currentLevel

            when(percentage){
                in 71.0 .. 100.0 -> {
                    // LEVEL 0
                    Log.d("BatteryBroadcastReceiver", "Level 0")

                    stopAllServices(context)
                    val locationService = Intent(context, LocationUtilities::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ContextCompat.startForegroundService(context, locationService)
                        Log.d("BatteryBroadcastReceiver", "locationService started as foreground")
                    }
                    else {
                        context.startService(locationService)
                        Log.d("BatteryBroadcastReceiver", "locationService started")
                    }
                    val dBService = Intent(context, DatabaseUpdateService::class.java)
                    val geoService = Intent(context, GeofencingService::class.java)
                    context.startService(dBService)
                    Log.d("BatteryBroadcastReceiver", "DBService started")
                    context.startService(geoService)
                    Log.d("BatteryBroadcastReceiver", "GeoService started")
                    WakeLockManager.acquireWakeLock(context)


                }
                in 51.0 .. 70.0 -> {
                    // LEVEL 1
                    Log.d("BatteryBroadcastReceiver", "Level 1")

                    stopAllServices(context)
                    val locationService = Intent(context, LocationUtilities::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                        ContextCompat.startForegroundService(context, locationService)
                        Log.d("BatteryBroadcastReceiver", "locationService started as foreground")
                    }
                    else {
                        context.startService(locationService)
                        Log.d("BatteryBroadcastReceiver", "locationService started")
                    }
                    val dBService = Intent(context, DatabaseUpdateService::class.java)
                    val geoService = Intent(context, GeofencingService::class.java)
                    context.startService(dBService)
                    Log.d("BatteryBroadcastReceiver", "DBService started")
                    context.startService(geoService)
                    Log.d("BatteryBroadcastReceiver", "GeoService started")
                    WakeLockManager.acquireWakeLock(context)
                    createNotificationChannels(context)
                    showNotificationBrightness(context)
                    showNotificationDarkMode(context)
                }
                in 21.0 .. 50.0 -> {
                    // LEVEL 2
                    Log.d("BatteryBroadcastReceiver", "Level 2")

                    stopAllServices(context)
                    val locationService = Intent(context, LocationUtilities::class.java)
                    locationService.putExtra("UPDATE_TIME", 3000)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ContextCompat.startForegroundService(context, locationService)
                        Log.d("BatteryBroadcastReceiver", "locationService(update_time 3000) started as foreground")
                    }
                    else {
                        context.startService(locationService)
                        Log.d("BatteryBroadcastReceiver", "locationService(update_time 3000) started")
                    }
                    val dBService = Intent(context, DatabaseUpdateService::class.java)
                    val geoService = Intent(context, GeofencingService::class.java)
                    context.startService(dBService)
                    Log.d("BatteryBroadcastReceiver", "DBService started")
                    context.startService(geoService)
                    Log.d("BatteryBroadcastReceiver", "GeoService started")
                    WakeLockManager.acquireWakeLock(context)

                }
                in 11.0 .. 20.0 -> {
                    // LEVEL 3
                    Log.d("BatteryBroadcastReceiver", "Level 3")

                    stopAllServices(context)
                    val locationService = Intent(context, LocationUtilities::class.java)
                    locationService.putExtra("UPDATE_TIME", 5000)
                    locationService.putExtra("ACCURACY", 1)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ContextCompat.startForegroundService(context, locationService)
                        Log.d("BatteryBroadcastReceiver", "locationService(update_time 5000, accuracy 1) started as foreground")
                    }
                    else {
                        context.startService(locationService)
                        Log.d("BatteryBroadcastReceiver", "locationService(update_time 5000, accuracy 1) started as foreground")
                    }
                    val geoService = Intent(context, GeofencingService::class.java)
                    context.startService(geoService)
                    Log.d("BatteryBroadcastReceiver", "GeoService started")
                    WakeLockManager.releaseWakeLock()
                }
                else -> {
                    //level 4
                    Log.d("BatteryBroadcastReceiver", "Level 4")

                    stopAllServices(context)
                    val locationService = Intent(context, LocationUtilities::class.java)
                    locationService.putExtra("UPDATE_TIME", 10000)
                    locationService.putExtra("ACCURACY", 1)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ContextCompat.startForegroundService(context, locationService)
                        Log.d("BatteryBroadcastReceiver", "locationService(update_time 10000, accuracy 1) started as foreground")
                    }
                    else{
                        context.startService(locationService)
                        Log.d("BatteryBroadcastReceiver", "locationService(update_time 10000, accuracy 1) started")
                    }
                    WakeLockManager.releaseWakeLock()
                    createNotificationChannels(context)
                    showNotificationBrightness(context)
                }
            }
            PreferencesHelper.putInt("LEVEL", lastExecutedLevel)
        }

        // onLevelManagement(percentage)
    }


    private fun stopAllServices(context: Context) {
        Log.d("batteryBroadcastReceiver_stopAllServices", "stopAllServices() called")
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningServices = manager.getRunningServices(Integer.MAX_VALUE)
        for (serviceInfo in runningServices) {
            if (serviceInfo.service.packageName == context.packageName) {
                val serviceName = serviceInfo.service.className
                val serviceIntent = Intent(context, Class.forName(serviceName))
                context.stopService(serviceIntent)
                Log.d("batteryBroadcastReceiver_stopAllServices", "$serviceName stopped")
            }
        }
    }


    private fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nameChB = getString(context, R.string.channel_nameB)
            val nameChD = getString(context, R.string.channel_nameD)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channelBrightness = NotificationChannel(CHANNEL_BRIGHTNESS, nameChB, importance)
            val channelDarkmode = NotificationChannel(CHANNEL_DARKMODE, nameChD, importance)

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channelBrightness)
            notificationManager.createNotificationChannel(channelDarkmode)
        }
    }

    private fun showNotificationBrightness(context: Context) {
        val notificationBrightness = NotificationCompat.Builder(context, CHANNEL_BRIGHTNESS)
            .setSmallIcon(R.drawable.brightness_alert_24dp)
            .setContentTitle("Ridurre luminosita'")
            .setContentText("CONSIGLIO: ridurre la luminosità del dispositivo")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(150, notificationBrightness)
        Log.d("BatteryBroadcastReceiver", "brightness notification showed")
    }
    private fun showNotificationDarkMode(context: Context) {
        val notificationDarkMode = NotificationCompat.Builder(context, CHANNEL_DARKMODE)
            .setSmallIcon(R.drawable.dark_mode_24dp)
            .setContentTitle("Attivare Dark Mode")
            .setContentText("CONSIGLIO: attivare la modalità dark mode")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(151, notificationDarkMode)
        Log.d("BatteryBroadcastReceiver", "dark mode notification showed")
    }

    private fun onCurrentLevel(percentage: Float): Int{
        return when(percentage){
            in 71.0 .. 100.0 -> 0
            in 51.0 .. 70.0 -> 1
            in 21.0 .. 50.0 -> 2
            in 11.0 .. 20.0 -> 3
            else -> 4
        }
    }

}