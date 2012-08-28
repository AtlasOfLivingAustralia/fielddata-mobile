package au.org.ala.fielddata.mobile.validation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
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
	private String thumbUri;
	private boolean expectingResult;

	public ImageBinder(CollectSurveyData ctx, Attribute attribute,
			View imageView) {
		this.ctx = ctx;
		this.attribute = attribute;
		expectingResult = false;
		ctx.addImageListener(this);
		thumb = (ImageView) imageView.findViewById(R.id.photoThumbnail);
		SurveyViewModel model = ctx.getViewModel();
		thumbUri = model.getRecord().getValue(attribute);

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
				thumbUri = fileUri.toString();
				bind();
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
				if (thumbUri == null || "".equals(thumbUri)) {
					return;
				}
				Uri imageUri = Uri.parse(thumbUri);
				// Can't view files using the ACTION_VIEW.  Need to update
				// the Photo taking to allow the media manager to scan it.
				if ("content".equals(imageUri.getScheme())) {

					Intent intent = new Intent(Intent.ACTION_VIEW, imageUri);

					ctx.startActivity(intent);
				}
			}
		});
	

	}

	private void updateThumbnail() {
		if (thumbUri != null && "".equals(thumbUri) == false) {

			Uri uri = Uri.parse(thumbUri);
			try {
				
				 // Get the dimensions of the View
			    int targetW = thumb.getWidth();
			    int targetH = thumb.getHeight();
			    
			    // The view is created when the previous page is displayed
			    // so the imageview size is 0 at that point.
			    if (targetW == 0 || targetH == 0) {
			    	thumb.postDelayed(new Runnable() {
			    		public void run() {
			    			updateThumbnail();
			    		}
			    	}, 2000);
			    	return;
			    }
			  
			    // Get the dimensions of the bitmap
			    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			    bmOptions.inJustDecodeBounds = true;
			    BitmapFactory.decodeFile(uri.getEncodedPath(), bmOptions);
			    int photoW = bmOptions.outWidth;
			    int photoH = bmOptions.outHeight;
			  
			    // Determine how much to scale down the image
			    int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
			  
			    // Decode the image file into a Bitmap sized to fill the View
			    bmOptions.inJustDecodeBounds = false;
			    bmOptions.inSampleSize = scaleFactor;
			    bmOptions.inPurgeable = true;
			  
			    Bitmap bitmap = BitmapFactory.decodeFile(uri.getEncodedPath(), bmOptions);
			    thumb.setImageBitmap(bitmap);

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void onImageSelected(Uri imageUri) {
		if (!expectingResult) {
			return;
		}
		
		

		// For some reason, the camera application passes back a null intent
		// on my Galaxy Nexus, so we have to rely on the URI we created before
		// starting the Camera activity.
		if (imageUri != null) {
			thumbUri = imageUri.toString();
		}

		updateThumbnail();
		expectingResult = false;
	}
	
	private void addToGallery(Uri photo) {
	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	   
	    mediaScanIntent.setData(photo);
	    ctx.sendBroadcast(mediaScanIntent);
	}
	
	
	public void bind() {
		ctx.getViewModel().getRecord().setValue(attribute, thumbUri);
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
