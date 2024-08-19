## Comandi per la Profilazione
### Resettare log Batteria
Comando da eseguire prima di registrare nuovi dati
 ```bash
adb shell dumpsys batterystats --reset
```
### Comando per Creare il Bugreport
``` bash
adb bugreport [path/]bugreport.zip
```
 
## Assunzioni e Annotazioni per il progetto
### Applicazione
* È importante non chiudere l'applicazione quando viene visualizzata la schermata di caricamento del database in quanto comprometterebbe il corretto funzionamento della stessa
* Potrebbe verificarsi che Android dica che l'applicazione non risponda al primo avvio, ciò è normale in quanto viene inizializzato il database nel thread principale
Questo comportamento è stato pensato perchè utilizzare il database e quindi richiamarne le sue funzioni prima che venga inizializzato al 100% potrebbe comprometterne la completa integrità
in quanto si fermerebbe il processo di popolamento dello stesso
  * Se si mettesse il database in un thread (come abbiamo tentato), comunque bisognerebbe bloccare il resto di inizializzazione dell'applicazione arrivando al medesimo risultato
* In seguito alla prima attivazione della modalità di risparmio energetico tramite lo switch in LevelsFragment, potrebbe non essere segnato il livello energetico eseguito a causa del
breve intervallo di tempo che intercorre tra la registrazione del receiver e il salvataggio del corrispondente valore sulla seekbar.
Si può risolvere disattivando e riattivando nuovamente la modalità di risparmio
* Il valore corrispondente al livello eseguito sulla seekbar non viene riportato in tempo reale in quanto manca un boradcast receiver atto ad identificare questi cambiamenti. 
In un primo momento questo broadcast receiver è stato implementato ma causava malfunzionamenti nei servizi difficili da debuggare a causa della complessità della gestione dell'interazione dei componenti 
* 
### GPS e Location Provider
* Il GPS, sopratutto nei luoghi chiusi ci può mettere fino a 3 minuti per riuscire ad ottenere la posizione. Per cercare di ridurre questa
latenza è stato introdotto un sistema "ibrido" che attiva e disattiva un listener per ottenere la posizione attraverso internet (wi-fi + Rete cellulare)
fin tanto che non risulta almeno un satellite agganciato al GPS del dispositivo. 
È inoltre implementato un sistema per ottenere l'ultima posizione conosciuta in modo da avviare l'applicazione con dei dati (anche se approssimativi)

NOTA: Il network provider fornisce la posizione senza l'utilizzo del GPS anche se risulta, dopo un po' di test, che ritorna la posizione con una frequenza di aggiornamento
non corrispondente a quella indicata (fornisce due/tre aggiornamenti consecutivi per poi non fornirne più per quasi un minuto)
* Non abbiamo usato la libreria Fused location in quanto è una libreria che offre la posizione dell'utente in modo ottimizzato
mentre per l'obbiettivo della nostra applicazione è proprio partire da uno stato iniziale di forte consumo energetico per poterlo poi ridurre mediante tecniche ad hoc
* È stato introdotto un servizio di monitoraggio del segnale GPS che, attraverso apposito listener, riesce a rilevare il numero di satelliti a cui il dispositivo è agganciato in un specifico istante.
Siccome questa informazione è utile sia agli sviluppatori che all'utente per capire la potenza del segnale o eventuali funzionamenti anomali dell'applicazione è stato introdotto nelle notifiche
di questo servizio un aggiornamento di stato che risponde alla seguente scaletta (in base al numero di satelliti collegati)
    * 0 -> Segnale assente 
    * 1-3 -> Segnale molto scarso 
    * 4-7 -> Segnale scarso 
    * 8-12 -> Segnale buono
    * \>13 -> Segnale ottimo

