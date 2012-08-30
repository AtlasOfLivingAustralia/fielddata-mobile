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
import au.org.ala.fielddata.mobile.service.StorageManager;
import au.org.ala.fielddata.mobile.validation.Validator.ValidationResult;

public class ImageBinder implements Binder {

	private ImageView thumb;
	private CollectSurveyData ctx;
	private Attribute attribute;
	private Uri thumbUri;
	private boolean expectingResult;
	private boolean thumbnailRendered;
	
	public ImageBinder(CollectSurveyData ctx, Attribute attribute,
			View imageView) {
		this.ctx = ctx;
		this.attribute = attribute;
		expectingResult = false;
		ctx.addImageListener(this);
		thumb = (ImageView) imageView.findViewById(R.id.photoThumbnail);
		SurveyViewModel model = ctx.getViewModel();
		thumbUri = model.getRecord().getUri(attribute);

		updateThumbnail();

		addEventHandlers(imageView);
	}

	private void addEventHandlers(View view) {
		ImageButton takePhoto = (ImageButton) view.findViewById(R.id.takePhoto);

		takePhoto.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				Uri fileUri = StorageManager
						.getOutputMediaFileUri(StorageManager.MEDIA_TYPE_IMAGE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
				// Unfortunately, this URI isn't being returned in the result
				// as expected so we have to eagerly bind it to our Record.
				thumbUri = fileUri;
				expectingResult = true;
				ctx.startActivityForResult(intent,
						CollectSurveyData.TAKE_PHOTO_REQUEST);
			}
		});

		ImageButton selectFromGallery = (ImageButton) view
				.findViewById(R.id.selectFromGallery);
		selectFromGallery.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				expectingResult = true;
				ctx.startActivityForResult(
						Intent.createChooser(intent, "Select Photo"),
						CollectSurveyData.SELECT_FROM_GALLERY_REQUEST);

			}
		});

		thumb.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (thumbUri == null) {
					return;
				}
				// Can't view files using the ACTION_VIEW.  Need to update
				// the Photo taking to allow the media manager to scan it.
				if ("content".equals(thumbUri.getScheme())) {

					Intent intent = new Intent(Intent.ACTION_VIEW, thumbUri);

					ctx.startActivity(intent);
				}
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
			    }
			    else if ("content".equals(thumbUri.getScheme())) {
			    	bitmap = MediaStore.Images.Thumbnails.getThumbnail(
			    			ctx.getContentResolver(), 
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
		int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
  
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
		
		Log.d("ImageBinder", "Selected: "+imageUri);

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
	
	private void addToGallery(Uri photo) {
	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	   
	    mediaScanIntent.setData(photo);
	    ctx.sendBroadcast(mediaScanIntent);
	}
	
	
	public void bind() {
		ctx.getViewModel().getRecord().setUri(attribute, thumbUri);
	}
	
	public void onAttributeChange(Attribute attribute) {
		if (attribute.getServerId() != this.attribute.getServerId()) {
			return;
		}
		updateThumbnail();
	}

	public void onValidationStatusChange(Attribute attribute, ValidationResult result) {
		if (attribute.getServerId() != this.attribute.getServerId()) {
			return;
		}
		// TODO need to render an error somehow, maybe replace the thumbnail
		// with an error icon?
		//view.setError(result.getMessage(ctx));
	}

}
