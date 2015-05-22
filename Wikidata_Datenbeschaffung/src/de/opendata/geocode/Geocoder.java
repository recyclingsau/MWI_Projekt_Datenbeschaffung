package de.opendata.geocode;

import de.opendata.main.Helper;

/**
 * class Geocoder
 * Reads geocoordinates from DB, uses a webservice to get address information and stores them in DB
 * 
 * @author Anna Drützler
 * @version 0.1
 * */

public class Geocoder {

	/**
	 * Runs the geocoder
	 * @return Returns true if successful
	 */
	public static boolean runGeocoder() {

		// Connect to Database
		DBCommunicator comm = new DBCommunicator(Helper.DATABASE_PATH,
				Helper.SCHEMA, Helper.DB_USERNAME,
				Helper.DB_PASSWORD);

		boolean success = comm.completeData();

		return success;
	}
}
