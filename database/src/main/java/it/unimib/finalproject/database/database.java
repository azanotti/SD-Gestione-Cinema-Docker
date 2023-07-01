package it.unimib.finalproject.database;

import java.util.HashMap;
import java.util.Map;

public class database {

    private HashMap<String,String> dati;

    private static database _instance;

    private database(){
        dati = new HashMap<String,String>();
    }

    public static synchronized database getInstance(){
        if(_instance == null){
            _instance = new database();
        }
        return _instance;
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // metodo per la modifica del VALUE associato ad una KEY
    public synchronized String modificaPerChiave(String key, String jsonMOD) {
        String ris;
        ris = ricercaPerChiave(key);
        if(ris.contains("ERROR")) return ris;
        ris = eliminaPerChiave(key);
        if(ris.contains("ERROR")) return ris;
        ris = aggiunta(key, jsonMOD);
        if(ris.contains("ERROR")) return ris;
        return "OK_VALUE_UPDATED";

    }

    // metodo per la trasformazione di un VALUE(ORG) in un VALUE(MOD) [funziona visto che json e' univoco nel database]
    public synchronized String modificaPerJSON(String jsonORG, String jsonMOD) {
        String ris;
        ris = ricercaPerJSON(jsonORG);
        if(ris.contains("ERROR")) return ris;
        String key = getKeyFromValue(jsonORG);
        return modificaPerChiave(key, jsonMOD);
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // metodo per la modifica di una KEY associata ad un VALUE [funziona visto che json e' univoco nel database]
    public synchronized String modificaChiavePerJSON(String keyMOD, String json){
        String ris;
        ris = ricercaPerJSON(json);
        if(ris.contains("ERROR")) return ris;
        String key = getKeyFromValue(json);
        return modificaChiavePerChiave(key, keyMOD);
    }

    // metodo per la trasformazione di una KEY(ORG) in un altra KEY(MOD)
    public synchronized String modificaChiavePerChiave(String keyORG, String keyMOD){
        if(!dati.containsKey(keyORG)) return "ERROR_REQUESTED_KEY_IS_NOT_PRESENT";
        if(dati.containsKey(keyMOD)) return "ERROR_NEW_KEY_IS_ALREADY_PRESENT_IN_DB";
        String json = dati.get(keyORG);
        String ris;
        ris = eliminaPerChiave(keyORG);
        if(ris.contains("ERROR")) return ris;
        ris = aggiunta(keyMOD, json);
        if(ris.contains("ERROR")) return ris;
        return "OK_KEY_UPDATED";
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // ricerca data la KEY come filtro di ricerca, restituisce coppia KEY VALUE
    public String ricercaPerChiave(String key){
        if(!dati.containsKey(key)) return "ERROR_REQUESTED_KEY_IS_NOT_PRESENT";
        return "\""+key+"\":\""+dati.get(key)+"\"";
    }

    // ricerca dato il VALUE come filtro di ricerca [funziona visto che json e' univoco nel database], restituisce coppia KEY VALUE
    public String ricercaPerJSON(String json) {
        if(!dati.containsValue(json)) return "ERROR_REQUESTED_JSON_IS_NOT_PRESENT";
        String key = getKeyFromValue(json);
        return ricercaPerChiave(key);
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // eliminazione coppia KEY VALUE dato come filtro la KEY
    public synchronized String eliminaPerChiave(String key) {
        String ris;
        ris = ricercaPerChiave(key);
        if(ris.contains("ERROR")) return ris;
        dati.remove(key);
        return "OK_ENTRY_DELETED";
    }

    // eliminazione coppia KEY VALUE dato come filtro il VALUE [funziona visto che json e' univoco nel database]
    public synchronized String eliminaPerJSON(String json){
        String ris;
        ris = ricercaPerJSON(json);
        if(ris.contains("ERROR")) return ris;
        String key = getKeyFromValue(json);
        return eliminaPerChiave(key);
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // aggiunge nel database la coppia KEY VALUE
    public synchronized String aggiunta(String key, String json) {
        if(dati.containsKey(key)) return "ERROR_NEW_KEY_IS_ALREADY_PRESENT_IN_DB";
        dati.put(key, json);
        return "OK_PAIR_ADDED";
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    // metodo di supporto che viene utilizzato da i metodi precedenti
    // permette di ricavare la KEY dato un VALUE
    private String getKeyFromValue(String value){
        for (Map.Entry<String,String> entry : dati.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    // contenuto del intero database in una stringa formattata
    // formattazione:
    /*
    * dati è il valore e non intestazione così come il json
    * {
    * "key1":"value1",
    * "key2":"value2",
    * "key3":"value3"
    * }
    */
    public String toString(){
        String ris = "{ ";
        for (int i = 0; i < dati.size(); i++) {
            String key = (String)(dati.keySet().toArray()[i]); //dati
            String json = dati.get(key);
            ris = ris +"\n\""+key+"\":\""+json+"\",";
        }
        ris = ris.substring(0, ris.length()-1);
        ris = ris + "\n}";
        return ris;
    }

    // contenuto del database filtrato per Classe
    // formattazione stringa:
    /*
     * {
    * "key1":"value1",
    * "key2":"value2",
    * "key3":"value3"
    * }
     */
    public String showPezzo(String filtro) {
        String ris = "  ";
        for (int i = 0; i < dati.size(); i++) {
            String key = (String)(dati.keySet().toArray()[i]); //dati
            if(key.startsWith(filtro)){
                String json = dati.get(key);
                ris = ris +"\""+key+"\":\""+json+"\";";
            }
        }
        ris = ris.substring(0, ris.length()-1);
        if(ris.length()==1) return "ERROR_CLASS_NOT_PRESENT_IN_DB";
        return ris;
    }
}