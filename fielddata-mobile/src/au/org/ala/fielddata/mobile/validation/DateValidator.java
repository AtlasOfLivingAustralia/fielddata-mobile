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

import java.util.Date;

import android.util.Log;
import au.org.ala.fielddata.mobile.R;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.Record;

/**
 * Validates that a date is not in the future.
 */
public class DateValidator implements Validator {

	public ValidationResult validate(Record record, Attribute attribute) {
		Date value = record.getDate(attribute);
		boolean valid = value.before(new Date());
		
		if (valid) {
			
			Log.d("DateValidator", "date valid: "+value);
			return new ValidationResult(attribute);
		}
		else {
			Log.d("DateValidator", "date invalid;");
			
			return new ValidationResult(attribute, R.string.futureDateMessage);
		}
	}
	
	
}
