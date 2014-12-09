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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.wikidata.wdtk.datamodel.helpers.Datamodel;
import org.wikidata.wdtk.datamodel.interfaces.EntityDocumentProcessor;
import org.wikidata.wdtk.datamodel.interfaces.ItemDocument;
import org.wikidata.wdtk.datamodel.interfaces.ItemIdValue;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.PropertyDocument;
import org.wikidata.wdtk.datamodel.interfaces.SiteLink;
import org.wikidata.wdtk.datamodel.interfaces.Statement;
import org.wikidata.wdtk.datamodel.interfaces.StatementGroup;
import org.wikidata.wdtk.datamodel.interfaces.Value;
import org.wikidata.wdtk.datamodel.interfaces.ValueSnak;
import org.wikidata.wdtk.datamodel.json.jackson.JacksonValueSnak;

import entities.Item;
import entities.Property;

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
	public int itemCount = 0;

	/**
	 * Counts the found jobs in all items
	 */
	public int jobsCount = 0;

	/**
	 * Counts the found persons in all items
	 */
	public int personsCount = 0;

	/**
	 * Counts the found properties in all items
	 */
	public int propertyCount = 0;

	// TODO: Bei Einfügen neuer Items:
	// public int *item*Count = 0;

	/**
	 * Collects all found Persons as instances of
	 * {@link org.wikidata.wdtk.datamodel.interfaces.ItemDocument}.
	 */
	public ArrayList<ItemDocument> persons = new ArrayList<ItemDocument>();

	/**
	 * Collects all found Jobs as instances of
	 * {@link org.wikidata.wdtk.datamodel.interfaces.ItemDocument}.
	 */
	public ArrayList<ItemDocument> jobs = new ArrayList<ItemDocument>();

	/**
	 * Collects all found Properties as instances of
	 * {@link org.wikidata.wdtk.datamodel.interfaces.PropertyDocument}.
	 */
	public ArrayList<PropertyDocument> properties = new ArrayList<PropertyDocument>();

	// TODO: Bei Einfügen neuer Items:
	// public ArrayList<ItemDocument> *item* = new ArrayList<ItemDocument>();

	/**
	 * Filter to check if item is a human
	 */
	static final ItemIdValue humanFilterClass = Datamodel
			.makeWikidataItemIdValue("Q5");

	/**
	 * Filter to check if item is a job
	 */
	static final ItemIdValue jobFilterClass = Datamodel
			.makeWikidataItemIdValue("Q28640");

	// TODO: Bei Einfügen neuer Items:
	// static final ItemIdValue *item*FilterClass = Datamodel
	// .makeWikidataItemIdValue("*item-ID*");

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

		boolean isHuman = false;
		boolean hasAlmaMater = false;
		boolean isJob = false;

		// TODO: Bei Einfügen neuer Items:
		// boolean is*Item* = false;

		// Iterate over all statements of the current item and filter by its
		// properties
		for (StatementGroup statementGroup : itemDocument.getStatementGroups()) {
			switch (statementGroup.getProperty().getId()) {

			case "P69": // = "alma mater"
				hasAlmaMater = true;
				break;

			case "P31": // = "instance of"

				/*
				 * Check by defined filters, if the value of property P31 is a
				 * searched Entity
				 */
				isHuman = containsValue(statementGroup, humanFilterClass);
				isJob = containsValue(statementGroup, jobFilterClass);
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
		if (isHuman && hasAlmaMater) {
			this.personsCount++;
			persons.add(itemDocument);
		}

		// Check if item is job
		if (isJob) {
			this.jobsCount++;
			jobs.add(itemDocument);
		}

		// TODO: Bei Einfügen neuer Items :
		/*
		 * if (isWantedItem) { this.wantedItemCount++; this.printedStatus =
		 * false;
		 * 
		 * // Hinzufügen des Items zu Liste
		 * wantedItemArrayList.add(itemDocument); }
		 */

		// Print status every 100.000 processed items
		// TODO: Anzahl von Administrator einstellen lassen
		if (this.itemCount % 100000 == 0) {
			printStatus();
		}

		/*
		 * Because of heap memory problems we have to process our stored items
		 * now and then. After we converted them into SQL-Statements, we can
		 * clear the Arrays to free memory
		 * 
		 * TODO: Optimieren! Irgendwie klappt die Speicherfreigabe noch nicht
		 * wie geplant. Läuft bei 3GB Heap trotzdem voll!
		 */

		if (persons.size() >= 10000) { // TODO: Größe der Blöcke von
										// Administrator einstellen lassen

			// Status message
			// TODO: In Logger implementieren
			System.out
					.println("10000 Personen erreicht. Erstelle SQL-Befehle!");

			// Convert objects in this.jobs-List to Instances of
			// entities.Item for easier handling.
			//
			// TODO: Eigentlich wird nichts außer den Claims konvertiert.
			// Eventuell kann man diesen einen Methodenaufruf sparen. Allerdings
			// sollten wir eher auf Speicheroptimierung als auf Performance ein
			// Auge haben denn dieser Schritt wird ja nur ein paar mal
			// aufgerufen
			convertItemsAndCreateSQL(persons, "PERSONS");

			// Clear this.persons-List to free memory
			persons.clear();

			// Status message
			// TODO: In Logger implementieren
			System.out.println("Erstellung der SQL-Befehle abgeschlossen!");
		}

		if (jobs.size() >= 10000) { // TODO: Größe der Blöcke von Administrator
									// einstellen lassen

			// Status message
			// TODO: In Logger implementieren
			System.out.println("10000 Jobs erreicht. Erstelle SQL-Befehle!");

			// Convert objects in this.jobs-List to Instances of
			// entities.Item for easier handling.
			//
			// TODO: Wie oben
			convertItemsAndCreateSQL(jobs, "JOBS");

			// Clear this.jobs-List to free memory
			jobs.clear();

			// Status message
			// TODO: In Logger implementieren
			System.out.println("Erstellung der SQL-Befehle abgeschlossen!");
		}

		// TODO: Bei Einfügen neuer Items:
		/*
		 * 
		 * System.out.println("10000 Jobs erreicht. Erstelle SQL-Befehle!");
		 * 
		 * if (*itemsList*.size() >= 10000) {
		 * 
		 * System.out.println("10000 *Items* erreicht. Schreibe in Datenbank!");
		 * 
		 * convertAndWriteItemsToDB(*itemsList*, "*ITEMS (Tabellenname)*");
		 * 
		 * System.out.println("Erstellung der SQL-Befehle abgeschlossen!");
		 */

	}

	/**
	 * Processes items from wikidata dump. Filters them by their properties and
	 * writes them into the static lists of this class. Called indirecly in
	 * {@link Helper#processEntitiesFromWikidataDump(EntityDocumentProcessor)}.
	 * 
	 * @param propertyDocument
	 *            Current property to be read and stored
	 * 
	 * @author Marco Kinkel
	 */
	@Override
	public void processPropertyDocument(PropertyDocument propertyDocument) {

		this.propertyCount++;

		properties.add(propertyDocument);

		propertyDocument.getEntityId().getId();
		propertyDocument.getLabels();
		propertyDocument.getDescriptions();

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
	 * Prints the current status to the system output.
	 * 
	 * @author Markus Kroetzsch
	 * 
	 *         TODO: Überarbeiten... Evtl in Logger integrieren!
	 */
	private void printStatus() {
		System.out.println("*** Found " + personsCount + " Persons and "
				+ jobsCount + " Jobs within " + itemCount + " items.");
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

		// Collection to store the converted Item-Objects
		ArrayList<Item> itemCollection = new ArrayList<Item>();

		// Iterate over all itemDocuments
		while (itemDocuments.size() != 0) {

			// Free memory at every step
			ItemDocument itemDoc = itemDocuments.remove(0);

			// Convert ItemDocument to Item
			i = convertToItem(itemDoc);

			// Add Item to Collection which will be given to the SQL creation
			// method after the loop
			itemCollection.add(i);
		}

		// Create and store SQL statements to store the converted Items in the
		// DB later on
		SQLMethods.createAllSQLstatements(itemCollection, tableName);

	}

	/**
	 * Converts {@link org.wikidata.wdtk.datamodel.interfaces.PropertyDocument}s
	 * to {@link entities.Property}s, creates and stores SQL-statements to write
	 * them into the DB later on.
	 * 
	 * @param propertyDocuments
	 *            List of propertyDocuments to be converted
	 * 
	 * @author Marco Kinkel
	 */
	protected void convertPropertiesAndCreateSQL(
			ArrayList<PropertyDocument> propertyDocuments) {

		Property p = null;

		// Collection to store the converted Property-Objects
		ArrayList<Property> propertyCollection = new ArrayList<Property>();

		// Iterate over all propertyDocuments
		while (propertyDocuments.size() != 0) {

			// Free memory at every step
			PropertyDocument propertyDoc = propertyDocuments.remove(0);

			// Convert PropertyDocument to Property
			p = convertToProperty(propertyDoc);

			// Add Property to Collection which will be given to the SQL
			// creation
			// method after the loop
			propertyCollection.add(p);

		}

		// Create and store SQL statements to store the converted properties in
		// the
		// DB later on
		SQLMethods.createPropertiesStatements(propertyCollection);

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
		// TODO: Bei neuem Item:
		/*
		 * convertItemsAndCreateSQL(*item-List*, *ITEMS (Tabellenname)*);
		 */

		convertPropertiesAndCreateSQL(properties);
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
	private Item convertToItem(ItemDocument itemDoc) {

		Iterator<Statement> statementIterator = itemDoc.getAllStatements();
		HashMap<String, String> claims = new HashMap<String, String>();

		// Iterate through statements to create map of claims
		while (statementIterator.hasNext()) {
			Statement statement = statementIterator.next();

			// Get ID of property (P xyz)
			String propId = statement.getClaim().getMainSnak().getPropertyId()
					.getId();
			String value = null;
			JacksonValueSnak snak = null;

			// Try to convert value of property to JacksonValue. If it fails, it
			// has no or an unknown value
			try {
				snak = (JacksonValueSnak) statement.getClaim().getMainSnak();

				value = snak.getDatavalue()
						.accept(new MyValueVisitor<String>());

			} catch (ClassCastException e) {
				// Value is either unknown or not existent, so value gets null

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

			claims.put(propId, value);

		}

		// Create Item-Object and fill attributes
		Item i = new Item();

		i.id = itemDoc.getItemId().getId();
		i.alias = new HashMap<String, List<MonolingualTextValue>>(
				itemDoc.getAliases());
		i.label = new HashMap<String, MonolingualTextValue>(itemDoc.getLabels());
		i.desc = new HashMap<String, MonolingualTextValue>(
				itemDoc.getDescriptions());
		i.link = new HashMap<String, SiteLink>(itemDoc.getSiteLinks());
		i.claim = claims;

		return i;
	}

	/**
	 * Converts {@link org.wikidata.wdtk.datamodel.interfaces.PropertyDocument} to {@link entities.Property}.
	 *  
	 * @param propertyDoc The PropertyDocument to be converted
	 * @return Converted Property-Object
	 * 
	 * @author Marco Kinkel
	 */
	private Property convertToProperty(PropertyDocument propertyDoc) {

		Property p = new Property();

		p.id = propertyDoc.getEntityId().getId();
		p.label = new HashMap<String, MonolingualTextValue>(
				propertyDoc.getLabels());
		p.desc = new HashMap<String, MonolingualTextValue>(
				propertyDoc.getDescriptions());

		return p;
	}

}
