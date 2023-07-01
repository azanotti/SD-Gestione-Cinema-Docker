package it.unimib.finalproject.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Logger;

public class Connettore {
	private static String readFromServer(BufferedReader br) throws IOException {
		StringBuilder responseBuilder = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null && !line.equals("END")) {
            responseBuilder.append(line);
        }
        String response = responseBuilder.toString();
        return response;
	}
	
	public static String sendMessage(Socket socket, String message) throws IOException {	
		DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        String response = "";
        try {
        	outToServer.writeBytes(message);
        	response = readFromServer(bufferedReader);
        } catch (Exception e) {
        	Logger.getLogger(Connettore.class.getName()).severe("There was an error accessing the DB: " + e.getMessage());
        }
        return response;
               
	}
}
