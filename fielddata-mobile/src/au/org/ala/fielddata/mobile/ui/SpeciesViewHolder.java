package au.org.ala.fielddata.mobile.ui;

import java.io.File;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.R;
import au.org.ala.fielddata.mobile.model.Species;
import au.org.ala.fielddata.mobile.service.StorageManager;

public class SpeciesViewHolder {
	StorageManager cacheManager;
	ImageView icon = null;
	TextView scientificName = null;
	TextView commonName = null;
	
	public SpeciesViewHolder(View row) {
		this.icon = (ImageView)row.findViewById(R.id.imageView1);
		this.scientificName = (TextView)row.findViewById(R.id.scientificName);
		this.commonName = (TextView)row.findViewById(R.id.commonName);
		cacheManager = new StorageManager(row.getContext());
	}
	
	public void populate(Species species) {
		// Should *not* be doing this on the UI thread.
		File profileImage = cacheManager.getProfileImage(species);
		
		if (profileImage != null) {
			
			Drawable d = Drawable.createFromPath(profileImage.getAbsolutePath());
			
			icon.setImageDrawable(d);
			
		}
		else {
			icon.setImageResource(R.drawable.ic_action_search);
		}
		scientificName.setText(species.scientificName);
		commonName.setText(species.commonName);
	}
}
