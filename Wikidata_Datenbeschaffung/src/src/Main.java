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
		
		// Read properties from config-file
		Helper.loadConfiguration();
		
		// Initialize and configure logging for periodically println of status
		Helper.configureLogging();

		EntityTimerProcessor.logger.info("Using schema " + Helper.SCHEMA + ".");
		EntityTimerProcessor.logger.info("Create statements to empty DB.");
		
		// Empty all non-static DB-Tables
		SQLMethods.createItemDeletionStatements("PERSONS");
		SQLMethods.createItemDeletionStatements("JOBS");
		SQLMethods.createItemDeletionStatements("EDUCATIONINSTITUTES");
		SQLMethods.createItemDeletionStatements("CITIES");
		SQLMethods.createItemDeletionStatements("STATES");
		// TODO: When adding new tables - Call deletion-method!
		SQLMethods.createOtherDeletionStatement("GUI_TEXTS");
		SQLMethods.createOtherDeletionStatement("LANGUAGES");

		// Create processor to read and process wikidata-dump
		ItemProcessor processor = new ItemProcessor();

		// Read Wikidata-dump (online or offline, controlled by {@link
		// Helper#OFFLINE_MODE}).
		// Data gets processed in methods {@link
		// ItemProcessor#processItemDocument} and
		// {@link ItemProcessor#processPropertyDocument}
		boolean successful = Helper.processEntitiesFromWikidataDump(processor);
		
		// If error occured, end program
		if(!successful){
			EntityTimerProcessor.logger.info("End of program.");
			return;
		}

		// Items from Wikidata were processed in blocks of a defined amount.
		// The last blocks (with less entries than the maximum amount per block)
		// needs to be converted manually
		processor.convertAllAndCreateSQL();

		// Connect to database and execute all saved queries
		successful = SQLMethods.executeQueries();
		
		// If error occured, end program
		if(!successful){
			EntityTimerProcessor.logger.info("End of program.");
			return;
		}

		EntityTimerProcessor.logger.info("Success! End of program.");
		
	}
}
