package au.org.ala.fielddata.mobile.map;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class SingleSelectionOverlay extends ItemizedOverlay<OverlayItem> {

	private LocationListener listener;
	private List<OverlayItem> items = new ArrayList<OverlayItem>();
	private Drawable marker = null;
	private OverlayItem inDrag = null;
	private ImageView dragImage = null;
	private int xDragImageOffset = 0;
	private int yDragImageOffset = 0;
	private int xDragTouchOffset = 0;
	private int yDragTouchOffset = 0;

	public SingleSelectionOverlay(Drawable marker, ImageView dragImage, LocationListener listener) {
		super(marker);
		this.marker = marker;
		this.listener = listener;
		this.dragImage = dragImage;
		xDragImageOffset = dragImage.getDrawable().getIntrinsicWidth() / 2;
		yDragImageOffset = dragImage.getDrawable().getIntrinsicHeight();

		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return (items.get(i));
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);

		boundCenterBottom(marker);
	}

	@Override
	public int size() {
		return (items.size());
	}

	protected void addItem(OverlayItem item) {
		items.add(item);
		populate();
	}
	
	public GeoPoint getSelectedPoint() {
		return items.get(0).getPoint();
	}
	
	@Override
	public boolean onTap(GeoPoint arg0, MapView arg1) {
		if (size() == 0) {
			updateSelection(new OverlayItem(arg0, "", ""));
		}
		else {
			return super.onTap(arg0, arg1);
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		final int action = event.getAction();
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		boolean result = false;

		if (action == MotionEvent.ACTION_DOWN) {

			for (OverlayItem item : items) {
				Point p = new Point(0, 0);

				mapView.getProjection().toPixels(item.getPoint(), p);

				if (hitTest(item, marker, x - p.x, y - p.y)) {
					result = true;
					inDrag = item;
					items.remove(inDrag);
					populate();

					xDragTouchOffset = 0;
					yDragTouchOffset = 0;

					setDragImagePosition(p.x, p.y);
					dragImage.setVisibility(View.VISIBLE);

					xDragTouchOffset = x - p.x;
					yDragTouchOffset = y - p.y;

					break;
				}
			}
		} else if (action == MotionEvent.ACTION_MOVE && inDrag != null) {
			setDragImagePosition(x, y);
			result = true;
		} else if (action == MotionEvent.ACTION_UP && inDrag != null) {
			dragImage.setVisibility(View.GONE);

			GeoPoint pt = mapView.getProjection().fromPixels(
					x - xDragTouchOffset, y - yDragTouchOffset);
			OverlayItem toDrop = new OverlayItem(pt, inDrag.getTitle(),
					inDrag.getSnippet());

			updateSelection(toDrop);
			inDrag = null;
			result = true;
		}

		return (result || super.onTouchEvent(event, mapView));
	}
	
	private void updateSelection(OverlayItem point) {
		addItem(point);
		listener.onLocationChanged(pointToLocation(point.getPoint()));
		
	}
	
	private Location pointToLocation(GeoPoint point) {
		Location selectedLocation = new Location("User Selection");
		selectedLocation.setLatitude(point.getLatitudeE6()/1000000d);
		selectedLocation.setLongitude(point.getLongitudeE6()/1000000d);
		
		return selectedLocation;
	}

	private void setDragImagePosition(int x, int y) {
		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dragImage
				.getLayoutParams();

		lp.setMargins(x - xDragImageOffset - xDragTouchOffset, y
				- yDragImageOffset - yDragTouchOffset, 0, 0);
		dragImage.setLayoutParams(lp);
	}

}
