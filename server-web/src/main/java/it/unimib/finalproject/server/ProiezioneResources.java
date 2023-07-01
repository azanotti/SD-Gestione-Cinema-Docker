package it.unimib.finalproject.server;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("proiezione")
public class ProiezioneResources {
	final ObjectMapper objectMapper = new ObjectMapper();
	final static ObjectMapper staticObjectMapper = new ObjectMapper();
	
	private static String sendMessageToDB(String message) {
	    try {
	        Socket socket = DBConnectionPool.getConnection();
	        String response = "";
			try {
				response = Connettore.sendMessage(socket, message);
			} catch (IOException e) {
				Logger.getLogger(ProiezioneResources.class.getName()).severe("Errore durante l'invio messaggio al DB: " + e.getMessage());
			}
	        DBConnectionPool.releaseConnection(socket);
	        return response;
	    } catch (InterruptedException e) {
	    	Logger.getLogger(ProiezioneResources.class.getName()).severe("Errore durante la connessione al DB: " + e.getMessage());
	        return "NULL";
	    }
	}
	
	public static Proiezione exportProiezione(String id) {
		if(id.indexOf("proiezione") == 0) {
			//Prendo la proiezione richiesta dal DB
	    	String proiezione = sendMessageToDB("GET " + id + "\n");
	    	
	    	//Definisco un oggetto Proiezione vuoto
	    	Proiezione proiezioneReturn = null;
	    	
	    	//Controllo se il DB ha ritornato 0 entries
	    	if(proiezione.contains("ERROR_REQUESTED_KEY_IS_NOT_PRESENT")) {
	    		return proiezioneReturn;
	    	}
	    	
	    	//Divido la chiave dal corpo
	    	String proiezioneArray[] = proiezione.split(":", 2);
	    	
	    	//Rimuovo i doppi apici    	
	    	String proiezioneJson = proiezioneArray[1].substring(1, proiezioneArray[1].length()-1);
	    	
	    	//Converto il risultato nella classe Proiezione e ritorno il risultato
	    	try {
	    		proiezioneReturn = staticObjectMapper.readValue(proiezioneJson, Proiezione.class);
	        } catch (JsonProcessingException e) {
	            e.printStackTrace();
	        }
	    	return proiezioneReturn;
		} else {
			return null;
		}
	}
	
