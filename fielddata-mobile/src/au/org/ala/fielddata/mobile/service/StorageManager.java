package au.org.ala.fielddata.mobile.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import au.org.ala.fielddata.mobile.model.Species;

/**
 * The StorageManager is responsible for managing files such as profile
 * images and (maybe?) map tiles that could potentially take up a fair
 * bit of space.
 * We store them locally in the application cache but handle the case that
 * the o/s deletes them.
 */
public class StorageManager {

	private Context ctx;
	private FieldDataService downloadService;
	
	public StorageManager(Context ctx) {
		this.ctx = ctx;
		downloadService = new FieldDataService(ctx);
	}
	
	/**
	 * Returns the profile image, potentially downloading it if necessary.
	 * Must be called from a background thread.
	 * @param species the species to get the image for.
	 * @return the File containing the profile image, null if the species
	 * does not have a defined profile image.
	 */
	public File getProfileImage(Species species) {
		
		String fileName = species.getImageFileName(); 
		if (fileName == null) {
			Log.i("CacheManager", "Species "+species.scientificName+" does not have a profile image");
			return null;
		}
		
		File cacheDir = ctx.getCacheDir();
		File profileImage = new File(cacheDir, fileName+".jpg");
		
		if (!profileImage.exists()) {
			try {
				downloadService.downloadSpeciesProfileImage(fileName, profileImage);
			}
			catch (Exception e) {
				// If we are offline the download will fail.
			}
		}
		return profileImage;
	}
	
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;

	/** Create a file Uri for saving an image or video */
	public static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}
	
	public static boolean canWriteToExternalStorage() {
		String state = Environment.getExternalStorageState();
		 return Environment.MEDIA_MOUNTED.equals(state);
	}
	
	/** Create a File for saving an image or video */
	@TargetApi(8)
	private static File getOutputMediaFile(int type){
		
		if (!canWriteToExternalStorage()) {
			throw new RuntimeException("External storage is not writable!");
		}
		
		
		
		File mediaStorageDir = null;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "FieldData");
		}
		else {
			mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "FieldData");
		}
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
	
	public Bitmap bitmapFromFile(Uri path, int targetW, int targetH) {
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path.getEncodedPath(), bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		// Determine how much to scale down the image
		int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

		// Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		Bitmap bitmap = BitmapFactory.decodeFile(path.getEncodedPath(), bmOptions);
		return bitmap;
	}
	
	public Bitmap bitmapFromUri(Uri uri, int targetW, int targetH) {
		Log.d("StorageManager", "Creating bitmap from :"+uri.toString());
		Bitmap bitmap = null;
		if ("file".equals(uri.getScheme())) {
			bitmap = bitmapFromFile(uri, targetW, targetH);
		} else if ("content".equals(uri.getScheme())) {
			bitmap = MediaStore.Images.Thumbnails.getThumbnail(ctx.getContentResolver(),
					Long.parseLong(uri.getLastPathSegment()),
					MediaStore.Images.Thumbnails.MICRO_KIND, null);
		}
		return bitmap;
	}
	
}
