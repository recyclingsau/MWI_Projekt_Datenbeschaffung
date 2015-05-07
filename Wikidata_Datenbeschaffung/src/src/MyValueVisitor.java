package src;

import java.util.List;
import java.util.AbstractMap.SimpleEntry;

import org.wikidata.wdtk.datamodel.interfaces.DatatypeIdValue;
import org.wikidata.wdtk.datamodel.interfaces.EntityIdValue;
import org.wikidata.wdtk.datamodel.interfaces.GlobeCoordinatesValue;
import org.wikidata.wdtk.datamodel.interfaces.MonolingualTextValue;
import org.wikidata.wdtk.datamodel.interfaces.QuantityValue;
import org.wikidata.wdtk.datamodel.interfaces.StringValue;
import org.wikidata.wdtk.datamodel.interfaces.TimeValue;
import org.wikidata.wdtk.datamodel.interfaces.ValueVisitor;

import entities.ClaimValue;
import entities.Item;

/**
 * "Visits" values of claims and reads (and converts) them depending on their
 * type. Always returns a String.
 * 
 * @author Marco Kinkel
 * @version 0.1
 * 
 * @param <T> 
 */
public class MyValueVisitor<T> implements ValueVisitor<ClaimValue> {

	/**
	 * Visits undefined value
	 * 
	 * @param value Undefined value
	 * @return Always returns "Datatype undefined"
	 */
	@Override
	public ClaimValue visit(DatatypeIdValue value) {
		return new ClaimValue("", Item.Datatype.UNDEFINED);
	}

	/**
	 * Visits EntityIdValue and returns its ID
	 * 
	 * @param value Value that represents an entity from Wikidata
	 * @return ID of the entity as string (with leading Q or P)
	 */
	@Override
	public ClaimValue visit(EntityIdValue value) {
		return new ClaimValue(value.getId(), Item.Datatype.ENTITY);
	}

	/**
	 * Visits GlobeCoordinates and returns its coordinates as string
	 * 
	 * @param value Value that represents global coordinates
	 * @return String like TODO
	 */
	@Override
	public ClaimValue visit(GlobeCoordinatesValue value) {
		return new ClaimValue("" + value.getLatitude() + ", " + value.getLongitude(), Item.Datatype.COORDINATES);
	}

	/**
	 * Visits MonolingualTextValue and returns its content
	 * 
	 * @param value Value that represents a text in a certain language
	 * @return Text in certain language as string
	 */
	@Override
	public ClaimValue visit(MonolingualTextValue value) {
		return new ClaimValue(value.getText(), Item.Datatype.STRING);
	}

	/**
	 * Visits QuantityValue and returns its numeric value as string
	 * 
	 * @param value Value that represents a Quantity
	 * @return Numeric value as string
	 */
	@Override
	public ClaimValue visit(QuantityValue value) {
		return new ClaimValue("" + value.getNumericValue(), Item.Datatype.STRING);
	}

	/**
	 * Visits StringValue and returns its content
	 * 
	 * @param value Value that represents a custom string
	 * @return Content of StringValue
	 */
	@Override
	public ClaimValue visit(StringValue value) {
		return new ClaimValue(value.getString(), Item.Datatype.STRING);
	}

	/**
	 * Visits TimeValue and returns Date
	 * 
	 * @param value Value that represents a date or time
	 * @return Date like YYYY-MM-DD
	 */
	@Override
	public ClaimValue visit(TimeValue value) {

		String day = "" + value.getDay();
		String month = "" + value.getMonth();
		String year = "" + value.getYear();

		if (day.length() == 1) {
			day = "0" + day;
		}
		if (month.length() == 1) {
			month = "0" + month;
		}

		return new ClaimValue(year + "-" + month + "-" + day, Item.Datatype.TIME);
	}
};