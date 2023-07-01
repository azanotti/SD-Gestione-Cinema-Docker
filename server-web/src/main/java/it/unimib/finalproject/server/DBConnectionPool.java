package it.unimib.finalproject.server;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

public class DBConnectionPool {
    private static final int MAX_POOL_SIZE = 10;
    private static final BlockingQueue<Socket> availableConnections = new ArrayBlockingQueue<>(MAX_POOL_SIZE);
    
    static {
    	Logger.getLogger(DBConnectionPool.class.getName()).info("Creo " + MAX_POOL_SIZE + " socket per la connessione al DB");
        // Initialize the connection pool with socket instances
        for (int i = 0; i < MAX_POOL_SIZE; i++) {
            try {
                Socket socket = new Socket("localhost", 3030);
                availableConnections.add(socket);
            } catch (IOException e) {
            	Logger.getLogger(DBConnectionPool.class.getName()).severe("Errore durante la creazione della socket: " + e.getMessage());
            }
        }
    }
    
    public static Socket getConnection() throws InterruptedException {
        // Retrieve a connection from the pool
        return availableConnections.take();
    }
    
    public static void releaseConnection(Socket socket) {
        // Release the connection back to the pool
        availableConnections.offer(socket);
    }
}
