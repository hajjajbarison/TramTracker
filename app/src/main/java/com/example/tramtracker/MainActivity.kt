@file:Suppress("DEPRECATION")

package com.example.tramtracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.DisplayMetrics
import android.Manifest
import android.app.ActivityManager
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.tramtracker.database.DatabaseHelper
import com.example.tramtracker.database.Directions
import com.example.tramtracker.database.Stop
import com.example.tramtracker.ui.DetailActivity
import com.example.tramtracker.ui.ListActivity
import com.example.tramtracker.ui.SettingsActivity
import com.example.tramtracker.utilities.BatteryBroadcastReceiver
import com.example.tramtracker.utilities.DatabasePopulator
import com.example.tramtracker.utilities.DatabaseUpdateService
import com.example.tramtracker.utilities.GeofencingService
import com.example.tramtracker.utilities.LocationUtilities
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.osmdroid.config.Configuration.*
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import com.example.tramtracker.utilities.PositionConstants
import com.example.tramtracker.utilities.PreferencesHelper
import com.example.tramtracker.utilities.WakeLockManager
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var fermataVicina1 : CardView
    private lateinit var nomeFermataVicina1 : TextView
    private lateinit var nomeFermataVicina2 : TextView
    private lateinit var fermataVicina2 : CardView
    private lateinit var bottomNavigationView : BottomNavigationView
    //private lateinit var cacheManager: CacheManager


    var userLatitude : Double = 0.0
    var userLongitude : Double = 0.0

    //Funzione che si occupa di implementare il broadcast receiver per la ricezione della posizione in tempo reale
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val latitude = intent!!.getStringExtra(PositionConstants.EXTRA_LATITUDE)
            val longitude = intent.getStringExtra(PositionConstants.EXTRA_LONGITUDE)

            if (latitude != null && longitude != null && latitude != "null" && longitude != "null") {
                val distanceMap = LocationUtilities.getDistancesMap(getDatabaseInstance(applicationContext)
                    .getPartialStopsList(Directions.ANDATA), latitude.toDouble(), longitude.toDouble())

                val stop1 = getDatabaseInstance(applicationContext).getStop(distanceMap.values.take(2)[0])
                val stop2 = getDatabaseInstance(applicationContext).getStop(distanceMap.values.take(2)[1])

                findViewById<CardView>(R.id.fermata_vicina_1)

                userLatitude = latitude.toDouble()
                userLongitude = longitude.toDouble()

                findViewById<TextView>(R.id.nome_fermata_vicina_1).text = stop1?.name
                findViewById<TextView>(R.id.nome_fermata_vicina_2).text = stop2?.name

                findViewById<CardView>(R.id.fermata_vicina_1).isClickable = true
                findViewById<CardView>(R.id.fermata_vicina_2).isClickable = true
            }
        }
    }

    // Variabile per richiedere i permessi per la localizzazione e bloccare l'applicazione finchè l'utente non li accetta o rifiuta
    /*private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            initializeApp()
        } else {
            Log.e("MainActivity", "Permessi non concessi")
            finish()
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        showLoadingPanel()

        // Load/initialize the osmdroid configuration.
        getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        findViewById<View>(R.id.blockingView).setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                view.performClick()
                true
            } else {
                true
            }
        }

        map = findViewById(R.id.map)
        initializeMap(this, map)

        // Controllo e richiesta dei permessi per la posizione
        val permissionList = arrayListOf<String>()

        // Notifiche (a partire da Android 13)
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED)
            permissionList.add(Manifest.permission.POST_NOTIFICATIONS)


        // Location approssimata
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        // Location esatta
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)

        // Chiedo permessi
        if (permissionList.size > 0){
            // Mancano alcuni permessi, provo a chiederli
            requestPermissions(permissionList.toTypedArray(), 1)
        } // Ricevo aggiornamenti in onRequestPermissionResult()
        else{
            initializeApp()
        }

    }
        /*if (allPermissionsGranted()) {
            initializeApp()
        } else {
            requestPermissions()
        }*/

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("MainActivity", "Chiamato onRequestPermissionsResult")
        // Controllo ogni permesso
        for ((index, result) in grantResults.withIndex()){
            if (result == PackageManager.PERMISSION_DENIED){
                Log.d("MainActivity", "C'è almeno un permesso negato: ${permissions[index]}")
                finish()
                return
            }
        }
        initializeApp()
    }

    /*private fun allPermissionsGranted() = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).all {
        ContextCompat.checkSelfPermission(
            this, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
*/
    private fun initializeApp() {
        showLoadingPanel()

        //Initialize database
        //Thread{
        initializeDatabase(this, getDatabaseInstance(this))

        //}.start()
        hideLoadingPanel()

        addMarker(this, map, getDatabaseInstance(this))

        // Configura CacheManager
        /*cacheManager = CacheManager(map)

        // Definisci i limiti dell'area da scaricare
        val boundingBoxes = listOf(
            //BoundingBox(45.44265857188915, 11.88865541035437, 45.42930543331658, 11.891200636893348),  //Prime 5 fermate nord
            //BoundingBox(45.42825595001562, 11.890114928755665, 45.41993015626234, 11.878367132065492), //Metà arcella, stazione
            //BoundingBox(45.41693140500545, 11.879765330949569, 45.40748537169584, 11.878885042549703), //Stazione - Ponti romani
            //BoundingBox(45.40522359806657, 11.87666072469161, 45.40074942471088, 11.877792192620603),  //Tram livio - santo
            BoundingBox(45.399220356993666, 11.877908004174342, 45.389848445102594, 11.869967303555676),  //Prato della valle - cavallotti
            //BoundingBox(45.38728619270078, 11.867390063857664, 45.36754584639247, 11.874130833697784)   //Bassanello - Capolinea guizza
        )
        Handler(Looper.getMainLooper()).postDelayed({
            val minZoom = 10
            val maxZoom = 20
            //downloadMultipleAreas(boundingBoxes, minZoom, maxZoom)
        }, 10000) // 10000 millisecondi = 10 secondi
        */

        fermataVicina1 = findViewById(R.id.fermata_vicina_1)
        nomeFermataVicina1 = findViewById(R.id.nome_fermata_vicina_1)
        fermataVicina2 = findViewById(R.id.fermata_vicina_2)
        nomeFermataVicina2 = findViewById(R.id.nome_fermata_vicina_2)


        fermataVicina1.setOnClickListener {
            val stop = getDatabaseInstance(this).getStop(nomeFermataVicina1.text.toString(), Directions.ANDATA)
            if (stop != null) {
                val startPoint = GeoPoint(stop.latitude, stop.longitude)
                map.controller.setCenter(startPoint)
                map.controller.zoomTo(19.0)

                // Forza l'aggiornamento della mappa
                map.invalidate()

                // Utilizza Handler per posticipare la cattura della mappa senza bloccare il thread principale
                Handler(Looper.getMainLooper()).postDelayed({
                    val path = captureMapView(map, this)

                    val fermataVicina1ToDetail = Intent(this, DetailActivity::class.java).apply {
                        putExtra("INTENT_FERMATA_VICINA1", nomeFermataVicina1.text)
                        putExtra("IMAGE_PATH", path)
                    }

                    startActivity(fermataVicina1ToDetail)
                }, 500) // Ritardo di 3 secondi, regola questo valore se necessario
            }
        }

        fermataVicina2.setOnClickListener {
            val stop = getDatabaseInstance(this).getStop(nomeFermataVicina2.text.toString(), Directions.ANDATA)
            if (stop != null) {
                val startPoint = GeoPoint(stop.latitude, stop.longitude)
                map.controller.setCenter(startPoint)
                map.controller.zoomTo(19.0)

                // Forza l'aggiornamento della mappa
                map.invalidate()

                // Utilizza Handler per posticipare la cattura della mappa senza bloccare il thread principale
                Handler(Looper.getMainLooper()).postDelayed({
                    val path = captureMapView(map, this)

                    val fermataVicina2ToDetail = Intent(this, DetailActivity::class.java).apply {
                        putExtra("INTENT_FERMATA_VICINA2", nomeFermataVicina2.text)
                        putExtra("IMAGE_PATH", path)
                    }

                    startActivity(fermataVicina2ToDetail)
                }, 500) // Ritardo di 3 secondi, regola questo valore se necessario
            }
        }

        fermataVicina1.isClickable = false
        fermataVicina2.isClickable = false


        bottomNavigationView = findViewById(R.id.bottom_navigation_view)

        bottomNavigationView.selectedItemId = R.id.navigation_mappa

        bottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {

                R.id.navigation_lista -> {
                    val mapToList = Intent(this@MainActivity, ListActivity::class.java)
                    startActivity(mapToList)
                    true
                }

                R.id.navigation_impostazioni -> {
                    val mapToSettings = Intent(this@MainActivity, SettingsActivity::class.java)
                    startActivity(mapToSettings)
                    true
                }

                else -> false
            }
        }

        val centerBtn = findViewById<Button>(R.id.centerBtn)

        centerBtn.setOnClickListener{
            map.controller.animateTo(GeoPoint(userLatitude, userLongitude))
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, IntentFilter(PositionConstants.ACTION_LOCATION_UPDATE))

        PreferencesHelper.init(this)
        if(PreferencesHelper.getBoolean("SWITCH", false)) {
            registerBatteryBroadcastReceiver(this)
        } else {
            Log.d("MainActivity_onCreate", "Level 0")

            stopAllServices(this)

            // Avvia gli aggiornamenti della posizione
            //LocationUtilities.startLocationUpdates(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, Intent(this, LocationUtilities::class.java))
                Log.d("MainActivity_onCreate", "locationService started as foreground")
            }
            else {
                ContextCompat.startForegroundService(this, Intent(this, LocationUtilities::class.java))
                Log.d("MainActivity_onCreate", "locationService started")
            }

            // Aggiornamento in background del database
            val dBService = Intent(this, DatabaseUpdateService::class.java)
            startService(dBService)
            Log.d("MainActivity_onCreate", "DBService started")
            // Geofencing
            val geoService = Intent(this, GeofencingService::class.java)
            startService(geoService)
            Log.d("MainActivity_onCreate", "GeoService started")
            WakeLockManager.acquireWakeLock(this)
        }
    }

    private fun showLoadingPanel() {
        findViewById<View>(R.id.loadingPanel).visibility = View.VISIBLE
        findViewById<View>(R.id.blockingView).visibility = View.VISIBLE
    }

    private fun hideLoadingPanel() {
        findViewById<View>(R.id.blockingView).visibility = View.GONE
        findViewById<View>(R.id.loadingPanel).visibility = View.GONE
    }

    private fun stopAllServices(context: Context) {

        Log.d("MainActivity_stopAllServices", "stopAllServices() called")
        // Ottiene il gestore dei servizi
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // Ottiene tutti i servizi in esecuzione
        /**
         * Nota documentazione della funzione getRunningServices:
         * "As of Build.VERSION_CODES.O, this method is no longer available to third party applications.
         * For backwards compatibility, it will still return the caller's own"
         */
        val runningServices = manager.getRunningServices(Integer.MAX_VALUE)

        for (serviceInfo in runningServices) {
            // Verifica se il servizio appartiene all'app
            if (serviceInfo.service.packageName == context.packageName) {
                // Prendo il nome del servizio
                val serviceName = serviceInfo.service.className
                val serviceIntent = Intent(context, Class.forName(serviceName))
                // Stop del service
                context.stopService(serviceIntent)
                Log.d("MainActivity_stopAllServices", "$serviceName stopped")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume() // Needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onPause() {
        super.onPause()
        map.onPause() // Needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterBatteryBroadcastReceiver(this)

        Log.d("MainActivity_onDestroy", "MainActivity destroyed")
        stopAllServices(this)
        WakeLockManager.releaseWakeLock()
    }

    /*private fun downloadMultipleAreas(boundingBoxes: List<BoundingBox>, minZoom: Int, maxZoom: Int) {
        for (boundingBox in boundingBoxes)
            cacheManager.downloadAreaAsync(this, boundingBox, minZoom, maxZoom)
    }*/

    companion object {
        private const val MIN_MAP_UPDATE_DISTANCE = 0f          //Distance in meters
        private const val MIN_MAP_UPDATE_TIME : Long = 1000            //Time in milliseconds

        private var batteryBroadcastReceiver: BatteryBroadcastReceiver? = null
        fun getDatabaseInstance(context : Context) : DatabaseHelper{
            return DatabaseHelper(context)
        }
        fun initializeDatabase(context : Context, db : DatabaseHelper){

            if(db.isDatabaseEmpty()){
                DatabasePopulator.addStops(db)
                DatabasePopulator.addTimeTable(db)
                DatabasePopulator.addImages(context, db)
            }

            if(DatabasePopulator.isInternalStorageEmpty(context)){
                DatabasePopulator.copyImagesToInternalStorage(context, db)
                DatabasePopulator.addImages(context, db)
            }
        }
        fun initializeMap(context : Context, map: MapView) {
            getInstance().osmdroidTileCache = context.filesDir

            map.setTileSource(TileSourceFactory.MAPNIK)
            map.setMultiTouchControls(true)

            // Configura la cache della mappa
            val tileCacheSize : Long = 500 * 1024 * 1024 // 500 MB
            getInstance().tileFileSystemCacheMaxBytes = tileCacheSize
            getInstance().tileFileSystemCacheTrimBytes = tileCacheSize / 2

            setupOverlays(context, map)

            //Enable rotation gesture in map
            val rotationGestureOverlay = RotationGestureOverlay(map)
            rotationGestureOverlay.isEnabled
            map.overlays.add(rotationGestureOverlay)

        }

        private fun addMarker(context : Context, map : MapView, db : DatabaseHelper){
            // Note, "context" refers to your activity/application context.
// You can simply do resources.displayMetrics when inside an activity.
// When you aren't in an activity class, you will need to have passed the context
// to the non-activity class.
            val dm : DisplayMetrics = context.resources.displayMetrics
            val scaleBarOverlay = ScaleBarOverlay(map)
            scaleBarOverlay.setCentred(true)
//play around with these values to get the location on screen in the right place for your application
            scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10)
            map.overlays.add(scaleBarOverlay)

            // Creazione dei Marker
            var marker: Marker
            var position : GeoPoint

            var nome : String
            var latitudine : Double
            var longitudine : Double

            // Creazione dei marker sulla mappa
            // prendo tutte le fermate dal database
            val listaFermate : List<Stop> = db.getStopsList()
            // Ciclo for per la creazione dei marker sulla mappa
            for (fermata in listaFermate){
                nome = fermata.name
                latitudine = fermata.latitude
                longitudine = fermata.longitude
                // setto la posizione
                position = GeoPoint(latitudine, longitudine)
                // creo il marker
                marker = Marker(map)
                // setto la posizione del marker
                marker.position = position
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = nome
                // aggiungo il marker alla mappa
                map.overlays.add(marker)

                val currentName = nome

                // Aggiunta di un evento al marker (navigazione ai dettagli della fermata)
                marker.setOnMarkerClickListener { _, _ ->
                    if(context is MainActivity){
                        val startPoint = GeoPoint(latitudine, longitudine)
                        map.controller.setCenter(startPoint)
                        map.controller.zoomTo(19.0)

                        // Forza l'aggiornamento della mappa
                        map.invalidate()

                        Handler(Looper.getMainLooper()).postDelayed({
                            val path = captureMapView(map, context)

                            val markerToDetail = Intent(context, DetailActivity::class.java).apply {
                                putExtra("INTENT_MARKER", currentName)
                                putExtra("IMAGE_PATH", path)
                            }
                            context.startActivity(markerToDetail)
                        }, 500) // Ritardo di 3 secondi, regola questo valore se necessario
                    }
                    true
                }
            }
        }

        private fun setupOverlays(context : Context, map : MapView){
            //Set initial point and zoom in map
            val mapController = map.controller
            mapController.setZoom(16.4)
            val startPoint = GeoPoint(45.408150582099154, 11.87804985047689)
            mapController.setCenter(startPoint)

            val locationProvider = GpsMyLocationProvider(context).apply {
                //Set custom parameter to try to get the position faster
                addLocationSource(LocationManager.GPS_PROVIDER)
                addLocationSource(LocationManager.NETWORK_PROVIDER)
                locationUpdateMinDistance = MIN_MAP_UPDATE_DISTANCE
                locationUpdateMinTime = MIN_MAP_UPDATE_TIME
            }

            val locationOverlay = MyLocationNewOverlay(locationProvider, map)
            locationOverlay.enableMyLocation()
            locationOverlay.enableFollowLocation()

            map.overlays.add(locationOverlay)
        }


        fun registerBatteryBroadcastReceiver(context: Context){
            if(batteryBroadcastReceiver == null){
                batteryBroadcastReceiver = BatteryBroadcastReceiver()
                context.applicationContext.registerReceiver(batteryBroadcastReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                Log.d("MainActivity_registerBatteryBroadcastReceiver", "receiver registered")
            }
        }

        fun unregisterBatteryBroadcastReceiver(context: Context){
            if(batteryBroadcastReceiver != null){
                context.applicationContext.unregisterReceiver(batteryBroadcastReceiver)
                batteryBroadcastReceiver = null
                Log.d("MainActivity_unregisterBatteryBroadcastReceiver", "receiver unregistered")
            }
        }

        //Funzione per fare lo screen della mappa in una immagine da usare in seguito
        private fun captureMapView(map : MapView, context: Context) : String{
            // Crea un oggetto Bitmap con le dimensioni della MapView
            val bitmap = Bitmap.createBitmap(map.width, map.height, Bitmap.Config.ARGB_8888)

            // Crea un oggetto Canvas e disegna la MapView sul Canvas
            val canvas = Canvas(bitmap)
            map.draw(canvas)

            // Salva il Bitmap in un file
            return saveBitmapToFile(bitmap, context)
        }

        //Funzione per salvare lo screen della mappa su file
        private fun saveBitmapToFile(bitmap: Bitmap, context : Context) : String {
            val filename = "map_screenshot.png"
            val file = File(File(context.filesDir, "images"), filename)

            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return file.absolutePath
        }
    }
}