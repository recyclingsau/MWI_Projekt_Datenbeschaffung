package src;

/**
 * Main class to contain main mehthod
 * 
 * @author Marco
 * @version 0.1
 */
public class Main {

	/**
	 * Main method. Calls methods to clear database, read data from wikidata,
	 * parse them and write them to the database.
	 *
	 * @param args Argumente werden nicht verarbeitet
	 * 
	 * @author Marco Kinkel
	 */
	public static void main(String[] args) {

		// Initialize and configure logging for periodically println of status
		Helper.configureLogging();

		System.out.println("Lösche Datenbank...");

		// Completely deletion of database
		SQLMethods.createPropertyDeletionStatements();
		SQLMethods.createItemDeletionStatements("PERSONS");
		SQLMethods.createItemDeletionStatements("JOBS");

		// TODO: When adding new tables - Call deletion-method!

		// Create processor to read and process wikidata-dump
		ItemProcessor processor = new ItemProcessor();

		// Read Wikidata-dump (online or offline, controlled by {@link
		// Helper#OFFLINE_MODE}).
		// Data gets processed in methods {@link
		// ItemProcessor#processItemDocument} and
		// {@link ItemProcessor#processPropertyDocument}
		Helper.processEntitiesFromWikidataDump(processor);

		// Items from Wikidata were processed in blocks of a defined amount.
		// The last blocks (with less entries than the maximum amount per block)
		// needs to be converted manually
		processor.convertAllAndCreateSQL();

		// Connect to database and execute all saved queries
		SQLMethods.executeQueries();

		System.out.println("Fertig!");

	}

}
