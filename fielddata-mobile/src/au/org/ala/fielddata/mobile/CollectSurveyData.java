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

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Species;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.service.FieldDataService;
import au.org.ala.fielddata.mobile.service.LoginService;
import au.org.ala.fielddata.mobile.ui.SpeciesSelectionListener;
import au.org.ala.fielddata.mobile.validation.Binder;
import au.org.ala.fielddata.mobile.validation.DateBinder;
import au.org.ala.fielddata.mobile.validation.LocationBinder;
import au.org.ala.fielddata.mobile.validation.RequiredValidator;
import au.org.ala.fielddata.mobile.validation.SpinnerBinder;
import au.org.ala.fielddata.mobile.validation.TextViewBinder;
import au.org.ala.fielddata.mobile.validation.Validator;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

public class CollectSurveyData extends SherlockFragmentActivity implements SpeciesSelectionListener {

	public static final String SURVEY_BUNDLE_KEY = "SurveyIdKey";
	public static final String RECORD_BUNDLE_KEY = "RecordIdKey";
    
	public static final int SELECT_LOCATION_REQUEST = 1;
	
	private SurveyViewModel surveyViewModel;
	private BinderManager binder;
	private SurveyBuilder builder;
	private SurveyPagerAdapter pagerAdapter;
	private ViewPager pager;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binder = new BinderManager();
		
        int surveyId = getIntent().getIntExtra(SURVEY_BUNDLE_KEY, 0);
        int recordId = getIntent().getIntExtra(RECORD_BUNDLE_KEY, 0);
        
        if (savedInstanceState != null) {
        	if (surveyId == 0) {
            	surveyId = savedInstanceState.getInt(SURVEY_BUNDLE_KEY);
            }
            if (recordId == 0) {
            	recordId = savedInstanceState.getInt(RECORD_BUNDLE_KEY);
            }
        }
        
        
        setContentView(R.layout.activity_collect_survey_data);
        Record record = null;
        Survey survey;
        if (recordId > 0) {
        	record = initRecord(recordId, surveyId);
        	surveyId = record.survey_id;
        }
        try {
        	survey = initSurvey(surveyId);
        }
        catch (Exception e) {
        	throw new RuntimeException(e);
        }
        if (recordId <= 0) {
        	record = initRecord(recordId, surveyId);
        }
        surveyViewModel = new SurveyViewModel(survey, record);
        builder = new SurveyBuilder(this, surveyViewModel);
        
        pagerAdapter = new SurveyPagerAdapter(getSupportFragmentManager());
        pager = (ViewPager)findViewById(R.id.surveyPager);
        pager.setAdapter(pagerAdapter);
        
