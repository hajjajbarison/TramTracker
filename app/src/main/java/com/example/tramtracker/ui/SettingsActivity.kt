package com.example.tramtracker.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tramtracker.MainActivity
import com.example.tramtracker.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

/*
Activity che andrà a contenere tutti i fragment relativi alle impostazioni: livelli, monitoraggio
e maggiori dettagli sul risparmio.
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var bottomNavigationViewSettings : BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        bottomNavigationViewSettings = findViewById(R.id.bottom_navigation_view_settings)

        bottomNavigationViewSettings.selectedItemId = R.id.navigation_impostazioni

        bottomNavigationViewSettings.setOnItemSelectedListener { item ->
            when(item.itemId) {

                R.id.navigation_mappa -> {
                    val settingsToMap = Intent(this@SettingsActivity, MainActivity::class.java)
                    startActivity(settingsToMap)
                    true
                }

                R.id.navigation_lista -> {
                    val settingsToList = Intent(this@SettingsActivity, ListActivity::class.java)
                    startActivity(settingsToList)
                    true
                }

                else -> false
            }
        }

    }
}