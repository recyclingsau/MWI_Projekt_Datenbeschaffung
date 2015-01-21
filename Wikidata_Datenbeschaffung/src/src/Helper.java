package src;

/*
 * #%L
 * Wikidata Toolkit Examples
 * %%
 * Copyright (C) 2014 Wikidata Toolkit Developers
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.Sites;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;

import src.EntityTimerProcessor.TimeoutException;

/**
 * Hilfsklasse zum lesen statischer Konfigurationsvariablen und Logging-Methoden
 * 
 * Zum größten Teil der Beispielklasse org.wikidata.wdtk.examples.ExampleHelpers
 * des WikidataToolkits v0.3 von Markus Kroetsch entnommen
 *
 * @author Markus Kroetzsch
 * @author Marco Kinkel
 * 
 * @version 0.1
 *
 */
public class Helper {
	/**
	 * If set to true, all example programs will run in offline mode. Only data
	 * dumps that have been downloaded in previous runs will be used.
	 * 
	 * Can be overwritten by custom_properties-file
	 */
	public static boolean OFFLINE_MODE = true;

	/**
	 * Timeout to abort processing after a short while or 0 to disable timeout.
	 * If set, then the processing will cleanly exit after about this many
	 * seconds, as if the dump file would have ended there. This is useful for
	 * testing (and in particular better than just aborting the program) since
	 * it allows for final processing and proper closing to happen without
	 * having to wait for a whole dump file to process.
	 * 
	 * Will be overwritten by custom_properties-file
	 */
	public static int TIMEOUT_SEC = 100;

	public static int UPDATE_INTERVAL_SEC = 10;
	public static String LOGFILE_PATH = "C:\\wikidata\\logfiles\\";
	public static String LOGGING_LEVEL = "INFO";
	public static int LOGFILE_COUNT = 10;
	public static String DUMPFILE_PATH = "C:\\wikidata";
	public static int BLOCK_SIZE = 10000;
	public static String DATABASE_PATH = "localhost";
	public static String DB_USERNAME = "";
	public static String DB_PASSWORD = "";
	public static String SCHEMA = "a";
	public static String VIEWS_PATH = "\\views\\";
	// TODO: When adding a new configuration attribute

	/**
	 * Collects all sites of the Wikimedia Foundation that can be linked in
	 * Wikidata-Entities Read by
	 * {@link SQLMethods#createLinksStatements(String, String, java.util.Map)}
	 */
	public static Sites sites;

	/**
	 * Defines how messages should be logged. This method can be modified to
	 * restrict the logging messages that are shown on the console or to change
	 * their formatting. See the documentation of Log4J for details on how to do
	 * this.
	 * 
	 * @author Markus Kroetzsch
	 * 
	 */
	public static void configureLogging() {
		// Create the appender that will write log messages to the console.
		ConsoleAppender consoleAppender = new ConsoleAppender();
		FileAppender fileAppender = new FileAppender();

		// Get timestamp for logfilepath
		Calendar rightNow = Calendar.getInstance();

		String year = "" + rightNow.get(Calendar.YEAR);
		String month = "" + (rightNow.get(Calendar.MONTH) + 1);
		String date = "" + rightNow.get(Calendar.DATE);
		String hour = "" + rightNow.get(Calendar.HOUR_OF_DAY);
		String minute = "" + rightNow.get(Calendar.MINUTE);
		String second = "" + rightNow.get(Calendar.SECOND);

		if (month.length() == 1)
			month = "0" + month;
		if (date.length() == 1)
			date = "0" + date;
		if (hour.length() == 1)
			hour = "0" + hour;
		if (minute.length() == 1)
			minute = "0" + minute;
		if (second.length() == 1)
			second = "0" + second;

		String timestamp = year + "_" + month + "_" + date + "-" + hour + "_"
				+ minute + "_" + second;

		fileAppender.setFile(LOGFILE_PATH + timestamp + ".txt");

		String pattern;

		// Define the pattern of log messages.
		if (Helper.LOGGING_LEVEL.equalsIgnoreCase(Level.DEBUG.toString())) {
			pattern = "%c{1}:%L %d{yyyy-MM-dd HH:mm:ss} %-5p - %m%n";
		} else {
			// Add name of class and line number
			pattern = "%d{yyyy-MM-dd HH:mm:ss} %-5p - %m%n";
		}
		consoleAppender.setLayout(new PatternLayout(pattern));
		fileAppender.setLayout(new PatternLayout(pattern));

		// Change to Level.ERROR for fewer messages:
		consoleAppender.setThreshold(Level.toLevel(Helper.LOGGING_LEVEL));
		fileAppender.setThreshold(Level.toLevel(Helper.LOGGING_LEVEL));

		consoleAppender.activateOptions();
		fileAppender.activateOptions();
		Logger.getRootLogger().addAppender(consoleAppender);
		Logger.getRootLogger().addAppender(fileAppender);
	}

	/**
	 * Reads configuration-file and writes values into static variables
	 * 
	 * @return Returns true if load was successful
	 */
	public static boolean loadConfiguration(String path) {
		try {
			ConfigScanner parser = new ConfigScanner(path);

			try {
				parser.processLineByLine();
			} catch (IOException e) {
				System.out.println("Error while parsing config-file at "
						+ parser.getFilePath().getAbsolutePath() + ". Abort!");
				return false;
			}
		} catch (FileNotFoundException e) {
			System.out.println("Didn't find config-file expected at " + path
					+ " . Abort!");
			return false;
		}
		return true;
	}

