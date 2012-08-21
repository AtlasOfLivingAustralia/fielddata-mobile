package au.org.ala.fielddata.mobile.pref;

import android.annotation.TargetApi;
import android.os.Bundle;

@TargetApi(11)
public class PreferenceFragment extends android.preference.PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int preferenceResourceId = getActivity().getResources().getIdentifier(
				getArguments().getString("resource"), "xml", getActivity().getPackageName());
		addPreferencesFromResource(preferenceResourceId);
	}

	
}
