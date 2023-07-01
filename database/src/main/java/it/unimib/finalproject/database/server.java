package it.unimib.finalproject.database;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

public class server {

    private ServerSocket sSocket;
    private boolean run;
    private int port;
    private controller ctr;

    public server(int port) throws IOException {
    	try {
			ctr = controller.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
        this.port = port;
        this.sSocket = new ServerSocket(this.port);
    }

    // metodo che fa partire il server
    // ascolta sulla ServerSocket creata dal costruttore possibili messaggi al server
    // nel caso client ha richiesto connessione crea una socket che verra' usata dal Client per comunicare
    // crea un thread che andra' a lavorare sulla Socket appena creata e l'istanza di Controller
    public void start() {
        this.run = true;
        Logger.getLogger(getClass().getName()).info("Server is listening on port: " + port);
        try {
            while (run) {
                Socket cs = sSocket.accept();
                Logger.getLogger(getClass().getName())
                        .info("New Client Connected on port " + cs.getPort());
                new Thread(new serverthread(cs, ctr)).start(); // Put to a new thread.
            }
        } catch (IOException e) {
            Logger.getLogger(getClass().getName()).severe(e.getMessage());
        }
    }

    // metodo che fa terminare il server
    public void stop() {
        this.run = false;
    }
}