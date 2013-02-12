package au.org.ala.fielddata.mobile.ui;

import java.io.File;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.nrmplus.R;
import au.org.ala.fielddata.mobile.model.Species;
import au.org.ala.fielddata.mobile.service.StorageManager;

/**
 * Caches references to the UI components that display the details 
 * of a single Species.
 * Used to prevent continual calls to findViewById during View recycling
 * while the list is scrolling. 
 */
public class SpeciesViewHolder {
	StorageManager cacheManager;
	ImageView icon = null;
	TextView scientificName = null;
	TextView commonName = null;
	

	public SpeciesViewHolder(View row) {
		this(row, false);
	}
	
	public SpeciesViewHolder(View row, boolean focusable) {
		this.icon = (ImageView)row.findViewById(R.id.imageView1);
		this.scientificName = (TextView)row.findViewById(R.id.scientificName);
		this.commonName = (TextView)row.findViewById(R.id.commonName);
		scientificName.setFocusable(focusable);
		scientificName.setFocusableInTouchMode(focusable);
		
		cacheManager = new StorageManager(row.getContext());
	}
	
	/**
	 * Populates the contents of the contained views using the supplied
	 * Species object.
	 * @param species contains the species data to display.
	 */
	public void populate(Species species) {
		// Should *not* be doing this on the UI thread.
		setImage(species.getImageFileName());
		scientificName.setText(species.scientificName);
		commonName.setText(species.commonName);
	}
	
	public void populate(String scientificName, String commonName, String imageFileName) {
		setImage(imageFileName);
		this.scientificName.setText(scientificName);
		this.commonName.setText(commonName);
	}
	
	private void setImage(String fileName) {
		File profileImage = cacheManager.getProfileImage(fileName, false);
		
		if (profileImage != null) {
			
			Drawable d = Drawable.createFromPath(profileImage.getAbsolutePath());
			
			icon.setImageDrawable(d);
			
		}
		else {
			icon.setImageResource(R.drawable.ic_action_search);
		}
	}
	
	public void setError(CharSequence error) {
		scientificName.setError(error);
	}
	
	public boolean requestFocus() {
		return scientificName.requestFocus();
	}
}
