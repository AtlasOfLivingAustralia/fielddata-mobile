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
package au.org.ala.fielddata.mobile.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import au.org.ala.fielddata.mobile.model.Species;

import com.actionbarsherlock.app.SherlockListFragment;

public class SpeciesListFragment extends SherlockListFragment {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        init();
    }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Activity activity = getSherlockActivity();
		if (activity instanceof SpeciesSelectionListener) {
			Species species = (Species)l.getAdapter().getItem(position);
			((SpeciesSelectionListener)activity).onSpeciesSelected(species);
		}
	}
	
    private void init() {
    	SpeciesListAdapter adapter = new SpeciesListAdapter(getActivity());
    	setListAdapter(adapter);
    	
    }
	
	
}
