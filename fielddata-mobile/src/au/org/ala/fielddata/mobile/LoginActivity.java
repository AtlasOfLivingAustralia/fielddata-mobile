package au.org.ala.fielddata.mobile;

import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.pref.Preferences;
import au.org.ala.fielddata.mobile.service.FieldDataService;
import au.org.ala.fielddata.mobile.service.LoginService;

import com.actionbarsherlock.app.SherlockActivity;

public class LoginActivity extends SherlockActivity implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		Button button = (Button) findViewById(R.id.loginBtn);
		button.setOnClickListener(this);
	}

	public void onClick(View v) {
		if (v.getId() == R.id.loginBtn) {
			final EditText username = (EditText) findViewById(R.id.username);
			final EditText password = (EditText) findViewById(R.id.userPassword);
			final String portalName = new Preferences(this).getFieldDataPortalName();

			new AsyncTask<Void, Void, Void>() {
				private Exception e;

				public Void doInBackground(Void... args) {
					LoginService loginService = new LoginService(LoginActivity.this);

					try {
						loginService.login(username.getText().toString(), password.getText()
								.toString(), portalName);

						downloadSurveys();
						// return to the main activity
						finish();

					} catch (Exception e) {
						this.e = e;
					}
					return null;
				}

				protected void onPostExecute(Void result) {
					if (e != null) {

						AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);

						builder.setTitle(R.string.login_failed);
						builder.setMessage(R.string.unknown_user);
						builder.setNegativeButton(R.string.close, new Dialog.OnClickListener() {

							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
							}
						});
						builder.show();
					}
				}

			}.execute();
		}
	}

	private void downloadSurveys() {

		FieldDataService service = new FieldDataService(LoginActivity.this);
		List<Survey> surveys = service.downloadSurveys();

		GenericDAO<Survey> surveyDAO = new GenericDAO<Survey>(LoginActivity.this);
		for (Survey survey : surveys) {
			surveyDAO.save(survey);
		}

	}
}
