package au.org.ala.fielddata.mobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import au.org.ala.fielddata.mobile.nrmplus.R;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.dao.SpeciesDAO;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Species;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.model.User;
import au.org.ala.fielddata.mobile.pref.Preferences;
import au.org.ala.fielddata.mobile.service.FieldDataService;
import au.org.ala.fielddata.mobile.service.FieldDataService.SurveyDownloadCallback;
import au.org.ala.fielddata.mobile.service.LoginService;
import au.org.ala.fielddata.mobile.service.dto.LoginResponse;

import com.actionbarsherlock.app.SherlockActivity;

/**
 * Displays a login form to the user and initiates the login process.
 */
public class LoginActivity extends SherlockActivity implements OnClickListener {

	private ProgressDialog pd;
	private String[] portals; 
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_login);
		portals = getResources().getStringArray(R.array.portals);
		if (portals.length > 1) {
			findViewById(R.id.portalLbl).setVisibility(View.VISIBLE);
			findViewById(R.id.portal).setVisibility(View.VISIBLE);	
		}
		else {
		
			Preferences preferences = new Preferences(this);
			preferences.setFieldDataPortalName(portals[0]);
		}
		
		Button button = (Button) findViewById(R.id.loginBtn);
		button.setOnClickListener(this);
		
		Button registrationButton = (Button)findViewById(R.id.registerBtn);
		registrationButton.setOnClickListener(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (pd != null && pd.isShowing()) {
			pd.dismiss();
		}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.loginBtn) {
			
			Preferences preferences = new Preferences(this);
			
			final EditText usernameField = (EditText) findViewById(R.id.username);
			final EditText passwordField = (EditText) findViewById(R.id.userPassword);
			Spinner portal = (Spinner) findViewById(R.id.portal);
			final String portalName;
			if (portals.length > 1) {
				portalName = (String)portal.getSelectedItem();
				preferences.setFieldDataPortalName(portalName);
				
			}
			else {
				portalName = preferences.getFieldDataPortalName();
			}
			
			pd = ProgressDialog.show(LoginActivity.this, "Logging in", 
					preferences.getFieldDataServerUrl(false, true), true, false, null);
			
			new AsyncTask<Void, Integer, Void>() {
				private Exception e;

				public Void doInBackground(Void... args) {
					SurveyDownloadCallback callback = new SurveyDownloadCallback() {
						
						public void surveysDownloaded(int number, int count) {
							publishProgress(number);
						}
					};
					LoginService loginService = new LoginService(LoginActivity.this);

					try {
						String username = usernameField.getText().toString();
						String password = passwordField.getText().toString();
						LoginResponse response = loginService.login(username, password, portalName);

						publishProgress(0);
						clearPersistantData();
						initialiseUserAndSurveys(response, callback);
						// return to the main activity
						finish();

					} catch (Exception e) {
						this.e = e;
						Log.e("LoginActivity", "Login failed, ",e);
					}
					return null;
				}

				
				
				@Override
				protected void onProgressUpdate(Integer... values) {
					int value = values[0];
					if (value == 0) {
						pd.setTitle("Downloading surveys...");
					}
					else {
						pd.setMessage("Downloading survey "+value+"...");
					}
				}

				protected void onPostExecute(Void result) {
					
					if (pd.isShowing()) {
						pd.dismiss();
					}
					
					if (e != null) {

						AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

						builder.setTitle(R.string.login_failed);
						builder.setMessage(R.string.unknown_user);
						builder.setNegativeButton(R.string.close, new Dialog.OnClickListener() {

							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
						builder.show();
					}
				}

			}.execute();
		}
		else if (v.getId() == R.id.registerBtn) {
			Preferences prefs = new Preferences(this);
			String registrationUrl = prefs.getUnproxiedFieldDataServerUrl()+"/condamine/vanilla/usersignup.htm";
			Intent registrationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(registrationUrl));
			startActivity(registrationIntent);
		}
	}

	private void clearPersistantData() {
		
		GenericDAO<User> userDAO = new GenericDAO<User>(this);
		userDAO.deleteAll(User.class);
		
		GenericDAO<Survey> surveyDAO = new GenericDAO<Survey>(this);
		surveyDAO.deleteAll(Survey.class);
		
		GenericDAO<Record> recordDAO = new GenericDAO<Record>(this);
		recordDAO.deleteAll(Record.class);
		
		SpeciesDAO speciesDAO = new SpeciesDAO(this);
		speciesDAO.deleteAll(Species.class);
	}
	
	/**
	 * Persist the user object and downloaded surveys
	 * @param response
	 */
	private void initialiseUserAndSurveys(LoginResponse response, SurveyDownloadCallback callback) {

		GenericDAO<User> userDAO = new GenericDAO<User>(LoginActivity.this);
		userDAO.save(response.user);
		
		new FieldDataService(this).downloadSurveys(callback);

	}
}
