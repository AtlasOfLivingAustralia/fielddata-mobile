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
import java.util.UUID;

import com.google.gson.annotations.SerializedName;

import android.net.Uri;
import au.org.ala.fielddata.mobile.model.Attribute.AttributeType;

public class Record extends Persistent {

	@SerializedName("id")
	public String uuid;
	
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
	
	public static class StringValue {
		public String value;
		
		public boolean uri;
		
		public String getValue() {
			return value;
		}
	}

	
	static class AttributeValue {
		public Integer id = 1;
		public Integer server_id;
		
		public Integer attribute_id;
		
		protected StringValue value = new StringValue();
		
		public void setValue(String value) {
			this.value.value = value;
		}
		
		public void setUri(Uri uri) {
			value.uri = true;
			if (uri == null) {
				value.value = "";
			}
			else {
				value.value = uri.toString();
			}
		}
		
		public Uri getUri() {
			if (value.value == null || "".equals(value.value)) {
				return null;
			}
			return Uri.parse(value.value);
		}
		
		public String nullSafeValue() {
			if (value.value == null) {
				return "";
			}
			return value.value;
		}
		
		
	}
	
	class PropertyAttributeValue extends AttributeValue {
		
		private AttributeType attributeType;
		public PropertyAttributeValue(Attribute attribute) {
			attributeType = attribute.getType();
			if (attributeType == null) {
				throw new NullPointerException("Attribute has null type: "+attribute);
			}
		}
		
		public String nullSafeValue() {
			
			switch (attributeType) {
			case SPECIES_P:
				if (taxon_id != null) {
					return Integer.toString(taxon_id);
				}
				return "";
			case POINT:
				String result = "";
				if (latitude != null && longitude != null) {
					result = Double.toString(latitude)+","+Double.toString(longitude);
				}
				return result;
			case ACCURACY:
				if (accuracy != null) {
					return Double.toString(accuracy);	
				}
				return "";
			case NUMBER:
				if (number != null) {
					return Integer.toString(number);
				}
				return "";
			case NOTES:
				return notes;
			case WHEN:
				if (when != null) {
					return Long.toString(when);
				}
				return "";
			default:
				return "";
			}
		}
		
		public void setValue(String value) {
			
			this.value.value = value;
			
			switch (attributeType) {
			case SPECIES_P:
				break;
			case POINT:
				break;
			case ACCURACY:
				accuracy = toDouble(value);
				break;
			case NUMBER:
				number = toInteger(value);
				break;
			case NOTES:
				notes = value;
				break;
			case WHEN:
				when = toLong(value);
				break;
			default:
				;
			}
		}
		
		private Long toLong(String value) {
			if (value != null) {
				try {
					return Long.parseLong(value);
				}
				catch (Exception e) {}
					
			}
			return null;
		}
		
		private Integer toInteger(String value) {
			if (value != null) {
				try {
					return Integer.parseInt(value);
				}
				catch (Exception e) {}
					
			}
			return null;
		}
		
		private Double toDouble(String value) {
			if (value != null) {
				try {
					return Double.parseDouble(value);
				}
				catch (Exception e) {}
					
			}
			return null;
		}
	}
	
	public Record() {
		uuid = UUID.randomUUID().toString();
		attributeValues = new ArrayList<Record.AttributeValue>();
		
		
	}

	protected AttributeValue valueOf(Attribute attribute) {
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
			attributeValues.add(value);
		}
		
		return value;
	}
	
	
	public String getValue(Attribute attribute) {
		AttributeValue attributeVal = valueOf(attribute);
		
		return attributeVal.nullSafeValue();

	}

	public void setValue(Attribute attribute, String value) {
	
		AttributeValue attrValue = valueOf(attribute);
		attrValue.setValue(value);
		
	}
	
	public void setUri(Attribute attribute, Uri value) {
		checkUriSupport(attribute);
		AttributeValue attrValue = valueOf(attribute);
		attrValue.setUri(value);
	}
	
	public Uri getUri(Attribute attribute) {
		checkUriSupport(attribute);
		AttributeValue attrValue = valueOf(attribute);
		return attrValue.getUri();
	}
	
	
	private void checkUriSupport(Attribute attribute) {
		boolean supportsUri = false;
		switch (attribute.getType()) {
		case IMAGE:
		case FILE:
		case AUDIO:
			supportsUri = true;
			break;
			default:
				supportsUri = false;
		}
		if (!supportsUri) {
			throw new IllegalArgumentException("Attributes of type: "+
					attribute.getType()+" do not support URI typed values");
		}
	}
	
	public void setValue(Attribute attribute, Date value) {
		if (!attribute.getType().isDateType()) {
			throw new IllegalArgumentException("Attribute is not a date attribute");
		}
		valueOf(attribute).setValue(Long.toString(value.getTime()));
	}

	public Date getDate(Attribute attribute) {
		if (!attribute.getType().isDateType()) {
			throw new IllegalArgumentException("Attribute is not a date attribute");
		}
		String dateStr = valueOf(attribute).nullSafeValue();
		Date date = new Date();
		if (date != null) {
			try {
				date = new Date(Long.parseLong(dateStr));
			}
			catch (NumberFormatException e) {
				
			}
		}
		return date;
	}

}
