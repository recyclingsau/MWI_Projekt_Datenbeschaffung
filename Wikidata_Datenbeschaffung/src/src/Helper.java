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

import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.Sites;
import org.wikidata.wdtk.dumpfiles.DumpProcessingController;

import src.EntityTimerProcessor.TimeoutException;

/**
 * Hilfsklasse für statische Konfigurationsvariablen (TODO: In Datei
 * verschieben) und Logging-Methoden
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
	 * TODO: Zugriff für Administratoranwendung
	 */
	public static final boolean OFFLINE_MODE = true;

	/**
	 * Timeout to abort processing after a short while or 0 to disable timeout.
	 * If set, then the processing will cleanly exit after about this many
	 * seconds, as if the dump file would have ended there. This is useful for
	 * testing (and in particular better than just aborting the program) since
	 * it allows for final processing and proper closing to happen without
	 * having to wait for a whole dump file to process.
	 * 
	 * TODO: Zugriff für Administratoranwendung
	 */
	public static final int TIMEOUT_SEC = 100;

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
	 *         TODO: Ablösen durch eigenen Logger?
	 */
	public static void configureLogging() {
		// Create the appender that will write log messages to the console.
		ConsoleAppender consoleAppender = new ConsoleAppender();

		// Define the pattern of log messages.
		// Insert the string "%c{1}:%L" to also show class name and line.
		String pattern = "%d{yyyy-MM-dd HH:mm:ss} %-5p - %m%n";
		consoleAppender.setLayout(new PatternLayout(pattern));

		// Change to Level.ERROR for fewer messages:
		consoleAppender.setThreshold(Level.INFO);

		consoleAppender.activateOptions();
		Logger.getRootLogger().addAppender(consoleAppender);
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
	 */
	public static void processEntitiesFromWikidataDump(
			EntityDocumentProcessor entityDocumentProcessor) {

		// Controller object for processing dumps
		DumpProcessingController dumpProcessingController = new DumpProcessingController(
				"wikidatawiki");

		dumpProcessingController.setOfflineMode(OFFLINE_MODE);

		// Define directory for the dumps
		// If not exists, a directory "dumpfiles" will be created
		// Standard is user.dir
		// TODO: Als Einstellung für Administrator freischalten
		try {
//			dumpProcessingController.setDownloadDirectory(System
//					.getProperty("user.dir"));
			dumpProcessingController.setDownloadDirectory("C:\\wikidata\\");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Kein Zugriff auf Dumpfiles möglich!");
			// TODO: Kein Zugriff auf Dumpfiles -> Keine Verarbeitung. Abbruch!
		}

		// Download the sites table dump and extract information
		try {
			sites = dumpProcessingController.getSitesInformation();
		} catch (IOException e) {
			e.printStackTrace();
			// TODO: Seiten können nicht geladen werden. Abbruch bei
			// OFFLINE_MODE == true, da ja nicht auf eine Online-Verbindung
			// gewartet werden kann?
		}

		// Also add a timer that reports some basic progress information
		// TODO: Ablösung durch eigenen Timer/Logger?
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
			// Timer created manual timeout. No further errorhandling
		}

		// Print final timer results
		entityTimerProcessor.stop();
	}
}