* Come riportato nella [documentazione ufficiale Android](https://developer.android.com/about/versions/oreo/background-location-limits), il servizio GPS è molto limitato in background.
  Esso infatti, appena l'applicazione perde lo stato di foreground, mantiene la sua esecuzione, e quindi l'aggiornamento della posizione per circa 60 secondi, prima di essere terminato dal sistema
  fin tanto che l'applicazione non ritorna in stato foreground.
  A causa di questa limitazione il servizio di Geofence da noi implementato non sempre potrebbe funzionare in background. Abbiamo però osservato che il GPS non viene terminato qualora qualsiasi altra applicazione
  o servizio intergrato nel telefono la stia usando, fornendo quindi aggiornamenti anche alla nostra applicazione permettendone il corretto funzionamento.
  Affermato ciò, è stata creata un'app ad hoc per attivare e disattivare lo schermo del telefono per simulare un utilizzo normale del device al fine di estrapolare dati di utilizzo della batteria e risparmio energetico con il GPS funzionante in background anche se parzialmente
  Si auspica che, in condizioni normali, il device non avrà questa come unica applicazione installata e che altre applicazioni sfruttino il GPS mentre la nostra è in background garantendone il corretto funzionamento

### Mappa 
* L'omino giallo raffigurato nella mappa come indicatore della posizione attuale è quello prestabilito da OpenStreetMap. Questa icona, quando l'utente si sposta, si trasforma in una
freccia bianca che indica la direzione verso la quale si sta spostando l'utente. Questo comportamento non è modificabile o disattivabile attraverso alcuna API della mappa; l'unica azione 
consentita è quella di usare delle icone custom sia per l'omino "statico" che per la freccia in movimento. La limitazione è che essendo una semplice operazione che ridisegna le icone, esse vengono posizionata
con meno precisione rispetto a quelle originale (a causa delle dimensioni delle immagine custom e dello "spazio vuoto" tra la cornice dell'immagine e l'immagine stessa).
Inoltre sovrascrivendo l'immagine della freccia si ottiene che essa, indicando la direzione, continua a ruotare facendo quindi ruotare l'immagine ridisegnata
* Per visualizzare correttamente la mappa bisogna necessariamente essere online
* Per la schermata di DetailActivity viene catturata una immagine dalla mappa, siccome soprattutto ai primi avvii la mappa non è tutta scaricata in cache, quindi si potrebbe vedere un'immagine sgranata.
La cosa migliora però più esecuzioni si fanno e più si esplora e naviga nella mappa, magari caricando e zoommando in determinate zone
* Per risolvere questo problema si è pensato di scaricare la mappa prima di poterla usare, così che le immagini non siano sgranate e l'applicazione funzioni offline; si sono riscontrate però delle problematiche
  * Non è possibile visualizzare la mappa finchè non si è tutta scaricata, ed essendo la mappa dettagliata di Padova molto grande richiedeva una quantità enorme di tempo
  * Lo spazio occupato era troppo alto
  * Esistono diversi provider per scaricare la mappa, alcuni troppo lenti nel download o che bloccavano le richieste dopo un determinato numero di accessi o tile scaricate

### Database
* Per la creazione del database sono presenti delle funzioni nella classe DatabasePopulator che simulano la creazione del database da un database remoto contenuto in qualche server centrale,
La scelta di popolare in locale il database e non da remoto è stata dettata dalla semplificazioni di questa parte di applicazione non essendo il core del progetto stesso. Analogamente è stato
fatto con inserimenti di immagini nella cartella delle risorse per poi copiarle nello storage interno (immaginando che le immagini della cartella risorse in realtà sono contenute in un server remoto)
* Non essendoci un database remoto a cui appoggiarsi è di vitale importanza per tutto il funzionamento dell'applicazione di non chiuderla mentre il database viene inizializzato la prima volta; se ciò accadesse si potrebbe 
  avere un database popolato solo in parte che non verrà più completato durante le successive esecuzione (è predisposta la parte di completamento del database nella funzione update ma, con sqlite3, si riscontrano problemi di
duppicazione dei dati)

## SuppressLint
### Classe DatabasePopulator
* @SuppressLint("DiscouragedApi") -> Utilizzato per rimuovere il warning relativo all'utilizzo della funzione ``resources.getIdentifier`` ricavando le risorse per nome e non per identificativo
  Il warning si riferiva nello specifico all'utilizzo dell'id e non del nome per poter sfruttare ottimizzazioni a tempo di compilazione; cosa non praticabile in quanto i nomi delle risorse sono contenuti 
  in una variabile ricavata da una query al database, rendendo quindi l'ID non noto a priori
### Classe MainActivity
* @file:Suppress("DEPRECATION") -> Utilizzato per rimuovere i warning sull'utilizzo di funzioni deprecate per quanto riguarda la gestione della mappa. Siccome la mappa open street map contiene poca documentazione online
e per lo più è sviluppata in java, alcune funzioni sono state deprecate in java e non si riesce a trovare un valido sostituto non deprecato

### Classe BatteryBroadcastReceiver
* @Suppress("DEPRECATION") -> Nota documentazione della funzione getRunningServices: "As of Build.VERSION_CODES.O, this method is no longer available to third party applications.
For backwards compatibility, it will still return the caller's own"

### Classe LevelsFragment
* @Suppress("DEPRECATION") -> Nota documentazione della funzione getRunningServices: "As of Build.VERSION_CODES.O, this method is no longer available to third party applications.
  For backwards compatibility, it will still return the caller's own"