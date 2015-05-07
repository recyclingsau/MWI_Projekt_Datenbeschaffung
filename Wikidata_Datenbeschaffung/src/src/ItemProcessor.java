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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.SiteLink;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.TermedDocument;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;
import org.wikidata.wdtk.datamodel.json.jackson.JacksonValueSnak;

import entities.ClaimValue;
import entities.Item;
import entities.WikidataObject;

/**
 * Implementation of a
 * {@link org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor}
 * 
 * Reads, filters and converts a Wikidata dump and calls methods to create
 * SQL-Statements to save its items and properties persistently
 * 
 * @author Marco Kinkel
 * @author Markus Kroetzsch
 * @version 0.1
 */
public class ItemProcessor implements EntityDocumentProcessor {

	/**
	 * Counts the processed items (Q and P)
	 */
	public int itemCount;

	/**
	 * Counts the found jobs in all items
	 */
	public int jobsCount;

	/**
	 * Counts the found persons in all items
	 */
	public int personsCount;

	/**
	 * Counts the found education institutes in all items
	 */
	public int educationInstitutesCount;

	/**
	 * Counts the found cities in all items
	 */
	public int citiesCount;

	/**
	 * Counts the found states in all items
	 */
	public int statesCount;

	/**
	 * Counts the found GUI-Texts in all items
	 */
	public int guiTextsCount;

	/**
	 * Collects all found Persons as instances of
	 * {@link org.wikidata.wdtk.datamodel.interfaces.ItemDocument}.
	 */
	public ArrayList<ItemDocument> persons;

	/**
	 * Collects all found Jobs as instances of
	 * {@link org.wikidata.wdtk.datamodel.interfaces.ItemDocument}.
	 */
	public ArrayList<ItemDocument> jobs;

	/**
	 * Collects all found education institues as instances of
	 * {@link org.wikidata.wdtk.datamodel.interfaces.ItemDocument}.
	 */
	public ArrayList<ItemDocument> educationInstitutes;

	/**
	 * Collects all found cities as instances of
	 * {@link org.wikidata.wdtk.datamodel.interfaces.ItemDocument}.
	 */
	public ArrayList<ItemDocument> cities;

	/**
	 * Collects all found states as instances of
	 * {@link org.wikidata.wdtk.datamodel.interfaces.ItemDocument}.
	 */
	public ArrayList<ItemDocument> states;

	/**
	 * Collects all found Properties as instances of
	 * {@link org.wikidata.wdtk.datamodel.interfaces.PropertyDocument}.
	 */
	public ArrayList<TermedDocument> guiElements;

	// TODO: Bei Einfügen neuer Items:
	// public ArrayList<ItemDocument> *item* = new
	// ArrayList<ItemDocument>();

	/**
	 * Collects Strings of all processed Items
	 */
	private TreeSet<String> processedItems;

	/**
	 * Filter to check if item is a job
	 */
	final ItemIdValue jobFilterClass;

	/**
	 * Filter to check if item is a university
	 */
	final ArrayList<ItemIdValue> educationInstituteFilterList;

	/**
	 * Filter to check if item is a city
	 */
	final ItemIdValue cityFilterClass;

	/**
	 * Filter to check if item is a state/county
	 */
	final ItemIdValue stateFilterClass_1;
	final ItemIdValue stateFilterClass_2;

	// TODO: Bei Einfügen neuer Items:
	// static final ItemIdValue *item*FilterClass = Datamodel
	// .makeWikidataItemIdValue("*item-ID*");

	/**
	 * Map to store the needed property-IDs for each category of items
	 */
	private HashMap<String, ArrayList<String>> neededProperties;

	/**
	 * List to store the needed property-IDs for persons
	 */
	private ArrayList<String> propertiesForPersons;

	/**
	 * List to store the needed property-IDs for jobs
	 */
	private ArrayList<String> propertiesForJobs;

	/**
	 * List to store the needed property-IDs for educationInstitutes
	 */
	private ArrayList<String> propertiesForEducationInstitutes;

	/**
	 * List to store the needed property-IDs for cities
	 */
	private ArrayList<String> propertiesForCities;

