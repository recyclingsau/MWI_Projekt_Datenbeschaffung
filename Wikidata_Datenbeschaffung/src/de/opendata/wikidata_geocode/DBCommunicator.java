package de.opendata.wikidata_geocode;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

import org.json.JSONException;

import src.EntityTimerProcessor;

/**
 * class DBCommunicator
 * 
 * @author Anna Drützler
 * @version 1.0
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
	 * Vervollständigen der Geo-Daten in wikidata SQL-Datenbank
	 * 
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
					+ " MAX(if(property='P1134',value,0)) AS Lage_Ort,"
					+ " MAX(if(property='P969',value,0)) AS Adresse,"
					+ " MAX(if(property='P625',value,0)) AS Koordinaten "
					+ " FROM educationinstitutes_claim GROUP by item_id) as t"
					+ " WHERE (Land ='0' OR Strasse='0' OR Hausnummer='0'  "
					+ " OR PLZ='0' OR Lage_Ort='0' OR Adresse='0' ) AND Koordinaten !=0 ;";

			rs = stmt.executeQuery(anfrage);

			// Vorbereitung Webservice (empty constructor)
			webservice = new WebserviceCall();

			if (rs != null && webservice != null) {

				// Jeden Datensatz durchgehen

				while (rs.next()) {

					// Koordinaten an Komma trennen
					String[] koordinaten = rs.getString("Koordinaten").split(
							",");
					koordinaten[0] = koordinaten[0].trim();
					koordinaten[1] = koordinaten[1].trim();

					// getrennte Geodaten an den Webwervice übergeben,

					boolean success = webservice.fetchAdressInfo(
							koordinaten[0], koordinaten[1]);
					if (success) {

						// 2. DB Verbindung für das Einfügen der Daten
						// Connection con2;
						// con2 = DriverManager.getConnection(url, user,
						// password);

						Statement stmt2 = con.createStatement();

						// letzten Property-Key ermitteln
						String maxKey = "SELECT property_key FROM educationinstitutes_claim "
								+ "WHERE item_id = '"
								+ rs.getString("item_id")
								+ "' ORDER BY property_key DESC LIMIT 1;";

						ResultSet rs2 = stmt2.executeQuery(maxKey);
						rs2.next();
						int max = rs2.getInt("property_key");

						max++;

						// Geo-Daten einzeln mit Property-Key in DB speichern

						if (!webservice.country.equals("")) {
							if (rs.getString("Land").equals("0")) {
								String update = "INSERT INTO educationinstitutes_claim"
										+ " (item_id, property, property_key, value)"
										+ "VALUES ('"
										+ rs.getString("item_id")
										+ "', "
										+ "'P17',"
										+ max
										+ ", '"
										+ webservice.country + "') ;";

								src.EntityTimerProcessor.logger.debug(update);

								con.prepareStatement(update).execute();
								max++;
							}
						}

						if (!webservice.road.equals("")) {
							if (rs.getString("Adresse").equals("0")) {
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

								src.EntityTimerProcessor.logger.debug(update);

								con.prepareStatement(update).execute();
								max++;
							} else if (rs.getString("Adresse").substring(0, 1)
									.equals("Q")
									&& Pattern.matches("[0-9]+",
											rs.getString("Land").split("Q")[1])) {
								String update = "UPDATE educationinstitutes_claim SET "
										+ "value= "
										+ webservice.road
										+ " "
										+ webservice.house_number
										+ "WHERE item_id="
										+ rs.getString("item_id")
										+ " AND property = 'P969' AND property_key= "
										+ rs.getString("property_key") + ";";

								src.EntityTimerProcessor.logger.debug(update);

								con.prepareStatement(update).execute();
								max++;
							}
						}

						if (!webservice.zip_code.equals("")) {
							if (rs.getString("PLZ").equals("0")) {
								String update = "INSERT INTO educationinstitutes_claim"
										+ " (item_id, property, property_key, value)"
										+ "VALUES ('"
										+ rs.getString("item_id")
										+ "', "
										+ "'P281',"
										+ max
										+ ", '"
										+ webservice.zip_code + "') ;";

								src.EntityTimerProcessor.logger.debug(update);

								con.prepareStatement(update).execute();
								max++;
							} else if (rs.getString("PLZ").substring(0, 1)
									.equals("Q")
									&& Pattern.matches("[0-9]+",
											rs.getString("Land").split("Q")[1])) {
								String update = "UPDATE educationinstitutes_claim SET "
										+ "value= "
										+ webservice.zip_code
										+ "WHERE item_id="
										+ rs.getString("item_id")
										+ " AND property = 'P281' AND property_key= "
										+ rs.getString("property_key") + ";";

								src.EntityTimerProcessor.logger.debug(update);

								con.prepareStatement(update).execute();
								max++;
							}
						}
						if (!webservice.city.equals("")) {
							if (rs.getString("Lage_Ort").equals("0")) {
								String update = "INSERT INTO educationinstitutes_claim"
										+ " (item_id, property, property_key, value)"
										+ "VALUES ('"
										+ rs.getString("item_id")
										+ "', "
										+ "'P1134',"
										+ max
										+ ", '"
										+ webservice.city + "') ;";

								src.EntityTimerProcessor.logger.debug(update);

								con.prepareStatement(update).execute();
								max++;
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

}
