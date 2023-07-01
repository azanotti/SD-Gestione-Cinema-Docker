package it.unimib.finalproject.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class controller {
    
    private database db;
    private static controller _instance;

    private controller() {
        this.db = database.getInstance();
        selezionaDatabase();
    }

    public static synchronized controller getInstance() {
        if(_instance == null){
            _instance = new controller();
        }
        return _instance;
    }

    // cerca nella directory se esiste database.json
    // se trovato estrapoliamo dati dal file e li inseriamo nel database 
    // se non trovato creaimo il file json e lo inizializziamo vuoto
    public void selezionaDatabase(){
        try{
            File f = new File("./database.json");
            Logger.getLogger(getClass().getName()).info("Carico DB esistente");
            BufferedReader reader = new BufferedReader(new FileReader(f));
            String lin = "";
            int cont = 0;
            while((lin = reader.readLine()) != null) {
                if(!(lin.equals("{ ") || lin.equals("}"))){
                    cont++;
                    if(lin.charAt(lin.length()-1) == ',') lin = lin.substring(0, lin.length()-1);
                    String[] coppia = lin.split(":",2);
                    coppia[0] = coppia[0].substring(1, coppia[0].length()-1);
                    coppia[1] = coppia[1].substring(1, coppia[1].length()-1);
                    aggiunta(coppia[0],coppia[1]);
                }
            }
            Logger.getLogger(getClass().getName()).info("Entrate caricate: " + cont);
            reader.close();
        }
        catch(Exception e){
            File f = new File("./database.json");
            Logger.getLogger(getClass().getName()).warning("DB non trovato, creo file database.json nuovo");
            try {
				f.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}     
        }
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // modifica json data chiave
    public String modificaPerChiave(String a, String b) {
        String schermo = db.modificaPerChiave(a,b);
        Logger.getLogger(getClass().getName()).info(schermo);
        return schermo;
    }

    // modifica json dato json
    public String modificaPerJSON(String a, String b) {
        String schermo = db.modificaPerJSON(a,b);
        Logger.getLogger(getClass().getName()).info(schermo);
        return schermo;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // modifica chiave dato json
    public String modificaChiavePerJSON(String  a, String b) {
        String schermo = db.modificaChiavePerJSON(a,b);
        Logger.getLogger(getClass().getName()).info(schermo);
        return schermo;
    }

    // modifica chiave data chiave
    public String modificaChiavePerChiave(String a, String b) {
        String schermo = db.modificaChiavePerChiave(a,b);
        Logger.getLogger(getClass().getName()).info(schermo);
        return schermo;
    }
        
    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    
    // ricerca data chiave
    public String ricercaPerChiave(String a) {
        return db.ricercaPerChiave(a);
    }
    
    // ricerca dato json
    public String ricercaPerJSON(String json) {
        return db.ricercaPerJSON(json);
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // eliminazione data chiave
    public String eliminaPerChiave(String a) {
        String schermo = db.eliminaPerChiave(a);
        Logger.getLogger(getClass().getName()).info(schermo);
        return schermo;
    }

    // eliminazione dato json
    public String eliminaPerJSON(String a) {
        String schermo = db.eliminaPerJSON(a);
        Logger.getLogger(getClass().getName()).info(schermo);
        return schermo;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // aggiunta (chiave, valore), se chiave già esistente nel database errore
    public String aggiunta(String a, String b) {
        String schermo = db.aggiunta(a,b);
        Logger.getLogger(getClass().getName()).info(schermo);
        return schermo;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // save contenuto database nel file .json
    public String save() {
        File f = new File("./database.json");
        FileWriter writer;
		try {
			writer = new FileWriter(f);
			writer.write(db.toString());
	        writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        String schermo = "Eseguo il salvataggio DB";
        Logger.getLogger(getClass().getName()).info(schermo);
        return schermo;
    }

    // exit, salvataggio del database sul file .json per poi chiudere il programma
    public void exit() {
        save();
        System.exit(0);
    }

    // chiude il programma senza salvare il database sul file .json
    public void quit() {
        System.exit(0);
    }

    // show totale del contenuto del database
    public String showDB(){
        String ris = db.toString();
        Logger.getLogger(getClass().getName()).info(ris);
        return ris;
    }

    // mostra porzione del contenuto del database
    // il filtro e' una Classe (la prima parte della chiave)
    public String showPezzo(String a) {
        String ris = "";
		ris = db.showPezzo(a);
        Logger.getLogger(getClass().getName()).info(ris);
        return ris;
    }

    // help, mostra tutti i comandi possibili del database
    public String help(){
        String ris = "COMANDI POSSIBILI \n --- \n "
        + "UPDATEKV key newValue: modifica dati associati ad una chiave \nUPDATEVV valure newValure: modifica dati associati ad un value \n \n"
        + "UPDATEKK key newKey: modifica chiave in chiave \nUPDATEVK value newKey: modifica chiave in chiave \n \n"
        + "GET key : ottieni dati associati ad una chiave \nGET value: ottieni dati associati ad un value \n \n" 
        + "DELETE key : elimina dato data chiave \nDELETE value : elimina dato dato value \n \n" 
        + "SET key value : aggiunge una riga nella tabella \n \n" 
        + "GET ALL : mostra tutto il contenuto del database \n \n"
        + "GET CLASS key : mostra tutte le classi key \n \n"
        + "SAVE : salva il database in un file \n \n"
        + "GET HELP: mostra tutti comandi possibili \n"
        + "EXIT : salva ed esci da questo inferno \nQUIT : esci da questo inferno \n"
        + "---";
        Logger.getLogger(getClass().getName()).info(ris);
        return ris;
    }
}