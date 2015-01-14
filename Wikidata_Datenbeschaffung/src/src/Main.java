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
	 * @param args
	 *            Argumente werden nicht verarbeitet
	 * 
	 * @author Marco Kinkel
	 */
	public static void main(String[] args) {

		// Read properties from config-files
		Helper.loadConfiguration("/opt/wikidata/config/custom_properties");
		Helper.loadConfiguration("/opt/wikidata/config/db_properties");

		// Initialize and configure logging for periodically println of status
		Helper.configureLogging();
		
		// Delete old logfiles
		EntityTimerProcessor.logger.info("Deleting old logfiles.");
		Helper.deleteOldLogfiles();
		
		// Test DB-Connection
		if(SQLMethods.openSQLconnection() == null){
			EntityTimerProcessor.logger.info("End of program.");
			return;
		};

		// Change schema which will be updated now
		Helper.changeSchemaInProgram();

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
		if (successful) {
			EntityTimerProcessor.logger.info("\n" + processor.personsCount
					+ " persons,\n" + processor.jobsCount + " jobs,\n"
					+ processor.educationInstitutesCount
					+ " education institutes,\n" + processor.citiesCount
					+ " cities,\n" + processor.statesCount + " states,\n"
					+ "...found in " + processor.itemCount + " items.");
			// TODO: When adding new Items
		} else {
			EntityTimerProcessor.logger.info("End of program.");
			return;
		}

		// Items from Wikidata were processed in blocks of a defined amount.
		// The last blocks (with less entries than the maximum amount per block)
		// needs to be converted manually
		processor.convertAllAndCreateSQL();

		EntityTimerProcessor.logger.info("Executing "
				+ SQLMethods.queries.size() + " queries...");

		// Connect to database and execute all saved queries
		successful = SQLMethods.executeQueries();

		// If error occured, end program
		if (successful) {
			EntityTimerProcessor.logger.info("Queries successfully executed.");
		} else {
			EntityTimerProcessor.logger.info("End of program.");
			return;
		}
		
		// Begin of data transformation / cleansing
		EntityTimerProcessor.logger.info("Begin of data transformation and cleansing...");
		
		successful = de.opendata.wikidata_geocode.Geocoder.runGeocoder();
		
		// If error occured, end program
		if (successful) {
			EntityTimerProcessor.logger.info("Data successfully transformated and cleaned.");
		} else {
			EntityTimerProcessor.logger.info("End of program.");
			return;
		}
		
		// Refresh materialized views
		EntityTimerProcessor.logger.info("Refreshing materialized views...");
		
		successful = SQLMethods.refreshViews();
		
		// If error occured, end program
		if (successful) {
			EntityTimerProcessor.logger.info("Views successfully refreshed.");
		} else {
			EntityTimerProcessor.logger.info("End of program.");
			return;
		}

		// After schema is updated, we change the SCHEMA-attribute in the
		// config-file.
		// From then, the user application will use the updated schema
		EntityTimerProcessor.logger
		.info("Changing value of SCHEMA in config-file ...");
		Helper.changeSchemaInConfig();

		EntityTimerProcessor.logger.info("Success! End of program.");

	}
}
