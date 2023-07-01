package it.unimib.finalproject.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class serverthread implements Runnable {

    private Socket clientSocket;
    private controller ctr;
    private PrintWriter out;
    private BufferedReader in;

    public serverthread(Socket clientSocket, controller ctr) {
        this.clientSocket = clientSocket;
        this.ctr = ctr;
    }

    @Override
    // metodo implementato da Runnable che fa partire il thread
    public void run() {
        // Do client process
        try {
        	out= new PrintWriter(clientSocket.getOutputStream(), true);
			inFromClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
        closeConnection();
    }

    // lettura da Client della richiesta che ci ha mandato
    // il thread poi agesce di conseguenza leggendo il comando
    private String inFromClient() throws Exception {

        String messageFromClient = "";

        /*
         *  Do not use try with resources because once -
         *  - it exits the block it will close your client socket too.
         */

        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String line = "";
            while ((line = in.readLine()) != null) {
                if(line.equals("EXIT")){
                    break;
                }
                
                // Controllo se si tratta di un operazione di aggiunta
                if(line.contains("SET")) {
                	String[] splitLine = line.split(" ", 3);
                	outToClient(ctr.aggiunta(splitLine[1], splitLine[2]));
                }
                
                // Aggiorna value in base alla chiave
                if(line.contains("UPDATEKV")) {
                	String[] splitLine = line.split(" ", 3);
                	outToClient(ctr.modificaPerChiave(splitLine[1], splitLine[2]));
                }
                // Aggiorna value in base alla value precedente
                if(line.contains("UPDATEVV")) {
                	String[] splitLine = line.split(" ", 3);
                	outToClient(ctr.modificaPerJSON(splitLine[1], splitLine[2]));
                }
                // Aggiorna key in base alla key precedente
                if(line.contains("UPDATEKK")) {
                	String[] splitLine = line.split(" ", 3);
                	outToClient(ctr.modificaChiavePerChiave(splitLine[1], splitLine[2]));
                }
                // Aggiorna key in base alla value
                if(line.contains("UPDATEVK")) {
                	String[] splitLine = line.split(" ", 3);
                	outToClient(ctr.modificaChiavePerJSON(splitLine[2], splitLine[1]));
                }
                // Ricerca per chiave
                if(line.contains("GET")) {
                	if(line.contains("CLASS")) {
                		String[] splitLine = line.split(" ", 3);
                		outToClient(ctr.showPezzo(splitLine[2]));
                	} else {
                		String[] splitLine = line.split(" ", 2);
                    	if(splitLine[1].equals("HELP")) {
                    		outToClient(ctr.help());
                    	} else if (splitLine[1].equals("ALL")) {
                    		outToClient(ctr.showDB());
                    	} else if(splitLine[1].contains("{")) {
                    		outToClient(ctr.ricercaPerJSON(splitLine[1]));
                    	} else {
                    		outToClient(ctr.ricercaPerChiave(splitLine[1]));
                    	}
                	}
                }
                // Operazione di delete
                if(line.contains("DELETE")) {
                	String[] splitLine = line.split(" ", 2);
                	if(splitLine[1].contains("{")) {
                		outToClient(ctr.eliminaPerJSON(splitLine[1]));
                	} else {
                		outToClient(ctr.eliminaPerChiave(splitLine[1]));
                	}
                }
                
                // Controllo operazione di salvataggio DB
                if(line.equals("SAVE")){
                	ctr.save();
                    outToClient("OK_DATABASE_SAVED");
                }
                // Controllo uscita con salvataggio
                if(line.equals("QUIT")) {
                	ctr.exit();
                }
            }
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).severe("InFromClientErr - " + e.getMessage());
        }
        return messageFromClient.trim().equals("") ? "ERROR_INPUT_NOT_GIVEN" : messageFromClient;
    }

    // metodo che manda al Clieant la stringa di risposta alla richiesta
    private void outToClient(String message) {
        message = message + "\nEND\n"; // Append termination signal
        out.write(message);
        out.flush();
    }

    // metodo che chiude Socket, PrintWriter, BufferedReader
    private void closeConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (NullPointerException | IOException e) {
            Logger.getLogger(getClass().getName()).severe(e.getMessage());
        }
    }
}