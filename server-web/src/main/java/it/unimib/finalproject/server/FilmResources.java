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

@Path("film")
public class FilmResources {
	final ObjectMapper objectMapper = new ObjectMapper();
	
	private static String sendMessageToDB(String message) {
	    try {
	        Socket socket = DBConnectionPool.getConnection();
	        String response = "";
			try {
				response = Connettore.sendMessage(socket, message);
			} catch (IOException e) {
				Logger.getLogger(FilmResources.class.getName()).severe("Errore durante l'invio messaggio al DB: " + e.getMessage());
			}
	        DBConnectionPool.releaseConnection(socket);
	        return response;
	    } catch (InterruptedException e) {
	    	Logger.getLogger(FilmResources.class.getName()).severe("Errore durante la connessione al DB: " + e.getMessage());
	        return "NULL";
	    }
	}
	
    /**
     * Implementazione di GET "/film".
     * @throws JsonProcessingException 
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFilms() throws JsonProcessingException { 
    	//Arraylist temporanea per contenere tutti i film
    	ArrayList<Film> filmList = new ArrayList<Film>();
    	//Contatto il DB per estrarre la lista di FILM
    	String films = sendMessageToDB("GET CLASS film\n");
    	//Controllo se il DB ha ritornato 0 entries
    	if(films.contains("ERROR_CLASS_NOT_PRESENT_IN_DB")) {
    		return Response.status(Response.Status.NOT_FOUND).build();
    	}
    	//Divido la stringa ritornata per i ; separatori
    	String filmArray[] = films.split(";");
    	//Itero sull'array di film
    	for(int i = 0; i < filmArray.length; i++) {
    		//Estraggo il valore dalla chiave
    		String filmValue = filmArray[i].split(":", 2)[1];
    		//Rimuovo i "" all inizio e alla fine
    		String filmJson = filmValue.substring(1, filmValue.length()-1);
        	//Converto il risultato nella classe Film e ritorno il risultato
        	try {
        		//Faccio il mapping della stringa JSON con la class Film
                Film filmObj = objectMapper.readValue(filmJson, Film.class);
                //Aggiungo l'oggetto Film all'arraylist di ritorno
                filmList.add(filmObj);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
    	}
    	//Controllo se almeno un film e' stato aggiunto altrimenti mi fermo
    	if(filmList.size() > 0) {
    		return Response.ok(filmList, MediaType.APPLICATION_JSON).build();
    	}else {
    		return Response.status(Response.Status.NOT_FOUND).build();
    	}
		     
    }
    
    //Ritorno un film specifico
    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFilm(@PathParam("id") String id) {
    	if(id.indexOf("film") == 0) {
    		String film;
        	//Prendo il film richiesto dal DB
        	film = sendMessageToDB("GET " + id + "\n");  
        	//Controllo se il DB ha ritornato 0 entries
        	if(film.contains("ERROR_REQUESTED_KEY_IS_NOT_PRESENT")) {
        		return Response.status(Response.Status.NOT_FOUND).build();
        	}
        	//Divido la chiave dal corpo
        	String films[] = film.split(":", 2);
        	//Rimuovo i doppi apici    	
        	String filmJson = films[1].substring(1, films[1].length()-1);
        	//Converto il risultato nella classe Film e ritorno il risultato
        	try {
                Film filmObj = objectMapper.readValue(filmJson, Film.class);
                return Response.ok().entity(filmObj).build();
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
    public Response setFilms(String body) {
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
			Logger.getLogger(FilmResources.class.getName()).severe("Errore durante la processazione del JSON: " + e1.getMessage());
		}
    	
    	if(id.indexOf("film") == 0) {
    		//Controllo se l'oggetto e' gia' presente
        	String existingFilm = sendMessageToDB("GET " + id + "\n");
        	if(!existingFilm.contains("ERROR_REQUESTED_KEY_IS_NOT_PRESENT")) {
        		return Response.status(Response.Status.BAD_REQUEST).build();
        	}
        	
        	//Creo l'oggetto nel DB
        	String addCommand = "SET " + id + " " + body + "\n";
        	sendMessageToDB(addCommand);
        	
        	//Ritorno l'id dell'oggetto creato
    		try {
    		    var uri = new URI("/film/" + id);
    		    return Response.created(uri).build();
    		} catch (URISyntaxException e) {
    			Logger.getLogger(FilmResources.class.getName()).severe("Errore durante la creazione URI di ritorno: " + e.getMessage());
    		    return Response.serverError().build();
    		}
    	} else {
    		return Response.status(Response.Status.BAD_REQUEST).build();
    	}    	
    }
    
    @DELETE
    @Path("/{id}")
    public Response deleteFilmById (@PathParam("id") String id) {
    	
    	//Controllo se sto effettivamente eliminando un film
    	if(id.indexOf("film") == 0) {
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