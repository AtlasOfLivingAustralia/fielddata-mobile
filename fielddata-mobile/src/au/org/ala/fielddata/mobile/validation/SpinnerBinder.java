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
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.SurveyViewModel;
import au.org.ala.fielddata.mobile.validation.Validator.ValidationResult;

public class SpinnerBinder implements Binder, OnItemSelectedListener {

	private Spinner view;
	private Attribute attribute;
	private SurveyViewModel model;
	private Context ctx;
	private boolean updating;
	

	public SpinnerBinder(Context ctx, Spinner view, Attribute attribute, SurveyViewModel model) {
		this.ctx = ctx;
		this.view = view;
		this.attribute = attribute;
		this.model = model;
		updating = false;
		view.setOnItemSelectedListener(this);

	}

	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		
		bind();
	}

	public void onNothingSelected(AdapterView<?> parent) {
		bind("");
	}
	
	public void onAttributeChange(Attribute attribute) {
		if (attribute.getServerId() != this.attribute.getServerId()) {
			return;
		}
		try {
			updating = true;
			bind();
		}
		finally {
			updating = false;
		}
	}

	public void onAttributeInvalid(Attribute attribute, ValidationResult result) {
		if (attribute.getServerId() != this.attribute.getServerId()) {
			return;
		}
		
		View selected = view.getSelectedView();
		if (selected instanceof TextView) {
			((TextView) selected).setError(result.getMessage(ctx));
		}
	}
	
	public void bind() {
		if (!updating) {
			bind(nullSafeText());
		}
	}
	
	private void bind(String value) {
		model.setValue(attribute, value);
	}

	private String nullSafeText() {
		CharSequence text = view.getSelectedItem().toString();
		if (text == null) {
			return "";
		}
		return text.toString();
	}

}
