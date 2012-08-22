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
import java.util.List;

import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.Attribute.AttributeOption;
import au.org.ala.fielddata.mobile.model.Attribute.AttributeType;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Species;
import au.org.ala.fielddata.mobile.ui.SpeciesViewHolder;
import au.org.ala.fielddata.mobile.validation.Binder;
import au.org.ala.fielddata.mobile.validation.DateBinder;
import au.org.ala.fielddata.mobile.validation.RequiredValidator;
import au.org.ala.fielddata.mobile.validation.SpinnerBinder;
import au.org.ala.fielddata.mobile.validation.TextViewBinder;
import au.org.ala.fielddata.mobile.validation.Validator;

public class SurveyBuilder {
	
	private FragmentActivity viewContext;
	private List<Binder> binders;
	
	public SurveyBuilder(FragmentActivity viewContext) {
		this.viewContext = viewContext;
		binders = new ArrayList<Binder>();
	}
	
	public boolean accepts(Attribute attribute) {
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

	public View buildInput(Attribute attribute, Record record) {
		View view;
		switch (attribute.getType()) {
		case STRING:
			view = buildEditText(attribute, record, InputType.TYPE_CLASS_TEXT);
			break;
		case INTEGER:
		case NUMBER:
			view = buildEditText(attribute, record, InputType.TYPE_CLASS_NUMBER);
			break;
		case DECIMAL:
		case ACCURACY:
			
			view = buildEditText(attribute, record, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			break;
		case MULTI_SELECT:
		case STRING_WITH_VALID_VALUES:
			view = buildSpinner(attribute, record);
			break;
		case NOTES:
			view = buildEditText(attribute, record, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			break;
		case WHEN:
			view = buildDatePicker(attribute, record);
			break;
		case SPECIES_P:
			view = buildSpeciesPicker(attribute, record);
			break;
		default:
		    view = buildEditText(attribute, record, InputType.TYPE_CLASS_TEXT);
		    break;
		}
		return view;
	}
	
	public void bindAll() {
		for (Binder binder : binders) {
			binder.bind();
		}
	}
	
	private Spinner buildSpinner(Attribute attribute, Record record) {
		Spinner spinner = new Spinner(viewContext);
		spinner.setPrompt("Select "+attribute.description);
		ArrayAdapter<AttributeOption> adapter = new ArrayAdapter<AttributeOption>(viewContext, android.R.layout.simple_list_item_1, attribute.options);
		spinner.setAdapter(adapter);
		Binder binder = new SpinnerBinder(viewContext, spinner, attribute, record, validatorFor(attribute));
		binders.add(binder);
		return spinner;
	}

	private View buildEditText(Attribute attribute, Record record, int inputType) {
		EditText view = new EditText(viewContext);
		view.setInputType(inputType);
		view.setText(record.getValue(attribute));
		TextViewBinder binder = new TextViewBinder(viewContext, view, attribute, record,  validatorFor(attribute));
		binders.add(binder);
		return view;
	}
	
	public View buildLabel(Attribute attribute) {
		TextView view = new TextView(viewContext);
		view.setText(attribute.description);
		return view;
	}
	
	public View buildDatePicker(Attribute attribute, Record record) {
		
		View row = viewContext.getLayoutInflater().inflate(R.layout.date_field, null);
		Binder binder = new DateBinder(viewContext, row, attribute, record, validatorFor(attribute));
		binders.add(binder);
		return row;
	}
	
	public View buildSpeciesPicker(Attribute attribute, Record record) {
		
		Species species = new Species("test", "another test", R.drawable.nassella_neesiana_thumb); //record.getSpecies();
		
		View row = viewContext.getLayoutInflater().inflate(R.layout.species_row, null);
		new SpeciesViewHolder(row).populate(species);
		
		
		return row;
	}
	
	private Validator validatorFor(Attribute attribute) {
		
		Validator validator = null;
		if (attribute.required != null && attribute.required) {
			validator = new RequiredValidator();
		}
		return validator;
		
	}
}
