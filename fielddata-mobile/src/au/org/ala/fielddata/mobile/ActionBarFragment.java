package au.org.ala.fielddata.mobile;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.pref.Preferences;
import au.org.ala.fielddata.mobile.ui.NumberedImageButton;

/**
 * Controller for the action bar displayed at the bottom of most activities
 * in the application.
 */
public class ActionBarFragment extends Fragment implements OnClickListener {

	private NumberedImageButton savedRecords;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	
		View actions = inflater.inflate(R.layout.action_bar, container);
		addEventHandlers(actions);
		setRetainInstance(true);
		
		return actions;
	}
	
	
	/**
	 * Counts the number of saved records and updates the display on the saved 
	 * records button.	
	 */
	@Override
	public void onResume() {
		super.onResume();
		
		new AsyncTask<Void, Void, Integer>() {

			@Override
			protected Integer doInBackground(Void... params) {
				GenericDAO<Record> recordDAO = new GenericDAO<Record>(getActivity());
				return recordDAO.count(Record.class);
			}

			@Override
			protected void onPostExecute(Integer numSavedRecords) {
				savedRecords.setNumber(numSavedRecords > 0 ? numSavedRecords : null);
			}
			
		}.execute();
		
		
	}
	
	private void addEventHandlers(View actionBar) {
		savedRecords = (NumberedImageButton)actionBar.findViewById(R.id.viewSavedRecordsButton);
		savedRecords.setOnClickListener(this);
		ImageButton button = (ImageButton)actionBar.findViewById(R.id.newRecordButton);
		button.setOnClickListener(this);
		button = (ImageButton)actionBar.findViewById(R.id.viewSpeciesListButton);
		button.setOnClickListener(this);
	}

	/**
	 * Handles selection of the buttons in the action bar.
	 */
	public void onClick(View v) {
		if (v.getId() == R.id.viewSavedRecordsButton) {
			Intent intent = new Intent(getActivity(), ViewSavedRecordsActivity.class);
			startActivity(intent);
		} else if (v.getId() == R.id.newRecordButton) {
			Intent intent = new Intent(getActivity(), CollectSurveyData.class);
			intent.putExtra(CollectSurveyData.SURVEY_BUNDLE_KEY, new Preferences(getActivity()).getCurrentSurvey());
			startActivity(intent);
		} else if (v.getId() == R.id.viewSpeciesListButton) {
			Intent intent = new Intent(getActivity(), SpeciesListActivity.class);
			startActivity(intent);
		}

	}

	

}
