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
package au.org.ala.fielddata.mobile.validation;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.SurveyViewModel;
import au.org.ala.fielddata.mobile.model.Attribute.AttributeOption;
import au.org.ala.fielddata.mobile.validation.Validator.ValidationResult;

public class SpinnerBinder extends AbsBinder implements OnItemSelectedListener, OnTouchListener {

	private SurveyViewModel model;
	private Context ctx;
	private boolean updating;
	private boolean bindEnabled;
	

	public SpinnerBinder(Context ctx, Spinner view, Attribute attribute, SurveyViewModel model) {
		super(attribute, view);
		this.ctx = ctx;
		this.model = model;
		updating = false;
		bindEnabled = false;
		update();
		view.setOnItemSelectedListener(this);
		view.setOnTouchListener(this);
		
	}

	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		Log.d("SpinnerBinder", "onItemSelected");
		if (bindEnabled) {
			bind();
		}
	}

	public void onNothingSelected(AdapterView<?> parent) {
		if (bindEnabled) {
			bind("");
		}
	}
	
	
	/**
	 * The reason this is necessary is spinners fire onItemSelected events
	 * automatically when they are first layed out (including after an 
	 * orientation change for example).
	 * We don't actually want to trigger validation unless the user has
	 * interacted with the Spinner (or the user has pressed Save).
	 */
	public boolean onTouch(View v, MotionEvent event) {
		Log.d("SpinnerBinder", "onTouch");
		bindEnabled = true;
		return false;
	}

	public void onAttributeChange(Attribute attribute) {
		if (attribute.getServerId() != this.attribute.getServerId()) {
			return;
		}
		update();
	}

	private void update() {
		try {
			updating = true;
			Spinner spinner = (Spinner)view;
			String value = model.getValue(attribute);
			if (value != null) {
				ArrayAdapter<AttributeOption> adapter = (ArrayAdapter<AttributeOption>)spinner.getAdapter();
				for (int i=0; i<adapter.getCount(); i++) {
					AttributeOption option = adapter.getItem(i);
					if (value.equals(option.value)) {
						spinner.setSelection(i);
						break;
					}
				}
			}
		}
		finally {
			updating = false;
		}
	}
	
	public void onValidationStatusChange(Attribute attribute, ValidationResult result) {
		Log.d("SpinnerBinder", "onValidationStatusChange: "+attribute+", "+result.isValid());
		if (attribute.getServerId() != this.attribute.getServerId()) {
			return;
		}
		
		View selected = ((Spinner)view).getSelectedView();
		if (selected instanceof TextView) {
			TextView textView = (TextView)selected;
			if (result.isValid()) {
				textView.setError(null);
			}
			else {
				
			    textView.setError(result.getMessage(ctx));
			}
		}
	}
	
	public void bind() {
		Log.d("SpinnerBinder", "bind");
		if (!updating) {
			
			bindEnabled = true;
			Log.d("SpinnerBinder", "bind - not updating");
			bind(nullSafeText());
		}
	}
	
	private void bind(String value) {
		model.setValue(attribute, value);
	}

	private String nullSafeText() {
		CharSequence text = ((Spinner)view).getSelectedItem().toString();
		if (text == null) {
			return "";
		}
		return text.toString();
	}

}
