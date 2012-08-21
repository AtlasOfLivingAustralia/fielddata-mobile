package au.org.ala.fielddata.mobile;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.Attribute.AttributeOption;
import au.org.ala.fielddata.mobile.model.Attribute.AttributeType;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Record.AttributeValue;
import au.org.ala.fielddata.mobile.ui.DatePickerFragment;
import au.org.ala.fielddata.mobile.validation.Binder;
import au.org.ala.fielddata.mobile.validation.RequiredValidator;
import au.org.ala.fielddata.mobile.validation.SpinnerBinder;
import au.org.ala.fielddata.mobile.validation.TextViewBinder;
import au.org.ala.fielddata.mobile.validation.Validator;

public class SurveyBuilder {
	
	private FragmentActivity viewContext;
	private List<Binder> binders;
	
	public SurveyBuilder(FragmentActivity viewContext) {
		this.viewContext = viewContext;
		binders = new ArrayList<Binder>();
	}
	
	public boolean accepts(Attribute attribute) {
		AttributeType type = attribute.getType();
		if (type == null) {
			return false;
		}
		if (attribute.isModeratorAttribute()) {
			return false;
		}
		switch (type) {
		case HTML:
		case HTML_COMMENT:
		case HTML_HORIZONTAL_RULE:
		case HTML_NO_VALIDATION:
			return false;
		}
		Log.d("SurveyBuilder", attribute.scope);
		return true;
	}

	public View buildInput(Attribute attribute, Record record) {
		AttributeValue value = record.valueOf(attribute);
		View view;
		switch (attribute.getType()) {
		case STRING:
			view = buildEditText(attribute, value, InputType.TYPE_CLASS_TEXT);
			break;
		case INTEGER:
		case ACCURACY:
			view = buildEditText(attribute, value, InputType.TYPE_CLASS_NUMBER);
			break;
		case DECIMAL:
			view = buildEditText(attribute, value, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			break;
		case MULTI_SELECT:
		case STRING_WITH_VALID_VALUES:
			view = buildSpinner(attribute, value);
			break;
		case NOTES:
			view = buildEditText(attribute, value, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			break;
		case WHEN:
			view = buildDatePicker(attribute, value);
			break;
		default:
		    view = buildEditText(attribute, value, InputType.TYPE_CLASS_TEXT);
		    break;
		}
		return view;
	}
	
	public void bindAll() {
		for (Binder binder : binders) {
			binder.bind();
		}
	}
	
	private Spinner buildSpinner(Attribute attribute, AttributeValue value) {
		Spinner spinner = new Spinner(viewContext);
		spinner.setPrompt("Select "+attribute.description);
		ArrayAdapter<AttributeOption> adapter = new ArrayAdapter<AttributeOption>(viewContext, android.R.layout.simple_list_item_1, attribute.options);
		spinner.setAdapter(adapter);
		Binder binder = new SpinnerBinder(viewContext, spinner, value, validatorFor(attribute));
		binders.add(binder);
		return spinner;
	}

	private View buildEditText(Attribute attribute, AttributeValue value, int inputType) {
		EditText view = new EditText(viewContext);
		
		view.setText(value.nullSafeValue());
		TextViewBinder binder = new TextViewBinder(viewContext, view, value, validatorFor(attribute));
		binders.add(binder);
		return view;
	}
	
	public View buildLabel(Attribute attribute) {
		TextView view = new TextView(viewContext);
		view.setText(attribute.description);
		return view;
	}
	
	public View buildDatePicker(Attribute attribute, AttributeValue value) {
		
		// Probably should inflate this layout from a file
		String date = DateFormat.getMediumDateFormat(viewContext).format(System.currentTimeMillis());
		LinearLayout layout = new LinearLayout(viewContext);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		
		TextView label = new TextView(viewContext);
		label.setText(date);
		
		Button changeButton = new Button(viewContext);
		changeButton.setText("Edit");
		changeButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				DialogFragment picker = new DatePickerFragment();
				picker.show(viewContext.getSupportFragmentManager(), "datePicker");
			}
		});
		// We want the label to fill most of the horizontal space.
		LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f);
		layout.addView(label, params);
		layout.addView(changeButton);
		
		return layout;
	}
	
	private Validator validatorFor(Attribute attribute) {
		
		Validator validator = null;
		if (attribute.required != null && attribute.required) {
			validator = new RequiredValidator();
		}
		return validator;
		
	}
}
