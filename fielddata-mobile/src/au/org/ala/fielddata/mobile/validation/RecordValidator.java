package au.org.ala.fielddata.mobile.validation;

import java.util.ArrayList;
import java.util.List;

import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.validation.Validator.ValidationResult;

public class RecordValidator {

	public static class RecordValidationResult {
		List<ValidationResult> results;
		
		RecordValidationResult(List<ValidationResult> results) {
			this.results = results;
		}
		
		public boolean valid() {
			return results.isEmpty();
		}
		
		public List<ValidationResult> invalidAttributes() {
			return results;
		}
	}
	
	public RecordValidationResult validateRecord(Survey survey, Record record) {
		
		List<ValidationResult> results = new ArrayList<Validator.ValidationResult>(survey.attributes.size());
		for (Attribute attribute : survey.allAttributes()) {
			Validator validator = validatorFor(attribute);
			if (validator != null) {
				ValidationResult result = validator.validate(record, attribute); 
				if (!result.isValid()) {
					results.add(result);
				}
			}
		}
		return new RecordValidationResult(results);
		
	}
	
	private Validator validatorFor(Attribute attribute) {

		Validator validator = null;
		if (attribute.required != null && attribute.required) {
			validator = new RequiredValidator();
		}
		return validator;

	}

}
