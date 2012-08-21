package au.org.ala.fielddata.mobile.model;


public class RecordProperty extends Attribute {

	public int id;
	
	public AttributeType getType() {
		return AttributeType.fromCode(name);
	}
	
	public Integer getServerId() {
		return id;
	}
}
