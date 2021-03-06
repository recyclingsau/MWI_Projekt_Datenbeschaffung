package de.opendata.geocode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.JSONException;
import org.json.JSONObject;

import de.opendata.main.EntityTimerProcessor;
import de.opendata.main.SQLMethods;

/**
 * class WebserviceCall: Calls WebService, reads and stores address information
 * into variables.
 * 
 * @author Anna Dr�tzler
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

	/**
	 * Empty constructor
	 */
	public WebserviceCall() {
	}

 
 /**
  * Calling web service for records.
  * @param lat Latitude of geo-coordinates in format "###.#####"
  * @param lon Longitude of geo-coordinates in format "###.#####"
  * @return Returns true if successful
  */

	public boolean fetchAdressInfo(String lat, String lon) {

		// Web service with parameters filled.
		String inquiry = WEBSERVICEURL + "&lat=" + lat + "&lon=" + lon;

		// HTTP connection
		URL weburl;
		String output = "";
		String line;
		try {
			weburl = new URL(inquiry);

			HttpURLConnection conweb = (HttpURLConnection) weburl
					.openConnection();
			conweb.setRequestMethod("GET");

			// Read output
			 BufferedReader in = new BufferedReader(new InputStreamReader(
			 conweb.getInputStream(), StandardCharsets.UTF_8));
			
			 while ((line = in.readLine()) != null) {
			 output = output + line;
			 }
			 in.close(); // close connection
		} catch (IOException e) {
			EntityTimerProcessor.logger.error("Error using webservice.");
			return false;
		}
		try {
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
		} catch (JSONException e) {
			EntityTimerProcessor.logger.error("Error reading JSON-Object.");
			return false;
		}
		return true;
	}

	/**
	 * Get value from Json-Object
	 * @param key Value to read from JSON
	 * @param o JSON-Object where value is stored
	 * @return String-representation of value
	 * @throws JSONException If JSON-Object cannot be parsed
	 */
	private String fetchValue(String key, JSONObject o) throws JSONException {
		String ret;
		if (o.has(key)) {
			ret = o.getString(key);

			// Prevent SQL injection
			ret = SQLMethods.preventSQLinjection(ret, false);
		} else {
			ret = "";
		}
		return ret;
	}
}
