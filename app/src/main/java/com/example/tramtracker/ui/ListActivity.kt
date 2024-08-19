package com.example.tramtracker.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.tramtracker.MainActivity
import com.example.tramtracker.R
import com.example.tramtracker.database.DatabaseHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

/*
Activity che andrà a contenere i Fragment TramListFragment e MoreDetailFragment.
All'interno dell'activity viene gestita anche la navigazione, con utilizzo di intent, da DetailActivity
a MoreDetaiLFragment.
 */

class ListActivity : AppCompatActivity() {

    private var isFromDetailActivity: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_activity)

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val intent = intent
        val nomeFermata : String

        isFromDetailActivity = intent.hasExtra("INTENT_MAGGIORI_DETTAGLI")

        setupBottomNavigation()

        if (isFromDetailActivity) {
            nomeFermata = intent.getStringExtra("INTENT_MAGGIORI_DETTAGLI").orEmpty()

            // loadFragmentWithBundle("INTENT_MAGGIORI_DETTAGLI", nomeFermata)

            val bundle = Bundle().apply {
                putString("INTENT_MAGGIORI_DETTAGLI", nomeFermata)
                putBoolean("FROM_DETAIL_ACTIVITY", isFromDetailActivity)
            }
            val fragment = MoreDetailFragment().apply {
                arguments = bundle
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view_list, fragment)
                .commit()

        }

    }

    private fun setupBottomNavigation() {
        val bottomNavigationViewList = findViewById<BottomNavigationView>(R.id.bottom_navigation_view_list)

        bottomNavigationViewList.selectedItemId = R.id.navigation_lista

        bottomNavigationViewList.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_mappa -> {
                    val listToMap = Intent(this@ListActivity, MainActivity::class.java)
                    startActivity(listToMap)
                    true
                }
                R.id.navigation_lista -> {
                    val listToList = Intent(this@ListActivity, ListActivity::class.java)
                    startActivity(listToList)
                    true
                }
                R.id.navigation_impostazioni -> {
                    val listToSettings = Intent(this@ListActivity, SettingsActivity::class.java)
                    startActivity(listToSettings)
                    true
                }
                else -> false
            }
        }
    }

    companion object {
        fun getDatabaseInstance(context: Context): DatabaseHelper {
            return MainActivity.getDatabaseInstance(context)
        }
    }

}