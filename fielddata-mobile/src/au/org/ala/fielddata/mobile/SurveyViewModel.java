package au.org.ala.fielddata.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.util.Log;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.Attribute.AttributeType;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Species;
import au.org.ala.fielddata.mobile.model.Survey;

public class SurveyViewModel {
	/** Defines the survey we are rendering in this Activity */
	private Survey survey;
	/** The attributes of the survey, split into pages */
	private List<List<Attribute>> attributes;

	/** The data collected for the Survey */
	private Record record;
	
	/**
	 * Compares two Attributes by their weight.
	 * Not null safe!
	 */
	static class WeightComparitor implements Comparator<Attribute> {

		public int compare(Attribute lhs, Attribute rhs) {
			return lhs.weight.compareTo(rhs.weight);
		}
		
	}
	
	public SurveyViewModel(Survey survey, Record record) {
		this.survey = survey;
		this.record = record;
		attributes = new ArrayList<List<Attribute>>();
		
		sortAttributes();
	}
	
	public int getPageCount() {
		return attributes.size()+2;
	}
	
	public List<Attribute> getPage(int pageNum) {
		return attributes.get(pageNum);
	}
	
	public void speciesSelected(Species species) {
		record.taxon_id = species.server_id;
		record.scientificName = species.scientificName;
		
	}
	
	public Record getRecord(){ 
		return record;
	}

	private void sortAttributes() {
		List<Attribute> allAttributes = new ArrayList<Attribute>(survey.attributes);
		allAttributes.addAll(survey.recordProperties);
		Collections.sort(allAttributes, new WeightComparitor());
		
		List<Attribute> filteredAttributes = new ArrayList<Attribute>(
				allAttributes.size());

		for (Attribute attribute : allAttributes) {
			if (supports(attribute)) {
				filteredAttributes.add(attribute);
			}
			if (attribute.getType() == AttributeType.HTML_HORIZONTAL_RULE) {
				if (filteredAttributes.size() > 0) {
					attributes.add(filteredAttributes);
					filteredAttributes = new ArrayList<Attribute>(
							allAttributes.size());
				}
			}
		}
		if (filteredAttributes.size() > 0) {
			attributes.add(filteredAttributes);
		}
	}

	private boolean supports(Attribute attribute) {
		AttributeType type = attribute.getType();
		if (type == null) {
			return false;
		}
		if (attribute.isModeratorAttribute()) {
			return false;
		}
		switch (type) {
		case HTML:
		case HTML_COMMENT:
		case HTML_HORIZONTAL_RULE:
		case HTML_NO_VALIDATION:
			return false;
		}
		Log.d("SurveyBuilder", attribute.scope);
		return true;
	}
}