        getSupportActionBar().setTitle(survey.name);
        getSupportActionBar().setSubtitle(survey.description);
       
        
        
    }

	
	private Survey initSurvey(int surveyId) throws Exception {
		GenericDAO<Survey> surveyDAO = new GenericDAO<Survey>(this);
		return surveyDAO.findByServerId(Survey.class, surveyId);	   
	}
	
	public void onSpeciesSelected(Species selectedSpecies) {
		
		surveyViewModel.speciesSelected(selectedSpecies);
		pager.setCurrentItem(1);
		SpannableString title = new SpannableString(selectedSpecies.scientificName);
        title.setSpan(new StyleSpan(Typeface.ITALIC), 0, title.length(), 0);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setSubtitle(selectedSpecies.commonName);
       
	}
	
	public void onLocationSelected(Location location) {
		binder.locationChanged(location);
	}
	private Record initRecord(int recordId, int surveyId) {
		Record record;
		if (recordId <= 0) {
			record = new Record();
			record.survey_id = surveyId;
			record.when = System.currentTimeMillis();
		
		}
		else {
			GenericDAO<Record> recordDAO = new GenericDAO<Record>(this);
			record = recordDAO.load(Record.class, recordId);
		}
		return record;
	}
	
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.activity_mobile_field_data, menu);
        return true;
    }
    
    private void buildSurveyForm(View page, int pageNum) {
    	TableLayout tableLayout = (TableLayout)page.findViewById(R.id.surveyGrid);
    	Display display = getWindowManager().getDefaultDisplay();
    	@SuppressWarnings("deprecation")
		int width = display.getWidth();
    	Log.d("Size", "Width: "+width);
    	
    	
    	boolean twoColumns = false;
    	if (twoColumns) {
    		tableLayout.setColumnStretchable(1, true);
    	}
    	else {
    		tableLayout.setColumnStretchable(0, true);
    	}
    	
    	List<Attribute> pageAttributes = surveyViewModel.getPage(pageNum);;
    	int rowCount = pageAttributes.size();
    	for (int i=0; i<rowCount; i++) {
    		TableRow row = new TableRow(this);
    		Attribute attribute = pageAttributes.get(i);
    		
    		row.addView(builder.buildLabel(attribute));
    		
    		if (!twoColumns) {
    			tableLayout.addView(row);
    			row = new TableRow(this);
    		}
    		View inputView = builder.buildInput(attribute);
    		binder.configureBindings(inputView, attribute);
    		row.addView(inputView);
    	
    		tableLayout.addView(row);
    	}
    	
    	Button saveButton = (Button)page.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				binder.bindAll();
				new SaveRecordTask().execute(surveyViewModel.getRecord());
				
			}
		});
    }
    
    class BinderManager {
    	private List<Binder> binders;
    	private LocationBinder locationBinder;
    	
    	public BinderManager() {
    		binders = new ArrayList<Binder>();
    	}
    	
    	public void configureBindings(View view, Attribute attribute) {
    		
    		Record record = surveyViewModel.getRecord();
    		
    		Binder binder = null;
    		// Some attribute types require special bindings.
    		switch(attribute.getType()) {
    		case WHEN:
    		case TIME:
    			binder = new DateBinder(CollectSurveyData.this, view, attribute, record, validatorFor(attribute));
    			break;
    		case POINT:
    			locationBinder = new LocationBinder(view, record);
    			binder = locationBinder;
    			break;
    		default:
    			binder = bindByViewClass(view, attribute);
    			break;
    		}
    		
    		
    		if (binder != null) {
    			binders.add(binder);
    		}
    	}
    	
    	public void locationChanged(Location location) {
    		if (locationBinder != null) {
    			locationBinder.locationChanged(location);
    		}
    	}
    	
    	private Binder bindByViewClass(View view, Attribute attribute) {
    		Record record = surveyViewModel.getRecord();
    		Binder binder = null;
    		if (view instanceof TextView) {
    			binder = new TextViewBinder(CollectSurveyData.this, (TextView)view, attribute, record,  validatorFor(attribute));
    			
    		}
    		else if (view instanceof Spinner) {
    			binder = new SpinnerBinder(CollectSurveyData.this, (Spinner)view, attribute, record, validatorFor(attribute));
    			
    		}
    		return binder;
    	}
    	
    	public void bindAll() {
    		for (Binder binder : binders) {
    			binder.bind();
    		}
    	}
    	
    	private Validator validatorFor(Attribute attribute) {
    		
    		Validator validator = null;
    		if (attribute.required != null && attribute.required) {
    			validator = new RequiredValidator();
    		}
    		return validator;
    		
    	}
    	
    	
    }
    
    
    class SurveyPagerAdapter extends FragmentPagerAdapter {

    	public SurveyPagerAdapter(FragmentManager manager) {
    		super(manager);
    	}
    	
		@Override
		public Fragment getItem(int page) {
	
			SurveyPage surveyPage = new SurveyPage();
			Bundle args = new Bundle();
			args.putInt("pageNum", page);
			surveyPage.setArguments(args);
			return surveyPage;
		}

		@Override
		public int getCount() {
			return surveyViewModel.getPageCount();
		}
    	
    }
    
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == SELECT_LOCATION_REQUEST) {
            if (resultCode == RESULT_OK) {
                onLocationSelected((Location)data.getExtras().get(LocationSelectionActivity.LOCATION_BUNDLE_KEY));
            }
        }
    }
    
    public static class SurveyPage extends Fragment {

    	private int pageNum;
    	
    	@Override
    	public void onCreate(Bundle savedInstanceState) {
    		super.onCreate(savedInstanceState);
    		pageNum = getArguments() != null ? getArguments().getInt("pageNum") : 0;
    		
    	}
    	
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.survey_data_page, container, false);
			((CollectSurveyData)getActivity()).buildSurveyForm(view, pageNum);
			return view;
		}
    	
    }
    
    
    class SaveRecordTask extends AsyncTask<Record, Void, Boolean> {

        
		@Override
		protected Boolean doInBackground(Record... params) {
			boolean success = true;
			try {
				
				GenericDAO<Record> recordDao = new GenericDAO<Record>(getApplicationContext());
				recordDao.save(surveyViewModel.getRecord());
				
				
				LoginService loginService = new LoginService(CollectSurveyData.this);
				loginService.login();
				
				FieldDataService recordService = new FieldDataService(CollectSurveyData.this);
				List<Record> records = new ArrayList<Record>();
				records.add(surveyViewModel.getRecord());
				
				recordService.sync(records);
				
			}
			catch (Exception e) {
				success = false;
				Log.e("SurveyUpload", "Upload failed", e);
			}
			return success;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			String message = result ? "Upload successful!" : "Upload failed!";
			Toast.makeText(CollectSurveyData.this, message , Toast.LENGTH_SHORT).show();
			finish();
		}
    	
    }
}
