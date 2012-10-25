package au.org.ala.fielddata.mobile.validation;

import java.text.DecimalFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.location.Location;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import au.org.ala.fielddata.mobile.CollectSurveyData;
import au.org.ala.fielddata.mobile.R;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.SurveyViewModel;
import au.org.ala.fielddata.mobile.validation.Validator.ValidationResult;

public class LocationBinder extends AbsBinder {

	private static final int GPS_TIMEOUT = 45; // seconds
	
	private TextView latitude;
	private TextView longitude;
	private TextView accuracy;
	private TextView noLocation;
	private TextView time;
	private SurveyViewModel model;
	private CollectSurveyData ctx;
	private boolean gpsTrackingOn;
	private Button gpsButton;
	
	private ScheduledFuture<?> timer;
	private ScheduledExecutorService scheduler;
	
	public LocationBinder(CollectSurveyData context, View locationView, Attribute locationAttribute, SurveyViewModel model) {
		super(locationAttribute, locationView);
		
		latitude = (TextView)locationView.findViewById(R.id.latitude);
		longitude = (TextView)locationView.findViewById(R.id.longitude);
		accuracy = (TextView)locationView.findViewById(R.id.accuracy);
		noLocation = (TextView)locationView.findViewById(R.id.noLocation);
		time = (TextView)locationView.findViewById(R.id.time);
		
		this.model = model;
		this.ctx = context;
		
		gpsTrackingOn = ctx.isGpsTrackingEnabled();
		addEventHandlers(locationView);
		updateText();
		
		
		
	}
	
	private void addEventHandlers(View view) {
	gpsButton = (Button)view.findViewById(R.id.gpsButton);
	gpsButton.setOnClickListener(new OnClickListener() {
		
		public void onClick(View v) {
			if (!gpsTrackingOn) {
				startLocationUpdates();		
			}
			else {
				stopLocationUpdates();
			}
		}

		
	});
	
	Button showOnMapButton = (Button)view.findViewById(R.id.showMapButton);
	showOnMapButton.setOnClickListener(new OnClickListener() {
		
		public void onClick(View v) {
			ctx.selectLocation();
		}
	});
	}
	
	private void startLocationUpdates() {
		Log.d("LocationBinder", "Starting location updates");
		ctx.startLocationUpdates();
		gpsTrackingOn = true;
		updateText();
		if (scheduler == null) {
			scheduler = Executors.newScheduledThreadPool(1);
		}
		timer = scheduler.schedule(new Runnable() {
			public void run() {
				cancelLocationUpdates();
			}
		}, GPS_TIMEOUT, TimeUnit.SECONDS);
	}
	
	public void cancelLocationUpdates() {
		Log.i("LocationBinder", "Cancelling location updates due to timeout!");
		gpsButton.post(new Runnable() {
			public void run() {
				stopLocationUpdates();
			}
		});
	}
	
	
	public void onAttributeChange(Attribute attribute) {
		stopLocationUpdates();
	}

	private void stopLocationUpdates() {
		if (timer != null) {
			timer.cancel(false);
			timer = null;
		}
		ctx.stopLocationUpdates();
		gpsTrackingOn = false;
		updateText();
	}

	public void onValidationStatusChange(Attribute attribute, ValidationResult result) {
		if (result.isValid()) {
			noLocation.setError(null);
		}
		else {
			noLocation.setError("Select a location"); 	//result.getMessage(ctx));
		}
		
	}
	private void updateText() {
		
		Location location = model.getLocation();
		if (location != null && !gpsTrackingOn) {
			DecimalFormat format = new DecimalFormat("###.000000");
			
			latitude.setText("lat: "+format.format(location.getLatitude()));
			longitude.setText("lon: "+format.format(location.getLongitude()));
			if (location.hasAccuracy()) {
				accuracy.setText("accuracy: "+location.getAccuracy()+ " m");
			}
			else {
				accuracy.setText("accuracy: unknown");
			}	
			
			time.setText("time: "+DateFormat.getTimeFormat(ctx).format(location.getTime()));
			
			noLocation.setVisibility(View.GONE);
			latitude.setVisibility(View.VISIBLE);
			longitude.setVisibility(View.VISIBLE);
			accuracy.setVisibility(View.VISIBLE);
			time.setVisibility(View.VISIBLE);
		}
		else {
			if (gpsTrackingOn) {
				noLocation.setHint("Acquiring location...");
			}
			else {
				noLocation.setHint("No location supplied");
			}
			noLocation.setVisibility(View.VISIBLE);
			latitude.setVisibility(View.GONE);
			longitude.setVisibility(View.GONE);
			accuracy.setVisibility(View.GONE);
			time.setVisibility(View.GONE);
			
		}
		if (gpsTrackingOn) {
			gpsButton.setText("Cancel");
		}
		else {
			gpsButton.setText("Start GPS");
		}
		
	}
	
	/**
	 * This method does nothing, the binding of the location is performed
	 * by the CollectSurveyData activity as it requires the involvement 
	 * of other activities (such as the LocationSelectionActivity).
	 */
	public void bind() {}

}
