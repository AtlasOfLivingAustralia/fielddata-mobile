package au.org.ala.fielddata.mobile.map;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

class WayPoints implements Parcelable {
	
	private List<WayPoint> photoPoints;
	private List<WayPoint> verticies;
	private boolean polygonClosed;
	
	public WayPoints() {
		photoPoints = new ArrayList<WayPoint>();
		verticies = new ArrayList<WayPoint>();
		polygonClosed = false;
	}
	
	public WayPoints(Parcel in) {
		photoPoints = new ArrayList<WayPoint>();
		in.readTypedList(photoPoints, WayPoint.CREATOR);
		verticies = new ArrayList<WayPoint>();
		in.readTypedList(verticies, WayPoint.CREATOR);
		boolean[] wrapper = new boolean[1];
		in.readBooleanArray(wrapper);
		polygonClosed = wrapper[0];
		
	}
	
	public boolean isClosed() {
		return polygonClosed;
	}
	
	public void setClosed(boolean close) {
		polygonClosed = close;
	}
	
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(photoPoints);
		dest.writeTypedList(verticies);
		dest.writeBooleanArray(new boolean[]{polygonClosed});
	}
	public int describeContents() {
		return 0;
	}
	
	public List<LatLng> verticies() {
		List<LatLng> coordinates = new ArrayList<LatLng>(verticies.size());
		for (WayPoint wayPoint : verticies) {
			LatLng latlng = wayPoint.coordinate();
			if (latlng != null) {
				coordinates.add(latlng);
			}
		}
		if (polygonClosed && verticies.size() >= 3) {
			coordinates.add(verticies.get(0).coordinate());
		}
		return coordinates;
	}
	
	public List<WayPoint> getVerticies() {
		return verticies;
	}
	
	public List<WayPoint> getPhotoPoints() {
		return photoPoints;
	}
	
	public WayPoint findById(String markerId) {
		for (WayPoint wayPoint : verticies) {
			if (markerId.equals(wayPoint.markerId)) {
				return wayPoint;
			}
		}
		for (WayPoint wayPoint : photoPoints) {
			if (markerId.equals(wayPoint.markerId)) {
				return wayPoint;
			}
		}
		return null;
	}
	
	public void addVertex(WayPoint vertex) {
		verticies.add(vertex);
	}
	
	public void addPhotoPoint(WayPoint photoPoint) {
		photoPoints.add(photoPoint);
	}
	public static final Parcelable.Creator<WayPoints> CREATOR = new Parcelable.Creator<WayPoints>() {
		public WayPoints createFromParcel(Parcel in) {
			return new WayPoints(in);
		}
		public WayPoints[] newArray(int size) {
			return new WayPoints[size];
		}
	};
	
}