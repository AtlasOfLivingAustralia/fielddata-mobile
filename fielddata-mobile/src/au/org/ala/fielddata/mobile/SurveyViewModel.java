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

package au.org.ala.fielddata.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.location.Location;
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
	
	/** The currently selected Species - cached here to avoid database access */
	private Species species;
	
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
		return attributes.size();
	}
	
	public List<Attribute> getPage(int pageNum) {
		return attributes.get(pageNum);
	}
	
	public void speciesSelected(Species species) {
		this.species = species;
		record.taxon_id = species.server_id;
		record.scientificName = species.scientificName;
		
	}
	
	public Species getSelectedSpecies() {
		return species;
	}
	
	public Record getRecord(){ 
		return record;
	}
	
	public Survey getSurvey() {
		return survey;
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

	public void locationSelected(Location location) {
		getRecord().longitude = location.getLongitude();
		getRecord().latitude = location.getLatitude();
		getRecord().accuracy = Double.valueOf(location.getAccuracy());
	}
}
