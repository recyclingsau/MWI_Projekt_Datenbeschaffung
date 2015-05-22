package de.opendata.geocode;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

import de.opendata.main.EntityTimerProcessor;

/**
 * class DBCommunicator
 * 
 * @author Anna Drützler
 * @author Marco Kinkel
 * @version 0.1
 * */

public class DBCommunicator {

	private String db_path, user, password, db_schema;

	/**
	 * Constructor.
	 * 
	 * @param db_path
	 * @param db_schema
	 * @param user
	 * @param password
	 */

	public DBCommunicator(String db_path, String db_schema, String user,
			String password) {

		this.db_path = db_path;
		this.user = user;
		this.password = password;
		this.db_schema = db_schema;
	}

	/**
	 * Complete geo-data in wikidata SQL-DB
	 * 
	 * @return Returns true if successful
	 * 
	 * @author Anna Drützler
	 * @author Marco Kinkel
	 * **/
	public boolean completeData() {
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://" + this.db_path + "_" + this.db_schema;
		Connection con;
		ResultSet rs;
		WebserviceCall webservice;

		try {
			Class.forName(driver);
		} catch (ClassNotFoundException e) {
			EntityTimerProcessor.logger.error("Couldn't find DB-driver class.");
			return false;
		}

		try {
			con = DriverManager.getConnection(url, this.user, this.password);
		} catch (SQLException e) {
			EntityTimerProcessor.logger.error("Couldn't connect to database.");
			return false;
		}

		try {
			Statement stmt = con.createStatement();

			String anfrage = "SELECT  item_id,Land,Adresse,Strasse,Hausnummer,PLZ,Lage_Ort,Koordinaten "
					+ " FROM (SELECT item_id,"
					+ " MAX(if(property='P17',value,0)) AS Land,"
					+ " MAX(if(property='P669',value,0)) AS Strasse,"
					+ " MAX(if(property='P670',value,0)) AS Hausnummer,"
					+ " MAX(if(property='P281',value,0)) AS PLZ,"
					+ " MAX(if(property='P276',value,0)) AS Lage_Ort,"
					+ " MAX(if(property='P969',value,0)) AS Adresse,"
					+ " MAX(if(property='P625',value,0)) AS Koordinaten "
					+ " FROM educationinstitutes_claim GROUP by item_id) as t"
					+ " WHERE (Land ='0' OR Strasse='0' OR Hausnummer='0'  "
					+ " OR PLZ='0' OR Lage_Ort='0' OR Adresse='0' ) AND Koordinaten !=0 ;";

			rs = stmt.executeQuery(anfrage);

			// Prepare webservice (empty constructor)
			webservice = new WebserviceCall();

			if (rs != null && webservice != null) {

				while (rs.next()) {

					// Split coordinates
					String[] koordinaten = rs.getString("Koordinaten").split(
							",");

					koordinaten[0] = koordinaten[0].trim();
					koordinaten[1] = koordinaten[1].trim();

					// Call webservice using geo-coordinates

					boolean success = webservice.fetchAdressInfo(
							koordinaten[0], koordinaten[1]);
					if (success) {

	
						// Store geo-data for property-key in db

						if (!webservice.country.equals("")) {
							if (rs.getString("Land").equals("0")) {

								// Get new property-key
								int max = getNewPropertyKey(
										rs.getString("item_id"), "P17", con);

								String genericQid = rs.getString("item_id")
										+ "_P17";

								String updateClaims = "INSERT INTO educationinstitutes_claim"
										+ " (item_id, property, property_key, value)"
										+ "VALUES ('"
										+ rs.getString("item_id")
										+ "', "
										+ "'P17',"
										+ max
										+ ", '"
										+ genericQid + "') ;";

								EntityTimerProcessor.logger
										.debug(updateClaims);
								con.prepareStatement(updateClaims).execute();

								String updateLabel = "INSERT INTO states_label"
										+ " (item_id, language, label) "
										+ "VALUES ('" + genericQid
										+ "', 'EN', '" + webservice.country
										+ "') ;";

								EntityTimerProcessor.logger
										.debug(updateLabel);
								con.prepareStatement(updateLabel).execute();
							}
						}

						if (!webservice.road.equals("")) {
							if (rs.getString("Adresse").equals("0")) {

								// Get new property-key
								int max = getNewPropertyKey(
										rs.getString("item_id"), "P969", con);

								String update = "INSERT INTO educationinstitutes_claim"
										+ " (item_id, property, property_key, value)"
										+ "VALUES ('"
										+ rs.getString("item_id")
										+ "', "
										+ "'P969',"
										+ max
										+ ", '"
										+ webservice.road
										+ " "
										+ webservice.house_number + "') ;";

								EntityTimerProcessor.logger.debug(update);

								con.prepareStatement(update).execute();
								max++;
							} else if (isQid(rs.getString("Adresse"))) {

								String update = "UPDATE educationinstitutes_claim SET "
										+ "value= "
										+ webservice.road
										+ " "
										+ webservice.house_number
										+ "WHERE item_id="
										+ rs.getString("item_id")
										+ " AND property = 'P969' AND property_key= "
										+ rs.getString("property_key") + ";";

								EntityTimerProcessor.logger.debug(update);

								con.prepareStatement(update).execute();
							}
						}

						if (!webservice.zip_code.equals("")) {
							if (rs.getString("PLZ").equals("0")) {

								// Get new property-key
								int max = getNewPropertyKey(
										rs.getString("item_id"), "P281", con);

								String update = "INSERT INTO educationinstitutes_claim"
										+ " (item_id, property, property_key, value)"
										+ "VALUES ('"
										+ rs.getString("item_id")
										+ "', "
										+ "'P281',"
										+ max
										+ ", '"
										+ webservice.zip_code + "') ;";

								EntityTimerProcessor.logger.debug(update);

								con.prepareStatement(update).execute();
							} else if (isQid(rs.getString("PLZ"))) {

								String update = "UPDATE educationinstitutes_claim SET "
										+ "value= "
										+ webservice.zip_code
										+ "WHERE item_id="
										+ rs.getString("item_id")
										+ " AND property = 'P281' AND property_key= "
										+ rs.getString("property_key") + ";";

								EntityTimerProcessor.logger.debug(update);

								con.prepareStatement(update).execute();
							}
						}
						if (!webservice.city.equals("")) {
							if (rs.getString("Lage_Ort").equals("0")) {

								String genericQid = rs.getString("item_id")
										+ "_P276";

								// Get new property-key
								int max = getNewPropertyKey(
										rs.getString("item_id"), "P276", con);

								String updateClaims = "INSERT INTO educationinstitutes_claim"
										+ " (item_id, property, property_key, value)"
										+ "VALUES ('"
										+ rs.getString("item_id")
										+ "', "
										+ "'P276',"
										+ max
										+ ", '"
										+ genericQid + "') ;";

								EntityTimerProcessor.logger
										.debug(updateClaims);
								con.prepareStatement(updateClaims).execute();

								String updateLabel = "INSERT INTO cities_label"
										+ " (item_id, language, label) "
										+ "VALUES ('" + genericQid
										+ "', 'EN', '" + webservice.city + "') ;";

								EntityTimerProcessor.logger
										.debug(updateLabel);
								con.prepareStatement(updateLabel).execute();
							}
						}

					}
				}

			}
		} catch (SQLException e) {
			EntityTimerProcessor.logger
					.error("Couldn't create Temporary View.");
			return false;
		}

		try {
			con.close();
		} catch (SQLException e) {
			EntityTimerProcessor.logger.error("Couldn't close DB connection.");
			return false;
		}
		return true;
	}

	
	/**
	 * Get help-key for update in claims-table
	 * 
	 * @param item_id
	 * 				ID of affected item
	 * @param property_id
	 * 				ID of affected property
	 * @param con
	 * 				DB-Connection
	 * 
	 * @return New help-key
	 * 
	 * @author Marco Kinkel
	 **/
	private int getNewPropertyKey(String item_id, String property_id,
			Connection con) throws SQLException {
		Statement stmt = con.createStatement();
		int max = 0;

		// get last property-key
		String query = "SELECT MAX(property_key) as property_key FROM educationinstitutes_claim "
				+ "WHERE item_id = '"
				+ item_id
				+ "' AND property = '"
				+ property_id + "';";

		ResultSet rs = stmt.executeQuery(query);
		if (rs.first()) {

			max = rs.getInt("property_key");
		}

		stmt.close();

		return (max + 1);
	}
	
	
	/**
	 * Check if given value is a Q-ID (e.g. Q12345)
	 * 
	 * @param value
	 * 				Value of claims-table to be checked
	 * 
	 * @return True if value is Q-ID
	 * 
	 * @author Marco Kinkel
	 **/
	private boolean isQid(String value){
		return (value.substring(0, 1).equals("Q")
				&& Pattern.matches("[0-9]+",value.split("Q")[1]));
	}

}
