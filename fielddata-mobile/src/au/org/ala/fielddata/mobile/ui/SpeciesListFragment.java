package au.org.ala.fielddata.mobile.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
	
	class ViewHolder {
    	ImageView icon = null;
    	TextView scientificName = null;
    	TextView commonName = null;
    	
    	public ViewHolder(View row) {
    		this.icon = (ImageView)row.findViewById(R.id.imageView1);
    		this.scientificName = (TextView)row.findViewById(R.id.scientificName);
			this.commonName = (TextView)row.findViewById(R.id.commonName);
    	}
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
			ViewHolder viewHolder = (ViewHolder)row.getTag();
			if (viewHolder == null) {
				viewHolder = new ViewHolder(row);
				row.setTag(viewHolder);
			}
			
			
			viewHolder.icon.setImageResource(getItem(position).profileImagePath);
			viewHolder.scientificName.setText(getItem(position).scientificName);
			viewHolder.commonName.setText(getItem(position).commonName);
			return row;
		}
    	
    	
    }
}
