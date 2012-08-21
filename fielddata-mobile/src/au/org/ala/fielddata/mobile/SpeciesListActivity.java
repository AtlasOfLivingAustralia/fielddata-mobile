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
