package de.opendata.wikidata_geocode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

public class WebserviceAufruf {

	public String road;
	public String house_number;
	public String city;
	public String plz;
	public String country;
	public String country_code;
	public final String WEBSERVICEURL = "http://nominatim.openstreetmap.org/reverse?format=json";

	public WebserviceAufruf() {

	}

	// Webservice für Datensätze aufrufen

	public void fetchAdressInfo(String lat, String lon) throws IOException,
			JSONException {
		
		// Webservice mit Parametern gefüllt
		String anfrage = WEBSERVICEURL + "&lat=" + lat
				+ "&lon=" + lon;

	//	System.out.println(anfrage);
		
		// HTTP-Verbindung aufbauen
		URL weburl;

		weburl = new URL(anfrage);

		HttpURLConnection conweb = (HttpURLConnection) weburl.openConnection();
		conweb.setRequestMethod("GET");

		// Ausgabe lesen
		BufferedReader in = new BufferedReader(new InputStreamReader(
				conweb.getInputStream()));

		String ausgabe = "";
		String zeile;

		while ((zeile = in.readLine()) != null) {
			ausgabe = ausgabe + zeile;
		}
		in.close(); // Verbindung trennen

		// JSON
		JSONObject geodaten = new JSONObject(ausgabe);

		JSONObject address = geodaten.getJSONObject("address");

		this.road = this.holeWert("road", address);
		this.house_number = this.holeWert("house_number", address);
		this.city = this.holeWert("city", address);
		if (this.city.equals("")) {
			this.city = this.holeWert("town", address);
		}
		else if (this.city.equals("")) {
			this.city = this.holeWert("city_district", address); 
		} 
		
		else if (this.city.equals("")) {
			this.city = this.holeWert("suburb", address); 
		}

		this.plz = this.holeWert("postcode", address);
		this.country = this.holeWert("country", address);
		this.country_code = this.holeWert("country_code", address);

	 /* System.out.println("Straße: " +road); 
		 System.out.println("Hausnummer: " +house_number);
		 System.out.println("Stadt: " +city); 
		 System.out.println("PLZ: " +plz);
		 System.out.println("Land: " +country); 
		 System.out.println("Landcode: " +country_code); */
		 

	}
	 // Wofür?
	private String holeWert(String key, JSONObject o) throws JSONException {
		String ret;
		if (o.has(key)) {
			ret = o.getString(key);
			
			// Prevent SQL injection
			ret = ret.replaceAll("'", "`");
			ret = ret.replaceAll("\"", "`");
		} else {
			ret = "";
		}
		return ret;
	}
}
