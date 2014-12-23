package src;

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

import entities.Item;
import entities.WikidataObject;

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
	 * @return Connection to work with. Returns null, if no connection possible. AutoCommit is FALSE!
	 * 
	 *         TODO: Es muss sichergestellt werden, dass eine Verbindung zur DB
	 *         besteht, bevor die Dumps verarbeitet werden. Falls nicht, muss
	 *         das ganze Programm abbrechen!
	 */
	protected static Connection openSQLconnection() {

		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception e) {
			System.err.println("Keine Treiber-Klasse!");
			return null;
		}

		Connection con = null;

		try {
			con = DriverManager
					.getConnection("jdbc:mysql://" + Helper.DATABASE_PATH + "?user=" + Helper.DB_USERNAME + "&password=" + Helper.DB_PASSWORD);
			con.setAutoCommit(false);
			return con;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Creates and stores SQL statements for list of {@link entities.Item}s.
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
			createOthersStatements("DESC", tableName, i.id, i.desc);

			// Create and store SQL statements for labels
			createOthersStatements("LABEL", tableName, i.id, i.label);
		}
	}

	/**
	 * Creates and stores SQL statements for {@link entities.WikidataObject}.
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
				label = label.replaceAll("'", "`");
				label = label.replaceAll("\"", "`");

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
					desc = desc.replaceAll("'", "`");
					desc = desc.replaceAll("\"", "`");

				} catch (Exception e) {
					// No description in this language found. No further
					// errorhandling needed
				}

				// Create SQL query dynamically
				String query = "INSERT INTO wikidata.GUI_TEXTS VALUES('"
						+ obj.id + "', '" + language + "', '" + label + "', '"
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
				description = description.replaceAll("'", "`");
				description = description.replaceAll("\"", "`");

				String label = "";

				// Create SQL query dynamically
				String query = "INSERT INTO wikidata.GUI_TEXTS VALUES('"
						+ obj.id + "', '" + language + "', '" + label + "', '"
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

		// SQL to empty alias-table
		String query_alias = "DELETE FROM wikidata." + tableName + "_alias;";
		queries.add(query_alias);

		// SQL to empty label-table
		String query_label = "DELETE FROM wikidata." + tableName + "_label;";
		queries.add(query_label);

		// SQL to empty descriptions-table
		String query_desc = "DELETE FROM wikidata." + tableName + "_desc;";
		queries.add(query_desc);

		// SQL to empty claims-table
		String query_claim = "DELETE FROM wikidata." + tableName + "_claim;";
		queries.add(query_claim);

		// SQL to empty link-table
		String query_link = "DELETE FROM wikidata." + tableName + "_link;";
		queries.add(query_link);

	}

	/**
	 * Create and store SQL statements to empty GUI-text-table
	 */
	public static void createGuiTextsDeletionStatement() {

		String query = "DELETE FROM wikidata.GUI_TEXTS;";
		queries.add(query);
	}

	/**
	 * Create and store SQL statements to empty languages-table
	 */
	public static void createLanguagesDeletionStatement() {

		String query = "DELETE FROM wikidata.languages;";
		queries.add(query);
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

		// Iterate over claims
		for (Entry<String, String> entry : i.claim.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			// Prevent SQL injection
			if (value != null) {
				value = value.replaceAll("'", "`");
				value = value.replaceAll("\"", "`");
			}

			// Get initial index for help key. We need this one in out DB
			// because item_id and property_id are no unique key
			int newIndex = 0;

			// Try to get index value of current property key
			if (keyCounter.get(key) != null) {
				// Add 1 to index value
				newIndex = keyCounter.get(key) + 1;
			}

			// Put new index value to keyCounter
			keyCounter.put(key, newIndex);

			// Create SQL query dynamically
			String query = "INSERT INTO wikidata." + tableName
					+ "_claim values('" + i.id + "', '" + key + "', '"
					+ newIndex + "', '" + value + "');";

			// Store SQL query
			queries.add(query);

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
			value = value.replaceAll("'", "`");
			value = value.replaceAll("\"", "`");

			// Create SQL query dynamically
			String query = "INSERT INTO wikidata." + tableName + "_"
					+ tableType + " VALUES('" + id + "', '" + key + "', '"
					+ value + "');";

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
			int newIndex = 0;

			// Iterate over all aliases with current language
			for (MonolingualTextValue textValue : entry.getValue()) {

				// Get alias
				value = textValue.getText();

				// Prevent SQL injection
				value = value.replaceAll("'", "`");
				value = value.replaceAll("\"", "``");

				// Try to get index value of current language key
				if (keyCounter.get(key) != null) {
					// Add 1 to index value
					newIndex = keyCounter.get(key) + 1;
				}

				// Put new index value to keyCounter
				keyCounter.put(key, newIndex);

				// Create SQL query dynamically
				String query = "INSERT INTO wikidata." + tableName
						+ "_alias VALUES('" + id + "', '" + key + "', '"
						+ newIndex + "', '" + value + "');";

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
			url = url.replaceAll("'", "%27");
			url = url.replaceAll("\"", "%22");

			// Create SQL query dynamically
			String query = "INSERT INTO wikidata." + tableName
					+ "_link VALUES('" + id + "', '" + language + "', '"
					+ group + "', '" + url + "');";

			// Store SQL
			queries.add(query);

		}
	}

	/**
	 * Create connection to DB and execute predefined SQL queries
	 */
	protected static void executeQueries() {

		Connection con = openSQLconnection();

		try {
			Iterator<String> queryIterator = queries.iterator();

			// Iterate over stored SQL statements
			while (queryIterator.hasNext()) {
				String query = queryIterator.next();

				// Print query
				// TODO: In Logger ablösen
				System.out.println(query);

				// Create and execute statement
				Statement stmt = con.createStatement();
				stmt.executeUpdate(query);
				stmt.close();
			}

			con.commit();

		} catch (SQLException e) {
			try {
				// Rollback everything at error
				con.rollback();

			} catch (SQLException e1) {
				// Rollback was not successful.
				// Maybe connection is lost. In this case, DBMS should rollback
				// by itself
				e1.printStackTrace();
			}
			e.printStackTrace();
		} finally {
			if (con != null) {
				try {
					// Close connection
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
