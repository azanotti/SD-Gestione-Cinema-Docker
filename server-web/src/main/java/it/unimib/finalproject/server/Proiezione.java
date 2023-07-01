package it.unimib.finalproject.server;

import java.util.logging.Logger;

public class Proiezione {
	private String id;
	private String data;
	private String ora;
	private String filmId;
	private String salaId;
	private Posto[][] prenotazioniSala;
	
	//Metodi getter
	public String getId() {
		return this.id;
	}
	public String getData() {
		return this.data;
	}
	public String getOra() {
		return this.ora;
	}
	public String getFilmId() {
		return this.filmId;
	}
	public String getSalaId() {
		return this.salaId;
	}
	public Posto[][] getPrenotazioniSala() {
		return prenotazioniSala;
	}
	
	//Metodi setter
	public void setId(String id) {
		this.id = id;
	}
	public void setData(String data) {
		this.data = data;
	}
	public void setOra(String ora) {
		this.ora = ora;
	}
	public void setFilmId(String filmId) {
		this.filmId = filmId;
	}
	public void setSalaId(String salaId) {
		this.salaId = salaId;
	}
	
	//Metodo per inizializzare i posti
	public void initializePosti() {
		Sala salaRichiesta = SalaResources.exportSala(salaId);
		if(salaRichiesta != null) {
			prenotazioniSala = new Posto[salaRichiesta.getNumeroFile()][salaRichiesta.getNumeroPostiPerFila()];
			
			for(int j = 0; j < salaRichiesta.getNumeroFile(); j++) {
				for(int k = 0; k < salaRichiesta.getNumeroPostiPerFila(); k++) {
					Posto newPosto = new Posto();
					newPosto.setFila(j);
					newPosto.setNumero(k);
					newPosto.setCodice("");
					prenotazioniSala[j][k] = newPosto;
				}
			}
		} else {
			Logger.getLogger(SalaResources.class.getName()).severe("Class richiesta dalla prenotazione non identificata");
		}
		
	}
	
	//Metodo per aggiornare il codice di un posto
	public void setCodicePosto(int fila, int numero, String codice) {
		prenotazioniSala[fila][numero].setCodice(codice);;
	}
	
	//Metodo per reimpostare un posto
	public void resetCodicePosto(int fila, int numero) {
		prenotazioniSala[fila][numero].setCodice("");;
	}
}
