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

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import au.org.ala.fielddata.mobile.R;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Species;

import com.actionbarsherlock.app.SherlockListFragment;

public class SpeciesListFragment extends SherlockListFragment {
	
	private List<Species> species;
		
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
    	new GetSpeciesTask().execute();
    }
	
	class GetSpeciesTask extends AsyncTask<Void, Void, List<Species>> {
		
    	protected List<Species> doInBackground(Void... ignored) {
    		GenericDAO<Species> dao = new GenericDAO<Species>(getActivity());
    		
    		List<Species> species = new ArrayList<Species>();
    		try {
    			species.addAll(dao.loadAll(Species.class));
    		}
    		catch (Exception e) {
    			e.printStackTrace();
    		}
    		return species;
    	}
    	
    	protected void onPostExecute(List<Species> speciesList) {
    		species = speciesList;
    		IconAndTextAdapter adapter = new IconAndTextAdapter();
    		
    		setListAdapter(adapter);
    	}
	}
    
    class IconAndTextAdapter extends ArrayAdapter<Species> {
    	public IconAndTextAdapter() {
    		super(getActivity(), R.layout.species_row, R.id.scientificName, species);
    	}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View row = super.getView(position, convertView, parent);
			SpeciesViewHolder viewHolder = (SpeciesViewHolder)row.getTag();
			if (viewHolder == null) {
				viewHolder = new SpeciesViewHolder(row);
				row.setTag(viewHolder);
			}
			
			viewHolder.populate(getItem(position));
			return row;
		}
    	
    	
    }
}
