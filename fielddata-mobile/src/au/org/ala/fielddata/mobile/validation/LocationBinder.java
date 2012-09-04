package au.org.ala.fielddata.mobile.validation;

import java.text.DecimalFormat;

import android.location.Location;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.CollectSurveyData;
import au.org.ala.fielddata.mobile.R;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.SurveyViewModel;
import au.org.ala.fielddata.mobile.ui.GPSFragment;
import au.org.ala.fielddata.mobile.validation.Validator.ValidationResult;

public class LocationBinder implements Binder {

	private TextView locationTextView;
	private SurveyViewModel model;
	private CollectSurveyData ctx;
	
	public LocationBinder(CollectSurveyData context, View locationView, Attribute locationAttribute, SurveyViewModel model) {
		locationTextView = (TextView)locationView.findViewById(R.id.latlong);
		this.model = model;
		this.ctx = context;
		
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
			ctx.selectLocation();
		}
	});
	}
	
	public void onAttributeChange(Attribute attribute) {

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
		Location location = model.getLocation();
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
	
	/**
	 * This method does nothing, the binding of the location is performed
	 * by the CollectSurveyData activity as it requires the involvement 
	 * of other activities (such as the LocationSelectionActivity).
	 */
	public void bind() {}

}
