package au.org.ala.fielddata.mobile;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Species;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.model.User;
import au.org.ala.fielddata.mobile.pref.Preferences;
import au.org.ala.fielddata.mobile.service.FieldDataService;
import au.org.ala.fielddata.mobile.service.LoginResponse;
import au.org.ala.fielddata.mobile.service.LoginService;

import com.actionbarsherlock.app.SherlockActivity;

public class LoginActivity extends SherlockActivity implements OnClickListener {

	private ProgressDialog pd;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		Button button = (Button) findViewById(R.id.loginBtn);
		button.setOnClickListener(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (pd.isShowing()) {
			pd.dismiss();
		}
	}

	public void onClick(View v) {
		if (v.getId() == R.id.loginBtn) {
			
			Preferences preferences = new Preferences(this);
			
			final EditText username = (EditText) findViewById(R.id.username);
			final EditText password = (EditText) findViewById(R.id.userPassword);
			final String portalName = preferences.getFieldDataPortalName();

			pd = ProgressDialog.show(LoginActivity.this, "Logging in", 
					preferences.getFieldDataServerUrl(), true, false, null);
			
			new AsyncTask<Void, Void, Void>() {
				private Exception e;

				public Void doInBackground(Void... args) {
					
					LoginService loginService = new LoginService(LoginActivity.this);

					try {
						LoginResponse response = loginService.login(username.getText().toString(), password.getText()
								.toString(), portalName);

						clearPersistantData();
						initialiseUserAndSurveys(response);
						// return to the main activity
						finish();

					} catch (Exception e) {
						this.e = e;
					}
					return null;
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
	}

	private void clearPersistantData() {
		
		GenericDAO<User> userDAO = new GenericDAO<User>(this);
		userDAO.deleteAll(User.class);
		
		GenericDAO<Survey> surveyDAO = new GenericDAO<Survey>(this);
		surveyDAO.deleteAll(Survey.class);
		
		GenericDAO<Record> recordDAO = new GenericDAO<Record>(this);
		recordDAO.deleteAll(Record.class);
		
		GenericDAO<Species> speciesDAO = new GenericDAO<Species>(this);
		speciesDAO.deleteAll(Species.class);
	}
	
	/**
	 * Persist the user object and downloaded surveys
	 * @param response
	 */
	private void initialiseUserAndSurveys(LoginResponse response) {

		GenericDAO<User> userDAO = new GenericDAO<User>(LoginActivity.this);
		userDAO.save(response.user);
		
		FieldDataService service = new FieldDataService(LoginActivity.this);
		List<Survey> surveys = service.downloadSurveys();

		GenericDAO<Survey> surveyDAO = new GenericDAO<Survey>(LoginActivity.this);
		for (Survey survey : surveys) {
			surveyDAO.save(survey);
		}

	}
}
