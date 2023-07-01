package it.unimib.finalproject.server;

import java.util.ArrayList;

public class Prenotazione {
	private String codice;
	private String proiezione;
	private ArrayList<Posto> posti;
	
	//Metodi getter
	public String getCodice() {
		return this.codice;
	}
	public String getProiezione() {
		return this.proiezione;
	}
	public ArrayList<Posto> getPosti(){
		return this.posti;
	}
	
	//Metodi setter
	public void setCodice(String codice) {
		this.codice = codice;
	}
	public void setProiezione(String proiezione) {
		this.proiezione = proiezione;
	}
	public void setPosti(ArrayList<Posto> posti) {
		this.posti = posti;
	}
	
	//Metodo per aggiungere un posto alla prenotazione
	public void addPosto(Posto posto) {
		posti.add(posto);
	}
	
	//Metodo per rimuovere un posto dalla prenotazione
	public void removePosto(Posto posto) {
		for(int i = 0; i < posti.size(); i++) {
			if(posti.get(i).getCodice() == posto.getCodice() && posti.get(i).getFila() == posto.getFila()) {
				posti.remove(posto);
			}
		}
	}	
}
