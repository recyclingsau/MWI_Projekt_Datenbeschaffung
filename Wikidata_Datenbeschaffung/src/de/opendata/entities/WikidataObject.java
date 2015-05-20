package de.opendata.entities;

import java.util.HashMap;

import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;

/**
 * Representation of a property or entity from Wikidata
 * Contains the id (as a String because of leading 'P' or 'Q'), Map of description and label by language
 * 
 * @author Marco Kinkel
 * @version 0.1
 */
public class WikidataObject {
	
	public String id;
	public HashMap<String, MonolingualTextValue> desc;
	public HashMap<String, MonolingualTextValue> label;

}
