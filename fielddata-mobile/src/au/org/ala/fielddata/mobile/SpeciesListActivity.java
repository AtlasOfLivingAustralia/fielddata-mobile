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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import au.org.ala.fielddata.mobile.model.Species;
import au.org.ala.fielddata.mobile.pref.Preferences;
import au.org.ala.fielddata.mobile.ui.SpeciesSelectionListener;

import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Presents a list of species to the user for information purposes.
 */
public class SpeciesListActivity extends SherlockFragmentActivity implements SpeciesSelectionListener {

    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.species_list_layout);
		
	}
    
	public void onSpeciesSelected(Species species) {
		
		String fieldDataUrl = new Preferences(this).getFieldDataServerUrl();
		fieldDataUrl += "/fieldguide/taxon.htm?id="+species.server_id;
		Uri uri = Uri.parse(fieldDataUrl);
    	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
    	startActivity(intent);
		
	}

    
    
    
    
}
