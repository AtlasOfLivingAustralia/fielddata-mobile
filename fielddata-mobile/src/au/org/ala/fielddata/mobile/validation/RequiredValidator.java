package au.org.ala.fielddata.mobile.validation;

public class RequiredValidator implements Validator {

	public boolean validate(String value) {
		return value != null && value.length() > 0;
	}
	
	
}
