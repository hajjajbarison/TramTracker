package com.example.tramtracker.database

//Classe dati che serve per modellare il dato "fermata"
//NOTA: Andata -> Dalla fermata "GUIZZA" alla fermata "PONTEVIGODARZARE"
enum class Directions {ANDATA, RITORNO}
data class Stop(val id : Int, val name : String, val direction : Directions, val latitude : Double, val longitude : Double, var description : String, var imagePath : String)