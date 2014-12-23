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
import java.io.IOException;
import java.util.Calendar;

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
	 * Can be overwritten by custom_properties-file
	 */
	public static int TIMEOUT_SEC = 100;

	public static int UPDATE_INTERVAL_SEC = 10;
	public static String LOGFILE_PATH = "C:\\wikidata\\logfiles";
	public static String LOGGING_LEVEL = "INFO";
	public static String DUMPFILE_PATH = "C:\\wikidata";
	public static int BLOCK_SIZE = 10000;
	public static String DATABASE_PATH = "localhost";
	public static String DB_USERNAME = "";
	public static String DB_PASSWORD = "";

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
		String month = "" + rightNow.get(Calendar.MONTH);
		String date = "" + rightNow.get(Calendar.DATE);
		String hour = "" + rightNow.get(Calendar.HOUR);
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

		String timestamp = year + "_" + month + "_" + date + "-" + hour + "_" + minute + "_" + second;

		fileAppender.setFile(LOGFILE_PATH + "\\" + timestamp + ".txt");

		String pattern;

		// Define the pattern of log messages.
		if (Helper.LOGGING_LEVEL.equalsIgnoreCase(Level.INFO.toString())) {
			pattern = "%d{yyyy-MM-dd HH:mm:ss} %-5p - %m%n";
		} else {
			// Add name of class and line number
			pattern = "%c{1}:%L %d{yyyy-MM-dd HH:mm:ss} %-5p - %m%n";
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
	 */
	public static void loadConfiguration() {
		File file = new File("./custom_properties");
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));

			String line;
			String attribute;
			String value;
			try {
				while ((line = br.readLine()) != null) {

					if (!line.equals("") && !line.substring(0, 2).equals("--")) {

						// Remove whitespaces
						line = line.replaceAll(" ", "");
						attribute = line.split("=")[0];
						value = line.split("=")[1];

						switch (attribute) {
						case "UPDATE_INTERVAL_SEC":
							UPDATE_INTERVAL_SEC = Integer.parseInt(value);
							break;
						case "LOGFILE_PATH":
							LOGFILE_PATH = value;
							break;
						case "LOGGING_LEVEL":
							LOGGING_LEVEL = value;
							break;
						case "OFFLINE_MODE":
							OFFLINE_MODE = Boolean.parseBoolean(value);
							break;
						case "TIMEOUT_SEC":
							TIMEOUT_SEC = Integer.parseInt(value);
							break;
						case "DUMPFILE_PATH":
							DUMPFILE_PATH = value;
							break;
						case "BLOCK_SIZE":
							BLOCK_SIZE = Integer.parseInt(value);
							break;
						case "DATABASE_PATH":
							DATABASE_PATH = value;
							break;
						case "DB_USERNAME":
							DB_USERNAME = value;
							break;
						case "DB_PASSWORD":
							DB_PASSWORD = value;
							break;
						}
					}
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO: Abbruch
			System.out
					.println("custom_properties-Datei nicht gefunden. Abbruch!");
			e.printStackTrace();
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
				entityDocumentProcessor, null, true);

		try {
			// Start processing (may trigger downloads where needed)
			dumpProcessingController.processMostRecentJsonDump();
		} catch (TimeoutException e) {
			EntityTimerProcessor.logger.info("Reached timer of " + TIMEOUT_SEC
					+ " seconds.");
			// Timer created manual timeout. No further errorhandling
		}

		// Print final timer results
		entityTimerProcessor.stop(); // Hammer Time!

		return true;
	}
}
