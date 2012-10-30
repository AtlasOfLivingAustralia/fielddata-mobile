package au.org.ala.fielddata.mobile.validation;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.R;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.validation.Validator.ValidationResult;

/**
 * The base class that is required to be implemented by Binder implementations.
 */
public abstract class AbsBinder implements Binder {

	protected Attribute attribute;
	protected View view;
	
	public AbsBinder(Attribute attribute, View view) {
		this.attribute = attribute;
		this.view = view;
	}
	
	public View getView() {
		return view;
	}
	
	public Attribute getAttribute() {
		return attribute;
	}
	
	protected TextView getErrorLabel() {
		ViewGroup container = (ViewGroup)view.getParent();
		TextView errorLabel = (TextView)container.findViewById(R.id.errorLabel);
		return errorLabel;
	}
	
	protected void setError(int messageId) {
		TextView errorLabel = getErrorLabel();
		errorLabel.setText(messageId);
		errorLabel.setVisibility(View.VISIBLE);
	}
	
	protected void clearError() {
		TextView errorLabel = getErrorLabel();
		errorLabel.setText("");
		errorLabel.setVisibility(View.GONE);
	}
	
	public void onValidationStatusChange(Attribute attribute, ValidationResult result) {
		Log.d("AbsBinder", "onValidationStatusChange: "+attribute+", "+result.isValid());
		if (attribute.getServerId() != this.attribute.getServerId()) {
			return;
		}
		
		if (result.isValid()) {
			clearError();
		}
		else {
			setError(result.getMessageId());
		}
		
	}
}