	/**
	 * List to store the needed property-IDs for states
	 */
	private ArrayList<String> propertiesForStates;

	/**
	 * Constructur Initializes several objects
	 */
	public ItemProcessor() {

		itemCount = 0;
		jobsCount = 0;
		personsCount = 0;
		educationInstitutesCount = 0;
		citiesCount = 0;
		statesCount = 0;
		guiTextsCount = 0;

		persons = new ArrayList<ItemDocument>();
		jobs = new ArrayList<ItemDocument>();
		educationInstitutes = new ArrayList<ItemDocument>();
		cities = new ArrayList<ItemDocument>();
		states = new ArrayList<ItemDocument>();
		guiElements = new ArrayList<TermedDocument>();

		processedItems = new TreeSet<String>();

		jobFilterClass = Datamodel.makeWikidataItemIdValue("Q28640");

		educationInstituteFilterList = new ArrayList<ItemIdValue>();

		cityFilterClass = Datamodel.makeWikidataItemIdValue("Q515");

		stateFilterClass_1 = Datamodel.makeWikidataItemIdValue("Q7275");
		stateFilterClass_2 = Datamodel.makeWikidataItemIdValue("Q6256");

		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q136822")); // Höhere Bildung
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q38723")); // Hochschule
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q3918")); // Universität
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q65379")); // Virginia Polytechnic
														// Institute and State
														// University
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q847027")); // Grande école
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q856234")); // Universitätsbibliothek
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q3592593")); // Fakultät für
														// Ingenieurwissenschaften
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q1365560")); // Fachhochschule
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q154773")); // Jeschiwa
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q170087")); // Heimvolkshochschule
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q346549")); // Technische Hochschule
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q383092")); // Kunsthochschule
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q811093")); // Bauakademie
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q1143635")); // Business School
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q1916585")); // Medizinische
														// Universität
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q2120173")); // Pädagogische
														// Hochschule
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q2983901")); // community colleges in
														// the United States
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q3520135")); // Deemed university
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q4129814")); // ??
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q11391922")); // public policy school
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q15456933")); // Ensino politécnico
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q15971964")); // Wirtschaftshochschule
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q265662")); // National university
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q875538")); // Öffentliche Universität
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q902104")); // Privatuniversität
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q987075")); // New Universities
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q1075106")); // State university
														// system 
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q1321960")); // Law School
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q1371037")); // Technische Universität
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q1407393")); // Fernuniversität
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q1755248")); // Virtuelle Universität
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q1767829")); // Volluniversität
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q2120466")); // Päpstliche Universität
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q3354859")); // collegiate university
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q3551775")); // Universität
														// (Frankreich)
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q3998530")); // ancient university of
														// Scotland
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q5690751")); // Universidad Laboral
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q7896281")); // University of Science
														// and Technology
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q15936437")); // Forschungsuniversität 
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q135436")); // École normale
														// supérieure
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q3578166")); // École nationale
														// supérieure
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q3255372")); // List of engineering
														// school in France
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q482329")); // An-Institut
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q9347372")); // Studium 
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q11862829")); // Einzelwissenschaft 
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q16861871")); // Ausbildung 
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q180958")); // Fakultät 
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q209465")); // Campus 
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q1706722")); // Wissenschaftlicher
														// Mitarbeiter
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q2376292")); // specialist degree
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q3592627")); // Höhere Bildung in
														// Frankreich
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q5819942")); // Höhere Bildung in
														// Chile
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q14282628")); // Höhere Bildung in
														// Kolumbien
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q17506135")); // Höhere Bildung in
														// Ungarn
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q1270577")); // Gesamthochschule 
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q5741962")); // Kategorie: Hochschule
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q5851345")); // Kategorie: Universität
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q8882486")); // Kategorie: University
														// Images
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q12885307")); // Technological
														// Educational Institute
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q7894996")); // University college
		educationInstituteFilterList.add(Datamodel
				.makeWikidataItemIdValue("Q9166713")); // Tertiärer
														// Bildungsbereich

		// Fill tables of needed properties

		propertiesForPersons = new ArrayList<String>();
		propertiesForJobs = new ArrayList<String>();
		propertiesForEducationInstitutes = new ArrayList<String>();
		propertiesForCities = new ArrayList<String>();
		propertiesForStates = new ArrayList<String>();

		propertiesForPersons.add("P69"); // Alma Mater
		propertiesForPersons.add("P106"); // Tätigkeit

		propertiesForEducationInstitutes.add("P571"); // Gründungsdatum
		propertiesForEducationInstitutes.add("P1128"); // Anzahl Mitarbeiter
		propertiesForEducationInstitutes.add("P669"); // Straße
		propertiesForEducationInstitutes.add("P670"); // Hausnummer
		propertiesForEducationInstitutes.add("P969"); // Adresse
		propertiesForEducationInstitutes.add("P17"); // Land -> Eigene Entity
		propertiesForEducationInstitutes.add("P281"); // PLZ
		propertiesForEducationInstitutes.add("P1329"); // Tel
		propertiesForEducationInstitutes.add("P856"); // HP-Link
		propertiesForEducationInstitutes.add("P854"); // URL
		propertiesForEducationInstitutes.add("P968"); // E-Mail
		propertiesForEducationInstitutes.add("P625"); // Geo-Koordinaten
		propertiesForEducationInstitutes.add("P276"); // Ort
		

		propertiesForCities.add("P17"); // Staat
		
		neededProperties = new HashMap<String, ArrayList<String>>();

		neededProperties.put("PERSONS", propertiesForPersons);
		neededProperties.put("JOBS", propertiesForJobs);
		neededProperties.put("EDUCATIONINSTITUTES",
				propertiesForEducationInstitutes);
		neededProperties.put("CITIES", propertiesForCities);
		neededProperties.put("STATES", propertiesForStates);

	}

