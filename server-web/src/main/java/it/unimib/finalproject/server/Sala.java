package it.unimib.finalproject.server;

public class Sala {
	private String id;
	private String nome;
	private int posti;
	private int numeroFile;
	private int numeroPostiPerFila;
	
	//Metodi getter
	public String getId() {
		return this.id;
	}
	public String getNome() {
		return this.nome;
	}
	public int getPosti() {
		return this.posti;
	}
	public int getNumeroFile() {
		return this.numeroFile;
	}
	public int getNumeroPostiPerFila() {
		return this.numeroPostiPerFila;
	}
	
	//Metodi setter
	public void setId(String id) {
		this.id = id;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public void setPosti(int posti) {
		this.posti = posti;
	}
	public void setNumeroFile(int numeroFile) {
		this.numeroFile = numeroFile;
	}
	public void setNumeroPostiPerFila(int numeroPostiPerFila) {
		this.numeroPostiPerFila = numeroPostiPerFila;
	}
}
