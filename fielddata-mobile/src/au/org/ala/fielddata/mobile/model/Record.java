/*******************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
 * All Rights Reserved.
 *  
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *  
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ******************************************************************************/

package au.org.ala.fielddata.mobile.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import au.org.ala.fielddata.mobile.model.Attribute.AttributeType;

public class Record extends Persistent{

	
	public Double latitude;
	public Double longitude;
	public Integer location; // Id of location object on the server.
	public Double accuracy;
	public Long when;
	public Date lateDate; // I think last sync date?
	public String notes;
	public Integer survey_id;
	public Integer number;
	public Integer taxon_id; // Server id of the taxon.
	public String scientificName;
	
	private List<AttributeValue> attributeValues;
	
	public static class AttributeValue {
		public Integer id = 1;
		public Integer server_id;
		
		public Integer attribute_id;
		public String value;
		
		public Attribute attribute;
		
		public void setValue(String value) {
			this.value = value;
		}
		
		public String nullSafeValue() {
			if (value == null) {
				return "";
			}
			return value;
		}
	}
	
	public class PropertyAttributeValue extends AttributeValue {
		
		private AttributeType attributeType;
		public PropertyAttributeValue(Attribute attribute) {
			attributeType = attribute.getType();
		}
		
		public String nullSafeValue() {
			
			switch (attributeType) {
			case SPECIES_P:
				return Integer.toString(taxon_id);
			case POINT:
				return "";
			case ACCURACY:
				return Double.toString(accuracy);
			case NUMBER:
				return Integer.toString(number);
			case NOTES:
				return notes;
			case WHEN:
				return Long.toString(when);
			default:
				return "";
			}
		}
		
		public void setValue(String value) {
			
			this.value = value;
			
			switch (attributeType) {
			case SPECIES_P:
				break;
			case POINT:
				break;
			case ACCURACY:
				break;
			case NUMBER:
				break;
			case NOTES:
				notes = value;
				break;
			case WHEN:
				when = Long.parseLong(value);
				break;
			default:
				;
			}
		}
	}
	
	public Record() {
		attributeValues = new ArrayList<Record.AttributeValue>();
		
		
	}

	public AttributeValue valueOf(Attribute attribute) {
		int id = attribute.getServerId();
		for (AttributeValue value : attributeValues) {
			if (id == value.attribute_id) {
				return value;
			}
		}
		
		AttributeValue value = null;
		if (attribute instanceof RecordProperty) {
			value = new PropertyAttributeValue(attribute);
			value.attribute_id = id;
		}
		else {
			value = new AttributeValue();
			value.attribute_id = id;
		}
		attributeValues.add(value);
		return value;
	}

	public void setValue(Attribute attribute, String value) {
		
		int id = attribute.server_id;
		for (AttributeValue attributeValue : attributeValues) {
			if (id == attributeValue.attribute_id) {
				attributeValue.value = value;
				return;
			}
		}
		
		AttributeValue attributeValue = new AttributeValue();
		attributeValue.attribute_id = id;
		attributeValue.value = value;
		attributeValues.add(attributeValue);
	}
	
	
	
}
