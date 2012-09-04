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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;
import android.util.SparseArray;
import au.org.ala.fielddata.mobile.model.Attribute.AttributeType;
import au.org.ala.fielddata.mobile.validation.RecordValidator;
import au.org.ala.fielddata.mobile.validation.RecordValidator.RecordValidationResult;
import au.org.ala.fielddata.mobile.validation.Validator.ValidationResult;

public class SurveyViewModel {
	/** Defines the survey we are rendering in this Activity */
	private Survey survey;
	/** The attributes of the survey, split into pages */
	private List<List<Attribute>> attributes;

	/** The data collected for the Survey */
	private Record record;
	
	/** Validates our record */
	private RecordValidator validator;

	/** The currently selected Species - cached here to avoid database access */
	private Species species;

	private PackageManager packageManager;

	private SparseArray<AttributeChangeListener> listeners;
	
	private TempValue tempValue;

	/**
	 * Compares two Attributes by their weight. Not null safe!
	 */
	static class WeightComparitor implements Comparator<Attribute> {

		public int compare(Attribute lhs, Attribute rhs) {
			return lhs.weight.compareTo(rhs.weight);
		}

	}
	
	public static class TempValue {
		Attribute attribute;
		String value;
		
		public TempValue(Attribute attribute, String value) {
			this.attribute = attribute;
			this.value = value;
		}
		
		public Attribute getAttribute() {
			return attribute;
		}
		
		public String getValue() {
			return value;
		}
	}

	public SurveyViewModel(Survey survey, Record record, PackageManager packageManager) {
		this.survey = survey;
		this.record = record;
		this.packageManager = packageManager;
		attributes = new ArrayList<List<Attribute>>();
		listeners = new SparseArray<AttributeChangeListener>();
		validator = new RecordValidator();
		
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
		
		Attribute changed = survey.propertyByType(AttributeType.SPECIES_P);
		
		validate(changed);	
	}
	
	public void locationSelected(Location location) {
		if (location != null) {
			record.longitude = location.getLongitude();
			record.latitude = location.getLatitude();
			if (location.hasAccuracy()) {
				record.accuracy = Double.valueOf(location.getAccuracy());
			}
		}
		else {
			record.longitude = null;
			record.latitude = null;
		}
		

		Attribute changed = survey.propertyByType(AttributeType.POINT);
		fireAttributeChanged(changed);
		validate(changed);
	}

	public Species getSelectedSpecies() {
		return species;
	}

	public Record getRecord() {
		return record;
	}

	public Survey getSurvey() {
		return survey;
	}

	public String getValue(Attribute attribute) {

		return record.getValue(attribute);
	}

	public void setValue(Attribute attribute, String value) {

		record.setValue(attribute, value);
		fireAttributeChanged(attribute);
		validate(attribute);
	}

	public void setAttributeChangeListener(AttributeChangeListener listener, Attribute attribute) {
		listeners.put(attribute.getServerId(), listener);
	}

	public void removeAttributeChangeListener(AttributeChangeListener listener) {
		listeners.delete(listeners.indexOfValue(listener));
	}

	private void fireAttributeChanged(Attribute attribute) {
		AttributeChangeListener listener = listeners.get(attribute.getServerId());
		if (listener != null) {
			listener.onAttributeChange(attribute);
		}
	}
	
	private void fireAttributeValidated(ValidationResult result) {
		AttributeChangeListener listener = listeners.get(result.getAttribute().getServerId());
		if (listener != null) {
			listener.onValidationStatusChange(result.getAttribute(), result);
		}
	}

	private void sortAttributes() {
		List<Attribute> allAttributes = survey.allAttributes();
		Collections.sort(allAttributes, new WeightComparitor());

		List<Attribute> filteredAttributes = new ArrayList<Attribute>(allAttributes.size());

		for (Attribute attribute : allAttributes) {
			if (supports(attribute)) {
				filteredAttributes.add(attribute);
			}
			if (attribute.getType() == AttributeType.HTML_HORIZONTAL_RULE) {
				if (filteredAttributes.size() > 0) {
					attributes.add(filteredAttributes);
					filteredAttributes = new ArrayList<Attribute>(allAttributes.size());
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
		case IMAGE:
			return deviceHasCamera();
		
		case LOCATION:
			return false; // We dont' support locations yet, but if we did we'd have to 
			// check if there were any defined for the survey or user.
		}
		return true;
	}

	private boolean deviceHasCamera() {
		return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	public int validate() {
		int firstInvalidPage = -1;
		RecordValidationResult result = validator.validateRecord(survey, record);
		if (!result.valid()) {
			for (ValidationResult attr : result.invalidAttributes()) {
				
				Log.i("SurveyViewModel", "Attribute invalid: "+attr.getAttribute());
				fireAttributeValidated(attr);
			}
			Attribute firstInvalid = result.invalidAttributes().get(0).getAttribute();
			firstInvalidPage = pageOf(firstInvalid);
		}
		return firstInvalidPage;
	}
	
	private void validate(Attribute attribute) {
		ValidationResult result = validator.validateRecordAttribute(attribute, record);
		fireAttributeValidated(result);
	}

	private int pageOf(Attribute firstInvalid) {
		int firstInvalidPage = -1;
		int pageNum = 0;
		Log.i("SurveyViewModel", "Invalid attribute "+firstInvalid);
		for (List<Attribute> page : attributes) {
			for (Attribute attribute : page) {
				if (firstInvalid.equals(attribute)) {
					Log.i("SurveyViewModel", "Invalid attribute "+attribute+" found on page "+pageNum);
					firstInvalidPage = pageNum;
					break;
				}
			}
			if (firstInvalidPage > 0) {
				break;
			}
			pageNum++;
		}
		return firstInvalidPage;
	}

	/**
	 * Temporary storage for the URI we are going to save a photo as. 
	 * This value needs to be persisted in the event the CollectSurveyData
	 * activity is killed while the camera activity is in the foreground. 
	 *
	 * @param fileUri the URI that will hold our photo.
	 */
	public void setTempValue(Attribute attribute, String value) {
		tempValue = new TempValue(attribute, value);
	}

	public TempValue clearTempValue() {
		TempValue tmp = tempValue;
		tempValue = null;
		return tmp;
	}
	
	public TempValue getTempValue() {
		return tempValue;
	}
	
	public void persistTempValue() {
		if (tempValue != null) {
			setValue(tempValue.getAttribute(), tempValue.getValue());
		}
		tempValue = null;
	}

	public Location getLocation() {
		Location location = null;
		if (record.latitude != null && record.longitude != null) {
			location = new Location("");
			location.setLatitude(record.latitude);
			location.setLongitude(record.longitude);
			if (record.accuracy != null) {
				location.setAccuracy(record.accuracy.floatValue());
			}
		}
		return location;
	}

}
