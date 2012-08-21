package au.org.ala.fielddata.mobile.pref;

import java.util.List;

import android.os.Build;
import android.os.Bundle;
import au.org.ala.fielddata.mobile.R;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class EditPreferences extends SherlockPreferenceActivity {

	@SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
        	addPreferencesFromResource(R.xml.preference1);
        }
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
    	loadHeadersFromResource(R.xml.preference_headers, target);
    }
}
