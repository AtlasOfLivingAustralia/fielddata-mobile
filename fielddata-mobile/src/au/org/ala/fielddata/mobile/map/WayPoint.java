package au.org.ala.fielddata.mobile.map;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

class WayPoint implements Parcelable {
	public Location location;
	public List<String> photos;
	public String markerId;
	
	public WayPoint(Location location, String markerId) {
		photos = new ArrayList<String>(4);
		this.location = location;
		this.markerId = markerId;
		
	}
	@SuppressWarnings("unchecked")
	public WayPoint(Parcel in) {
		location = in.readParcelable(WayPoint.class.getClassLoader());
		photos = in.readArrayList(WayPoint.class.getClassLoader());
		markerId = in.readString();
	}
	
	public int describeContents() {
		return 0;
	}
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(location, 0);
		dest.writeList(photos);
		dest.writeString(markerId);
	}
	public LatLng coordinate() {
		if (location == null) {
			return null;
		}
		return new LatLng(location.getLatitude(), location.getLongitude());
	}
	
	public static final Parcelable.Creator<WayPoint> CREATOR = new Parcelable.Creator<WayPoint>() {
		public WayPoint createFromParcel(Parcel in) {
			return new WayPoint(in);
		}
		public WayPoint[] newArray(int size) {
			return new WayPoint[size];
		}
	};
	
	
}