package com.example.tramtracker.utilities

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.util.Log
import com.example.tramtracker.database.DatabaseHelper
import com.example.tramtracker.database.DayType
import com.example.tramtracker.database.Directions
import com.example.tramtracker.database.Stop
import com.example.tramtracker.database.Timetable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.abs

/*Classe supplementare che serve per popolare il database alla prima esecuzione, questa classe verrebbe eventualmente sostituita dallo
* scaricamento dei dati da un database remoto in caso della sua esistenza*/
class DatabasePopulator {

    companion object{
        //Funzione per aggiungere tutte le fermate con i relativi dati nel database
        fun addStops(db : DatabaseHelper){
            //ANDATA
            db.insertStop(Stop(0, "Capolinea Guizza", Directions.ANDATA, 45.36803995202993, 11.874034727513244, "Parcheggio Capolinea Sud (265 posti), ingresso tangenziale (Corso Primo Maggio)", ""))
            db.insertStop(Stop(0, "Guizza", Directions.ANDATA, 45.3744239674946, 11.870668172568253, "Centro sportivo Rugby Petrarca, Motoi Sushi, Chiesa dei Santi Angeli Custodi", ""))
            db.insertStop(Stop(0, "Cuoco", Directions.ANDATA, 45.38006757331116, 11.870012869297906, "Piazzale Vincenzo Cuoco (mercato), Pasticceria San Marco", ""))
            db.insertStop(Stop(0, "Sacchetti", Directions.ANDATA, 45.383932064743284, 11.868626784293841, "Cinema Porto Astra", ""))
            db.insertStop(Stop(0, "Bassanello", Directions.ANDATA, 45.387044836130535, 11.867579553520777, "Lungargine Bassanello", ""))
            db.insertStop(Stop(0, "Cavallotti", Directions.ANDATA, 45.389934209405084, 11.870226868417456, "IIS Giovanni Valle, Radici Gourmet", ""))
            db.insertStop(Stop(0, "Santa Croce", Directions.ANDATA, 45.39205244986349, 11.870902664674, "Piazzale Santa Croce", ""))
            db.insertStop(Stop(0, "Diaz", Directions.ANDATA, 45.39506973984705, 11.872717449692509, "Campo Sportivo Comunale Silvio Appiani, WebFit Padova", ""))
            db.insertStop(Stop(0, "Cavalletto", Directions.ANDATA, 45.39766696238378, 11.874361848426634, "Liceo Scientifico Statale Enrico Fermi, Prato della Valle, Abbazia Santa Giustina", ""))
            db.insertStop(Stop(0, "Prato della Valle", Directions.ANDATA, 45.39976784615484, 11.87698690991903, "Prato della Valle, Via Roma", ""))
            db.insertStop(Stop(0, "Santo", Directions.ANDATA, 45.400816824967194, 11.87751690002691, "Basilica di Sant'Antonio di Padova, Monumento al Gattamelata", ""))
            db.insertStop(Stop(0, "Tito Livio", Directions.ANDATA, 45.40502918429312, 11.87712056919537, "Questura di Padova, Istituto Dante Alighieri, Liceo Ginnasio Tito Livio Padova", ""))
            db.insertStop(Stop(0, "Ponti Romani", Directions.ANDATA, 45.407633544422275, 11.878549271420773, "Ristorante da Pino, Caffè Pedrocchi, Piazza Cavour", ""))
            db.insertStop(Stop(0, "Eremitani", Directions.ANDATA, 45.41209954816401, 11.878637433549414, "Giardini dell'Arena, Cappella degli Scrovegni, Piazza Eremitani, Palazzo Zuckermann", ""))
            db.insertStop(Stop(0, "Trieste", Directions.ANDATA, 45.41407078147005, 11.879003149553892, "Interparking Italia Srl - Park Padova Stazione", ""))
            db.insertStop(Stop(0, "Stazione", Directions.ANDATA, 45.417528663748556, 11.878977084101392, "Stazione treni e bus (urbani ed extra-urbani)", ""))
            db.insertStop(Stop(0, "Borgomagno", Directions.ANDATA, 45.42021854935921, 11.879499930382238, "Cinema Multiastra", ""))
            db.insertStop(Stop(0, "Arcella", Directions.ANDATA, 45.422360355288035, 11.883297622975107, "IIS Giovanni Valle, Santuario di Sant'Antonino all'Arcella", ""))
            db.insertStop(Stop(0, "Dazio", Directions.ANDATA, 45.424150281815166, 11.884700469601224, "IIS Leonardo da Vinci (succursale)", ""))
            db.insertStop(Stop(0, "Palasport", Directions.ANDATA, 45.427821997922905, 11.887386435271528, "Palantenore - Palasport Arcella", ""))
            db.insertStop(Stop(0, "San Carlo", Directions.ANDATA, 45.43018853293722, 11.888774731550981, "Liceo Scientifico Eugenio Curiel (succursale), Parrocchia di San Carlo Borromeo in Padova", ""))
            db.insertStop(Stop(0, "San Gregorio", Directions.ANDATA, 45.43413529959932, 11.89078594282655, "Pasticceria Le Sablon, iSCAMPA - Escape Room Padova", ""))
            db.insertStop(Stop(0, "Saimp", Directions.ANDATA, 45.43696668380661, 11.891280482713809, "Gasoline Padova, ingresso Tangenziale Nord (Corso Tedici Giugno)", ""))
            db.insertStop(Stop(0, "Fornace Morandi", Directions.ANDATA, 45.43881829903532, 11.888963899050783, "Spiller - Padova, Parco Fornace Morandi", ""))
            db.insertStop(Stop(0, "Capolinea Pontevigodarzere", Directions.ANDATA, 45.44215303503694, 11.890221165690349, "Parcheggio Pontevigodarzere, La Mafaldina Pizzeria Pontevigodarzere, FitActive Padova", ""))

            //RITORNO
            db.insertStop(Stop(0, "Capolinea Guizza", Directions.RITORNO, 45.36799619624428, 11.873900934637005, "Parcheggio Capolinea Sud (265 posti), ingresso tangenziale (Corso Primo Maggio)", ""))
            db.insertStop(Stop(0, "Guizza", Directions.RITORNO, 45.37430170415007, 11.870569697648959, "Centro sportivo Rugby Petrarca, Motoi Sushi, Chiesa dei Santi Angeli Custodi", ""))
            db.insertStop(Stop(0, "Cuoco", Directions.RITORNO, 45.380010557507624, 11.869799191479828, "Piazzale Vincenzo Cuoco (mercato), Pasticceria San Marco", ""))
            db.insertStop(Stop(0, "Assunta", Directions.RITORNO, 45.38324769095392, 11.866068524190336, "Cinema Porto Astra", ""))
            db.insertStop(Stop(0, "Bassanello", Directions.RITORNO, 45.38718135610509, 11.867519743983792, "Lungargine Bassanello", ""))
            db.insertStop(Stop(0, "Cavallotti", Directions.RITORNO, 45.38997555720941, 11.870160395133956, "IIS Giovanni Valle, Radici Gourmet", ""))
            db.insertStop(Stop(0, "Santa Croce", Directions.RITORNO, 45.3922142601579, 11.870930204965411, "Piazzale Santa Croce", ""))
            db.insertStop(Stop(0, "Diaz", Directions.RITORNO, 45.39521827084803, 11.872748705492654, "Campo Sportivo Comunale Silvio Appiani, WebFit Padova", ""))
            db.insertStop(Stop(0, "Cavalletto", Directions.RITORNO, 45.39769050487771, 11.874341731906979, "Liceo Scientifico Statale Enrico Fermi, Prato della Valle, Abbazia Santa Giustina", ""))
            db.insertStop(Stop(0, "Prato della Valle", Directions.RITORNO, 45.39981210440599, 11.87695271179841, "Prato della Valle, Via Roma", ""))
            db.insertStop(Stop(0, "Santo", Directions.RITORNO, 45.40084177858516, 11.877441127634334, "Basilica di Sant'Antonio di Padova, Monumento al Gattamelata", ""))
            db.insertStop(Stop(0, "Tito Livio", Directions.RITORNO, 45.405018001047246, 11.877079342474524, "Questura di Padova, Istituto Dante Alighieri, Liceo Ginnasio Tito Livio Padova", ""))
            db.insertStop(Stop(0, "Ponti Romani", Directions.RITORNO, 45.40763354465395, 11.87849892965223, "Ristorante da Pino, Caffè Pedrocchi, Piazza Cavour", ""))
            db.insertStop(Stop(0, "Eremitani", Directions.RITORNO, 45.41211132844427, 11.87858541355169, "Giardini dell'Arena, Cappella degli Scrovegni, Piazza Eremitani, Palazzo Zuckermann", ""))
            db.insertStop(Stop(0, "Trieste", Directions.RITORNO, 45.414129198564446, 11.879057719205461, "Interparking Italia Srl - Park Padova Stazione", ""))
            db.insertStop(Stop(0, "Stazione", Directions.RITORNO, 45.41759393249844, 11.878497482075392, "Stazione treni e bus (urbani ed extra-urbani)", ""))
            db.insertStop(Stop(0, "Borgomagno", Directions.RITORNO, 45.420274368600275, 11.87949014275046, "Cinema Multiastra", ""))
            db.insertStop(Stop(0, "Arcella", Directions.RITORNO, 45.42237838938372, 11.8830822910153, "IIS Giovanni Valle, Santuario di Sant'Antonino all'Arcella", ""))
            db.insertStop(Stop(0, "Dazio", Directions.RITORNO, 45.4241776713371, 11.884614987962639, "IIS Leonardo da Vinci (succursale)", ""))
            db.insertStop(Stop(0, "Palasport", Directions.RITORNO, 45.427846777339425, 11.88728980343076, "Palantenore - Palasport Arcella", ""))
            db.insertStop(Stop(0, "San Carlo", Directions.RITORNO, 45.430206790703366, 11.888722699145573, "Liceo Scientifico Eugenio Curiel (succursale), Parrocchia di San Carlo Borromeo in Padova", ""))
            db.insertStop(Stop(0, "San Gregorio", Directions.RITORNO, 45.43417311665521, 11.890627986964335, "Pasticceria Le Sablon, iSCAMPA - Escape Room Padova", ""))
            db.insertStop(Stop(0, "Saimp", Directions.RITORNO, 45.43692325654795, 11.89122223381011, "Gasoline Padova, ingresso Tangenziale Nord (Corso Tedici Giugno)", ""))
            db.insertStop(Stop(0, "Fornace Morandi", Directions.RITORNO, 45.43881044969726, 11.888919152403902, "Spiller - Padova, Parco Fornace Morandi", ""))
            db.insertStop(Stop(0, "Capolinea Pontevigodarzere", Directions.RITORNO, 45.442145601050754, 11.890401282744332, "Parcheggio Pontevigodarzere, La Mafaldina Pizzeria Pontevigodarzere, FitActive Padova", ""))
        }

        //Funzione per aggiungere tutti gli orari di partenza da ogni singola fermata
        /*NOTA: Dati gli orari di partenza dai capolinea (dati pubblici), si è ricavata un vettore che conteggia i minuti impiegati per raggiungere
        * la fermata successiva per poi ricavare i vari conteggi temporali*/
        fun addTimeTable(db : DatabaseHelper){
            val outboundDelays = intArrayOf(0, 2, 2, 1, 2, 1, 1, 1, 1, 3, 2, 3, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 2)
            val returnDelays = intArrayOf(0, 2, 2, 1, 2, 1, 2, 1, 2, 1, 2, 1, 2, 2, 3, 2, 3, 1, 1, 1, 1, 2, 1, 2, 1)

            val pontevigodarzereWeekdayStarts = arrayOf("06:25", "06:37", "06:49", "07:01", "07:07", "07:13", "07:19", "07:25", "07:31", "07:37", "07:43", "07:49", "07:55", "08:01", "08:07", "08:13", "08:19", "08:25", "08:31", "08:37", "08:43", "08:50", "08:56", "09:02", "09:08", "09:15", "09:21", "09:27", "09:34", "09:41", "09:48", "09:54", "10:01", "10:07", "10:14", "10:20", "10:27", "10:33", "10:40", "10:46", "10:53", "10:59", "11:06", "11:12", "11:19", "11:25", "11:32", "11:39", "11:45", "11:52", "11:58", "12:04", "12:10", "12:16", "12:23", "12:29", "12:35", "12:41", "12:47", "12:53", "12:59", "13:05", "13:11", "13:17", "13:23", "13:29", "13:35", "13:41", "13:47", "13:53", "13:59", "14:05", "14:11", "14:18", "14:24", "14:31", "14:37", "14:44", "14:50", "14:57", "15:03", "15:10", "15:16", "15:23", "15:29", "15:36", "15:42", "15:49", "15:55", "16:02", "16:08", "16:15", "16:21", "16:28", "16:34", "16:41", "16:47", "16:54", "17:00", "17:07", "17:13", "17:20", "17:26", "17:32", "17:38", "17:44", "17:50", "17:56", "18:02", "18:08", "18:14", "18:20", "18:26", "18:32", "18:38", "18:44", "18:50", "18:56", "19:02", "19:08", "19:14", "19:20", "19:26", "19:32", "19:38", "19:44", "19:50", "19:56", "20:02", "20:08", "20:14", "20:20", "20:26", "20:32", "20:38", "20:44", "20:50", "20:56", "21:02", "21:15", "21:30", "21:45", "22:00", "22:15", "22:30", "22:45", "23:00", "23:15", "23:30", "23:45", "24:00", "24:15")
            val pontevigodarzereFestiveStarts = arrayOf("06:35", "06:50", "07:05", "07:19", "07:32", "07:46", "07:59", "08:13", "08:26", "08:40", "08:54", "09:07", "09:21", "09:34", "09:48", "10:01", "10:15", "10:29", "10:42", "10:56", "11:09", "11:23", "11:36", "11:50", "12:04", "12:17", "12:31", "12:44", "12:58", "13:11", "13:25", "13:38", "13:51", "14:04", "14:17", "14:28", "14:41", "14:53", "15:04", "15:17", "15:28", "15:41", "15:53", "16:04", "16:17", "16:28", "16:41", "16:53", "17:04", "17:17", "17:28", "17:41", "17:53", "18:04", "18:17", "18:28", "18:41", "18:53", "19:05", "19:17", "19:29", "19:45", "20:00", "20:15", "20:30", "20:45", "21:15", "21:45", "22:15", "22:45", "23:15")
            val guizzaWeekdayStarts = arrayOf("05:40", "05:52", "06:04", "06:16", "06:22", "06:28", "06:34", "06:40", "06:46", "06:52", "06:58", "07:04", "07:10", "07:16", "07:22", "07:28", "07:34", "07:40", "07:46", "07:52", "07:58", "08:04", "08:10", "08:16", "08:22", "08:28", "08:34", "08:40", "08:47", "08:53", "09:00", "09:06", "09:13", "09:19", "09:26", "09:32", "09:39", "09:45", "09:52", "09:58", "10:05", "10:11", "10:18", "10:24", "10:31", "10:38", "10:44", "10:51", "10:57", "11:04", "11:10", "11:17", "11:23", "11:30", "11:37", "11:43", "11:50", "11:56", "12:02", "12:08", "12:14", "12:20", "12:26", "12:32", "12:38", "12:44", "12:50", "12:56", "13:02", "13:08", "13:14", "13:20", "13:26", "13:33", "13:39", "13:46", "13:52", "13:59", "14:05", "14:12", "14:18", "14:25", "14:31", "14:38", "14:44", "14:51", "14:57", "15:04", "15:10", "15:17", "15:23", "15:30", "15:36", "15:43", "15:49", "15:56", "16:02", "16:09", "16:15", "16:22", "16:28", "16:35", "16:41", "16:47", "16:53", "16:59", "17:05", "17:11", "17:17", "17:23", "17:29", "17:35", "17:41", "17:47", "17:53", "17:59", "18:05", "18:11", "18:17", "18:23", "18:29", "18:35", "18:41", "18:47", "18:53", "18:59", "19:05", "19:11", "19:17", "19:23", "19:29", "19:35", "19:41", "19:47", "19:53", "19:59", "20:05", "20:11", "20:17", "20:30", "20:45", "21:00", "21:15", "21:30", "21:45", "22:00", "22:15", "22:30", "22:45", "23:00", "23:15", "23:30")
            val guizzaFestiveStarts = arrayOf("06:50", "07:05", "07:20", "07:34", "07:47", "08:01", "08:14", "08:28", "08:41", "08:55", "09:09", "09:22", "09:36", "09:49", "10:03", "10:16", "10:30", "10:44", "10:57", "11:11", "11:24", "11:38", "11:51", "12:05", "12:19", "12:32", "12:46", "12:59", "13:13", "13:26", "13:40", "13:53", "14:06", "14:19", "14:32", "14:45", "14:56", "15:06", "15:19", "15:32", "15:44", "15:56", "16:06", "16:19", "16:32", "16:44", "16:56", "17:06", "17:19", "17:32", "17:44", "17:56", "18:06", "18:19", "18:32", "18:44", "18:56", "19:08", "19:20", "19:32", "19:44", "20:00", "20:15", "20:30", "20:45", "21:00", "21:15", "21:30", "21:45", "22:00", "22:15", "22:30", "23:00", "23:30")

            for (time in pontevigodarzereWeekdayStarts) {
                var hour: Int = time.split(":")[0].toInt()
                var minute: Int = time.split(":")[1].toInt()

                //In base alle fermate presenti nel database con questi indici si va dal capolinea pontevigodarzere a capolinea guizza
                for (index in 50 downTo 26) {
                    minute += returnDelays[abs(index - 50)]

                    if (minute >= 60) {
                        hour++
                        minute -= 60
                    }

                    db.insertTimetable(
                        Timetable(index, hour.toString()
                        .padStart(2, '0'), minute.toString().padStart(2, '0'), DayType.FERIALE)
                    )
                }
            }

            for (time in pontevigodarzereFestiveStarts) {
                var hour: Int = time.split(":")[0].toInt()
                var minute: Int = time.split(":")[1].toInt()

                //In base alle fermate presenti nel database con questi indici si va dal capolinea pontevigodarzere a capolinea guizza
                for (index in 50 downTo 26) {
                    minute += returnDelays[abs(index - 50)]

                    if (minute >= 60) {
                        hour++
                        minute -= 60
                    }

                    db.insertTimetable(
                        Timetable(index, hour.toString()
                        .padStart(2, '0'), minute.toString().padStart(2, '0'), DayType.FESTIVO)
                    )
                }
            }

            for (time in guizzaWeekdayStarts) {
                var hour: Int = time.split(":")[0].toInt()
                var minute: Int = time.split(":")[1].toInt()

                //In base alle fermate presenti nel database con questi indici si va dal capolinea guizza a capolinea pontevigodarzere
                for (index in 1..25) {
                    minute += outboundDelays[index - 1]

                    if (minute >= 60) {
                        hour++
                        minute -= 60
                    }

                    db.insertTimetable(
                        Timetable(index, hour.toString()
                        .padStart(2, '0'), minute.toString().padStart(2, '0'), DayType.FERIALE)
                    )
                }
            }

            for (time in guizzaFestiveStarts) {
                var hour: Int = time.split(":")[0].toInt()
                var minute: Int = time.split(":")[1].toInt()

                //In base alle fermate presenti nel database con questi indici si va dal capolinea guizza a capolinea pontevigodarzere
                for (index in 1..25) {
                    minute += outboundDelays[index - 1]

                    if (minute >= 60) {
                        hour++
                        minute -= 60
                    }

                    db.insertTimetable(
                        Timetable(index, hour.toString()
                        .padStart(2, '0'), minute.toString().padStart(2, '0'), DayType.FESTIVO)
                    )
                }
            }
        }

        //Funzione per spostare le immagini dalla cartella raw all'internal storage (simulazione dello scaricamento dei dati da un database remoto)
        fun addImages(context : Context, db : DatabaseHelper) {
            val imagesDir = File(context.filesDir, "images")
            val images = imagesDir.listFiles()?.toList()
            val stops = db.getStopsList()

            val filePaths = mutableListOf<String>()

            if (images != null) {
                for (image in images)
                    filePaths.add(image.absolutePath)
            }

            for (stop in stops) {
                val fileName = filePaths.find {
                    it.contains(stop.name.lowercase().replace(" ", "_"))
                }

                if (fileName != null) {
                    stop.imagePath = fileName
                    db.updateStop(stop.id, stop)
                }
            }
        }

        //Funzione per verificare se la cartella nello storage interno predisposta per contenere le immagini è vuota o meno
        fun isInternalStorageEmpty(context: Context) : Boolean{
            val imagesDir = File(context.filesDir, "images")
            return imagesDir.listFiles()?.isEmpty() == null
        }

        //Funzione per copiare le immagini da una cartella raw allo storage interno
        fun copyImagesToInternalStorage(context: Context, db : DatabaseHelper){
            val stops = db.getStopsList()

            for(stop in stops){
                copyImageToInternalStorage(context, stop.name.lowercase().replace(" ","_"), stop.name.lowercase().replace(" ","_"))
            }
        }

        @SuppressLint("DiscouragedApi")
        private fun copyImageToInternalStorage(context: Context, resourceName: String, outputFileName: String) {
            val resources: Resources = context.resources
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                // Ottieni l'ID della risorsa in modo dinamico
                val resourceId = resources.getIdentifier(resourceName, "raw", context.packageName)
                if (resourceId == 0)
                    Log.e("IMAGE_TO_INTERNAL","Resource not found")

                // Apri l'immagine dalla cartella res/raw
                inputStream = resources.openRawResource(resourceId)

                // Ottieni la directory interna dell'applicazione
                context.dataDir
                val outputDir = File(context.filesDir, "images")
                if (!outputDir.exists())
                    outputDir.mkdir()

                // Crea il file di destinazione
                val outputFile = File(outputDir, "$outputFileName.jpg")
                outputStream = FileOutputStream(outputFile)

                // Copia il contenuto dell'immagine
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1)
                    outputStream.write(buffer, 0, bytesRead)

            } catch (e: IOException) {
                Log.e("IMAGE_TO_INTERNAL", e.toString())
            } finally {
                try {
                    inputStream?.close()
                    outputStream?.close()
                } catch (e: IOException) {
                    Log.e("IMAGE_TO_INTERNAL", e.toString())
                }
            }
        }
    }
}