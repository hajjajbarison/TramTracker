package com.example.tramtracker.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

//Classe che serve per la gestione di un database SQLite3, la classe contiene metodi per la gestione, aggiornamento ed inserimento di fermate ed orari
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION){

    //Contentitore di costanti per il nome delle tabelle e dei relativi attributi
    companion object{
        //Database Settings
        private const val DATABASE_NAME = "tramtracker.db"
        private const val DATABASE_VERSION = 1

        //Stop table settings
        private const val STOPS_TABLE_NAME = "fermate"
        private const val STOPS_COLUMN_ID = "id"
        private const val STOPS_COLUMN_NAME = "nome"
        private const val STOPS_COLUMN_DIRECTION = "direzione"
        private const val STOPS_COLUMN_LATITUDE = "latitudine"
        private const val STOPS_COLUMN_LONGITUDE = "longitudine"
        private const val STOPS_COLUMN_DESCRIPTION = "descrizione"
        private const val STOPS_COLUMN_IMAGE_PATH = "pathImmagine"

        //Timetable table settings
        private const val TIMETABLE_TABLE_NAME = "orari"
        private const val TIMETABLE_COLUMN_ID = "id"
        private const val TIMETABLE_COLUMN_STOP = "fermata"
        private const val TIMETABLE_COLUMN_HOUR = "ore"
        private const val TIMETABLE_COLUMN_MINUTE = "minuti"
        private const val TIMETABLE_COLUMN_DAYTYPE = "giorno"
    }

    //Creazione del database
    override fun onCreate(db: SQLiteDatabase?) {
        val creteStopsTableQuery =
            "CREATE TABLE $STOPS_TABLE_NAME(" +
                    "$STOPS_COLUMN_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "$STOPS_COLUMN_NAME TEXT(30) NOT NULL ," +
                    "$STOPS_COLUMN_DIRECTION TEXT CHECK($STOPS_COLUMN_DIRECTION IN ('ANDATA', 'RITORNO'))," +
                    "$STOPS_COLUMN_LATITUDE DOUBLE(5) NOT NULL ," +
                    "$STOPS_COLUMN_LONGITUDE DOUBLE(5) NOT NULL ," +
                    "$STOPS_COLUMN_DESCRIPTION TEXT(300) ," +
                    "$STOPS_COLUMN_IMAGE_PATH TEXT(30))"
        val createTimetableQuery =
            "CREATE TABLE $TIMETABLE_TABLE_NAME(" +
                    "$TIMETABLE_COLUMN_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                    "$TIMETABLE_COLUMN_STOP INTEGER NOT NULL," +
                    "$TIMETABLE_COLUMN_HOUR TEXT(2) NOT NULL," +
                    "$TIMETABLE_COLUMN_MINUTE TEXT(2) NOT NULL," +
                    "$TIMETABLE_COLUMN_DAYTYPE TEXT CHECK($TIMETABLE_COLUMN_DAYTYPE IN ('FERIALE','FESTIVO')) NOT NULL," +
                    "CONSTRAINT fk_stops" +
                    "    FOREIGN KEY ($TIMETABLE_COLUMN_STOP)\n" +
                    "    REFERENCES $STOPS_TABLE_NAME($STOPS_COLUMN_ID)" +
                    ")"

        db?.execSQL(creteStopsTableQuery)
        db?.execSQL(createTimetableQuery)
    }

    //Aggiornamento del database in caso il database helper rilevi una nuova versione
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //TODO("IMPLEMENTA MEGLIO QUESTA COSA")
        val dropStopsTableQuery = "DROP TABLE IF EXISTS $STOPS_TABLE_NAME"
        val dropTimetableQuery = "DROP TABLE IF EXISTS $TIMETABLE_TABLE_NAME"

        db?.execSQL(dropStopsTableQuery)
        db?.execSQL(dropTimetableQuery)

        onCreate(db)
    }

    //Funzioni per 'inserimento di una fermata
    fun insertStop(stop : Stop, tableName : String = STOPS_TABLE_NAME){
        val db = writableDatabase
        val values = ContentValues().apply {
            put(STOPS_COLUMN_NAME, stop.name)
            put(STOPS_COLUMN_DIRECTION, stop.direction.toString())
            put(STOPS_COLUMN_LATITUDE, stop.latitude)
            put(STOPS_COLUMN_LONGITUDE, stop.longitude)
            put(STOPS_COLUMN_DESCRIPTION, stop.description)
            put(STOPS_COLUMN_IMAGE_PATH, stop.imagePath)
        }
        db.insert(tableName, null ,values)
        db.close()
    }

    //Funzione per 'inserimento di un orario
    fun insertTimetable(timetable : Timetable, tableName : String = TIMETABLE_TABLE_NAME){
        val db = writableDatabase
        val values = ContentValues().apply {
            put(TIMETABLE_COLUMN_STOP, timetable.stop)
            put(TIMETABLE_COLUMN_HOUR, timetable.hour)
            put(TIMETABLE_COLUMN_MINUTE, timetable.minute)
            put(TIMETABLE_COLUMN_DAYTYPE, timetable.dayType.toString())
        }

        db.insert(tableName, null, values)
        db.close()
    }

    //Funzione per ottenere la lista di tutte le fermate
    fun getStopsList() : List<Stop>{
        val stopList = mutableListOf<Stop>()
        val db = readableDatabase
        val query = "SELECT * FROM $STOPS_TABLE_NAME;"

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()){
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(STOPS_COLUMN_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(STOPS_COLUMN_NAME))
            val direction = cursor.getString(cursor.getColumnIndexOrThrow(STOPS_COLUMN_DIRECTION))
            val latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(STOPS_COLUMN_LATITUDE))
            val longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(STOPS_COLUMN_LONGITUDE))
            val description = cursor.getString(cursor.getColumnIndexOrThrow(STOPS_COLUMN_DESCRIPTION))
            val imagePath = cursor.getString(cursor.getColumnIndexOrThrow(STOPS_COLUMN_IMAGE_PATH))

            stopList.add(Stop(id, name, enumValueOf<Directions>(direction), latitude, longitude, description, imagePath))
        }

        cursor.close()
        db.close()

        return stopList
    }

    //Funzione per ottenere parte delle fermate (tutte le fermate in una sola direzione)
    fun getPartialStopsList(dir : Directions) : List<Stop>{
        val stopList = mutableListOf<Stop>()
        val db = readableDatabase
        val query = "SELECT * FROM $STOPS_TABLE_NAME WHERE $STOPS_COLUMN_DIRECTION = \"$dir\" OR $STOPS_COLUMN_NAME = 'Assunta';"

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()){
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(STOPS_COLUMN_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(STOPS_COLUMN_NAME))
            val direction = cursor.getString(cursor.getColumnIndexOrThrow(STOPS_COLUMN_DIRECTION))
            val latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(STOPS_COLUMN_LATITUDE))
            val longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(STOPS_COLUMN_LONGITUDE))
            val description = cursor.getString(cursor.getColumnIndexOrThrow(STOPS_COLUMN_DESCRIPTION))
            val imagePath = cursor.getString(cursor.getColumnIndexOrThrow(STOPS_COLUMN_IMAGE_PATH))

            stopList.add(Stop(id, name, enumValueOf<Directions>(direction), latitude, longitude, description, imagePath))
        }

        val assuntaStop = stopList.find { it.name == "Assunta" }
        stopList.removeIf { it.name == "Assunta" }
        stopList.add(4, assuntaStop!!)

        cursor.close()
        db.close()

        return stopList
    }

    //Funzione per ottenere una fermata in base al duo ID
    fun getStop(id : Int) : Stop?{
        val db = readableDatabase
        val query = "SELECT * FROM $STOPS_TABLE_NAME WHERE $STOPS_COLUMN_ID = $id;"

        val cursor = db.rawQuery(query, null)

        //return qui dentro tanto fa un solo ciclo dato che ID è un campo univoco e non ci possono essere più di 1 record
        while (cursor.moveToNext()){
            val elemId = cursor.getInt(cursor.getColumnIndexOrThrow(STOPS_COLUMN_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(STOPS_COLUMN_NAME))
            val direction = cursor.getString(cursor.getColumnIndexOrThrow(STOPS_COLUMN_DIRECTION))
            val latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(STOPS_COLUMN_LATITUDE))
            val longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(STOPS_COLUMN_LONGITUDE))
            val description = cursor.getString(cursor.getColumnIndexOrThrow(STOPS_COLUMN_DESCRIPTION))
            val imagePath = cursor.getString(cursor.getColumnIndexOrThrow(STOPS_COLUMN_IMAGE_PATH))

            cursor.close()
            db.close()
            return Stop(elemId,name,enumValueOf<Directions>(direction),latitude,longitude,description, imagePath)
        }

        cursor.close()
        db.close()
        return null
    }

    //Funzione per ottenere una fermata in base al suo nome e direzione
    fun getStop(name : String, direction : Directions) : Stop?{
        val db = readableDatabase
        val query = "SELECT * FROM $STOPS_TABLE_NAME WHERE $STOPS_COLUMN_NAME = \"$name\" AND $STOPS_COLUMN_DIRECTION = \"$direction\";"

        val cursor = db.rawQuery(query, null)

        //return qui dentro tanto fa un solo ciclo dato che ID è un campo univoco e non ci possono essere più di 1 record
        while (cursor.moveToNext()){
            val id = cursor.getInt(cursor.getColumnIndexOrThrow(STOPS_COLUMN_ID))
            val elemName = cursor.getString(cursor.getColumnIndexOrThrow(STOPS_COLUMN_NAME))
            val elemDirection = cursor.getString(cursor.getColumnIndexOrThrow(STOPS_COLUMN_DIRECTION))
            val latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(STOPS_COLUMN_LATITUDE))
            val longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(STOPS_COLUMN_LONGITUDE))
            val description = cursor.getString(cursor.getColumnIndexOrThrow(STOPS_COLUMN_DESCRIPTION))
            val imagePath = cursor.getString(cursor.getColumnIndexOrThrow(STOPS_COLUMN_IMAGE_PATH))

            cursor.close()
            db.close()
            return Stop(id,elemName,enumValueOf<Directions>(elemDirection),latitude,longitude,description,imagePath)
        }

        cursor.close()
        db.close()
        return null
    }

    //Funzione per ottenere tutti gli orari di partenza
    fun getTimetableList() : List<Timetable>{
        val timeTableList = mutableListOf<Timetable>()
        val db = readableDatabase
        val query = "SELECT * FROM $TIMETABLE_TABLE_NAME;"

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()){
            val stop = cursor.getInt(cursor.getColumnIndexOrThrow(TIMETABLE_COLUMN_STOP))
            val hour = cursor.getString(cursor.getColumnIndexOrThrow(TIMETABLE_COLUMN_HOUR))
            val minute = cursor.getString(cursor.getColumnIndexOrThrow(TIMETABLE_COLUMN_MINUTE))
            val day = cursor.getString(cursor.getColumnIndexOrThrow(TIMETABLE_COLUMN_DAYTYPE))

            timeTableList.add(Timetable(stop, hour, minute, enumValueOf<DayType>(day)))
        }

        cursor.close()
        db.close()

        return timeTableList
    }

    /*fun getTimetableTimes(stopId : Int, day : DayType) : List<Timetable>{
        val timeTableList = mutableListOf<Timetable>()
        val db = readableDatabase
        val query = "SELECT * FROM $TIMETABLE_TABLE_NAME WHERE $TIMETABLE_COLUMN_STOP = $stopId AND $TIMETABLE_COLUMN_DAYTYPE = \"$day\";"

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()){
            val elemStop = cursor.getInt(cursor.getColumnIndexOrThrow(TIMETABLE_COLUMN_STOP))
            val hour = cursor.getString(cursor.getColumnIndexOrThrow(TIMETABLE_COLUMN_HOUR))
            val minute = cursor.getString(cursor.getColumnIndexOrThrow(TIMETABLE_COLUMN_MINUTE))
            val elemDay = cursor.getString(cursor.getColumnIndexOrThrow(TIMETABLE_COLUMN_DAYTYPE))

            timeTableList.add(Timetable(elemStop, hour, minute, enumValueOf<DayType>(elemDay)))
        }

        cursor.close()
        db.close()

        return timeTableList
    }*/

    //Funzione per ottenere i prossimi x orari di partenza in base all'orario attuale
    fun getNextTimetableTimes(currentHour : String, currentMinute : String, dayType : DayType, stopName : String, stopDirection : Directions, numberOfTimes : Int) : List<Timetable>{
        val stopId = getStop(stopName, stopDirection)?.id

        val timeTableList = mutableListOf<Timetable>()
        val db = readableDatabase
        val query = "SELECT * FROM $TIMETABLE_TABLE_NAME WHERE " +
                "($TIMETABLE_COLUMN_STOP = $stopId) AND " +
                "($TIMETABLE_COLUMN_DAYTYPE = \"$dayType\") AND " +
                "(($TIMETABLE_COLUMN_HOUR > \"${currentHour.padStart(2, '0')}\") OR " +
                "($TIMETABLE_COLUMN_HOUR = \"${currentHour.padStart(2, '0')}\" AND $TIMETABLE_COLUMN_MINUTE >= \"${currentMinute.padStart(2, '0')}\")) " +
                "ORDER BY $TIMETABLE_COLUMN_HOUR, $TIMETABLE_COLUMN_MINUTE " +
                "LIMIT $numberOfTimes"

        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()){
            val elemStop = cursor.getInt(cursor.getColumnIndexOrThrow(TIMETABLE_COLUMN_STOP))
            val hour = cursor.getString(cursor.getColumnIndexOrThrow(TIMETABLE_COLUMN_HOUR))
            val minute = cursor.getString(cursor.getColumnIndexOrThrow(TIMETABLE_COLUMN_MINUTE))
            val elemDay = cursor.getString(cursor.getColumnIndexOrThrow(TIMETABLE_COLUMN_DAYTYPE))

            timeTableList.add(Timetable(elemStop, hour, minute, enumValueOf<DayType>(elemDay)))
        }

        cursor.close()
        db.close()

        return timeTableList
    }

    //Funzione per aggiornare i dati di una fermata avente il suo ID
    fun updateStop(id : Int, stop : Stop){
        val db = writableDatabase

        val stopToUpdate = ContentValues()

        stopToUpdate.put(STOPS_COLUMN_NAME, stop.name)
        stopToUpdate.put(STOPS_COLUMN_DIRECTION, stop.direction.toString())
        stopToUpdate.put(STOPS_COLUMN_LATITUDE, stop.latitude)
        stopToUpdate.put(STOPS_COLUMN_LONGITUDE, stop.longitude)
        stopToUpdate.put(STOPS_COLUMN_DESCRIPTION, stop.description)
        stopToUpdate.put(STOPS_COLUMN_IMAGE_PATH, stop.imagePath)


        db.update(STOPS_TABLE_NAME, stopToUpdate, "$STOPS_COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
    }

    //Funzione per verificare se il database è vuoto
    fun isDatabaseEmpty() : Boolean{
        val db = readableDatabase

        val stopsTableEntry = db.rawQuery("SELECT COUNT(*) FROM $STOPS_TABLE_NAME;", null)
        val timetableTableEntry = db.rawQuery("SELECT COUNT(*) FROM $TIMETABLE_TABLE_NAME;", null)

        stopsTableEntry.moveToFirst()
        timetableTableEntry.moveToFirst()

        val stopElemCount = stopsTableEntry.getInt(0)
        val timetableElemCount = timetableTableEntry.getInt(0)

        stopsTableEntry.close()
        timetableTableEntry.close()
        db.close()

        return stopElemCount == 0 && timetableElemCount == 0
    }

    //Funzione per aggiornare il database
    /*NOTA: La parte commentata è una parte aggiuntiva che servirebbe, in caso di collegamento a database remoto, per rimuovere fermate o orari
    * che non ci sono più ed aggiungere eventuali nuove fermate ed orari
    * È attualmente commentata in quanto inutile in questo caso d'uso e provoca la duplicazione dei dati nelle tabelle*/
    fun updateDatabase(stopList : List<Stop>, timetableList : List<Timetable>){
        var db = writableDatabase


        val tmpStopTblName = "tmp_fermate"
        val tmpTableName = "tmp_orari"

        db.execSQL("PRAGMA foreign_keys = OFF; ")
        db.execSQL("DROP TABLE IF EXISTS $tmpStopTblName")

        db.execSQL("DROP TABLE IF EXISTS $tmpTableName")



        val tmpStopTblQuery = "CREATE TABLE $tmpStopTblName(" +
                "$STOPS_COLUMN_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "$STOPS_COLUMN_NAME TEXT(30) NOT NULL ," +
                "$STOPS_COLUMN_DIRECTION TEXT CHECK($STOPS_COLUMN_DIRECTION IN ('ANDATA', 'RITORNO'))," +
                "$STOPS_COLUMN_LATITUDE DOUBLE(5) NOT NULL ," +
                "$STOPS_COLUMN_LONGITUDE DOUBLE(5) NOT NULL ," +
                "$STOPS_COLUMN_DESCRIPTION TEXT(300) ," +
                "$STOPS_COLUMN_IMAGE_PATH TEXT(30))"
        val tmpTimeTableQuery = "CREATE TABLE $tmpTableName(" +
                "$TIMETABLE_COLUMN_ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," +
                "$TIMETABLE_COLUMN_STOP INTEGER NOT NULL," +
                "$TIMETABLE_COLUMN_HOUR TEXT(2) NOT NULL," +
                "$TIMETABLE_COLUMN_MINUTE TEXT(2) NOT NULL," +
                "$TIMETABLE_COLUMN_DAYTYPE TEXT CHECK($TIMETABLE_COLUMN_DAYTYPE IN ('FERIALE','FESTIVO')) NOT NULL," +
                "CONSTRAINT fk_stops" +
                "    FOREIGN KEY ($TIMETABLE_COLUMN_STOP)\n" +
                "    REFERENCES $STOPS_TABLE_NAME($STOPS_COLUMN_ID)" +
                ")"

        db.execSQL(tmpStopTblQuery)
        db.execSQL(tmpTimeTableQuery)
        db.close()


        for (stop in stopList)
            insertStop(stop, tmpStopTblName)

        for (departure in timetableList)
            insertTimetable(departure, tmpTableName)


        db = writableDatabase

        //Update data
        db.execSQL(
            "UPDATE $STOPS_TABLE_NAME SET " +
                    "$STOPS_COLUMN_NAME = (SELECT $tmpStopTblName.$STOPS_COLUMN_NAME FROM $tmpStopTblName WHERE $tmpStopTblName.$STOPS_COLUMN_ID = $STOPS_TABLE_NAME.$STOPS_COLUMN_ID), " +
                    "$STOPS_COLUMN_DIRECTION = (SELECT $tmpStopTblName.$STOPS_COLUMN_DIRECTION FROM $tmpStopTblName WHERE $tmpStopTblName.$STOPS_COLUMN_ID = $STOPS_TABLE_NAME.$STOPS_COLUMN_ID), " +
                    "$STOPS_COLUMN_LATITUDE = (SELECT $tmpStopTblName.$STOPS_COLUMN_LATITUDE FROM $tmpStopTblName WHERE $tmpStopTblName.$STOPS_COLUMN_ID = $STOPS_TABLE_NAME.$STOPS_COLUMN_ID), " +
                    "$STOPS_COLUMN_LONGITUDE = (SELECT $tmpStopTblName.$STOPS_COLUMN_LONGITUDE FROM $tmpStopTblName WHERE $tmpStopTblName.$STOPS_COLUMN_ID = $STOPS_TABLE_NAME.$STOPS_COLUMN_ID), " +
                    "$STOPS_COLUMN_DESCRIPTION = (SELECT $tmpStopTblName.$STOPS_COLUMN_DESCRIPTION FROM $tmpStopTblName WHERE $tmpStopTblName.$STOPS_COLUMN_ID = $STOPS_TABLE_NAME.$STOPS_COLUMN_ID) " +
                    "WHERE $STOPS_COLUMN_ID IN (SELECT $tmpStopTblName.$STOPS_COLUMN_ID FROM $tmpStopTblName)"
        )

        /*//Add new data
        db.execSQL(
            "INSERT INTO $STOPS_TABLE_NAME ($STOPS_COLUMN_ID, $STOPS_COLUMN_NAME, $STOPS_COLUMN_DIRECTION, $STOPS_COLUMN_LATITUDE, $STOPS_COLUMN_LONGITUDE, $STOPS_COLUMN_DESCRIPTION) " +
                    "SELECT $tmpStopTblName.$STOPS_COLUMN_ID, $tmpStopTblName.$STOPS_COLUMN_NAME, $tmpStopTblName.$STOPS_COLUMN_DIRECTION, $tmpStopTblName.$STOPS_COLUMN_LATITUDE, $tmpStopTblName.$STOPS_COLUMN_LONGITUDE, $tmpStopTblName.$STOPS_COLUMN_DESCRIPTION " +
                    "FROM $tmpStopTblName " +
                    "LEFT JOIN $STOPS_TABLE_NAME ON $tmpStopTblName.$STOPS_COLUMN_ID = $STOPS_TABLE_NAME.$STOPS_COLUMN_ID " +
                    "WHERE $STOPS_TABLE_NAME.$STOPS_COLUMN_ID IS NULL"
        )

        //Remove not more existing stops
        db.execSQL(
            "DELETE FROM $STOPS_TABLE_NAME " +
                    "WHERE $STOPS_COLUMN_ID IN (" +
                    "SELECT $STOPS_TABLE_NAME.$STOPS_COLUMN_ID " +
                    "FROM $STOPS_TABLE_NAME " +
                    "LEFT JOIN $tmpStopTblName ON $STOPS_TABLE_NAME.$STOPS_COLUMN_ID = $tmpStopTblName.$STOPS_COLUMN_ID " +
                    "WHERE $tmpStopTblName.$STOPS_COLUMN_ID IS NULL)"
        )

        // Update data
        db.execSQL(
            "UPDATE $TIMETABLE_TABLE_NAME SET " +
                    "$TIMETABLE_COLUMN_STOP = (SELECT $tmpTableName.$TIMETABLE_COLUMN_STOP FROM $tmpTableName WHERE $tmpTableName.$TIMETABLE_COLUMN_ID = $TIMETABLE_TABLE_NAME.$TIMETABLE_COLUMN_ID), " +
                    "$TIMETABLE_COLUMN_HOUR = (SELECT $tmpTableName.$TIMETABLE_COLUMN_HOUR FROM $tmpTableName WHERE $tmpTableName.$TIMETABLE_COLUMN_ID = $TIMETABLE_TABLE_NAME.$TIMETABLE_COLUMN_ID), " +
                    "$TIMETABLE_COLUMN_MINUTE = (SELECT $tmpTableName.$TIMETABLE_COLUMN_MINUTE FROM $tmpTableName WHERE $tmpTableName.$TIMETABLE_COLUMN_ID = $TIMETABLE_TABLE_NAME.$TIMETABLE_COLUMN_ID), " +
                    "$TIMETABLE_COLUMN_DAYTYPE = (SELECT $tmpTableName.$TIMETABLE_COLUMN_DAYTYPE FROM $tmpTableName WHERE $tmpTableName.$TIMETABLE_COLUMN_ID = $TIMETABLE_TABLE_NAME.$TIMETABLE_COLUMN_ID) " +
                    "WHERE $TIMETABLE_COLUMN_ID IN (SELECT $tmpTableName.$TIMETABLE_COLUMN_ID FROM $tmpTableName)"
        )


        // Add new data
        db.execSQL(
            "INSERT INTO $TIMETABLE_TABLE_NAME ($TIMETABLE_COLUMN_ID, $TIMETABLE_COLUMN_STOP, $TIMETABLE_COLUMN_HOUR, $TIMETABLE_COLUMN_MINUTE, $TIMETABLE_COLUMN_DAYTYPE) " +
                    "SELECT $tmpTableName.$TIMETABLE_COLUMN_ID, $tmpTableName.$TIMETABLE_COLUMN_STOP, $tmpTableName.$TIMETABLE_COLUMN_HOUR, $tmpTableName.$TIMETABLE_COLUMN_MINUTE, $tmpTableName.$TIMETABLE_COLUMN_DAYTYPE " +
                    "FROM $tmpTableName " +
                    "LEFT JOIN $TIMETABLE_TABLE_NAME ON $tmpTableName.$TIMETABLE_COLUMN_ID = $TIMETABLE_TABLE_NAME.$TIMETABLE_COLUMN_ID " +
                    "WHERE $TIMETABLE_TABLE_NAME.$TIMETABLE_COLUMN_ID IS NULL"
        )


        // Remove not more existing stops
        db.execSQL(
            "DELETE FROM $TIMETABLE_TABLE_NAME " +
                    "WHERE $TIMETABLE_COLUMN_ID IN (" +
                    "SELECT $TIMETABLE_TABLE_NAME.$TIMETABLE_COLUMN_ID " +
                    "FROM $TIMETABLE_TABLE_NAME " +
                    "LEFT JOIN $tmpTableName ON $TIMETABLE_TABLE_NAME.$TIMETABLE_COLUMN_ID = $tmpTableName.$TIMETABLE_COLUMN_ID " +
                    "WHERE $tmpTableName.$TIMETABLE_COLUMN_ID IS NULL)"
        )*/
    }
}