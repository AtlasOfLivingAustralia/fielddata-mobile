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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.model.User;
import au.org.ala.fielddata.mobile.pref.EditPreferences;
import au.org.ala.fielddata.mobile.pref.Preferences;
import au.org.ala.fielddata.mobile.service.FieldDataService;
import au.org.ala.fielddata.mobile.service.FieldDataServiceClient;
import au.org.ala.fielddata.mobile.service.LocationServiceHelper;
import au.org.ala.fielddata.mobile.ui.MenuHelper;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

/**
 * This class is the main activity for the Mobile Field Data application.
 */
public class MobileFieldDataDashboard extends SherlockFragmentActivity
		implements OnSharedPreferenceChangeListener {

	private Preferences preferences;
	private TextView status;
	private ListView surveyList;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setContentView(R.layout.activity_mobile_data_dashboard);
		
		preferences = new Preferences(this);
		
		surveyList = (ListView)findViewById(R.id.surveyList);
		status = (TextView)findViewById(R.id.status);
		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
			LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				showNoGpsDialog();
			}
		}
		Intent locationIntent = new Intent(this, LocationServiceHelper.class);
		startService(locationIntent);
		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Intent locationIntent = new Intent(MobileFieldDataDashboard.this, LocationServiceHelper.class);
		
		stopService(locationIntent);
	}
	
	private void showNoGpsDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    builder.setTitle("Enable GPS?")
	           .setMessage("The GPS on this device is currently disabled, do you want to enable it? \nEnabling GPS will allow accurate survey locations to be recorded.")
	           .setCancelable(false)
	           .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	                   startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	               }
	           })
	           .setNegativeButton("No", new DialogInterface.OnClickListener() {
	               public void onClick(final DialogInterface dialog, final int id) {
	                    dialog.cancel();
	               }
	           });
	    builder.create().show();
	}

	class Model {
		private List<Survey> surveys;
		private User user;
		private String portal;
		public Model(List<Survey> surveys, User user, String portal) {
			this.surveys = surveys;
			this.user = user;
			this.portal = portal;
		}
		public List<Survey> getSurveys() {
			return surveys;
		}
		public User getUser() {
			return user;
		}
		public String getPortal() {
			return portal;
		}
		
	}
	
	class InitDataTask extends AsyncTask<Void, Void, Model> {
		
		@Override
		protected Model doInBackground(Void... params) {
			GenericDAO<Survey> surveyDAO = new GenericDAO<Survey>(getApplicationContext());
			List<Survey> surveys = surveyDAO.loadAll(Survey.class);
			
			GenericDAO<User> userDAO = new GenericDAO<User>(getApplicationContext());
			List<User> users = userDAO.loadAll(User.class);
			User user = null;
			if (users.size() > 0) {
				user = users.get(0);
				
			}
			String portal = preferences.getFieldDataPortalName();
			
			return new Model(surveys, user, portal);
		}
		@Override
		protected void onPostExecute(Model model) {
			getSupportActionBar().setTitle(Utils.bold(model.getPortal()));
			
			User user = model.getUser();
			if (user != null) {
				getSupportActionBar().setSubtitle(Utils.bold("Welcome " + user.firstName + " " + user.lastName));
			}
			updateSurveyList(model.getSurveys());
			
		}
	}
	
	class StatusTask extends AsyncTask<Void, Void, AppStatus> {

		@Override
		protected AppStatus doInBackground(Void... params) {
			return checkStatus();
		}

		@Override
		protected void onPostExecute(AppStatus result) {

			if (result.isInitialised()) {
				if (result.isOnline()) {
					status.setText("Online");
				}
				else {
					status.setText("Offline");
				}				
			} else {
				if (!result.isOnline()) {
					showConnectionError();
				}
				else {
					redirectToLogin();
				}
			}
			setSupportProgressBarIndeterminateVisibility(false);
		}
		
	}
	
	@Override
	protected void onResume() {
		
		super.onResume();
		
		refreshPage();
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.unregisterOnSharedPreferenceChangeListener(this);
		
	}

	private void refreshPage() {
		
		
		PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preference1, true);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		// check if the preferences are set if not redirect
		if (preferences.getFieldDataServerHostName().equals("") ||
			preferences.getFieldDataContextName().equals("")) {
			redirectToPreferences();
		}
		else {
			setSupportProgressBarIndeterminateVisibility(true);
			
			new InitDataTask().execute();
			new StatusTask().execute();
		}
	}
	
	
	class AppStatus {
		
		private boolean online;
		private boolean initialised;

		public AppStatus(boolean initialised, boolean online) {
			this.online = online;
			this.initialised = initialised;
		}
		
		public boolean isOnline() {
			return online;
		}
		
		public boolean isInitialised() {
			return initialised;
		}
		
	}

	private AppStatus checkStatus() {
		
		boolean online = canAccessFieldDataServer();
		boolean initialised = isLoggedIn();
		return new AppStatus(initialised, online);
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
	
	private void redirectToLogin() {
		
        Intent intent = new Intent(this, LoginActivity.class);
	    startActivity(intent);
			
	}

	private boolean isLoggedIn() {

		String sessionKey = preferences.getFieldDataSessionKey();
		return sessionKey != null;
	}

	private boolean canAccessFieldDataServer() {
		boolean success = false;
		String fieldDataServer = preferences.getFieldDataServerHostName();
		try {
			FieldDataServiceClient service = new FieldDataServiceClient(this);
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
			setSupportProgressBarIndeterminateVisibility(true);
			
			new AsyncTask<Void, Void, Void>() {
				
				
			    @Override
				protected Void doInBackground(Void... params) {
			    	new FieldDataService(MobileFieldDataDashboard.this).downloadSurveys();
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					refreshPage();
				}
			}.execute();
			
			refreshPage();
			return true;
		}
		return new MenuHelper(this).handleMenuItemSelection(item);
    }

	private void updateSurveyList(List<Survey> surveys) {
		final Survey[] surveyArray = surveys
				.toArray(new Survey[surveys.size()]);

		if (surveys.size() > 0) {
			SurveyListAdapter items = new SurveyListAdapter(this, surveys);
			surveyList.setAdapter(items);
			surveyList.setOnItemClickListener(new OnItemClickListener() {
			
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Survey survey = surveyArray[position];
					if (survey != null) {
						preferences.setCurrentSurveyName(survey.name);
						preferences.setCurrentSurvey(survey.server_id);
						((SurveyListAdapter)surveyList.getAdapter()).refresh();
						
					}
				}
			});
			Integer selected = preferences.getCurrentSurvey();
			if (selected == null || selected <= 0) {
				preferences.setCurrentSurvey(surveyArray[0].server_id);
				preferences.setCurrentSurveyName(surveyArray[0].name);
			}
		} else {
			ArrayAdapter<String> items = new ArrayAdapter<String>(
					MobileFieldDataDashboard.this,
					R.layout.sherlock_spinner_item,
					new String[] { "No surveys" });
			surveyList.setAdapter(items);
			
		}
	}
	
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("serverHostName") || 
			key.equals("contextName"))  {
			preferences.setFieldDataSessionKey(null);
		}
	}

	
	class SurveyListAdapter extends ArrayAdapter<Survey> {
		public SurveyListAdapter(Context context, List<Survey> surveys) {
			super(context, R.layout.survey_row, R.id.surveyName, surveys);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			Survey survey = getItem(position);
			View row = super.getView(position, convertView, parent);
			TextView name = (TextView)row.findViewById(R.id.surveyName);
			name.setText(survey.name);
			TextView description = (TextView)row.findViewById(R.id.surveyDescription);
			description.setText(survey.description);
			
			ImageView defaultIcon = (ImageView)row.findViewById(R.id.defaulticon);
			if (survey.server_id.equals(preferences.getCurrentSurvey())) {
				defaultIcon.setVisibility(View.VISIBLE);
			}
			else {
				defaultIcon.setVisibility(View.GONE);
			}
			
			return row;
			
		}
		
		public void refresh() {
			notifyDataSetChanged();
		}
		
	}
	
}
