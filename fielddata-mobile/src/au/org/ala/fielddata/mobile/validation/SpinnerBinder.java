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
import au.org.ala.fielddata.mobile.model.Record.AttributeValue;

public class SpinnerBinder implements Binder, OnItemSelectedListener {

	private Spinner view;
	private AttributeValue value;
	private Validator validator;

	public SpinnerBinder(Context ctx, Spinner view, AttributeValue value,
			Validator validator) {
		this.view = view;
		this.value = value;
		this.validator = validator;
		view.setOnItemSelectedListener(this);

	}

	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		validate();
	}

	public void onNothingSelected(AdapterView<?> parent) {
		validate();
	}

	public boolean validate() {

		boolean valid = true;
		if (validator != null) {
			valid = validator.validate(nullSafeText());
			if (!valid) {
				View selected = view.getSelectedView();
				if (selected instanceof TextView) {
					((TextView) selected).setError("Uhoh");
				}
			}
		}
		return valid;

	}

	public void bind() {
		value.value = nullSafeText();
	}

	private String nullSafeText() {
		CharSequence text = view.getSelectedItem().toString();
		if (text == null) {
			return "";
		}
		return text.toString();
	}

}
