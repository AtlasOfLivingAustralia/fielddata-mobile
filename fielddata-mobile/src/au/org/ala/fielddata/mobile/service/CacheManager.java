package au.org.ala.fielddata.mobile.service;

import java.io.File;

import android.content.Context;
import android.util.Log;
import au.org.ala.fielddata.mobile.model.Species;

/**
 * The CacheManager is responsible for managing files such as profile
 * images and (maybe?) map tiles that could potentially take up a fair
 * bit of space.
 * We store them locally in the application cache but handle the case that
 * the o/s deletes them.
 */
public class CacheManager {

	private Context ctx;
	private FieldDataService downloadService;
	
	public CacheManager(Context ctx) {
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
			downloadService.downloadSpeciesProfileImage(fileName, profileImage);
		}
		return profileImage;
	}
	
}
