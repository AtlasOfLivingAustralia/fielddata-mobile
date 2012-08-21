package au.org.ala.fielddata.mobile.pref;

import android.content.Context;
import android.content.SharedPreferences.Editor;

public class Preferences {

	private static final String PREFERENCES_NAME = "MobileFieldDataPreferences";
	private static final String SURVEY_KEY = "SurveyPreference";
	private static final String SURVEY_NAME_KEY = "SurveyNamePreference";
	private static final String SESSION_KEY = "Session";
	private Context ctx; 
	
	public Preferences(Context ctx) {
		this.ctx = ctx;
	}
	
	public void setCurrentSurvey(Integer id) {
		Editor preferences = preferencesEditor();
		preferences.putInt(SURVEY_KEY, id).commit();
	}
	
	public Integer getCurrentSurvey() {
		return ctx.getSharedPreferences(PREFERENCES_NAME, 0).getInt(SURVEY_KEY, -1);
	}
	
	public void setCurrentSurveyName(String name) {
		Editor preferences = preferencesEditor();
		preferences.putString(SURVEY_NAME_KEY, name).commit();
	
	}
	
	public String getCurrentSurveyName() {
		return ctx.getSharedPreferences(PREFERENCES_NAME, 0).getString(SURVEY_NAME_KEY, "No survey");
	}
	
	private Editor preferencesEditor() {
		return ctx.getSharedPreferences(PREFERENCES_NAME, 0).edit();
	}

	public String getFieldDataServerUrl() {
		return "http://"+getFieldDataServerHostName()+":8080/BDRS/condamine";
	}
	
	public String getFieldDataServerHostName() {
		return "152.83.195.62";
		//return "192.168.0.8";
	}
	
	public String getFieldDataSessionKey() {
		return ctx.getSharedPreferences(PREFERENCES_NAME, 0).getString(SESSION_KEY, null);
	}
	
	public void setFieldDataSessionKey(String sessionKey) {
		Editor preferences = preferencesEditor();
		preferences.putString(SESSION_KEY, sessionKey).commit();
	
	}
}