	/**
	 * Processes entities (not properties!) from wikidata dump. Filters them by
	 * their properties and writes them into the static lists of this class.
	 * Called by
	 * {@link Helper#processEntitiesFromWikidataDump(EntityDocumentProcessor)}.
	 * 
	 * @param itemDocument
	 *            Current entity to be read, filtered and stored
	 * 
	 * @author Marco Kinkel
	 */
	@Override
	public void processItemDocument(ItemDocument itemDocument) {
		this.itemCount++;

		boolean isPerson = false;
		boolean isJob = false;
		boolean isEducationInstitute = false;
		boolean isCity = false;
		boolean isState = false;
		boolean hasCoordinates = false;

		// TODO: Bei Einfügen neuer Items:
		// boolean is*Item* = false;

		// Read ID of entity and check, if it's needed in the gui texts. Add it
		// to the List then.
		String itemID = itemDocument.getEntityId().getId();

		switch (itemID) {
		case "Q515": // City
		case "Q215627": // Person
		case "Q82799": // Name
		case "Q2385804": // Educational institution
		case "Q508719": // Alumni
		case "Q319608": // Address
			// TODO: Bei Einfügen neuer GUI-Texte mit Q-ID
			guiTextsCount++;
			guiElements.add(itemDocument);
			break;
		}

		// Iterate over all statements of the current item and filter by its
		// properties
		for (StatementGroup statementGroup : itemDocument.getStatementGroups()) {
			switch (statementGroup.getProperty().getId()) {

			case "P69": // = "alma mater"
				isPerson = true;
				break;

			case "P625": // = "coordinate location"
				hasCoordinates = true;
				break;

			case "P31": // = "instance of"

				/*
				 * Check by defined filters, if the value of property P31 is a
				 * searched Entity
				 */
				isJob = containsValue(statementGroup, jobFilterClass);
				isCity = containsValue(statementGroup, cityFilterClass);
				isState = containsValue(statementGroup, stateFilterClass_1)
						|| containsValue(statementGroup, stateFilterClass_2);
				isEducationInstitute = hasCoordinates
						&& containsValue(statementGroup,
								educationInstituteFilterList);
				break;

			/*
			 * TODO: Hier müssen alle anderen Bedingungen für Items abgefragt
			 * werden
			 */
			}
		}

		/*
		 * After checking all statements, decide if the current item needs to be
		 * stored and do so
		 */

		// Check if item is person
		if (isPerson) {
			personsCount++;
			persons.add(itemDocument);
		}

		// Check if item is job
		if (isJob) {
			jobsCount++;
			jobs.add(itemDocument);
		}

		// Check if item is education institue
		if (isEducationInstitute) {
			educationInstitutesCount++;
			educationInstitutes.add(itemDocument);
		}

		// Check if item is city
		if (isCity) {
			citiesCount++;
			cities.add(itemDocument);
		}

		// Check if item is state
		if (isState) {
			statesCount++;
			states.add(itemDocument);
		}

		// TODO: Bei Einfügen neuer Items :
		/*
		 * if (isWantedItem) { this.wantedItemCount++; this.printedStatus =
		 * false;
		 * 
		 * // Hinzufügen des Items zu Liste
		 * wantedItemArrayList.add(itemDocument); }
		 */

		/*
		 * Because of heap memory problems we have to process our stored items
		 * now and then. After we converted them into SQL-Statements, we can
		 * clear the Arrays to free memory
		 * 
		 * TODO: Optimieren! Irgendwie klappt die Speicherfreigabe noch nicht
		 * wie geplant. Läuft bei 3GB Heap trotzdem voll!
		 */

		if (persons.size() + jobs.size() + educationInstitutes.size()
				+ cities.size() + states.size() >= Helper.BLOCK_SIZE) {

			// Status message
			EntityTimerProcessor.logger.info("Reached " + Helper.BLOCK_SIZE
					+ " hits. Create SQL-statements...");

			// Convert found objects to Instances of
			// entities.Item for easier handling.
			//
			// TODO: Eigentlich wird nichts außer den Claims konvertiert.
			// Eventuell kann man diesen einen Methodenaufruf sparen. Allerdings
			// sollten wir eher auf Speicheroptimierung als auf Performance ein
			// Auge haben denn dieser Schritt wird ja nur ein paar mal
			// aufgerufen
			convertItemsAndCreateSQL(persons, "PERSONS");
			convertItemsAndCreateSQL(jobs, "JOBS");
			convertItemsAndCreateSQL(educationInstitutes, "EDUCATIONINSTITUTES");
			convertItemsAndCreateSQL(cities, "CITIES");
			convertItemsAndCreateSQL(states, "STATES");
			// TODO: Bei Einfügen neuer Items

			// Clear lists to free memory
			persons.clear();
			jobs.clear();
			educationInstitutes.clear();
			cities.clear();
			states.clear();
			// TODO: Bei Einfügen neuer Items

			// Status message
			EntityTimerProcessor.logger
					.info("Creation of SQL-Statements successful!");
		}
	}

