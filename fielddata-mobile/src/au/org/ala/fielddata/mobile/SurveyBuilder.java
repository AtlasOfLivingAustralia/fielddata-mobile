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

import java.util.List;

import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.Attribute.AttributeOption;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.model.SurveyViewModel;
import au.org.ala.fielddata.mobile.ui.CategorizedSpinner;
import au.org.ala.fielddata.mobile.ui.MultiSpinner;

/**
 * The SurveyBuilder is responsible for selecting and creating the appropriate
 * input widget for a particular Attribute type.
 */
public class SurveyBuilder {

	private FragmentActivity viewContext;
	private SurveyViewModel model;
	private BinderManager binder;
	
	public SurveyBuilder(FragmentActivity viewContext, SurveyViewModel model, BinderManager binder) {
		this.viewContext = viewContext;
		this.model = model;
		this.binder = binder;
	}

	public void buildSurveyForm(View page, int pageNum) {
		
		TableLayout tableLayout = (TableLayout) page.findViewById(R.id.surveyGrid);
		List<Attribute> pageAttributes = model.getPage(pageNum);

		int rowCount = pageAttributes.size();
		if (pageNum == 0) {
			TableRow row = new TableRow(viewContext);
			buildSurveyName(model.getSurvey(), row);
			addRow(tableLayout, row);
		}
		View previousView = null;
		for (int i = 0; i < rowCount; i++) {
			TableRow row = new TableRow(viewContext);

			Attribute attribute = pageAttributes.get(i);

			View inputView = buildFields(attribute, row);
			
			configureKeyboardBindings(previousView, inputView);
			binder.configureBindings(inputView, attribute);
			
			addRow(tableLayout, row);
			previousView = inputView;
		}

	}

	/**
	 * The purpose of this method is to configure EditText widgets so that:
	 * 1) If the next widget is an EditText, the keyboard has a "next" key.
	 * 2) If the next widget is not an EditText, the keyboard has a "done" key.
	 * This is to prevent the input sequence from jumping from a text field
	 * to the next text field, skipping other widgets (such as spinners) in the
	 * process. 
	 * @param previousView the previously created widget.
	 * @param inputView the widget created for the current field.
	 */
	private void configureKeyboardBindings(View previousView, View inputView) {
		if ((previousView instanceof EditText) &&
			!(inputView instanceof EditText)) {
			int imeOptions = ((EditText)previousView).getImeOptions();
			((EditText)previousView).setImeOptions(imeOptions | EditorInfo.IME_ACTION_DONE);
		}
	}

	private void addRow(TableLayout tableLayout, TableRow row) {
		TableRow.LayoutParams params = new TableRow.LayoutParams();
		params.setMargins(5, 5, 10, 10);
		params.width = TableRow.LayoutParams.MATCH_PARENT;
		params.height = TableRow.LayoutParams.WRAP_CONTENT;
		tableLayout.addView(row, params);
	}
	
	public View buildFields(Attribute attribute, ViewGroup parent) {
		LinearLayout layout = (LinearLayout) viewContext.getLayoutInflater().inflate(
				R.layout.label_and_field, parent);
		ViewGroup container = (ViewGroup) layout.getChildAt(0);
		buildLabel(attribute, container);
		viewContext.getLayoutInflater().inflate(
				R.layout.error_label, container).findViewById(R.id.errorLabel);
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
		case POINT_SOURCE:
		case STRING_WITH_VALID_VALUES:
			view = buildSpinner(attribute, parent);
			break;
		case CATEGORIZED_MULTI_SELECT:
			view = buildCategorizedSpinner(attribute, parent);
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
		
		AttributeOption empty = new AttributeOption();
		AttributeOption[] options = new AttributeOption[attribute.options.length+1];
		options[0] = empty;
		System.arraycopy(attribute.options, 0, options, 1, attribute.options.length);
		
		ArrayAdapter<AttributeOption> adapter = new  ArrayAdapter<AttributeOption>(viewContext,
				R.layout.multiline_spinner_item, options);
		adapter.setDropDownViewResource(R.layout.multiline_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		
		return spinner;
	}

	private Spinner buildCategorizedSpinner(Attribute attribute, ViewGroup parent) {

		ViewGroup row = (ViewGroup) viewContext.getLayoutInflater().inflate(
				R.layout.categorized_spinner_view, parent);

		CategorizedSpinner spinner = (CategorizedSpinner) row.findViewById(R.id.spinner);
		spinner.setPrompt("Select " + attribute.description);
		
		AttributeOption empty = new AttributeOption();
		AttributeOption[] options = new AttributeOption[attribute.options.length+1];
		options[0] = empty;
		System.arraycopy(attribute.options, 0, options, 1, attribute.options.length);
		
		spinner.setItems(options);
		
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
		TextView name = (TextView) row.findViewById(R.id.surveyName);
		name.setText(survey.name);
		name.setFocusableInTouchMode(true);
		name.setFocusable(true);
		TextView description = (TextView) row.findViewById(R.id.surveyDescription);
		description.setText(survey.description);

	}

}
