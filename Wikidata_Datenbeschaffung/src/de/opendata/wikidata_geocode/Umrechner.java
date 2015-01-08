package de.opendata.wikidata_geocode;

/**Klasse Umrechner: Berechnungen von Geo-Daten in Fließkommazahlen
 * @author Anna Drützler
 * @version 1.0
 * */

public class Umrechner {
	private String m_koordinate;

	public Umrechner(String koordinate) {
		this.m_koordinate = koordinate;
	}

	private float getGrad() {
		return Float.parseFloat(this.m_koordinate.substring(0,
				this.m_koordinate.indexOf("°")));
	}

	private float getMinute() {
		return Float.parseFloat(this.m_koordinate.substring(
				this.m_koordinate.indexOf("°") + 1,
				this.m_koordinate.indexOf("`")));
	}

	private float getSekunde() {
		return Float.parseFloat(this.m_koordinate.substring(
				this.m_koordinate.indexOf("`") + 1,
				this.m_koordinate.indexOf("``")));
	}

	// Umrechnen der Koordinaten (((Sekunden/60)+Minuten)/60)+Grad

	public double rechnen() {
		return (((this.getSekunde() / 60.0) + this.getMinute()) / 60.0)
				+ this.getGrad();
	}

}
