package it.unimib.finalproject.database;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
    	try {
            new server(3030).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

