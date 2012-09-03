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

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import au.org.ala.fielddata.mobile.map.SingleSelectionOverlay;
import au.org.ala.fielddata.mobile.ui.MenuHelper;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

public class LocationSelectionActivity extends SherlockMapActivity implements
		LocationListener {

	public static final String LOCATION_BUNDLE_KEY = "Location";
	private MapView mapView;
	private SingleSelectionOverlay selectionOverlay;
	private Location selectedLocation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_selection);

		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		mapView.getController().setZoom(7);
		mapView.getController().setCenter(new GeoPoint(-27561777, 151493591));
		addEventHandlers();

		
		initiaseOverlays();
		
		
	}

	private void initiaseOverlays() {
		Drawable marker = getResources().getDrawable(R.drawable.iconr);

		marker.setBounds(0, 0, marker.getIntrinsicWidth(),
				marker.getIntrinsicHeight());

		ImageView dragImage = (ImageView) findViewById(R.id.drag);
		selectionOverlay = new SingleSelectionOverlay(marker, dragImage, this);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Location location = (Location)extras.get(LOCATION_BUNDLE_KEY);
			if (location != null) {
				selectionOverlay.selectLocation(location);
				onLocationChanged(location);
			}
		}
		
		
		mapView.getOverlays().add(selectionOverlay);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		new MenuInflater(this).inflate(R.menu.common_menu_items, menu);

		return super.onCreateOptionsMenu(menu);
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
		button.setEnabled(false);

		button = (Button) findViewById(R.id.mapCurrentLocation);
		button.setOnClickListener(new OnClickListener() {

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
		final MyLocationOverlay overlay = new MyLocationOverlay(this, mapView);
		overlay.enableMyLocation();
		mapView.getOverlays().add(overlay);

		overlay.runOnFirstFix(new Runnable() {

			public void run() {
				GeoPoint point = overlay.getMyLocation();
				if (point != null) {
					mapView.getController().setCenter(point);
					mapView.getController().setZoom(18);
					selectionOverlay.selectLocation(point);
					runOnUiThread(new Runnable() {
						public void run() {
							Button button = (Button) findViewById(R.id.mapNext);
							button.setEnabled(true);
						}
					});

				}
			}
		});

	}

}
