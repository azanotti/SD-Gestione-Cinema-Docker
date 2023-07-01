package it.unimib.finalproject.server;

public class Posto {
	private int numero;
	private int fila;
	private String codice;
	
	//Metodi getter
	public int getNumero() {
		return this.numero;
	}
	public int getFila() {
		return this.fila;
	}
	public String getCodice() {
		return this.codice;
	}
	
	//Metodi setter
	public void setNumero(int numero) {
		this.numero = numero;
	}
	public void setFila(int fila) {
		this.fila = fila;
	}
	public void setCodice(String codice) {
		this.codice = codice;
	}
}
