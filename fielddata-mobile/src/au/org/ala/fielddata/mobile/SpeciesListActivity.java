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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import au.org.ala.fielddata.mobile.model.Species;
import au.org.ala.fielddata.mobile.pref.Preferences;
import au.org.ala.fielddata.mobile.ui.SpeciesSelectionListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Presents a list of species to the user for information purposes.
 */
public class SpeciesListActivity extends SherlockFragmentActivity implements SpeciesSelectionListener {

	private Preferences preferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.species_list_layout);
		preferences = new Preferences(this);
	}
    
	public void onSpeciesSelected(final Species species) {
		
		final CharSequence[] items = {"Record Observation", "View Field Guide"};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Species Action");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        if (item == 0) {
		        	recordObservation(species);
		        } else {
		        	showFieldGuide(species);
		        }
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void recordObservation(Species species) {
		Intent intent = new Intent(this, CollectSurveyData.class);
		intent.putExtra(CollectSurveyData.SURVEY_BUNDLE_KEY, preferences.getCurrentSurvey());
		intent.putExtra(CollectSurveyData.SPECIES, species.getId());
		startActivity(intent);
	}
	
	private void showFieldGuide(final Species species) {
		
		Intent intent = new Intent(this, FieldGuideActivity.class);
		intent.putExtra(CollectSurveyData.SPECIES, species.getId());
		startActivity(intent);
	}
    
}
