package de.opendata.wikidata_geocode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * class WebserviceCall: Calls WebService, reads and stores address information
 * into variables.
 * 
 * @author Anna Drützler
 * @version 1.0
 * */

public class WebserviceCall {

	public String road;
	public String house_number;
	public String city;
	public String zip_code;
	public String country;
	public String country_code;
	public final String WEBSERVICEURL = "http://nominatim.openstreetmap.org/reverse?format=json";

	public WebserviceCall() {
	}

	// Calling web service for records.

	public void fetchAdressInfo(String lat, String lon) throws IOException,
			JSONException {

		// Web service with parameters filled.
		String inquiry = WEBSERVICEURL + "&lat=" + lat + "&lon=" + lon;

		// HTTP connection
		URL weburl;

		weburl = new URL(inquiry);

		HttpURLConnection conweb = (HttpURLConnection) weburl.openConnection();
		conweb.setRequestMethod("GET");

		// Read output
		BufferedReader in = new BufferedReader(new InputStreamReader(
				conweb.getInputStream()));

		String output = "";
		String line;

		while ((line = in.readLine()) != null) {
			output = output + line;
		}
		in.close(); // close connection

		// Read JSON objects
		JSONObject geodata = new JSONObject(output);

		JSONObject address = geodata.getJSONObject("address");

		this.road = this.fetchValue("road", address);
		this.house_number = this.fetchValue("house_number", address);
		this.city = this.fetchValue("city", address);
		if (this.city.equals("")) {
			this.city = this.fetchValue("town", address);
		} else if (this.city.equals("")) {
			this.city = this.fetchValue("city_district", address);
		}

		else if (this.city.equals("")) {
			this.city = this.fetchValue("suburb", address);
		}

		this.zip_code = this.fetchValue("postcode", address);
		this.country = this.fetchValue("country", address);
		this.country_code = this.fetchValue("country_code", address);

	}

	// Wofür?
	private String fetchValue(String key, JSONObject o) throws JSONException {
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
