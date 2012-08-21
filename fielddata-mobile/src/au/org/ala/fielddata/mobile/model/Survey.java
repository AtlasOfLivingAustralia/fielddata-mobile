package au.org.ala.fielddata.mobile.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Survey extends Persistent {

	public static class SurveyDetails {
		public int id;
		public Long startDate;
		public Long endDate;
		public String name;
		public String description;
	}
	
	public String name;
	public String description;
	
	@SerializedName("attributesAndOptions")
	public List<Attribute> attributes;
	public List<RecordProperty> recordProperties;
	
	@SerializedName("indicatorSpecies_server_ids")
	public SurveyDetails details;
	
	public boolean hasSpecies() {
		return true;
	}
	
	public boolean hasNumber() {
		return true;
	}
	
	public boolean hasLocation() {
		return true;
	}
	
	public boolean hasAccuracy() {
		return true;
	}
	
	public boolean hasWhen() {
		return true;
	}
	
	public boolean hasTime() {
		return true;
	}
	
	public boolean hasCreated() {
		return true;
	}
	
	public boolean hasUpdated() {
		return true;
	}
	
	public Attribute getAttribute(int id) {
		for (Attribute attribute : attributes) {
			if (id == attribute.server_id) {
				return attribute;
			}
		}
		return null;
	}
	
	public String toString() {
		return name;
	}
	
}
