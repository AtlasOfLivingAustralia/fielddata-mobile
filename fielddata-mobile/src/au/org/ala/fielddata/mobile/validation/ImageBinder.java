package au.org.ala.fielddata.mobile.validation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import au.org.ala.fielddata.mobile.CollectSurveyData;
import au.org.ala.fielddata.mobile.R;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.model.SurveyViewModel;
import au.org.ala.fielddata.mobile.validation.Validator.ValidationResult;

public class ImageBinder implements Binder {

	private ImageView thumb;
	private CollectSurveyData ctx;
	private Attribute attribute;
	private Uri thumbUri;
	private SurveyViewModel model;
	private boolean expectingResult;
	private boolean thumbnailRendered;

	public ImageBinder(CollectSurveyData ctx, Attribute attribute, View imageView) {
		this.ctx = ctx;
		this.attribute = attribute;
		expectingResult = false;
		
		thumb = (ImageView) imageView.findViewById(R.id.photoThumbnail);
		model = ctx.getViewModel();
		thumbUri = model.getRecord().getUri(attribute);

		updateThumbnail();

		addEventHandlers(imageView);
	}

	private void addEventHandlers(View view) {
		ImageButton takePhoto = (ImageButton) view.findViewById(R.id.takePhoto);

		takePhoto.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				ctx.takePhoto(attribute);
			}
		});

		ImageButton selectFromGallery = (ImageButton) view.findViewById(R.id.selectFromGallery);
		selectFromGallery.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				ctx.selectFromGallery(attribute);

			}
		});

		thumb.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (thumbUri == null) {
					return;
				}
				
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(thumbUri, "image/*");
				ctx.startActivity(intent);
			}
		});

		thumb.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			public void onGlobalLayout() {
				if (!thumbnailRendered) {
					updateThumbnail();
				}
			}
		});

	}

	private void updateThumbnail() {
		if (thumbUri != null) {

			try {

				// Get the dimensions of the View
				int targetW = thumb.getWidth();
				int targetH = thumb.getHeight();

				// The view is created when the previous page is displayed
				// so the imageview size is 0 at that point.
				if (targetW == 0 || targetH == 0) {
					thumbnailRendered = false;
					return;
				}
				Bitmap bitmap = null;
				if ("file".equals(thumbUri.getScheme())) {
					bitmap = bitmapFromFile(targetW, targetH);
				} else if ("content".equals(thumbUri.getScheme())) {
					bitmap = MediaStore.Images.Thumbnails.getThumbnail(ctx.getContentResolver(),
							Long.parseLong(thumbUri.getLastPathSegment()),
							MediaStore.Images.Thumbnails.MICRO_KIND, null);
				}
				thumb.setImageBitmap(bitmap);
				thumbnailRendered = true;

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private Bitmap bitmapFromFile(int targetW, int targetH) {
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(thumbUri.getEncodedPath(), bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		// Determine how much to scale down the image
		int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(thumbUri.getEncodedPath(), bmOptions);
		return bitmap;
	}

	public void onImageSelected(Uri imageUri) {
		if (!expectingResult) {
			return;
		}

		Log.d("ImageBinder", "Selected: " + imageUri);

		// For some reason, the camera application passes back a null intent
		// on my Galaxy Nexus, so we have to rely on the URI we created before
		// starting the Camera activity.
		if (imageUri != null) {
			thumbUri = imageUri;
		}
		bind();
		updateThumbnail();
		expectingResult = false;
	}

	public void bind() {
		ctx.getViewModel().getRecord().setUri(attribute, thumbUri);
	}

	public void onAttributeChange(Attribute attribute) {
		if (attribute.getServerId() != this.attribute.getServerId()) {
			return;
		}
		thumbUri = model.getRecord().getUri(attribute);
		updateThumbnail();
	}

	public void onValidationStatusChange(Attribute attribute, ValidationResult result) {
		if (attribute.getServerId() != this.attribute.getServerId()) {
			return;
		}
		// TODO need to render an error somehow, maybe replace the thumbnail
		// with an error icon?
		// view.setError(result.getMessage(ctx));
	}

}
