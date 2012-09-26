package au.org.ala.fielddata.mobile.validation;

import java.text.DecimalFormat;

import android.location.Location;
import android.text.format.DateFormat;
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

public class LocationBinder extends AbsBinder {

	private TextView latitude;
	private TextView longitude;
	private TextView accuracy;
	private TextView noLocation;
	private TextView time;
	
	private SurveyViewModel model;
	private CollectSurveyData ctx;
	
	public LocationBinder(CollectSurveyData context, View locationView, Attribute locationAttribute, SurveyViewModel model) {
		super(locationAttribute, locationView);
		
		latitude = (TextView)locationView.findViewById(R.id.latitude);
		longitude = (TextView)locationView.findViewById(R.id.longitude);
		accuracy = (TextView)locationView.findViewById(R.id.accuracy);
		noLocation = (TextView)locationView.findViewById(R.id.noLocation);
		time = (TextView)locationView.findViewById(R.id.time);
		
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
			noLocation.setError(null);
		}
		else {
			noLocation.setError("Select a location"); 	//result.getMessage(ctx));
		}
		
	}
	private void updateText() {
		
		Location location = model.getLocation();
		if (location != null) {
			DecimalFormat format = new DecimalFormat("###.000000");
			
			latitude.setText("lat: "+format.format(location.getLatitude()));
			longitude.setText("lon: "+format.format(location.getLongitude()));
			if (location.hasAccuracy()) {
				accuracy.setText("accuracy: "+location.getAccuracy()+ " m");
			}
			else {
				accuracy.setText("accuracy: unknown");
			}	
			
			time.setText("Updated at: "+DateFormat.getTimeFormat(ctx).format(location.getTime()));
			
			noLocation.setVisibility(View.GONE);
			latitude.setVisibility(View.VISIBLE);
			longitude.setVisibility(View.VISIBLE);
			accuracy.setVisibility(View.VISIBLE);
			time.setVisibility(View.VISIBLE);
		}
		else {
			noLocation.setVisibility(View.VISIBLE);
			latitude.setVisibility(View.GONE);
			longitude.setVisibility(View.GONE);
			accuracy.setVisibility(View.GONE);
			time.setVisibility(View.GONE);
			
		}
		
	}
	
	/**
	 * This method does nothing, the binding of the location is performed
	 * by the CollectSurveyData activity as it requires the involvement 
	 * of other activities (such as the LocationSelectionActivity).
	 */
	public void bind() {}

}
