package au.org.ala.fielddata.mobile.validation;

import java.text.DecimalFormat;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.CollectSurveyData;
import au.org.ala.fielddata.mobile.LocationSelectionActivity;
import au.org.ala.fielddata.mobile.R;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.SurveyViewModel;
import au.org.ala.fielddata.mobile.ui.GPSFragment;
import au.org.ala.fielddata.mobile.validation.Validator.ValidationResult;

public class LocationBinder implements Binder {

	private TextView locationTextView;
	private SurveyViewModel model;
	private Location location;
	private FragmentActivity ctx;
	
	public LocationBinder(FragmentActivity context, View locationView, SurveyViewModel model) {
		locationTextView = (TextView)locationView.findViewById(R.id.latlong);
		this.model = model;
		this.ctx = context;
		Record record = model.getRecord();
		
		if (record.latitude != null && record.longitude != null) {
			location = new Location("");
			location.setLatitude(record.latitude);
			location.setLongitude(record.longitude);
			if (record.accuracy != null) {
				location.setAccuracy(record.accuracy.floatValue());
			}
		}
		addEventHandlers(locationView);
		updateText();
		
	}
	
	private void addEventHandlers(View view) {
	ImageButton gpsButton = (ImageButton)view.findViewById(R.id.gpsButton);
	gpsButton.setOnClickListener(new OnClickListener() {
		
		public void onClick(View v) {
			GPSFragment fragment = new GPSFragment();
			fragment.show(ctx.getSupportFragmentManager(), "gpsDialog");
			
		}
	});
	
	ImageButton showOnMapButton = (ImageButton)view.findViewById(R.id.showMapButton);
	showOnMapButton.setOnClickListener(new OnClickListener() {
		
		public void onClick(View v) {
			Intent intent = new Intent(ctx, LocationSelectionActivity.class);
			if (location != null) {
				intent.putExtra(LocationSelectionActivity.LOCATION_BUNDLE_KEY, location);
			}
			ctx.startActivityForResult(intent, CollectSurveyData.SELECT_LOCATION_REQUEST );
		}
	});
	}
	
	public void onAttributeChange(Attribute attribute) {
//		if (attribute.getServerId() != this.attribute.getServerId()) {
//			return;
//		}
		updateText();
	}

	public void onValidationStatusChange(Attribute attribute, ValidationResult result) {
//		if (attribute.getServerId() != this.attribute.getServerId()) {
//			return;
//		}
		if (result.isValid()) {
			locationTextView.setError(null);
		}
		else {
			locationTextView.setError("Select a location"); 	//result.getMessage(ctx));
		}
		
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
		model.locationSelected(location);
	}

}
