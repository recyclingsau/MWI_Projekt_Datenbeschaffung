package de.opendata.wikidata_geocode;

import java.io.IOException;
import java.sql.SQLException;
import org.json.*;

public class Geocoder {

	public static boolean runGeocoder() {
	

		// Verbindungsaufbau DB
		DBCommunicator comm = new DBCommunicator(src.Helper.DATABASE_PATH,src.Helper.SCHEMA,src.Helper.DB_USERNAME,src.Helper.DB_PASSWORD); 
		
		try {
			comm.completeData();
		} catch (ClassNotFoundException | SQLException | IOException
				| JSONException e) {

			src.EntityTimerProcessor.logger.error("Error.");
			return false;
		}
		
		return true;

	}
}
