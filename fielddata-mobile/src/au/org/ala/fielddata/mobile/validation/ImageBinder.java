package au.org.ala.fielddata.mobile.validation;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import au.org.ala.fielddata.mobile.CollectSurveyData;
import au.org.ala.fielddata.mobile.R;
import au.org.ala.fielddata.mobile.SurveyViewModel;
import au.org.ala.fielddata.mobile.model.Attribute;
import au.org.ala.fielddata.mobile.service.StorageManager;

public class ImageBinder implements Binder {

	private ImageView thumb;
	private CollectSurveyData ctx;
	private Attribute attribute;
	private String thumbUri;
	private boolean expectingResult;
	
	public ImageBinder(CollectSurveyData ctx, Attribute attribute, View imageView) {
		this.ctx = ctx;
		this.attribute = attribute;
		expectingResult = false;
		ctx.addImageListener(this);
		thumb = (ImageView)imageView.findViewById(R.id.photoThumbnail);
		SurveyViewModel model = ctx.getViewModel();
		thumbUri = model.getRecord().getValue(attribute);
		
		updateThumbnail();
		
		addEventHandlers(imageView);
	}
	
	private void addEventHandlers(View view) {
		ImageButton takePhoto = (ImageButton)view.findViewById(R.id.takePhoto);
		
		takePhoto.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				Uri fileUri = StorageManager.getOutputMediaFileUri(StorageManager.MEDIA_TYPE_IMAGE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
				// Unfortunately, this URI isn't being returned in the result 
				// as expected so we have to eagerly bind it to our Record.
				thumbUri = fileUri.toString();
				bind();
				expectingResult = true;
				ctx.startActivityForResult(intent, CollectSurveyData.TAKE_PHOTO_REQUEST );
			}
		});
		
		ImageButton selectFromGallery = (ImageButton)view.findViewById(R.id.selectFromGallery);
		selectFromGallery.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				expectingResult = true;
				ctx.startActivityForResult(Intent.createChooser(intent, "Select Photo"), CollectSurveyData.SELECT_FROM_GALLERY_REQUEST);
				
			}
		});
		
		thumb.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				if (thumbUri == null || "".equals(thumbUri)) {
					return;
				}
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(thumbUri));
				ctx.startActivity(intent);
			}
		});
		
	}
	
	private void updateThumbnail() {
		if (thumbUri != null && "".equals(thumbUri) == false) {
			
			Uri uri = Uri.parse(thumbUri);
			try {
				
				
				Bitmap image = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), uri);
				Bitmap imageThumb = Bitmap.createScaledBitmap(image, 100, 100, false);
				//Bitmap image = BitmapFactory.decodeFile(uri.getEncodedPath());
				thumb.setImageBitmap(imageThumb);
			}
			catch (Exception e) {
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
	
	public void bind() {
		ctx.getViewModel().getRecord().setValue(attribute, thumbUri);
	}

}
