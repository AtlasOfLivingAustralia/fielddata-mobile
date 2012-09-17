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
package au.org.ala.fielddata.mobile;

import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.Attribute.AttributeOption;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.model.SurveyViewModel;
import au.org.ala.fielddata.mobile.ui.MultiSpinner;

public class SurveyBuilder {

	private FragmentActivity viewContext;
	private SurveyViewModel model;

	public SurveyBuilder(FragmentActivity viewContext, SurveyViewModel model) {
		this.viewContext = viewContext;
		this.model = model;
	}

	public View buildFields(Attribute attribute, ViewGroup parent) {
		LinearLayout layout = (LinearLayout) viewContext.getLayoutInflater().inflate(
				R.layout.label_and_field, parent);
		ViewGroup container = (ViewGroup) layout.getChildAt(0);
		buildLabel(attribute, container);
		View inputField = buildInput(attribute, container);

		return inputField;
	}

	public View buildInput(Attribute attribute, ViewGroup parent) {
		Record record = model.getRecord();
		View view;
		switch (attribute.getType()) {
		case STRING:
			view = buildEditText(InputType.TYPE_CLASS_TEXT, parent);
			break;
		case INTEGER:
		case NUMBER:
			view = buildEditText(InputType.TYPE_CLASS_NUMBER, parent);
			break;
		case DECIMAL:
		case ACCURACY:
			view = buildEditText(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL,
					parent);
			break;
		case MULTI_SELECT:
		case STRING_WITH_VALID_VALUES:
			view = buildSpinner(attribute, parent);
			break;
		case NOTES:
			view = buildEditText(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE,
					parent);
			break;
		case WHEN:
			view = buildDatePicker(attribute, record, parent);
			break;
		case DWC_TIME:
			view = buildTimePicker(attribute, record, parent);
			break;
		case SPECIES_P:
			view = buildSpeciesPicker(attribute, parent);
			break;
		case POINT:
			view = buildLocationPicker(attribute, parent);
			break;
		case IMAGE:
			view = buildImagePicker(attribute, parent);
			break;
		case SINGLE_CHECKBOX:
			view = buildSingleCheckbox(attribute, parent);
			break;
		case MULTI_CHECKBOX:
			view = buildMultiSpinner(attribute, parent);
			break;
		default:
			view = buildEditText(InputType.TYPE_CLASS_TEXT, parent);
			break;
		}
		return view;
	}

	public View buildLabel(Attribute attribute, ViewGroup parent) {
		// TextView view = new TextView(viewContext);
		ViewGroup view = (ViewGroup) viewContext.getLayoutInflater().inflate(
				R.layout.label_text_view, parent);
		TextView textView = (TextView) view.findViewById(R.id.fieldLabel);

		textView.setText(Utils.bold(attribute.description));
		return view;
	}

	private View buildEditText(int inputType, ViewGroup parent) {

		ViewGroup row = (ViewGroup) viewContext.getLayoutInflater().inflate(
				R.layout.input_text_view, parent);
		EditText editTextview = (EditText) getFirstNonLabelView(row);
		editTextview.setInputType(inputType);
		return editTextview;
	}

	public View buildDatePicker(Attribute attribute, Record record, ViewGroup parent) {

		ViewGroup row = (ViewGroup) viewContext.getLayoutInflater().inflate(R.layout.date_field,
				parent);
		return getFirstNonLabelView(row);
	}

	public View buildTimePicker(Attribute attribute, Record record, ViewGroup parent) {

		ViewGroup row = (ViewGroup) viewContext.getLayoutInflater().inflate(R.layout.date_field,
				parent);
		return getFirstNonLabelView(row);
	}

	public View buildSpeciesPicker(Attribute attribute, ViewGroup parent) {
		long start = System.currentTimeMillis();

		ViewGroup row = (ViewGroup) viewContext.getLayoutInflater().inflate(R.layout.species_row,
				parent);

		long end = System.currentTimeMillis();
		Log.d("Perf", "SurveyBuilder.buildSpeciesPicker took " + (end - start) + " millis");

		return getFirstNonLabelView(row);
	}

	public View buildLocationPicker(Attribute attribute, ViewGroup parent) {
		ViewGroup row = (ViewGroup) viewContext.getLayoutInflater().inflate(
				R.layout.read_only_location, parent);
		return row;
	}

	public View buildImagePicker(Attribute attribute, ViewGroup parent) {

		ViewGroup row = (ViewGroup) viewContext.getLayoutInflater().inflate(
				R.layout.image_selection, parent);
		return getFirstNonLabelView(row);
	}

	public View buildSingleCheckbox(Attribute attribute, ViewGroup parent) {
		ViewGroup row = (ViewGroup) viewContext.getLayoutInflater().inflate(
				R.layout.input_single_checkbox_view, parent);
		CheckBox checkbox = (CheckBox) getFirstNonLabelView(row);
		return checkbox;
	}

	private Spinner buildSpinner(Attribute attribute, ViewGroup parent) {

		ViewGroup row = (ViewGroup) viewContext.getLayoutInflater().inflate(
				R.layout.input_spinner_view, parent);

		Spinner spinner = (Spinner) row.findViewById(R.id.spinner);
		spinner.setPrompt("Select " + attribute.description);
		ArrayAdapter<AttributeOption> adapter = new ArrayAdapter<AttributeOption>(viewContext,
				android.R.layout.simple_spinner_item, attribute.options);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		return spinner;
	}

	private MultiSpinner buildMultiSpinner(Attribute attribute, ViewGroup parent) {

		ViewGroup row = (ViewGroup) viewContext.getLayoutInflater().inflate(
				R.layout.input_multi_spinner_view, parent);

		// get the spinner child
		MultiSpinner multiSpinner = (MultiSpinner) getFirstNonLabelView(row);
		multiSpinner.setPrompt("Select " + attribute.description);

		return multiSpinner;
	}

	private View getFirstNonLabelView(ViewGroup row) {
		int i;
		for (i = 0; i < row.getChildCount(); i++) {
			if (!(row.getChildAt(i) instanceof TextView)) {
				break;
			} else if (row.getChildAt(i) instanceof EditText) {
				break;
			}
		}
		return row.getChildAt(i);
	}

	public void buildSurveyName(Survey survey, ViewGroup parent) {

		ViewGroup row = (ViewGroup) viewContext.getLayoutInflater().inflate(R.layout.survey_layout,
				parent);
		TextView name = (TextView) row.findViewById(android.R.id.text1);
		name.setText(survey.name);
		TextView description = (TextView) row.findViewById(android.R.id.text2);
		description.setText(survey.description);

	}

}
