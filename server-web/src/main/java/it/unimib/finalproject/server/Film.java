package it.unimib.finalproject.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.core.JsonProcessingException;

public class Film {
	//Attributi privati della classe film
	private String id;
	private String nome;
	private int anno;
	private int durata;
	private String regista;
	private String sinossi;
	private String immagine;
	
	//Metodi getter
	public String getId() {
		return this.id;
	}
	public String getNome() {
		return this.nome;
	}
	public int getAnno() {
		return this.anno;
	}
	public int getDurata() {
		return this.durata;
	}
	public String getRegista() {
		return this.regista;
	}
	public String getSinossi() {
		return this.sinossi;
	}
	public String getImmagine() {
		return this.immagine;
	}
	
	//Metodi setter
	public void setId(String id) {
		this.id = id;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public void setAnno(int anno) {
		this.anno = anno;
	}
	public void setDurata(int durata) {
		this.durata = durata;
	}
	public void setRegista(String regista) {
		this.regista = regista;
	}
	public void setSinossi(String sinossi) {
		this.sinossi = sinossi;
	}
	public void setImmagina(String immagine) {
		this.immagine = immagine;
	}
}
