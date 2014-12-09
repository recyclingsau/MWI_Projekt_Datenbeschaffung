package entities;

import java.util.HashMap;
import java.util.List;

import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.SiteLink;

/**
 * Representation of an entity from Wikidata with Q-ID
 * Contains the id (as a String because of leading 'Q'), Map of Claims (Property-Value-Pairs; both as Strings), 
 * Map of aliases by language, Map of description and label by language, Map of URLs to other Wikis
 * 
 * @author Marco Kinkel
 * @version 0.1
 */
public class Item {

	public String id;
	public HashMap<String, String> claim;
	public HashMap<String, List<MonolingualTextValue>> alias;
	public HashMap<String, MonolingualTextValue> desc;
	public HashMap<String, SiteLink> link;
	public HashMap<String, MonolingualTextValue> label;
	
}
