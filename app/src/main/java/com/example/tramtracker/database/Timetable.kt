package com.example.tramtracker.database

//Classe dati per modellare il tipo di dato "orario"; c'è anche configurato un enum per i due possibili tipi di giorno della settimana
enum class DayType {FERIALE, FESTIVO}
data class Timetable(val stop : Int, val hour : String, val minute : String, val dayType: DayType)