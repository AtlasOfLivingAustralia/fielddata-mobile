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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.AttributeChangeListener;
import au.org.ala.fielddata.mobile.model.SurveyViewModel;
import au.org.ala.fielddata.mobile.validation.Validator.ValidationResult;

public class TextViewBinder implements Binder, TextWatcher, AttributeChangeListener {

	private TextView view;
	private SurveyViewModel model;
	private Context ctx;
	private Attribute attribute;
	private boolean updating;
	
	public TextViewBinder(Context ctx, TextView view, Attribute attribute, SurveyViewModel model) {
		this.view = view;
		this.model = model;
		this.attribute = attribute;
		this.ctx = ctx;
		updating = false;
		view.setText(model.getValue(attribute));
		view.addTextChangedListener(this);
		
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (!updating) {
			bind(s);
		}
	}

	public void afterTextChanged(Editable s) {
		
	}
	
	
	
	
	public void onAttributeChange(Attribute attribute) {
		if (attribute.getServerId() != this.attribute.getServerId()) {
			return;
		}
		try {
			updating = true;
			view.setText(model.getValue(attribute));
		}
		finally {
			updating = false;
		}
		
	}

	public void onValidationStatusChange(Attribute attribute, ValidationResult result) {
		if (!attribute.equals(this.attribute)) {
			return;
		}
		Log.d("Binder", "TextViewBinder: "+result);
		
		if (result.isValid()) {
			view.setError(null);
		}
		else {
			view.setError(result.getMessage(ctx));
			view.requestFocus();
		}
	}
	
	public void bind() {
		bind(nullSafeText());
	}
	
	private void bind(CharSequence text) {
		model.setValue(attribute, text.toString());
	}
	
	private String nullSafeText() {
		CharSequence text = view.getText();
		if (text == null) {
			return "";
		}
		return text.toString();
	}
	
	
}