	/**
	 * Changes DB-schema in which the programm will write.
	 */
	public static void changeSchemaInProgram() {
		if (SCHEMA.equalsIgnoreCase("a")) {
			SCHEMA = "b";
		} else if (SCHEMA.equalsIgnoreCase("b")) {
			SCHEMA = "a";
		}
	}

	/**
	 * Changes DB-schema in config file to provide the updated schema to the
	 * user-application.
	 */
	public static void changeSchemaInConfig() {
		File file = new File(Main.DB_PROPERTIES_PATH);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));

			String line;
			String lineWOwhitespace;
			String property;
			String newtext = "";

			try {
				while ((line = br.readLine()) != null) {

					if (!line.equals("") && !line.substring(0, 1).equals("#")) {

						// Remove whitespaces
						lineWOwhitespace = line.replaceAll(" ", "");
						// Get property
						property = lineWOwhitespace.split("=")[0];

						if (property.equalsIgnoreCase("SCHEMA")) {
							newtext += "SCHEMA = "
									+ Helper.SCHEMA.toLowerCase() + "\n";
						} else {
							newtext += line + "\n";
						}
					} else {
						newtext += line + "\n";
					}
				}
				br.close();

				// Write changed config to file
				FileWriter writer = new FileWriter(file);
				writer.write(newtext);
				writer.close();

			} catch (IOException e) {
				System.out
						.println("Error while parsing custom_properties-file at "
								+ file.getAbsolutePath() + ". Abort!");
			}
		} catch (FileNotFoundException e) {
			System.out
					.println("Didn't find custom_properties-file expected at "
							+ file.getAbsolutePath() + ". Abort!");
		}
	}

	/**
	 * Processes all entities in a Wikidata dump using the given entity
	 * processor. By default, the most recent JSON dump will be used. In offline
	 * mode, only the most recent previously downloaded file is considered.
	 *
	 * @param entityDocumentProcessor
	 *            the object to use for processing entities in this dump
	 * 
	 * @author Markus Kroetzsch
	 * @author Marco Kinkel
	 */
	public static boolean processEntitiesFromWikidataDump(
			EntityDocumentProcessor entityDocumentProcessor) {

		// Controller object for processing dumps
		DumpProcessingController dumpProcessingController = new DumpProcessingController(
				"wikidatawiki");

		dumpProcessingController.setOfflineMode(OFFLINE_MODE);

		// Define directory for the dumps
		// If not exists, a directory "dumpfiles" will be created
		// Standard is user.dir
		try {
			// dumpProcessingController.setDownloadDirectory(System
			// .getProperty("user.dir"));
			dumpProcessingController.setDownloadDirectory(DUMPFILE_PATH);
		} catch (IOException e) {
			EntityTimerProcessor.logger
					.error("Can't read dumpfiles! Aborting...");

			return false;
			// TODO: Abbruch nur bei
			// OFFLINE_MODE == true, da ja nicht auf eine Online-Verbindung
			// gewartet werden kann?
		}

		// Download the sites table dump and extract information
		try {
			sites = dumpProcessingController.getSitesInformation();
		} catch (Exception e) {
			EntityTimerProcessor.logger
					.error("Can't read sitefiles! Aborting...");

			return false;
			// TODO: Abbruch nur bei
			// OFFLINE_MODE == true, da ja nicht auf eine Online-Verbindung
			// gewartet werden kann?
		}

		// Also add a timer that reports some basic progress information
		EntityTimerProcessor entityTimerProcessor = new EntityTimerProcessor(
				TIMEOUT_SEC);
		dumpProcessingController.registerEntityDocumentProcessor(
				entityTimerProcessor, null, true);

		// Subscribe to the most recent entity documents of type wikibase item
		dumpProcessingController.registerEntityDocumentProcessor(
				entityDocumentProcessor, null, false);

		try {
			// Start processing (may trigger downloads where needed)
			dumpProcessingController.processMostRecentJsonDump();
			// dumpProcessingController.processAllRecentRevisionDumps();
		} catch (TimeoutException e) {
			EntityTimerProcessor.logger.info("Reached timer of " + TIMEOUT_SEC
					+ " seconds.");
			// Timer created manual timeout. No further errorhandling
		}

		// Print final timer results
		entityTimerProcessor.stop(); // Hammer Time!

		return true;
	}

	public static void deleteOldLogfiles() {
		File logfileFolder = new File(LOGFILE_PATH);
		File[] logFiles = new File[0];

		if (logfileFolder != null) {
			logFiles = logfileFolder.listFiles();

			if (logFiles == null) {
				EntityTimerProcessor.logger.warn("No logfiles found.");
				return;
			}
		}

		// Sort files by extracting and comparing their creation dates
		Arrays.sort(logFiles, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				Date n1 = extractDate(f1.getName());
				Date n2 = extractDate(f2.getName());
				return n1.compareTo(n2);
			}

			@SuppressWarnings("deprecation")
			private Date extractDate(String name) {

				Date d = new Date();
				d.setYear(Integer.parseInt(name.substring(0, 4)));
				d.setMonth(Integer.parseInt(name.substring(5, 7)));
				d.setDate(Integer.parseInt(name.substring(8, 10)));
				d.setHours(Integer.parseInt(name.substring(11, 13)));
				d.setMinutes(Integer.parseInt(name.substring(14, 16)));
				d.setSeconds(Integer.parseInt(name.substring(17, 19)));

				return d;

			}
		});
		for (int count = 0; count < (logFiles.length - Helper.LOGFILE_COUNT); count++) {

			logFiles[count].delete();
		}
	}
}
