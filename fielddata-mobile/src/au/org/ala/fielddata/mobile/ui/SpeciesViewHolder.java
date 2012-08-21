package au.org.ala.fielddata.mobile.ui;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.R;
import au.org.ala.fielddata.mobile.model.Species;

public class SpeciesViewHolder {
	ImageView icon = null;
	TextView scientificName = null;
	TextView commonName = null;
	
	public SpeciesViewHolder(View row) {
		this.icon = (ImageView)row.findViewById(R.id.imageView1);
		this.scientificName = (TextView)row.findViewById(R.id.scientificName);
		this.commonName = (TextView)row.findViewById(R.id.commonName);
	}
	
	public void populate(Species species) {
		icon.setImageResource(species.profileImagePath);
		scientificName.setText(species.scientificName);
		commonName.setText(species.commonName);
	}
}
