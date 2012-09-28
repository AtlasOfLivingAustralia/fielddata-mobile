/*******************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
 * All Rights Reserved.
 *  
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *  
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ******************************************************************************/
package au.org.ala.fielddata.mobile;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import au.org.ala.fielddata.mobile.map.SingleSelectionOverlay;
import au.org.ala.fielddata.mobile.model.MapDefaults;
import au.org.ala.fielddata.mobile.service.LocationServiceHelper;
import au.org.ala.fielddata.mobile.ui.MenuHelper;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

/**
 * Displays a map that allows the user to select the location of their 
 * observation.
 */
public class LocationSelectionActivity extends SherlockMapActivity implements
		LocationListener {

	public static final String LOCATION_BUNDLE_KEY = "Location";
	public static final String MAP_DEFAULTS_BUNDLE_KEY = "MapDefaults";
	private MapView mapView;
	private SingleSelectionOverlay selectionOverlay;
	private Location selectedLocation;
	private MyLocationOverlay myLocationOverlay;

	private Tmp tmp;
	
	class Tmp extends LocationServiceHelper.LocationServiceConnection {
		public Tmp() {
			super(LocationSelectionActivity.this, 3000f);
		}
		public void onServiceConnected(ComponentName name, IBinder service) {
			super.onServiceConnected(name, service);
			serviceConnected(getLocationHistory());
		}
		
	}
	

	class HistoryOverlay extends BalloonItemizedOverlay<OverlayItem> {
		
		private List<Location> locationHistory;
		public HistoryOverlay(List<Location> history, Drawable marker) {
			super(marker, mapView);
			
			locationHistory = new ArrayList<Location>(history);
			populate();
		}
		@Override
		protected OverlayItem createItem(int arg0) {
			Log.d("LocationSelectionActivity", "Created item: "+arg0);
			Location loc = locationHistory.get(arg0);
			GeoPoint point = new GeoPoint((int)(loc.getLatitude()*1000000d), (int)(loc.getLongitude()*1000000d));
			OverlayItem item = new OverlayItem(point, "Accuracy: "+loc.getAccuracy(), "");
			return item;
		}

		@Override
		public int size() {
			return locationHistory.size();
		}
		
		
	}
	
	public void serviceConnected(List<Location> history) {
		
		Drawable marker = getResources().getDrawable(R.drawable.marker);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(), marker.getIntrinsicHeight());
		final List<Location> locationHistory = history;
		HistoryOverlay historyOverlay = new HistoryOverlay(locationHistory, marker);
		
		Log.d("LocationSelectionActivity", "serviceconnected, history= "+history.size());
		mapView.getOverlays().add(historyOverlay);
		if (historyOverlay.getCenter() != null) {
			mapView.getController().setCenter(historyOverlay.getCenter());
		}
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_selection);
		boolean setZoom = true;
		Location location = null;
		if (savedInstanceState != null) {
			setZoom = false;
			location = (Location)savedInstanceState.getParcelable(LOCATION_BUNDLE_KEY);
		}
		else {
		    location = (Location)getIntent().getParcelableExtra(LOCATION_BUNDLE_KEY);
		}
		mapView = (MapView) findViewById(R.id.mapview);
		initialiseOverlays();
		initialiseMap(location, setZoom);
		addEventHandlers();
		
		tmp = new Tmp();
		Intent intent = new Intent(this, LocationServiceHelper.class);
		bindService(intent, tmp, Context.BIND_AUTO_CREATE);

	}
	
	private void initialiseOverlays() {
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		
		mapView.getOverlays().add(myLocationOverlay);
		Drawable marker = getResources().getDrawable(R.drawable.iconr);

		marker.setBounds(0, 0, marker.getIntrinsicWidth(),
				marker.getIntrinsicHeight());

		ImageView dragImage = (ImageView) findViewById(R.id.drag);
		selectionOverlay = new SingleSelectionOverlay(mapView, marker, dragImage, this);
		
		mapView.getOverlays().add(selectionOverlay);
	}
	
	/**
	 * Adds the overlay, and the previously selected point.
	 * Sets the map zoom and centre.
	 */
	private void initialiseMap(Location location, boolean setZoom) {
		mapView.setBuiltInZoomControls(true);
		
		
		if (location != null) {
			selectionOverlay.selectLocation(location);
			if (setZoom) {
				mapView.getController().setZoom(16);
				mapView.getController().setCenter(selectionOverlay.getItem(0).getPoint());
			}
		}
		else {
			if (setZoom) {
				MapDefaults mapDefaults = (MapDefaults)getIntent().getParcelableExtra(MAP_DEFAULTS_BUNDLE_KEY);
				if (mapDefaults != null) {
					mapView.getController().setZoom(mapDefaults.zoom);
					
					mapView.getController().setCenter(
							new GeoPoint((int)(mapDefaults.center.y*1000000), (int)(mapDefaults.center.x*1000000)));
				}
			}
		}
	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		new MenuInflater(this).inflate(R.menu.common_menu_items, menu);

		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(LOCATION_BUNDLE_KEY, selectedLocation);
		
	}

	@Override
	public void onPause() {
		super.onPause();
		myLocationOverlay.disableMyLocation();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		return new MenuHelper(this).handleMenuItemSelection(item);
	}

	
	private void addEventHandlers() {
		Button button = (Button) findViewById(R.id.mapNext);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent result = new Intent();
				result.putExtra(LOCATION_BUNDLE_KEY, selectedLocation);
				setResult(RESULT_OK, result);
				finish();

			}
		});
		button.setEnabled(selectedLocation !=  null);

		ImageButton gps = (ImageButton) findViewById(R.id.mapCurrentLocation);
		gps.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				updateLocation();
			}
		});
	}

	@Override
	protected boolean isRouteDisplayed() {

		return false;
	}

	public void onLocationChanged(Location location) {
		selectedLocation = location;
		Button button = (Button) findViewById(R.id.mapNext);
		button.setEnabled(location != null);

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {}

	public void onProviderEnabled(String provider) {}

	public void onProviderDisabled(String provider) {}

	private void updateLocation() {
		
		myLocationOverlay.enableMyLocation();
		myLocationOverlay.runOnFirstFix(new Runnable() {

			public void run() {
				final GeoPoint point = myLocationOverlay.getMyLocation();
				if (point != null) {
					mapView.getController().setCenter(point);
					mapView.getController().setZoom(18);
					
					
					runOnUiThread(new Runnable() {
						public void run() {
							selectionOverlay.selectLocation(point);
						}
					});

				}
			}
		});

	}

}
