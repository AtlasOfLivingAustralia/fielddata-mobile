package au.org.ala.fielddata.mobile.map;


import android.annotation.TargetApi;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import au.org.ala.fielddata.mobile.model.MapDefaults;
import au.org.ala.fielddata.mobile.nrmplus.R;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class WayPointActivity extends SherlockFragmentActivity implements InfoWindowAdapter, OnMarkerDragListener {
	
	public static final String WAY_POINTS_KEY = "WAYPOINTS";
	
	public static final String MAP_DEFAULTS_BUNDLE_KEY = "MapDefaults";
	
	protected GoogleMap map;
	private WayPoints wayPoints;
	private Polyline polyline;
	private LocationManager locationManager;
	private boolean polygonClosed;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collect_waypoints);
		boolean setZoom = true;
		if (savedInstanceState != null) {
			setZoom = false;
			wayPoints = (WayPoints) savedInstanceState.getParcelable(WAY_POINTS_KEY);
			
		} else {
			wayPoints = (WayPoints) getIntent().getParcelableExtra(WAY_POINTS_KEY);
		}
		locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		initialiseMap(setZoom);
		addEventHandlers();

	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_collect_waypoints, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem openPolygon = menu.findItem(R.id.open_polygon);
		MenuItem closePolygon = menu.findItem(R.id.close_polygon);
	
		boolean closed = wayPoints != null ? wayPoints.isClosed() : false;
		openPolygon.setVisible(closed);
		closePolygon.setVisible(!closed);
		
		return true;
	}


	@Override
	@TargetApi(8)
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.add_waypoint) {
			addVertex(locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
		}
		else if (item.getItemId() == R.id.add_photopoint) {
			addPhotopoint(locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
		}
		else if (item.getItemId() == R.id.close_polygon) {
			wayPoints.setClosed(true);
			drawLine();
			supportInvalidateOptionsMenu();
		}
		else if (item.getItemId() == R.id.open_polygon) {
			wayPoints.setClosed(false);
			drawLine();
			supportInvalidateOptionsMenu();
		}
		
		else if (item.getItemId() == R.id.done) {
			
		}
		return true;
	}



	/**
	 * Adds the overlay, and the previously selected point. Sets the map zoom
	 * and centre.
	 */
	private void initialiseMap(boolean setZoom) {
		
		if (this.map == null) {
			SupportMapFragment mf = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map));
			// Check if we were successful in obtaining the map.
			if (mf != null) {
				mf.setRetainInstance(true);
				
				this.map = mf.getMap();
				this.map.setMyLocationEnabled(true);
				this.map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				this.map.setInfoWindowAdapter(this);
				this.map.setOnMarkerDragListener(this);
				
			}
		}
		
		if (wayPoints != null) {
			
			// There doesn't appear to be any way to get an instance of the
			// polyline after the map is restored from a bundle so I have
			// to clear the map and re-add everything.
			map.clear();
			final LatLngBounds.Builder builder = new LatLngBounds.Builder();
			LatLng location;
			for (WayPoint wayPoint : wayPoints.getVerticies()) {
				location = wayPoint.coordinate();
				builder.include(location);
				Marker marker = addMarker(location, BitmapDescriptorFactory.HUE_RED);
				wayPoint.markerId = marker.getId();
			}
			drawLine();
			if (setZoom) {
				findViewById(android.R.id.content).post(new Runnable() {
					public void run() {
						map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));
					}
				});
			}
			
		} else {
			wayPoints = new WayPoints();
			if (setZoom) {
				MapDefaults mapDefaults = (MapDefaults) getIntent().getParcelableExtra(
						MAP_DEFAULTS_BUNDLE_KEY);
				if (mapDefaults != null && mapDefaults.center != null) {
					
					LatLng latlng = new LatLng(mapDefaults.center.y, mapDefaults.center.x);
					map.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, mapDefaults.zoom));
				}
			}
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(WAY_POINTS_KEY, wayPoints);
	}
	
	
	public View getInfoContents(Marker arg0) {
		return getLayoutInflater().inflate(R.layout.waypoint_photo, null); 
	}

	public View getInfoWindow(Marker arg0) {
		return null;
	}

	private void addPhotopoint(Location location) {
		Marker marker = addMarker(new LatLng(location.getLatitude(), location.getLongitude()), BitmapDescriptorFactory.HUE_BLUE);
		
		WayPoint point = new WayPoint(location, marker.getId());
		wayPoints.addPhotoPoint(point);
	}
	
	private void addVertex(Location location) {
		Marker marker = addMarker(new LatLng(location.getLatitude(), location.getLongitude()), BitmapDescriptorFactory.HUE_RED);
		
		WayPoint point = new WayPoint(location, marker.getId());
		wayPoints.addVertex(point);
		drawLine();
		
	}
	
	
	private void drawLine() {
		if (polyline == null) {
			PolylineOptions options = new PolylineOptions()
				.color(Color.RED)
				.width(3)
				.addAll(wayPoints.verticies());
			
			polyline = map.addPolyline(options);
			
		}
		else {
			polyline.setPoints(wayPoints.verticies());
		}
	}
	
	
	private Marker addMarker(LatLng latlng, float colour) {
		MarkerOptions options = new MarkerOptions()
			.position(latlng)
			.draggable(true)
			.icon(BitmapDescriptorFactory.defaultMarker(colour));
			
		return map.addMarker(options);
	}

	@Override
	public void onPause() {
		super.onPause();
		map.setMyLocationEnabled(false);
	}

	private void addEventHandlers() {
		map.setOnMapLongClickListener(new OnMapLongClickListener() {
			
			public void onMapLongClick(LatLng location) {
				
				Location selectedLocation = locationFromClick(location);
				
				addVertex(selectedLocation);
			}

			
		});
		
		map.setOnMarkerClickListener(new OnMarkerClickListener() {
			public boolean onMarkerClick(Marker marker) {
				// Still want the default behaviour
				return false;
			}
		});
		
		
//		Button button = (Button) findViewById(R.id.mapNext);
//		button.setOnClickListener(new OnClickListener() {
//
//			public void onClick(View v) {
//				Intent result = new Intent();
//				//result.putExtra(LOCATION_BUNDLE_KEY, selectedLocation);
//				setResult(RESULT_OK, result);
//				finish();
//
//			}
//		});
		//button.setEnabled(selectedLocation != null);

//		ImageButton gps = (ImageButton) findViewById(R.id.mapCurrentLocation);
//		gps.setOnClickListener(new OnClickListener() {
//
//			public void onClick(View v) {
//				updateLocation();
//			}
//		});
	}

	private Location locationFromClick(LatLng arg0) {
		Location selectedLocation = new Location("On-screen map");
		selectedLocation.setTime(System.currentTimeMillis());
		selectedLocation.setLatitude(arg0.latitude);
		selectedLocation.setLongitude(arg0.longitude);
		return selectedLocation;
	}
	
	public void onMarkerDrag(Marker marker) {
		updateLine(marker);
	}
	public void onMarkerDragStart(Marker arg0) {
		
		
	}
	public void onMarkerDragEnd(Marker marker) {
		updateLine(marker);
	}
	
	private void updateLine(Marker marker) {
		WayPoint wayPoint = wayPoints.findById(marker.getId());
		wayPoint.location = locationFromClick(marker.getPosition());
		drawLine();
	}

	

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onProviderDisabled(String provider) {
	}

}
