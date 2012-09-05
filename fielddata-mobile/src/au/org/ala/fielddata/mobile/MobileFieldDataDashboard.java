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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.model.User;
import au.org.ala.fielddata.mobile.pref.EditPreferences;
import au.org.ala.fielddata.mobile.pref.Preferences;
import au.org.ala.fielddata.mobile.service.FieldDataService;
import au.org.ala.fielddata.mobile.service.UploadService;
import au.org.ala.fielddata.mobile.ui.MenuHelper;

import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * This class is the main activity for the Mobile Field Data application.
 */
public class MobileFieldDataDashboard extends SherlockFragmentActivity
		implements OnClickListener, OnSharedPreferenceChangeListener {

	private Preferences preferences;
	private TextView status;
	private Spinner surveySelector;
	private ProgressDialog pd;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferences = new Preferences(this);
		setContentView(R.layout.activity_mobile_data_dashboard);

		status = (TextView)findViewById(R.id.status);
		surveySelector = (Spinner)findViewById(R.id.surveySelector);
		addEventHandlers();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(UploadService.UPLOAD_FAILED);
		filter.addAction(UploadService.UPLOADED);
		
		LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				if (UploadService.UPLOAD_FAILED.equals(intent.getAction())) {
					Toast.makeText(getApplicationContext(), "Upload failed!", Toast.LENGTH_SHORT).show();
				}
				refreshPage();
			}
		}, filter);
		
		// check if the preferences are set if not redirect
		if (preferences.getFieldDataServerHostName().equals("") ||
			preferences.getFieldDataContextName().equals("") ||
			preferences.getFieldDataPath().equals("") ||
			preferences.getFieldDataPortalName().equals("")) {
			redirectToPreferences();
		}
	}
	
	

	private void addEventHandlers() {
		ImageButton button = (ImageButton) findViewById(R.id.viewSavedRecordsButton);
		button.setOnClickListener(this);
		button = (ImageButton) findViewById(R.id.newRecordButton);
		button.setOnClickListener(this);
		button = (ImageButton) findViewById(R.id.viewFieldGuideButton);
		button.setOnClickListener(this);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.viewSavedRecordsButton) {
			Intent intent = new Intent(this, ViewSavedRecordsActivity.class);
			startActivity(intent);
		} else if (v.getId() == R.id.newRecordButton) {
			Intent intent = new Intent(this, CollectSurveyData.class);
			intent.putExtra(CollectSurveyData.SURVEY_BUNDLE_KEY, preferences.getCurrentSurvey());
			startActivity(intent);
		} else if (v.getId() == R.id.viewFieldGuideButton) {
			Intent intent = new Intent(this, SpeciesListActivity.class);
			startActivity(intent);
		}

	}

	class InitTask extends AsyncTask<Void, Void, InitialisationResults> {

		@Override
		protected InitialisationResults doInBackground(Void... params) {
			return init();
		}

		@Override
		protected void onPostExecute(InitialisationResults result) {

			if (result.success && isInitialised()) {
				if (result.online) {
					status.setText("Online");
				}
				else {
					status.setText("Offline");
				}
				updateSurveyList(result.surveys);
				updateRecordCount(result.recordCount);
			} else {
				if (!result.online) {
					showConnectionError();
				}
			}
			pd.dismiss();
		}
		
	}
	

	@Override
	protected void onResume() {
		super.onResume();
		// will redirect if not logged in
		if (!redirectToLogin()) {
			refreshPage();
		}
	}

	private void refreshPage() {
		
		pd = ProgressDialog.show(this, "", 
				"Updating Survey List", true, false, null);
		new InitTask().execute();
		
		String portal = preferences.getFieldDataPortalName();
		getSupportActionBar().setTitle(Utils.bold(portal));
		
		GenericDAO<User> userDAO = new GenericDAO<User>(this);
		List<User> users = userDAO.loadAll(User.class);
		if (users.size() > 0) {
			User user = users.get(0);
			getSupportActionBar().setSubtitle(Utils.bold("Welcome " + user.firstName + " " + user.lastName));
		}
	}
	
	class InitialisationResults {
		public boolean online;
		public boolean success = true;
		public List<Survey> surveys;
		public int recordCount;

	}

	private InitialisationResults init() {
		InitialisationResults results = new InitialisationResults();
		results.online = canAccessFieldDataServer();
		/*
		if (!isInitialised()) {

			if (results.online) {
				Log.i("Status", "Online");

				// do first time initialisation.
				try {
					downloadSurveys();
				} catch (Exception e) {
					results.success = false;
				}

			} else {
				Log.i("Status", "Offline");

				// Can't do much here.
				results.success = false;
			}
		}*/
		if (results.success && isInitialised()) {
			populateResults(results);
		}
		return results;
	}

	private void populateResults(InitialisationResults results) {
		GenericDAO<Survey> surveyDAO = new GenericDAO<Survey>(this);
		results.surveys = surveyDAO.loadAll(Survey.class);
		GenericDAO<Record> recordDAO = new GenericDAO<Record>(
				MobileFieldDataDashboard.this);

		results.recordCount = recordDAO.count(Record.class);
	}

	private void showConnectionError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.initialisationErrorTitle);
		builder.setMessage(String.format(
				getResources().getString(R.string.initialisationError),
				preferences.getFieldDataServerUrl()));
		builder.setNegativeButton(R.string.close, new Dialog.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.show();
		return;
	}
	
	private void redirectToPreferences() {
		Intent intent = new Intent(this, EditPreferences.class);
		startActivity(intent);
	}
	
	private boolean redirectToLogin() {
		boolean didRedirect = false;
		if (!isInitialised() && canAccessFieldDataServer()) {
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
			didRedirect = true;
		} 
		
		return didRedirect;
	}

	private boolean isInitialised() {

		String sessionKey = preferences.getFieldDataSessionKey();
		return sessionKey != null;
	}

	private boolean canAccessFieldDataServer() {
		boolean success = false;
		String fieldDataServer = preferences.getFieldDataServerHostName();
		try {
			FieldDataService service = new FieldDataService(this);
			success = service.ping(5000);
			if (!success) {
				Log.i("Status", "Field data server at: " + fieldDataServer
						+ " is not reachable");
			}
			// ConnectivityManager manager =
			// (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
			// success = manager.requestRouteToHost(networkType, hostAddress)
		} catch (Exception e) {
			Log.e("Error", "Unable to location field data server at: "
					+ preferences.getFieldDataServerHostName(), e);
		}
		return success;
	}
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
    	inflater.inflate(R.menu.common_menu_items, menu);
    	inflater.inflate(R.menu.dashboard_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.sync) {
			refreshPage();
			return true;
		}
		return new MenuHelper(this).handleMenuItemSelection(item);
    }

	private void updateSurveyList(List<Survey> surveys) {
		final Survey[] surveyArray = surveys
				.toArray(new Survey[surveys.size()]);

		if (surveys.size() > 0) {
			ArrayAdapter<Survey> items = new ArrayAdapter<Survey>(
					MobileFieldDataDashboard.this,
					android.R.layout.simple_spinner_item, surveyArray);
			items.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			surveySelector.setAdapter(items);
			surveySelector.setOnItemSelectedListener(new OnItemSelectedListener() {
			
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					Survey survey = surveyArray[position];
					if (survey != null) {
						preferences.setCurrentSurveyName(survey.name);
						preferences.setCurrentSurvey(survey.server_id);
					}
				}

				public void onNothingSelected(AdapterView<?> parent) {
					
				}
	
			});
			Integer selected = preferences.getCurrentSurvey();
			int index = -1;
			if (selected != null) {
				for (int i = 0; i < surveyArray.length; i++) {
					if (surveyArray[i].getId() == selected) {
						index = i;
						break;
					}
				}

				System.out.println("Selected: " + selected);
				System.out.println("Selected index: " + index);

				if (index >= 0) {
					getSupportActionBar().setSelectedNavigationItem(index);
				}
			}
		} else {
			ArrayAdapter<String> items = new ArrayAdapter<String>(
					MobileFieldDataDashboard.this,
					R.layout.sherlock_spinner_item,
					new String[] { "No surveys" });
			getSupportActionBar().setListNavigationCallbacks(items,
					new OnNavigationListener() {

						public boolean onNavigationItemSelected(
								int itemPosition, long itemId) {
							return false;
						}
					});
		}
	}

	private void updateRecordCount(int count) {
		TextView view = (TextView) findViewById(R.id.noSavedRecords);
		if (count == 0) {
			view.setText(getResources().getString(
					R.string.no_saved_records_message));
		} else if (count == 1) {
			view.setText(getResources()
					.getString(R.string.saved_record_message));
		} else {
			view.setText(String.format(
					getResources().getString(R.string.saved_records_message),
					count));
		}
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("serverHostName") || 
			key.equals("contextName") ||	
			key.equals("path") ||
			key.equals("portalName")) {
			preferences.setFieldDataSessionKey(null);
		}
	}

	
}
