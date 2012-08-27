package au.org.ala.fielddata.mobile;

import android.os.Bundle;
import au.org.ala.fielddata.mobile.model.SurveyViewModel;

import com.actionbarsherlock.app.SherlockFragment;


/**
 * Makes use of the behaviour that fragments can survive the re-creation
 * of their containing activity to keep a reference to the view model
 * alive during configuration changes.
 */
public class SurveyModelHolder extends SherlockFragment {

	private SurveyViewModel model;
	
	public SurveyModelHolder(SurveyViewModel model) {
		this.model = model;
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		setRetainInstance(true);
		
		if (savedInstanceState == null) {
			((CollectSurveyData)getActivity()).setViewModel(model);
		}
		
	}
	
	
}
