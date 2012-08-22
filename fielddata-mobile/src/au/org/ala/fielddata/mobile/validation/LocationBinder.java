package au.org.ala.fielddata.mobile.validation;

import java.text.DecimalFormat;

import android.location.Location;
import android.view.View;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.R;
import au.org.ala.fielddata.mobile.model.Record;

public class LocationBinder implements Binder {

	private TextView locationTextView;
	private Record record;
	private Location location;
	
	public LocationBinder(View locationView, Record record) {
		locationTextView = (TextView)locationView.findViewById(R.id.latlong);
		this.record = record;
		
		updateText();
		
	}
	
	private void updateText() {
		
		String locationText = "";
		if (record.latitude != null && record.longitude != null) {
			DecimalFormat format = new DecimalFormat("###.000000");
			StringBuilder builder = new StringBuilder();
			
			builder.append(format.format(record.latitude));
			builder.append("\u00B0 N ");
			builder.append(format.format(record.longitude));
			builder.append("\u00B0 W ");
			
			locationText = builder.toString();
		}
		locationTextView.setText(locationText);
	}
	
	public void locationChanged(Location location) {
		this.location = location;
	}
	
	@Override
	public void bind() {
		record.longitude = location.getLongitude();
		record.latitude = location.getLatitude();
		record.accuracy = Double.valueOf(location.getAccuracy());
	}

}
