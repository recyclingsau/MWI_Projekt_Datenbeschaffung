package entities;

/**
 * Representation of a claim value of wikidata properties
 * 
 * @author Marco Kinkel
 * @version 0.1
 */
public class ClaimValue {
	
	public String value;
	public Item.Datatype type;
	
	public ClaimValue(String value, Item.Datatype type){
		this.value = value;
		this.type = type;
	}

}
