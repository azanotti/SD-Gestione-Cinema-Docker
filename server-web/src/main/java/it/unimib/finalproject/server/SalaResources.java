package it.unimib.finalproject.server;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.type.*;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.io.*;
import java.net.*;

@Path("sala")
public class SalaResources {
	final ObjectMapper objectMapper = new ObjectMapper();
	final static ObjectMapper staticObjectMapper = new ObjectMapper();
	
	private static String sendMessageToDB(String message) {
	    try {
	        Socket socket = DBConnectionPool.getConnection();
	        String response = "";
			try {
				response = Connettore.sendMessage(socket, message);
			} catch (IOException e) {
				Logger.getLogger(SalaResources.class.getName()).severe("Errore durante l'invio messaggio al DB: " + e.getMessage());
			}
	        DBConnectionPool.releaseConnection(socket);
	        return response;
	    } catch (InterruptedException e) {
	    	Logger.getLogger(SalaResources.class.getName()).severe("Errore durante la connessione al DB: " + e.getMessage());
	        return "NULL";
	    }
	}
	
	public static Sala exportSala(String id) {
		if(id.indexOf("sala") == 0) {
			//Prendo la sala richiesto dal DB
	    	String sala = sendMessageToDB("GET " + id + "\n");
	    	
	    	//Definisco un oggetto Sala vuoto
	    	Sala salaReturn = null;
	    	
	    	//Controllo se il DB ha ritornato 0 entries
	    	if(sala.contains("ERROR_REQUESTED_KEY_IS_NOT_PRESENT")) {
	    		return salaReturn;
	    	}
	    	
	    	//Divido la chiave dal corpo
	    	String sale[] = sala.split(":", 2);
	    	
	    	//Rimuovo i doppi apici    	
	    	String salaJson = sale[1].substring(1, sale[1].length()-1);
	    	
	    	//Converto il risultato nella classe Sala e ritorno il risultato
	    	try {
	    		salaReturn = staticObjectMapper.readValue(salaJson, Sala.class);
	        } catch (JsonProcessingException e) {
	            e.printStackTrace();
	        }
	    	return salaReturn;
		} else {
			return null;
		}
	}
	
    /**
     * Implementazione di GET "/sala".
     * @throws JsonProcessingException 
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSale() throws JsonProcessingException { 
    	//Arraylist temporanea per contenere tutti le sale
    	ArrayList<Sala> salaList = new ArrayList<Sala>();
    	//Contatto il DB per estrarre la lista di SALE
    	String sale = sendMessageToDB("GET CLASS sala\n");
    	//Controllo se il DB ha ritornato 0 entries
    	if(sale.contains("ERROR_CLASS_NOT_PRESENT_IN_DB")) {
    		return Response.status(Response.Status.NOT_FOUND).build();
    	}
    	//Divido la stringa ritornata per i ; separatori
    	String salaArray[] = sale.split(";");
    	//Itero sull'array di sale
    	for(int i = 0; i < salaArray.length; i++) {
    		//Estraggo il valore dalla chiave
    		String salaValue = salaArray[i].split(":", 2)[1];
    		//Rimuovo i "" all inizio e alla fine
    		String salaJson = salaValue.substring(1, salaValue.length()-1);
        	//Converto il risultato nella classe Sala e ritorno il risultato
        	try {
        		//Faccio il mapping della stringa JSON con la class Sala
                Sala salaObj = objectMapper.readValue(salaJson, Sala.class);
                //Aggiungo l'oggetto Sala all'arraylist di ritorno
                salaList.add(salaObj);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
    	}
    	//Controllo se almeno una sala e' stata aggiunta altrimenti mi fermo
    	if(salaList.size() > 0) {
    		return Response.ok(salaList, MediaType.APPLICATION_JSON).build();
    	}else {
    		return Response.status(Response.Status.NOT_FOUND).build();
    	}
		     
    }
    
    //Ritorno una sala specifica
    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSala(@PathParam("id") String id) {
    	if(id.indexOf("sala") == 0) {
    		//Prendo la sala richiesto dal DB
        	String sala = sendMessageToDB("GET " + id + "\n");  
        	//Controllo se il DB ha ritornato 0 entries
        	if(sala.contains("ERROR_REQUESTED_KEY_IS_NOT_PRESENT")) {
        		return Response.status(Response.Status.NOT_FOUND).build();
        	}
        	//Divido la chiave dal corpo
        	String sale[] = sala.split(":", 2);
        	//Rimuovo i doppi apici    	
        	String salaJson = sale[1].substring(1, sale[1].length()-1);
        	//Converto il risultato nella classe Sala e ritorno il risultato
        	try {
                Sala salaObj = objectMapper.readValue(salaJson, Sala.class);
                return Response.ok().entity(salaObj).build();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return Response.serverError().build();
            }
    	} else {
    		return Response.status(Response.Status.BAD_REQUEST).build();
    	}
    	
    	
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setSala(String body) {
    	//Definisco una stringa vuota per inserire l'ID
    	String id = "";
    	//Faccio il mapping della stringa JSON con un nodo JSON
    	try {
			JsonNode jsonNode = objectMapper.readValue(body, JsonNode.class);
			//Estraggo l'id dal nodo
			id = jsonNode.get("id").asText();
			//Estraggo il body in una stringa (senza pretty writer)
			body = jsonNode.toString();
		} catch (JsonProcessingException e1) {
			Logger.getLogger(SalaResources.class.getName()).severe("Errore durante la processazione del JSON: " + e1.getMessage());
		}
    	
    	//Controllo se l'oggetto e' gia' presente
    	String existingSala = sendMessageToDB("GET " + id + "\n");
    	if(!existingSala.contains("ERROR_REQUESTED_KEY_IS_NOT_PRESENT")) {
    		return Response.status(Response.Status.BAD_REQUEST).build();
    	}
    	
    	if(id.indexOf("sala") == 0) {
    		//Creo l'oggetto nel DB
        	String addCommand = "SET " + id + " " + body + "\n";
        	sendMessageToDB(addCommand);
        	
        	//Ritorno l'id dell'oggetto creato
    		try {
    		    var uri = new URI("/sala/" + id);
    		    return Response.created(uri).build();
    		} catch (URISyntaxException e) {
    			Logger.getLogger(SalaResources.class.getName()).severe("Errore durante la creazione URI di ritorno: " + e.getMessage());
    		    return Response.serverError().build();
    		}
    	} else {
    		return Response.status(Response.Status.BAD_REQUEST).build();
    	}
    }
    
    @DELETE
    @Path("/{id}")
    public Response deleteSalaById(@PathParam("id") String id) {
    	
    	//Controllo se sto effettivamente eliminando una sala
    	if(id.indexOf("sala") == 0) {
    		String response = sendMessageToDB("DELETE " + id + "\n");
    		if(response.contains("OK_ENTRY_DELETED")) {
    			return Response.status(Response.Status.NO_CONTENT).build();
    		} else {
    			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    		}
    	}
    	return Response.status(Response.Status.BAD_REQUEST).build();
    }
}