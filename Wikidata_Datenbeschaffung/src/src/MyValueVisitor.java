package src;

import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue;
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.QuantityValue;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;
import org.wikidata.wdtk.datamodel.interfaces.ValueVisitor;

/**
 * "Visits" values of claims and reads (and converts) them depending on their
 * type. Always returns a String.
 * 
 * @author Marco Kinkel
 * @version 0.1
 * 
 * @param <T> 
 */
public class MyValueVisitor<T> implements ValueVisitor<String> {

	/**
	 * Visits undefined value
	 * 
	 * @param value Undefined value
	 * @return Always returns "Datatype undefined"
	 */
	@Override
	public String visit(DatatypeIdValue value) {
		return "Datatype undefined";
	}

	/**
	 * Visits EntityIdValue and returns its ID
	 * 
	 * @param value Value that represents an entity from Wikidata
	 * @return ID of the entity as string (with leading Q or P)
	 */
	@Override
	public String visit(EntityIdValue value) {
		return value.getId();
	}

	/**
	 * Visits GlobeCoordinates and returns its coordinates as string
	 * 
	 * @param value Value that represents global coordinates
	 * @return String like TODO
	 */
	@Override
	public String visit(GlobeCoordinatesValue value) {
		return "" + value.getLatitude() + ", " + value.getLongitude();
	}

	/**
	 * Visits MonolingualTextValue and returns its content
	 * 
	 * @param value Value that represents a text in a certain language
	 * @return Text in certain language as string
	 */
	@Override
	public String visit(MonolingualTextValue value) {
		return value.getText();
	}

	/**
	 * Visits QuantityValue and returns its numeric value as string
	 * 
	 * @param value Value that represents a Quantity
	 * @return Numeric value as string
	 */
	@Override
	public String visit(QuantityValue value) {
		return "" + value.getNumericValue();
	}

	/**
	 * Visits StringValue and returns its content
	 * 
	 * @param value Value that represents a custom string
	 * @return Content of StringValue
	 */
	@Override
	public String visit(StringValue value) {
		return value.getString();
	}

	/**
	 * Visits TimeValue and returns Date
	 * 
	 * @param value Value that represents a date or time
	 * @return Date like YYYY-MM-DD
	 */
	@Override
	public String visit(TimeValue value) {

		String day = "" + value.getDay();
		String month = "" + value.getMonth();
		String year = "" + value.getYear();

		if (day.length() == 1) {
			day = "0" + day;
		}
		if (month.length() == 1) {
			month = "0" + month;
		}

		return year + "-" + month + "-" + day;
	}
};