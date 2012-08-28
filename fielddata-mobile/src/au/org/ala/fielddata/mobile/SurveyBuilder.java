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

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.Attribute.AttributeOption;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.SurveyViewModel;
import au.org.ala.fielddata.mobile.ui.GPSFragment;

public class SurveyBuilder {
	
	private FragmentActivity viewContext;
	private SurveyViewModel model;
	
	public SurveyBuilder(FragmentActivity viewContext, SurveyViewModel model) {
		this.viewContext = viewContext;
		this.model = model;
		
	}
	
	public View buildInput(Attribute attribute) {
		Record record = model.getRecord();
		View view;
		switch (attribute.getType()) {
		case STRING:
			view = buildEditText(InputType.TYPE_CLASS_TEXT);
			break;
		case INTEGER:
		case NUMBER:
			view = buildEditText(InputType.TYPE_CLASS_NUMBER);
			break;
		case DECIMAL:
		case ACCURACY:
			
			view = buildEditText(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			break;
		case MULTI_SELECT:
		case STRING_WITH_VALID_VALUES:
			view = buildSpinner(attribute);
			break;
		case NOTES:
			view = buildEditText(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			break;
		case WHEN:
			view = buildDatePicker(attribute, record);
			break;
		case DWC_TIME:
			view = buildTimePicker(attribute, record);
			break;
		case SPECIES_P:
			view = buildSpeciesPicker(attribute);
			break;
		case POINT:
			view = buildLocationPicker(attribute);
			break;
		case IMAGE:
			view = buildImagePicker(attribute);
			break;
		default:
		    view = buildEditText(InputType.TYPE_CLASS_TEXT);
		    break;
		}
		return view;
	}
	
	
	
	private Spinner buildSpinner(Attribute attribute) {
		Spinner spinner = (Spinner)viewContext.getLayoutInflater().inflate(R.layout.input_spinner_view, null);
		spinner.setPrompt("Select "+attribute.description);
		ArrayAdapter<AttributeOption> adapter = new ArrayAdapter<AttributeOption>(viewContext, android.R.layout.simple_spinner_item, attribute.options);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		return spinner;
	}

	private View buildEditText(int inputType) {
		EditText view = (EditText)viewContext.getLayoutInflater().inflate(R.layout.input_text_view, null);
		view.setInputType(inputType);
		return view;
	}
	
	public View buildLabel(Attribute attribute) {
		TextView view = new TextView(viewContext);
		SpannableStringBuilder builder = new SpannableStringBuilder(attribute.description);
		builder.setSpan(new StyleSpan(Typeface.BOLD), 0, builder.length(), 0);
		view.setText(builder);
		return view;
	}
	
	public View buildDatePicker(Attribute attribute, Record record) {
		
		View row = viewContext.getLayoutInflater().inflate(R.layout.date_field, null);
		return row;
	}
	
	public View buildTimePicker(Attribute attribute, Record record) {
		
		View row = viewContext.getLayoutInflater().inflate(R.layout.date_field, null);
		return row;
	}
	
	public View buildSpeciesPicker(Attribute attribute) {
		
		final View row = viewContext.getLayoutInflater().inflate(R.layout.species_row, null);
		
		return row;
	}
	
	public View buildLocationPicker(Attribute attribute) {
		View view = viewContext.getLayoutInflater().inflate(R.layout.read_only_location, null);
		ImageButton gpsButton = (ImageButton)view.findViewById(R.id.gpsButton);
		gpsButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				GPSFragment fragment = new GPSFragment();
				fragment.show(viewContext.getSupportFragmentManager(), "gpsDialog");
				
			}
		});
		
		ImageButton showOnMapButton = (ImageButton)view.findViewById(R.id.showMapButton);
		showOnMapButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(viewContext, LocationSelectionActivity.class);
				viewContext.startActivityForResult(intent, CollectSurveyData.SELECT_LOCATION_REQUEST );
			}
		});
		return view;
	}
	
	public View buildImagePicker(Attribute attribute) {
		View view = viewContext.getLayoutInflater().inflate(R.layout.image_selection, null);
		
		
		return view;
	}
	
}
