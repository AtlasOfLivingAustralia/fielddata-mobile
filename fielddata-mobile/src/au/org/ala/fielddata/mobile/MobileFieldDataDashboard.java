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

import java.net.InetAddress;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.pref.Preferences;
import au.org.ala.fielddata.mobile.service.FieldDataService;
import au.org.ala.fielddata.mobile.service.LoginService;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * This class is the main activity for the Mobile Field Data application.
 */
public class MobileFieldDataDashboard extends SherlockFragmentActivity
		implements OnClickListener {

	private Preferences preferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferences = new Preferences(this);
		setContentView(R.layout.activity_mobile_data_dashboard);

		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		addEventHandlers();

		new InitTask().execute();

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

			if (result.success) {
				updateSurveyList(result.surveys);
				updateRecordCount(result.recordCount);
			} else {
				showErrorAndClose();
			}
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
		}
		if (results.success) {
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

	private void showErrorAndClose() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.initialisationErrorTitle);
		builder.setMessage(String.format(
				getResources().getString(R.string.initialisationError),
				preferences.getFieldDataServerUrl()));
		builder.setNegativeButton(R.string.close, new Dialog.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		builder.show();
		return;
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

		new MenuInflater(this).inflate(R.menu.dashboard_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.sync) {
			new InitTask().execute();
			return true;
		}
		return false;
    }

	private void updateSurveyList(List<Survey> surveys) {
		final Survey[] surveyArray = surveys
				.toArray(new Survey[surveys.size()]);

		if (surveys.size() > 0) {
			ArrayAdapter<Survey> items = new ArrayAdapter<Survey>(
					MobileFieldDataDashboard.this,
					R.layout.sherlock_spinner_item, surveyArray);

			getSupportActionBar().setListNavigationCallbacks(items,
					new OnNavigationListener() {

						public boolean onNavigationItemSelected(
								int itemPosition, long itemId) {

							Survey survey = surveyArray[itemPosition];
							if (survey != null) {
								preferences.setCurrentSurveyName(survey.name);
								preferences.setCurrentSurvey(survey.server_id);
							}
							return false;
						}
					});
			Integer selected = preferences.getCurrentSurvey();
			int index = -1;
			if (selected != null) {
				for (int i = 0; i < surveyArray.length; i++) {
					if (surveyArray[i].id == selected) {
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

	private void downloadSurveys() {

		LoginService loginService = new LoginService(
				MobileFieldDataDashboard.this);
		loginService.login();
		FieldDataService service = new FieldDataService(
				MobileFieldDataDashboard.this);
		List<Survey> surveys = service.downloadSurveys();

		GenericDAO<Survey> surveyDAO = new GenericDAO<Survey>(
				MobileFieldDataDashboard.this);
		for (Survey survey : surveys) {
			surveyDAO.save(survey);
		}

	}
}