	/**
	 * Processes properties from wikidata dump. Adds them to
	 * {@link ItemProcessor#guiElements} if they are needed in the GUI. Called
	 * in
	 * {@link Helper#processEntitiesFromWikidataDump(EntityDocumentProcessor)}.
	 * 
	 * @param propertyDocument
	 *            Current property to be read and stored
	 * 
	 * @author Marco Kinkel
	 */
	@Override
	public void processPropertyDocument(PropertyDocument propertyDocument) {

		guiTextsCount++;

		// Read ID of property and check, if it's needed in the gui texts. Add
		// it to the List then.
		String propertyId = propertyDocument.getEntityId().getId();
		switch (propertyId) {
		case "P17": // State
		case "P106": // Job
		case "P571": // Year of foundation
		case "P1329": // Phone
		case "P968": // E-Mail
			// TODO: Bei Einfügen neuer GUI-Texte mit P-ID

			guiElements.add(propertyDocument);
		}

	}

	/**
	 * Checks if the given group of statements contains the given value as the
	 * value of a main snak of some statement.
	 *
	 * @param statementGroup
	 *            the statement group to scan
	 * @param value
	 *            the value to scan for
	 * @return true if value was found
	 * 
	 * @author Markus Kroetzsch
	 */
	private boolean containsValue(StatementGroup statementGroup, Value value) {
		// Iterate over all statements
		for (Statement s : statementGroup.getStatements()) {

			// Find the main claim and check if it has a value
			if (s.getClaim().getMainSnak() instanceof ValueSnak) {

				Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
				// Check if the value is an ItemIdValue
				if (value.equals(v)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the given group of statements contains the given value as the
	 * value of a main snak of some statement.
	 *
	 * @param statementGroup
	 *            the statement group to scan
	 * @param value
	 *            the value to scan for
	 * @return true if value was found
	 * 
	 * @author Markus Kroetzsch
	 * @author Marco Kinkel
	 */
	private boolean containsValue(StatementGroup statementGroup,
			ArrayList<ItemIdValue> valueList) {
		// Iterate over all statements
		for (Statement s : statementGroup.getStatements()) {

			// Find the main claim and check if it has a value
			if (s.getClaim().getMainSnak() instanceof ValueSnak) {

				Value v = ((ValueSnak) s.getClaim().getMainSnak()).getValue();
				// Check if the value is an ItemIdValue

				for (Value value : valueList) {
					if (value.equals(v)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Converts {@link org.wikidata.wdtk.datamodel.interfaces.ItemDocument}s to
	 * {@link entities.Item}s, creates and stores SQL-statements to write them
	 * into the DB later on.
	 * 
	 * @param itemDocuments
	 *            List of itemDocuments to be converted
	 * @param tableName
	 *            Name of table for creation of SQL-statements
	 * 
	 * @author Marco Kinkel
	 */
	protected void convertItemsAndCreateSQL(
			ArrayList<ItemDocument> itemDocuments, String tableName) {

		Item i = null;
		String itemId;

		// Collection to store the converted Item-Objects
		ArrayList<Item> itemCollection = new ArrayList<Item>();

		// Iterate over all itemDocuments
		while (itemDocuments.size() != 0) {

			// Free memory at every step
			ItemDocument itemDoc = itemDocuments.remove(0);

			itemId = itemDoc.getEntityId().getId();

			// Check if Item has already been processed in a block before
			// This shouldn't happen, because it would mean that it's more than
			// once in the dumpfile. Nevertheless it happens. Good job wikidata!
			if (processedItems.contains(itemId)) {
				EntityTimerProcessor.logger
						.warn("Jumped over duplicate entry with ID " + itemId);
			} else {

				// Add Item-ID to treeSet to make sure, it's already been
				// processed
				processedItems.add(itemId);

				// Convert ItemDocument to Item
				i = convertToItem(itemDoc, tableName);

				// Add Item to Collection which will be given to the SQL
				// creation
				// method after the loop
				itemCollection.add(i);
			}

		}

		// Create and store SQL statements to store the converted Items in the
		// DB later on
		SQLMethods.createAllSQLstatements(itemCollection, tableName);

	}

	/**
	 * Converts {@link org.wikidata.wdtk.datamodel.interfaces.gui-textDocument}s
	 * to {@link entities.WikidataObject}s, creates and stores SQL-statements to
	 * write them into the DB later on.
	 * 
	 * @param termedDocuments
	 *            List of gui-textDocuments to be converted
	 * 
	 * @author Marco Kinkel
	 */
	protected void convertGuiElementsAndCreateSQL(
			ArrayList<TermedDocument> termedDocuments) {

		WikidataObject obj = null;

		// Collection to store the converted gui-text-Objects
		ArrayList<WikidataObject> wikidataObjectCollection = new ArrayList<WikidataObject>();

		// Iterate over all gui-textDocuments
		while (termedDocuments.size() != 0) {

			// Free memory at every step
			TermedDocument termedDoc = termedDocuments.remove(0);

			// Convert gui-textDocument to gui-text
			obj = convertToWikidataObject(termedDoc);

			// Add gui-text to Collection which will be given to the SQL
			// creation
			// method after the loop
			wikidataObjectCollection.add(obj);

		}

		// Create and store SQL statements to store the converted properties in
		// the
		// DB later on
		SQLMethods.createWikidataObjectStatements(wikidataObjectCollection);

	}

	/**
	 * Converts all not yet converted items and properties. Only gets called one
	 * time by main-method. Because of block-by-block processing we need this
	 * method to process all remaining items and properties
	 * 
	 * @author Marco Kinkel
	 */
	protected void convertAllAndCreateSQL() {

		convertItemsAndCreateSQL(persons, "PERSONS");

		convertItemsAndCreateSQL(jobs, "JOBS");
		convertItemsAndCreateSQL(educationInstitutes, "EDUCATIONINSTITUTES");
		convertItemsAndCreateSQL(cities, "CITIES");
		convertItemsAndCreateSQL(states, "STATES");
		// TODO: Bei neuem Item:
		/*
		 * convertItemsAndCreateSQL(*item-List*, *ITEMS (Tabellenname)*);
		 */

		convertGuiElementsAndCreateSQL(guiElements);
	}

	/**
	 * Converts {@link org.wikidata.wdtk.datamodel.interfaces.ItemDocument} to
	 * {@link entities.Item}.
	 * 
	 * @param itemDoc
	 *            ItemDocument to be converted
	 * @return Converted Item-Object
	 * 
	 * @author Marco Kinkel
	 */
	private Item convertToItem(ItemDocument itemDoc, String tableName) {

		Iterator<Statement> statementIterator = itemDoc.getAllStatements();
		HashMap<String, List<ClaimValue>> claims = new HashMap<String, List<ClaimValue>>();
		List<ClaimValue> valueList;
		
		HashMap<String, String> valueMap = new HashMap<String, String>();

		// Get reference of needed Properties
		ArrayList<String> refToNeededProperties = neededProperties
				.get(tableName);

		// Create Item-Object and fill attributes
		Item i = new Item();

		i.id = itemDoc.getItemId().getId();
		i.alias = new HashMap<String, List<MonolingualTextValue>>(
				itemDoc.getAliases());
		i.label = new HashMap<String, MonolingualTextValue>(itemDoc.getLabels());
		i.desc = new HashMap<String, MonolingualTextValue>(
				itemDoc.getDescriptions());
		i.link = new HashMap<String, SiteLink>(itemDoc.getSiteLinks());

		// Iterate through statements to create map of claims
		while (statementIterator.hasNext()) {
			Statement statement = statementIterator.next();

			// Get ID of property (P xyz)
			String propId = statement.getClaim().getMainSnak().getPropertyId()
					.getId();

			// Check if property is needed in database
			if (refToNeededProperties.contains(propId)) {

				ClaimValue value = null;
				JacksonValueSnak snak = null;

				// Try to convert value of property to JacksonValue. If it
				// fails, it
				// has no or an unknown value
				try {
					snak = (JacksonValueSnak) statement.getClaim()
							.getMainSnak();

					value = snak.getDatavalue().accept(
							new MyValueVisitor<ClaimValue>());
				

				} catch (ClassCastException e) {
					// Value is either unknown or not existent, so value gets
					// null

					value = null;

					// Optional: Possibility to decide between no and unknown
					// value:
					/*
					 * try { // Try casting value to "No value" // If cast is
					 * successful, write "Kein Wert" to value JacksonNoValueSnak
					 * noValSnak = (JacksonNoValueSnak) statement
					 * .getClaim().getMainSnak(); value = "Kein Wert"; } catch
					 * (ClassCastException e1) { value = "Unbekannter Wert"; }
					 */
				}

				// Get already saved values of the same properties and add the
				// current one
				valueList = claims.get(propId);

				if (valueList == null) {
					valueList = new ArrayList<ClaimValue>();
				}
				valueList.add(value);

				claims.put(propId, valueList);
			}

		}

		i.claim = claims;

		return i;
	}

	/**
	 * Converts {@link org.wikidata.wdtk.datamodel.interfaces.TermedDocument} to
	 * {@link entities.WikidataObject}.
	 * 
	 * @param termedDoc
	 *            The PropertyDocument to be converted
	 * @return Converted Property-Object
	 * 
	 * @author Marco Kinkel
	 */
	private WikidataObject convertToWikidataObject(TermedDocument termedDoc) {

		WikidataObject p = new WikidataObject();

		p.id = termedDoc.getEntityId().getId();
		p.label = new HashMap<String, MonolingualTextValue>(
				termedDoc.getLabels());
		p.desc = new HashMap<String, MonolingualTextValue>(
				termedDoc.getDescriptions());

		return p;
	}

}
