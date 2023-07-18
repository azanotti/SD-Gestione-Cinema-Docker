package it.unimib.finalproject.server;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.*;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.io.*;
import java.net.*;

@Path("prenotazione")
public class PrenotazioneResources {

	final ObjectMapper objectMapper = new ObjectMapper();
	final static ObjectMapper staticObjectMapper = new ObjectMapper();
	
	private static String sendMessageToDB(String message) {
	    try {
	        Socket socket = DBConnectionPool.getConnection();
	        String response = "";
			try {
				response = Connettore.sendMessage(socket, message);
			} catch (IOException e) {
				Logger.getLogger(PrenotazioneResources.class.getName()).severe("Errore durante l'invio messaggio al DB: " + e.getMessage());
			}
	        DBConnectionPool.releaseConnection(socket);
	        return response;
	    } catch (InterruptedException e) {
	    	Logger.getLogger(PrenotazioneResources.class.getName()).severe("Errore durante la connessione al DB: " + e.getMessage());
	        return "NULL";
	    }
	}
	
	private String generateRandomString() {
		String uuid = UUID.randomUUID().toString();
		uuid = uuid.replace("-", "");
        return uuid;
	}
	
	@POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setProiezione(String body) {
    	//Definisco una stringa vuota per inserire l'ID
    	String proiezione = "";
    	//Faccio il mapping della stringa JSON con un nodo JSON
    	try {
			JsonNode jsonNode = objectMapper.readValue(body, JsonNode.class);
			//Estraggo l'id dal nodo
			proiezione = jsonNode.get("proiezione").asText();
			//Estraggo il body in una stringa (senza pretty writer)
			body = jsonNode.toString();
		} catch (JsonProcessingException e1) {
			Logger.getLogger(PrenotazioneResources.class.getName()).severe("Errore durante la processazione del JSON: " + e1.getMessage());
		}
    	
    	//Controllo se la proiezione esiste
    	if(proiezione.indexOf("proiezione") == 0) {
    		String checkExistingFilm = sendMessageToDB("GET " + proiezione + "\n");
        	if(checkExistingFilm.contains("ERROR_REQUESTED_KEY_IS_NOT_PRESENT")) {
        		return Response.status(Response.Status.BAD_REQUEST).build();
        	}
    	} else {
    		return Response.status(Response.Status.BAD_REQUEST).build();
    	}
    	
    	
    	//Genero un id casuale
    	String id = generateRandomString();
    	id = id.substring(0,5);
    	id = "prenotazione-" + id;
    	
    	//Aggiorno i posti disponibili nella proiezione
    	Prenotazione prenotazioneObj = null;
    	try {
            prenotazioneObj = objectMapper.readValue(body, Prenotazione.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    	
    	ArrayList<Posto> postiPrenotati = prenotazioneObj.getPosti();
    	Proiezione proiezioneRichiesta = ProiezioneResources.exportProiezione(proiezione);
    	
    	
    	//Controllo se la prenotazione e' effettuabile
    	boolean inserimentoValido = true;
    	
    	//Itero sui posti e se anche uno e' gia' prenotato mi fermo
    	//successivamente se false ritorna errore altrimenti imposta
    	//il codice sui posti selezionati
    	for(int i = 0; i < postiPrenotati.size(); i++) {
    		Posto[][] postiGiaPrenotati = proiezioneRichiesta.getPrenotazioniSala();
    		
    		Posto posto = postiPrenotati.get(i);
    		int fila = posto.getFila();
    		int numero = posto.getNumero();
    		posto.setCodice(id);
    		
    		if(!(postiGiaPrenotati[fila][numero].getCodice().equals("") || postiGiaPrenotati[fila][numero].getCodice().equals(id))) {
    			inserimentoValido = false;
    		}	
    	}
    	
    	//Effettuo la prenotazione
    	if(inserimentoValido) {
    		for(int i = 0; i < postiPrenotati.size(); i++) {
        		Posto[][] postiGiaPrenotati = proiezioneRichiesta.getPrenotazioniSala();
        		
        		Posto posto = postiPrenotati.get(i);
        		int fila = posto.getFila();
        		int numero = posto.getNumero();
        		posto.setCodice(id);
        		        		
        		proiezioneRichiesta.setCodicePosto(fila, numero, id);		
        	}
    	} else {
    		return Response.status(Response.Status.BAD_REQUEST).build();
    	}
    	
    	String proiezioneAggiornata = null;
    	try {
    		proiezioneAggiornata = objectMapper.writeValueAsString(proiezioneRichiesta);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	String updateCommand = "UPDATEKV " + proiezione + " " + proiezioneAggiornata + "\n";
    	sendMessageToDB(updateCommand);
    	
    	//Inserisco il codice nella prenotazione
    	prenotazioneObj.setCodice(id);
    	
    	//Converto l'oggetto in JSON
    	String prenotazioneJson = null;
    	try {
			prenotazioneJson = objectMapper.writeValueAsString(prenotazioneObj);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	//Creo l'oggetto nel DB
    	String addCommand = "SET " + id + " " + prenotazioneJson + "\n";
    	sendMessageToDB(addCommand);
    	
    	//Ritorno l'id dell'oggetto creato
		try {
		    var uri = new URI("/prenotazione/" + id);
		    return Response.created(uri).build();
		} catch (URISyntaxException e) {
			Logger.getLogger(PrenotazioneResources.class.getName()).severe("Errore durante la creazione URI di ritorno: " + e.getMessage());
		    return Response.serverError().build();
		}
    }
	
	//Ritorno una prenotazione specifica
    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPrenotazione(@PathParam("id") String id) {
    	if(id.indexOf("prenotazione") == 0) {
    		//Prendo la prenotazione richiesta dal DB
        	String prenotazione = sendMessageToDB("GET " + id + "\n");  
        	//Controllo se il DB ha ritornato 0 entries
        	if(prenotazione.contains("ERROR_REQUESTED_KEY_IS_NOT_PRESENT")) {
        		return Response.status(Response.Status.NOT_FOUND).build();
        	}
        	//Divido la chiave dal corpo
        	String prenotazioneArray[] = prenotazione.split(":", 2);
        	//Rimuovo i doppi apici    	
        	String prenotazioneJson = prenotazioneArray[1].substring(1, prenotazioneArray[1].length()-1);
        	//Converto il risultato nella classe Prenotazione e ritorno il risultato
        	try {
                Prenotazione prenotazioneObj = objectMapper.readValue(prenotazioneJson, Prenotazione.class);
                return Response.ok().entity(prenotazioneObj).build();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return Response.serverError().build();
            }
    	} else {
    		return Response.status(Response.Status.BAD_REQUEST).build();
    	}
    }
    
    @DELETE
    @Path("/{id}")
    public Response deletePrenotazioneById(@PathParam("id") String id) {
    	//Controllo se sto effettivamente eliminando una prenotazione
    	if(id.indexOf("prenotazione") == 0) {
    		//Prendo la prenotazione
    		String prenotazione = sendMessageToDB("GET " + id + "\n");  
        	//Controllo se il DB ha ritornato 0 entries
        	if(prenotazione.contains("ERROR_REQUESTED_KEY_IS_NOT_PRESENT")) {
        		return Response.status(Response.Status.NOT_FOUND).build();
        	}
        	//Divido la chiave dal corpo
        	String prenotazioneArray[] = prenotazione.split(":", 2);
        	//Rimuovo i doppi apici    	
        	String prenotazioneJson = prenotazioneArray[1].substring(1, prenotazioneArray[1].length()-1);
        	//Converto il risultato nella classe Prenotazione e ritorno il risultato
        	Prenotazione prenotazioneObj = null;
        	try {
        		prenotazioneObj = objectMapper.readValue(prenotazioneJson, Prenotazione.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        	
    		//Cancello i posti nella proiezione
    		ArrayList<Posto> postiPrenotati = prenotazioneObj.getPosti();
    		Proiezione proiezioneRichiesta = ProiezioneResources.exportProiezione(prenotazioneObj.getProiezione());
    		
    		for(int i = 0; i < postiPrenotati.size(); i++) {
    			Posto posto = postiPrenotati.get(i);
    			int fila = posto.getFila();
    			int numero = posto.getNumero();
    			
    			proiezioneRichiesta.resetCodicePosto(fila, numero);
    		}
    		
    		String proiezioneAggiornata = null;
        	try {
        		proiezioneAggiornata = objectMapper.writeValueAsString(proiezioneRichiesta);
    		} catch (JsonProcessingException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
        	
        	String updateCommand = "UPDATEKV " + prenotazioneObj.getProiezione() + " " + proiezioneAggiornata + "\n";
        	sendMessageToDB(updateCommand);
    		
    		
    		String response = sendMessageToDB("DELETE " + id + "\n");
    		if(response.contains("OK_ENTRY_DELETED")) {
    			return Response.status(Response.Status.NO_CONTENT).build();
    		} else {
    			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    		}
    	}
    	return Response.status(Response.Status.BAD_REQUEST).build();
    }
    
    @Path("/{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updatePrenotazioneById(@PathParam("id") String id, String body) {
    	String proiezione = null;
    	//Faccio il mapping della stringa JSON con un nodo JSON
    	try {
			JsonNode jsonNode = objectMapper.readValue(body, JsonNode.class);
			//Estraggo il body in una stringa (senza pretty writer)
			body = jsonNode.toString();
			proiezione = jsonNode.get("proiezione").asText();
		} catch (JsonProcessingException e1) {
			Logger.getLogger(PrenotazioneResources.class.getName()).severe("Errore durante la processazione del JSON: " + e1.getMessage());
		}
    	
    	Prenotazione nuovaPrenotazioneObj = null;
    	try {
    		nuovaPrenotazioneObj = objectMapper.readValue(body, Prenotazione.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    	
    	ArrayList<Posto> postiAggiornati = nuovaPrenotazioneObj.getPosti();
    	
    	if(id.indexOf("prenotazione") == 0) {
    		//Prendo la prenotazione
    		String prenotazione = sendMessageToDB("GET " + id + "\n");  
        	//Controllo se il DB ha ritornato 0 entries
        	if(prenotazione.contains("ERROR_REQUESTED_KEY_IS_NOT_PRESENT")) {
        		return Response.status(Response.Status.NOT_FOUND).build();
        	}
        	//Divido la chiave dal corpo
        	String prenotazioneArray[] = prenotazione.split(":", 2);
        	//Rimuovo i doppi apici    	
        	String prenotazioneJson = prenotazioneArray[1].substring(1, prenotazioneArray[1].length()-1);
        	//Converto il risultato nella classe Prenotazione e ritorno il risultato
        	Prenotazione prenotazioneObj = null;
        	try {
        		prenotazioneObj = objectMapper.readValue(prenotazioneJson, Prenotazione.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        	
    		//Cancello i posti nella proiezione
    		ArrayList<Posto> postiPrenotati = prenotazioneObj.getPosti();
    		Proiezione proiezioneRichiesta = ProiezioneResources.exportProiezione(prenotazioneObj.getProiezione());
    		
    		for(int i = 0; i < postiPrenotati.size(); i++) {
    			Posto posto = postiPrenotati.get(i);
    			int fila = posto.getFila();
    			int numero = posto.getNumero();
    			
    			proiezioneRichiesta.resetCodicePosto(fila, numero);
    		}
    		
    		String proiezioneAggiornata = null;
        	try {
        		proiezioneAggiornata = objectMapper.writeValueAsString(proiezioneRichiesta);
    		} catch (JsonProcessingException e1) {
    			// TODO Auto-generated catch block
    			e1.printStackTrace();
    		}
        	
        	Logger.getLogger(PrenotazioneResources.class.getName()).severe("Proiezione aggiornata 1: " + proiezioneAggiornata);
        	
        	String updateCommand = "UPDATEKV " + prenotazioneObj.getProiezione() + " " + proiezioneAggiornata + "\n";
        	sendMessageToDB(updateCommand);
    	}
    	
    	//Logica di prenotazione nuovi posti
    	Proiezione proiezioneRichiesta = ProiezioneResources.exportProiezione(proiezione);
    	Posto[][] postiGiaPrenotati = proiezioneRichiesta.getPrenotazioniSala();
    	//Controllo se la prenotazione e' effettuabile
    	boolean inserimentoValido = true;
    	
    	//Itero sui posti e se anche uno e' gia' prenotato mi fermo
    	//successivamente se false ritorna errore altrimenti imposta
    	//il codice sui posti selezionati
    	for(int i = 0; i < postiPrenotati.size(); i++) {
    		Posto[][] postiGiaPrenotati = proiezioneRichiesta.getPrenotazioniSala();
    		
    		Posto posto = postiPrenotati.get(i);
    		int fila = posto.getFila();
    		int numero = posto.getNumero();
    		posto.setCodice(id);
    		
    		if(!(postiGiaPrenotati[fila][numero].getCodice().equals("") || postiGiaPrenotati[fila][numero].getCodice().equals(id))) {
    			inserimentoValido = false;
    		}	
    	}
    	
    	//Effettuo la prenotazione
    	if(inserimentoValido) {
    		for(int i = 0; i < postiPrenotati.size(); i++) {
        		Posto[][] postiGiaPrenotati = proiezioneRichiesta.getPrenotazioniSala();
        		
        		Posto posto = postiPrenotati.get(i);
        		int fila = posto.getFila();
        		int numero = posto.getNumero();
        		posto.setCodice(id);
        		        		
        		proiezioneRichiesta.setCodicePosto(fila, numero, id);		
        	}
    	} else {
    		return Response.status(Response.Status.BAD_REQUEST).build();
    	}
    	
    	String proiezioneAggiornata = null;
    	try {
    		proiezioneAggiornata = objectMapper.writeValueAsString(proiezioneRichiesta);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	Logger.getLogger(PrenotazioneResources.class.getName()).severe("Proiezione aggiornata 2: " + proiezioneAggiornata);
    	
    	String updateCommand = "UPDATEKV " + proiezione + " " + proiezioneAggiornata + "\n";
    	sendMessageToDB(updateCommand);
		
		//Aggiorna la prenotazione
		updateCommand = "UPDATEKV" + id + " " + body + "\n";
		return Response.status(Response.Status.OK).build();
    }
}