    /**
     * Implementazione di GET "/proiezione".
     * @throws JsonProcessingException 
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSale() throws JsonProcessingException { 
    	//Arraylist temporanea per contenere tutti le proiezioni
    	ArrayList<Proiezione> proiezioneList = new ArrayList<Proiezione>();
    	//Contatto il DB per estrarre la lista di PROIEZIONE
    	String proiezioni = sendMessageToDB("GET CLASS proiezione\n");
    	//Controllo se il DB ha ritornato 0 entries
    	if(proiezioni.contains("ERROR_CLASS_NOT_PRESENT_IN_DB")) {
    		return Response.status(Response.Status.NOT_FOUND).build();
    	}
    	//Divido la stringa ritornata per i ; separatori
    	String proiezioneArray[] = proiezioni.split(";");
    	//Itero sull'array di proiezioni
    	for(int i = 0; i < proiezioneArray.length; i++) {
    		//Estraggo il valore dalla chiave
    		String proiezioneValue = proiezioneArray[i].split(":", 2)[1];
    		//Rimuovo i "" all inizio e alla fine
    		String proiezioneJson = proiezioneValue.substring(1, proiezioneValue.length()-1);
        	//Converto il risultato nella classe Proiezione e ritorno il risultato
        	try {
        		//Faccio il mapping della stringa JSON con la class Proiezione
                Proiezione proiezioneObj = objectMapper.readValue(proiezioneJson, Proiezione.class);
                //Aggiungo l'oggetto Proiezione all'arraylist di ritorno
                proiezioneList.add(proiezioneObj);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
    	}
    	//Controllo se almeno una proiezione e' stata aggiunta altrimenti mi fermo
    	if(proiezioneList.size() > 0) {
    		return Response.ok(proiezioneList, MediaType.APPLICATION_JSON).build();
    	}else {
    		return Response.status(Response.Status.NOT_FOUND).build();
    	}
		     
    }
    
    //Ritorno una proiezione specifica
    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProiezione(@PathParam("id") String id) {
    	if(id.indexOf("proiezione") == 0) {
    		//Prendo la proiezione richiesta dal DB
        	String proiezione = sendMessageToDB("GET " + id + "\n");  
        	//Controllo se il DB ha ritornato 0 entries
        	if(proiezione.contains("ERROR_REQUESTED_KEY_IS_NOT_PRESENT")) {
        		return Response.status(Response.Status.NOT_FOUND).build();
        	}
        	//Divido la chiave dal corpo
        	String proiezioneArray[] = proiezione.split(":", 2);
        	//Rimuovo i doppi apici    	
        	String proiezioneJson = proiezioneArray[1].substring(1, proiezioneArray[1].length()-1);
        	//Converto il risultato nella classe Proiezione e ritorno il risultato
        	try {
                Proiezione proiezioneObj = objectMapper.readValue(proiezioneJson, Proiezione.class);
                return Response.ok().entity(proiezioneObj).build();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                return Response.serverError().build();
            }
    	} else if(id.indexOf("film") == 0) {
    		//Arraylist temporanea per contenere tutti le proiezioni
        	ArrayList<Proiezione> proiezioneList = new ArrayList<Proiezione>();
        	//Contatto il DB per estrarre la lista di PROIEZIONE
        	String proiezioni = sendMessageToDB("GET CLASS proiezione\n");
        	//Controllo se il DB ha ritornato 0 entries
        	if(proiezioni.contains("ERROR_CLASS_NOT_PRESENT_IN_DB")) {
        		return Response.status(Response.Status.NOT_FOUND).build();
        	}

			
        	//Divido la stringa ritornata per i ; separatori
        	String proiezioneArray[] = proiezioni.split(";");
        	//Itero sull'array di proiezioni
        	for(int i = 0; i < proiezioneArray.length; i++) {
        		//Estraggo il valore dalla chiave
        		String proiezioneValue = proiezioneArray[i].split(":", 2)[1];
        		//Rimuovo i "" all inizio e alla fine
        		String proiezioneJson = proiezioneValue.substring(1, proiezioneValue.length()-1);
            	//Converto il risultato nella classe Proiezione e ritorno il risultato
            	try {
            		//Faccio il mapping della stringa JSON con la class Proiezione
                    Proiezione proiezioneObj = objectMapper.readValue(proiezioneJson, Proiezione.class);
					
                    if(proiezioneObj.getFilmId().equals(id)) {
                    	//Aggiungo l'oggetto Proiezione all'arraylist di ritorno
                        proiezioneList.add(proiezioneObj);
                    }
                    
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
        	}
			//Controllo se almeno una proiezione e' stata aggiunta altrimenti mi fermo
            if(proiezioneList.size() > 0) {
            	return Response.ok(proiezioneList, MediaType.APPLICATION_JSON).build();
            }else {
            	return Response.status(Response.Status.NOT_FOUND).build();
            }
    	}
    	return Response.status(Response.Status.BAD_REQUEST).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setProiezione(String body) {
    	//Definisco una stringa vuota per inserire l'ID
    	String id = "";
    	String filmId = "";
    	String salaId = "";
    	//Faccio il mapping della stringa JSON con un nodo JSON
    	try {
			JsonNode jsonNode = objectMapper.readValue(body, JsonNode.class);
			//Estraggo l'id dal nodo
			id = jsonNode.get("id").asText();
			filmId = jsonNode.get("filmId").asText();
			salaId = jsonNode.get("salaId").asText();
			//Estraggo il body in una stringa (senza pretty writer)
			body = jsonNode.toString();
		} catch (JsonProcessingException e1) {
			Logger.getLogger(ProiezioneResources.class.getName()).severe("Errore durante la processazione del JSON: " + e1.getMessage());
		}
    	
    	//Controllo se l'oggetto e' gia' presente
    	String existingProiezione = sendMessageToDB("GET " + id + "\n");
    	if(!existingProiezione.contains("ERROR_REQUESTED_KEY_IS_NOT_PRESENT")) {
    		return Response.status(Response.Status.BAD_REQUEST).build();
    	}
    	
    	//Controllo se il film esiste
    	if(filmId.indexOf("film") == 0) {
    		String checkExistingFilm = sendMessageToDB("GET " + filmId + "\n");
        	if(checkExistingFilm.contains("ERROR_REQUESTED_KEY_IS_NOT_PRESENT")) {
        		return Response.status(Response.Status.BAD_REQUEST).build();
        	}
    	} else {
    		return Response.status(Response.Status.BAD_REQUEST).build();
    	}
    	
    	//Controllo se il sala esiste
    	if(salaId.indexOf("sala") == 0) {
    		String checkExistingSala = sendMessageToDB("GET " + salaId + "\n");
        	if(checkExistingSala.contains("ERROR_REQUESTED_KEY_IS_NOT_PRESENT")) {
        		return Response.status(Response.Status.BAD_REQUEST).build();
        	}
    	} else {
    		return Response.status(Response.Status.BAD_REQUEST).build();
    	}
    	
    	//Istanzio bene la classe
    	Proiezione proiezioneObj = null;
    	try {
            proiezioneObj = objectMapper.readValue(body, Proiezione.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    	
    	if(proiezioneObj != null) {
    		proiezioneObj.initializePosti();
    	}
    	
    	//Riconverto in JSON
    	try {
			body = objectMapper.writeValueAsString(proiezioneObj);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	//Creo l'oggetto nel DB
    	String addCommand = "SET " + id + " " + body + "\n";
    	sendMessageToDB(addCommand);
    	
    	//Ritorno l'id dell'oggetto creato
		try {
		    var uri = new URI("/proiezione/" + id);
		    return Response.created(uri).build();
		} catch (URISyntaxException e) {
			Logger.getLogger(ProiezioneResources.class.getName()).severe("Errore durante la creazione URI di ritorno: " + e.getMessage());
		    return Response.serverError().build();
		}
    }
    
    @DELETE
    @Path("/{id}")
    public Response deleteProiezioneById(@PathParam("id") String id) {
    	
    	//Controllo se sto effettivamente eliminando una proiezione
    	if(id.indexOf("proiezione") == 0) {
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