package au.org.ala.fielddata.mobile.validation;

import java.text.DecimalFormat;

import android.location.Location;
import android.view.View;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.R;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.SurveyViewModel;
import au.org.ala.fielddata.mobile.validation.Validator.ValidationResult;

public class LocationBinder implements Binder {

	private TextView locationTextView;
	private Record record;
	private Location location;
	
	public LocationBinder(View locationView, SurveyViewModel model) {
		locationTextView = (TextView)locationView.findViewById(R.id.latlong);
		this.record = model.getRecord();
		
		if (record.latitude != null && record.longitude != null) {
			location = new Location("");
			location.setLatitude(record.latitude);
			location.setLongitude(record.longitude);
			if (record.accuracy != null) {
				location.setAccuracy(record.accuracy.floatValue());
			}
		}
		
		updateText();
		
	}
	
	public void onAttributeChange(Attribute attribute) {
//		if (attribute.getServerId() != this.attribute.getServerId()) {
//			return;
//		}
		bind();
	}

	public void onAttributeInvalid(Attribute attribute, ValidationResult result) {
//		if (attribute.getServerId() != this.attribute.getServerId()) {
//			return;
//		}
		
		locationTextView.setError("Select a location");//result.getMessage(ctx));
	}
	private void updateText() {
		
		String locationText = "";
		if (location != null) {
			DecimalFormat format = new DecimalFormat("###.000000");
			StringBuilder builder = new StringBuilder();
			
			builder.append(format.format(location.getLatitude()));
			builder.append("\u00B0 N\n");
			builder.append(format.format(location.getLongitude()));
			builder.append("\u00B0 W ");
			
			locationText = builder.toString();
		}
		locationTextView.setText(locationText);
	}
	
	public void locationChanged(Location location) {
		this.location = location;
		bind();
		updateText();
	}
	
	public void bind() {
		if (location != null) {
			record.longitude = location.getLongitude();
			record.latitude = location.getLatitude();
			record.accuracy = Double.valueOf(location.getAccuracy());
		}
	}

}
