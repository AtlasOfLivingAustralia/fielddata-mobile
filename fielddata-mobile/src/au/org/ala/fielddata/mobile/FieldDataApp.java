package au.org.ala.fielddata.mobile;

import java.io.File;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.app.ActivityManager;
import android.app.Application;
import au.org.ala.fielddata.mobile.nrmplus.R;

/**
 * Performs application initialisation.
 */
public class FieldDataApp extends Application {

	
	@Override
	public void onCreate() {
		super.onCreate();
		
		File cacheDir = new File(getCacheDir(), "images"); 
		int memoryInMB = ((ActivityManager)getSystemService(ACTIVITY_SERVICE)).getMemoryClass();
		long totalAppHeap = memoryInMB * 1024 * 1024;
		int cacheSize =  (int)totalAppHeap/4; // Use a max of a quarter of the heap for image cache.
		DisplayImageOptions options = new DisplayImageOptions.Builder().
				cacheInMemory().
				cacheOnDisc().
				showStubImage(R.drawable.species_list_icon).
				build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
			.memoryCacheSize(cacheSize)
			.defaultDisplayImageOptions(options)
			.discCache(new UnlimitedDiscCache(cacheDir))
			.build();
		ImageLoader.getInstance().init(config);
		
	}
}
