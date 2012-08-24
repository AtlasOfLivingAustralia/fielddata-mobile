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
import android.widget.TextView;
import au.org.ala.fielddata.mobile.R;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.Record;

public class TextViewBinder implements Binder, TextWatcher {

	private TextView view;
	private Record record;
	private Context ctx;
	private Validator validator;
	private Attribute attribute;
	
	public TextViewBinder(Context ctx, TextView view, Attribute attribute, Record record, Validator validator) {
		this.view = view;
		this.record = record;
		this.attribute = attribute;
		view.setText(record.getValue(attribute));
		view.addTextChangedListener(this);
	}

	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) {
		validate();
	}

	public void afterTextChanged(Editable s) {
		
	}
	
	public boolean validate() {
		boolean valid = true;
		if (validator != null) {
			valid = validator.validate(nullSafeText());
			if (!valid) {
				view.setError(ctx.getText(R.string.requiredMessage));
			}
		}
		return valid;
	}
	
	public void bind() {
		record.setValue(attribute, nullSafeText());
	}
	
	private String nullSafeText() {
		CharSequence text = view.getText();
		if (text == null) {
			return "";
		}
		return text.toString();
	}
	
	
}
