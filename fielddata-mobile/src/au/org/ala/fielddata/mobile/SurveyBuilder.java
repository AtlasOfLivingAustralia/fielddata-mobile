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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.Attribute.AttributeOption;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Species;
import au.org.ala.fielddata.mobile.ui.GPSFragment;
import au.org.ala.fielddata.mobile.ui.SpeciesListAdapter;
import au.org.ala.fielddata.mobile.ui.SpeciesViewHolder;

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
		default:
		    view = buildEditText(InputType.TYPE_CLASS_TEXT);
		    break;
		}
		return view;
	}
	
	
	
	private Spinner buildSpinner(Attribute attribute) {
		Spinner spinner = new Spinner(viewContext);
		spinner.setPrompt("Select "+attribute.description);
		ArrayAdapter<AttributeOption> adapter = new ArrayAdapter<AttributeOption>(viewContext, android.R.layout.simple_list_item_1, attribute.options);
		spinner.setAdapter(adapter);
		return spinner;
	}

	private View buildEditText(int inputType) {
		EditText view = new EditText(viewContext);
		view.setInputType(inputType);
		return view;
	}
	
	public View buildLabel(Attribute attribute) {
		TextView view = new TextView(viewContext);
		view.setText(attribute.description);
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
		
		Species species = model.getSelectedSpecies();
		final View row = viewContext.getLayoutInflater().inflate(R.layout.species_row, null);
		
		row.setOnClickListener(new android.view.View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog.Builder builder = new AlertDialog.Builder(viewContext);
				final SpeciesListAdapter adapter = new SpeciesListAdapter(viewContext);
				builder.setAdapter(adapter, new android.content.DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						Species selected = adapter.getItem(which);
						model.speciesSelected(selected);
						new SpeciesViewHolder(row).populate(selected);
					}
				});
				builder.setTitle("Select a species");
				builder.show();
				
			}
		});
			
		if (species != null) {
			new SpeciesViewHolder(row).populate(species);
		}
		return row;
	}
	
	public View buildLocationPicker(Attribute attribute) {
		View view = viewContext.getLayoutInflater().inflate(R.layout.read_only_location, null);
		Button gpsButton = (Button)view.findViewById(R.id.gpsButton);
		gpsButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				GPSFragment fragment = new GPSFragment();
				fragment.show(viewContext.getSupportFragmentManager(), "gpsDialog");
				
			}
		});
		
		Button showOnMapButton = (Button)view.findViewById(R.id.showMapButton);
		showOnMapButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(viewContext, LocationSelectionActivity.class);
				viewContext.startActivityForResult(intent, -1 );
			}
		});
		return view;
	}
	
}
