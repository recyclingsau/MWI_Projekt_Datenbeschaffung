package de.opendata.main;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.SiteLink;

import de.opendata.entities.ClaimValue;
import de.opendata.entities.Item;
import de.opendata.entities.Item.Datatype;
import de.opendata.entities.WikidataObject;

/**
 * Class containing all methods to communicate with MySQL-Database
 * 
 * @author Marco Kinkel
 * @version 0.1
 */
public class SQLMethods {

	/**
	 * List to store all SQL-Queries in. Must be FiFo because delete-statements
	 * are added at first
	 */
	public static ArrayList<String> queries = new ArrayList<String>();

	/**
	 * Opens connection to database using {@link com.mysql.jdbc.Driver}
	 * 
	 * @return Connection to work with. Returns null, if no connection possible.
	 *         AutoCommit is FALSE!
	 * 
	 *         TODO: Es muss sichergestellt werden, dass eine Verbindung zur DB
	 *         besteht, bevor die Dumps verarbeitet werden. Falls nicht, muss
	 *         das ganze Programm abbrechen!
	 */
	protected static Connection openSQLconnection() {

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			EntityTimerProcessor.logger
					.error("No driver-class found for DB-Connection. Abort!");
			return null;
		}

		Connection con = null;

		try {
			// con = DriverManager.getConnection("jdbc:mysql://"
			// + Helper.DATABASE_PATH + "_" + Helper.SCHEMA + "?user="
			// + Helper.DB_USERNAME + "&password=" + Helper.DB_PASSWORD);

			String url = "jdbc:mysql://" + Helper.DATABASE_PATH + "_"
					+ Helper.SCHEMA;

			con = DriverManager.getConnection(url, Helper.DB_USERNAME,
					Helper.DB_PASSWORD);

			con.setAutoCommit(false);
			return con;
		} catch (SQLException e) {
			EntityTimerProcessor.logger
					.error("Connection to DB failed. Abort!");
			return null;
		}
	}

	/**
	 * Creates and stores SQL statements for list of {@link de.opendata.entities.Item}s.
	 * Statements get executed in {@link SQLMethods#executeQueries()}.
	 * 
	 * @param itemList
	 *            List of Items to be converted into SQL statements
	 * @param tableName
	 *            Name of table in DB (for instance "PERSONS" or "JOBS")
	 */
	public static void createAllSQLstatements(ArrayList<Item> itemList,
			String tableName) {

		for (Item i : itemList) {

			// Create and store SQL statements for claims
			createClaimsStatements(i, tableName);

			// Create and store SQL statements for aliases
			createAliasesStatements(tableName, i.id, i.alias);

			// Create and store SQL statements for links
			createLinksStatements(tableName, i.id, i.link);

			// Create and store SQL statements for descriptions
			createOthersStatements("desc", tableName, i.id, i.desc);

			// Create and store SQL statements for labels
			createOthersStatements("label", tableName, i.id, i.label);
		}
	}

	/**
	 * Creates and stores SQL statements for {@link de.opendata.entities.WikidataObject}.
	 * Statements get executed in {@link SQLMethods#executeQueries()}.
	 * 
	 * @param wikidataObjectList
	 *            List of gui-texts to be converted into SQL statements
	 */
	public static void createWikidataObjectStatements(
			ArrayList<WikidataObject> wikidataObjectList) {

		for (WikidataObject obj : wikidataObjectList) {

			// Remove all entries in gotic language because this character set
			// is not included in UTF-8
			obj.label.remove("got");

			// Iterate over every language
			for (String language : obj.label.keySet()) {

				// Read label of gui-text in actual language
				String label = obj.label.get(language).getText();

				// Prevent SQL injection
				label = preventSQLinjection(label, false);

				String desc = "";

				// Try reading description of the actual language.
				// Can be unexistant because we are looping over the languages
				// of the labels-Map.
				// Because we only have 0-1 label and 0-1 description per
				// language, we can try to connect these in a line.
				try {
					// If a description for this language was found, delete this
					// description after reading it.
					// So we don't create duplicate entries in the next loop.
					desc = obj.desc.remove(language).getText();

					// Prevent SQL injection
					desc = preventSQLinjection(desc, false);

				} catch (Exception e) {
					// No description in this language found. No further
					// errorhandling needed
				}

				// Create SQL query dynamically
				String query = "INSERT INTO gui_texts (item_id, language, label, description) VALUES('"
						+ obj.id
						+ "', '"
						+ language
						+ "', '"
						+ label
						+ "', '"
						+ desc + "');";

				// Store SQL query
				queries.add(query);

			}

			// After iterating over language keys for labels, we have to read
			// the remaining languages for descriptions. These automatically
			// don't have a label because they weren't deleted in the loop
			// before.
			for (String language : obj.desc.keySet()) {

				String description = obj.desc.get(language).getText();

				// Prevent SQL injection
				description = preventSQLinjection(description, false);

				String label = "";

				// Create SQL query dynamically
				String query = "INSERT INTO gui_texts (item_id, language, label, description) VALUES('"
						+ obj.id
						+ "', '"
						+ language
						+ "', '"
						+ label
						+ "', '"
						+ description + "');";

				// Store SQL query
				queries.add(query);

			}

		}

	}

	/**
	 * Create and store SQL statements to empty all tables for special type of
	 * item defined in param tableName
	 * 
	 * @param tableName
	 *            Type of item and name of table to be emptied (for instance
	 *            "PERSONS" or "JOBS")
	 */
	public static void createItemDeletionStatements(String tableName) {

		// Lock and empty alias-table
		// String lock_alias = "LOCK TABLE " + tableName
		// + "_alias WRITE;";
		// queries.add(lock_alias);
		String empty_alias = "DELETE FROM " + tableName.toLowerCase()
				+ "_alias;";
		queries.add(empty_alias);

		// Lock and empty label-table
		// String lock_label = "LOCK TABLE " + tableName
		// + "_label WRITE;";
		// queries.add(lock_label);
		String empty_label = "DELETE FROM " + tableName.toLowerCase()
				+ "_label;";
		queries.add(empty_label);

		// Lock and empty descriptions-table
		// String lock_desc = "LOCK TABLE " + tableName.toLowerCase() +
		// "_desc WRITE;";
		// queries.add(lock_desc);
		String empty_desc = "DELETE FROM " + tableName.toLowerCase() + "_desc;";
		queries.add(empty_desc);

		// Lock and empty claim-table
		// String lock_claim = "LOCK TABLE " + tableName
		// + "_claim WRITE;";
		// queries.add(lock_claim);
		String empty_claim = "DELETE FROM " + tableName.toLowerCase()
				+ "_claim;";
		queries.add(empty_claim);

		// Lock and empty link-table
		// String lock_link = "LOCK TABLE " + tableName.toLowerCase() +
		// "_link WRITE;";
		// queries.add(lock_link);
		String empty_link = "DELETE FROM " + tableName.toLowerCase() + "_link;";
		queries.add(empty_link);

	}

	/**
	 * Create and store SQL statements to empty other tables
	 */
	public static void createOtherDeletionStatement(String tableName) {

		// String lock = "LOCK TABLE " + tableName.toLowerCase() + " WRITE;";
		// queries.add(lock);

		String empty = "DELETE FROM " + tableName.toLowerCase() + ";";
		queries.add(empty);
	}

	/**
	 * Create and store SQL statements to write claims of single Item into DB
	 * 
	 * @param i
	 *            Item whose claims need to be converted to SQL statements
	 * @param tableName
	 *            Type of item and name of table in which claims should be
	 *            stored (for instance "PERSONS" or "JOBS")
	 */
	protected static void createClaimsStatements(Item i, String tableName) {

		// Counter for help key to have unique keys in DB entries
		HashMap<String, Integer> keyCounter = new HashMap<String, Integer>();

		// Stores all generated Q-IDs to prevent redundant entries in
		// claims-table
		ArrayList<String> generatedQIDs = new ArrayList<String>();

		// To check if adding an entry to the claims-table is necessary
		boolean addClaim = true;

		// Iterate over claims
		for (Entry<String, List<ClaimValue>> entry : i.claim.entrySet()) {
			String key = entry.getKey();
			Iterator<ClaimValue> iter = entry.getValue().iterator();
			while (iter.hasNext()) {

				// Get value
				ClaimValue claim = iter.next();
				if (claim != null) {

					// Prevent SQL injection
					if (claim.value != null) {
						claim.value = preventSQLinjection(claim.value, false);
					}

					// Get initial index for help key. We need this one in our
					// DB
					// because item_id and property_id are not unique
					int newIndex = 1;

					// Try to get index value of current property key
					if (keyCounter.get(key) != null) {
						// Add 1 to index value
						newIndex = keyCounter.get(key) + 1;
					}

					// Put new index value to keyCounter
					keyCounter.put(key, newIndex);

					// if datatype is String
					if (claim.type == Datatype.STRING) {

						String label_table = "";
						String label_id = i.id + "_" + key;
						boolean jump = false;

						// Find name of table to add dummy label and description
						// value
						switch (key) {
						case "P17":
							label_table = "states";
							break;
						case "P276":
							label_table = "cities";
							break;
						case "P106":
							label_table = "jobs";
							break;
						case "P69":
							label_table = "educationinstitutes";
							break;
						default:
							jump = true;
							break;
						// TODO: When adding new entitys, where string values in
						// claims are possible
						}

						if (!jump) {

							// Check if combination of Q-ID and P-ID already has
							// an
							// entry in claim-table.
							// If so, don't add it there but still add the value
							// in
							// the
							// label- and description-tables.
							if (!generatedQIDs.contains(label_id)) {
								generatedQIDs.add(label_id);
							} else {
								addClaim = false;
							}

							// Create SQL query dynamically
							String query = "INSERT INTO "
									+ label_table
									+ "_label (item_id, language, label) VALUES('"
									+ label_id + "', 'en', '" + claim.value
									+ "');";
							// Store SQL query
							queries.add(query);

							// Value is now the generated label id
							claim.value = label_id;
						}
					}

					if (addClaim) {

						// Create SQL query dynamically
						String query = "INSERT INTO "
								+ tableName.toLowerCase()
								+ "_claim (item_id, property, property_key, value) VALUES('"
								+ i.id + "', '" + key + "', '" + newIndex
								+ "', '" + claim.value + "');";

						// Store SQL query
						queries.add(query);

					}
				} else
					EntityTimerProcessor.logger.warn("Entit�t " + i.id
							+ " besitzt null-Claim.");
			}
		}
	}

	/**
	 * Create and store SQL statements to write descriptions or labels of single
	 * Item (represented by its ID) into DB
	 * 
	 * @param tableType
	 *            Defines if content contains labels or descriptions ("desc" or
	 *            "label")
	 * @param tableName
	 *            Type of item and name of table in which descriptions or labels
	 *            should be stored (for instance "PERSONS" or "JOBS")
	 * @param id
	 *            ID of the item (with leading 'Q')
	 * @param content
	 *            Map of descriptions or labels to be converted into SQL
	 *            statements
	 */
	protected static void createOthersStatements(String tableType,
			String tableName, String id,
			HashMap<String, MonolingualTextValue> content) {

		// Validate tableType
		if (!(tableType.equalsIgnoreCase("desc") | tableType
				.equalsIgnoreCase("label"))) {
			return;
		}

		// Remove all entries in gotic language because this character set
		// is not included in UTF-8
		content.remove("got");

		// Iterate over descriptions or labels
		for (Entry<String, MonolingualTextValue> entry : content.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue().getText();

			// Prevent SQL injection
			value = preventSQLinjection(value, false);

			// Create SQL query dynamically
			String query = "INSERT INTO " + tableName.toLowerCase() + "_"
					+ tableType.toLowerCase() + " VALUES('" + id + "', '" + key
					+ "', '" + value + "');";

			// Store SQL query
			queries.add(query);

		}
	}

	/**
	 * Create and store SQL statements to write aliases for a single Item
	 * (represented by its ID) into the DB
	 * 
	 * @param tableName
	 *            Type of item and name of table in which aliases should be
	 *            stored (for instance "PERSONS" or "JOBS")
	 * @param id
	 *            ID of the item (with leading 'Q')
	 * @param content
	 *            Map of aliases to be converted into SQL statements
	 */
	protected static void createAliasesStatements(String tableName, String id,
			Map<String, List<MonolingualTextValue>> content) {

		// Counter for help key to have unique keys in DB entries
		HashMap<String, Integer> keyCounter = new HashMap<String, Integer>();

		// Remove all entries in gotic language because this character set
		// is not included in UTF-8
		content.remove("got");

		// Iterate over aliases
		for (Entry<String, List<MonolingualTextValue>> entry : content
				.entrySet()) {

			// Get language
			String key = entry.getKey();
			String value = "";

			// Get initial index for help key. We need this one in out DB
			// because item_id and language are no unique key
			int newIndex = 1;

			// Iterate over all aliases with current language
			for (MonolingualTextValue textValue : entry.getValue()) {

				// Get alias
				value = textValue.getText();

				// Prevent SQL injection
				value = preventSQLinjection(value, false);

				// Try to get index value of current language key
				if (keyCounter.get(key) != null) {
					// Add 1 to index value
					newIndex = keyCounter.get(key) + 1;
				}

				// Put new index value to keyCounter
				keyCounter.put(key, newIndex);

				// Create SQL query dynamically
				String query = "INSERT INTO "
						+ tableName.toLowerCase()
						+ "_alias (item_id, language, language_key, alias) VALUES('"
						+ id + "', '" + key + "', '" + newIndex + "', '"
						+ value + "');";

				// Store SQL
				queries.add(query);

			}
		}
	}

	/**
	 * Create and store SQL statements to write links for a single Item
	 * (represented by its ID) into the DB.
	 * 
	 * Uses {@link Helper#sites} to create URLs.
	 * 
	 * @param tableName
	 *            Type of item and name of table in which links should be stored
	 *            (for instance "PERSONS" or "JOBS")
	 * @param id
	 *            ID of the item (with leading 'Q')
	 * @param content
	 *            Map of links to be converted into SQL statements
	 * 
	 */
	protected static void createLinksStatements(String tableName, String id,
			Map<String, SiteLink> content) {

		// Remove all entries in gotic language because this character set
		// is not included in UTF-8
		content.remove("got");

		// Iterate over all links of item
		for (Entry<String, SiteLink> entry : content.entrySet()) {

			// Get key of link. Will not be written to DB
			String key = entry.getKey();

			// Get language-code of link
			String language = Helper.sites.getLanguageCode(key);

			// Create URL of link
			String url = Helper.sites.getPageUrl(key, entry.getValue()
					.getPageTitle());

			// Get group of link (for instance wikipedia, wikiquote, wikivoyage)
			String group = Helper.sites.getGroup(key);

			// Prevents SQL injection
			url = preventSQLinjection(url, true);

			// Create SQL query dynamically
			String query = "INSERT INTO " + tableName.toLowerCase()
					+ "_link (item_id, language, wiki_id, url) VALUES('" + id
					+ "', '" + language + "', '" + group + "', '" + url + "');";

			// Store SQL
			queries.add(query);

		}
	}

	/**
	 * Create connection to DB and execute predefined SQL queries
	 */
	protected static boolean executeQueries() {

		Connection con = openSQLconnection();

		try {
			Iterator<String> queryIterator = queries.iterator();

			// Iterate over stored SQL statements
			while (queryIterator.hasNext()) {
				String query = queryIterator.next();

				// Print query
				EntityTimerProcessor.logger.debug(query);

				try {
					// Create and execute statement
					Statement stmt = con.createStatement();
					stmt.executeUpdate(query);
					stmt.close();
					if (Helper.COMMIT_MODE.equals("SINGLE")) {
						con.commit();
					}
				} catch (SQLException e) {
					EntityTimerProcessor.logger.error(e.getMessage());
					if (Helper.COMMIT_MODE.equals("SINGLE")) {
						EntityTimerProcessor.logger.info("Commit mode is SINGLE. Trying to rollback single false query: " + query + "...");
					}
					else {
						EntityTimerProcessor.logger.info("Commit mode is ALL. Trying to rollback all queries...");
					}
							
					try {
						// Rollback work to keep DB integrity
						con.rollback();

					} catch (SQLException e1) {
						// Rollback was not successful.
						// Maybe connection is lost. In this case, DBMS should
						// rollback by itself
						EntityTimerProcessor.logger
								.error("Rollback not successful. Please check data integrity of DB!");
					}
				}
			}
		} finally {
			if (con != null) {
				try {

					if (Helper.COMMIT_MODE.equals("ALL")) {
						con.commit();
					}
					// Close connection
					con.close();
				} catch (SQLException e) {
					EntityTimerProcessor.logger
							.error("Connection to DB could not be closed!");
				}
			}
		}
		// This method always returns true by now.
		return true;
	}

	/**
	 * Refresh all Views in Database
	 */
	protected static void refreshViews() {

		EntityTimerProcessor.logger
				.info("Refreshing education institutes view...");
		boolean successful = refreshSingleView("educationinstitutes");

		if (!successful) {
			EntityTimerProcessor.logger
					.error("Refresh of education institutes view failed!");
		}

		EntityTimerProcessor.logger.info("Refreshing persons view...");
		successful = refreshSingleView("persons");

		if (!successful) {
			EntityTimerProcessor.logger
					.error("Refresh of persons view failed!");
		}

		EntityTimerProcessor.logger.info("Refreshing search function view...");
		successful = refreshSingleView("sufu");

		if (!successful) {
			EntityTimerProcessor.logger
					.error("Refresh of search function view failed!");
		}

		EntityTimerProcessor.logger.info("Refreshing languages view...");
		successful = refreshSingleView("languages");

		if (!successful) {
			EntityTimerProcessor.logger
					.error("Refresh of languages function view failed!");
		}

	}

	/**
	 * Refresh view in DB by reading and executing a .sql-File
	 * 
	 * @param nameOfViewFile
	 * 			name of .sql-File in view-folder (without ".sql")
	 */
	private static boolean refreshSingleView(String nameOfViewFile) {

		String query = "";

		// Read SQL-File
		try {
			FileReader reader = new FileReader(Helper.VIEWS_PATH
					+ nameOfViewFile + ".sql");

			for (int c; (c = reader.read()) != -1;) {
				query += (char) c;
			}
			reader.close();

		} catch (IOException e) {
			EntityTimerProcessor.logger.error("View file " + Helper.VIEWS_PATH
					+ nameOfViewFile
					+ ".sql not found! Can't refresh this view!");
			return false;
		}

		// Execute SQL
		Connection con = openSQLconnection();

		try {
			// query = query.replaceAll("`", "");
			query = query.trim();
			String[] queries = query.split(";");

			for (String singleQuery : queries) {

				con.prepareStatement(singleQuery.toLowerCase()).execute();
			}
			con.commit();

		} catch (SQLException e) {

			EntityTimerProcessor.logger
					.error("Can't execute SQL-statement to update view "
							+ nameOfViewFile + ". Try to rollback...");
			EntityTimerProcessor.logger.error(e.getMessage());

			try {
				con.rollback();
			} catch (SQLException e1) {
				EntityTimerProcessor.logger.error("Rollback of view-update "
						+ nameOfViewFile + " was not successful.");
			}
			return false;
		} finally {
			try {
				con.close();
			} catch (SQLException e) {
				EntityTimerProcessor.logger
						.error("DB-Connection could not be closed.");
				return false;
			}
		}

		return true;

	}
	
	/**
	 * Remove SQL-Injection-letters from texts
	 * 
	 * @param raw
	 * 			String to be checked for unallowed letters
	 * @param isUrl
	 * 			Is true if raw string is URL
	 * @return String with no unallowed letters for SQL-statement
	 */
	public static String preventSQLinjection(String raw, boolean isUrl){
		if(isUrl) {
			raw = raw.replaceAll("'", "%27");
			raw = raw.replaceAll("\"", "%22");
		}
		else {
			raw = raw.replaceAll("'", "`");
			raw = raw.replaceAll("\"", "``");
		}
		
		raw = raw.replaceAll("\\\\", "");
		raw = raw.replaceAll(";", ",");	
		
		return raw;
	}

}
