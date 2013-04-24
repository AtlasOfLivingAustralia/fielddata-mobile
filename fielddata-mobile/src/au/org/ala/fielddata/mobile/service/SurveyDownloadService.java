package au.org.ala.fielddata.mobile.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import au.org.ala.fielddata.mobile.service.FieldDataService.SurveyDownloadCallback;


public class SurveyDownloadService extends IntentService implements SurveyDownloadCallback {

	public static final String PROGRESS_ACTION = "Progress";
	public static final String FINISHED_ACTION = "Finished";
	public static final String NUMBER_EXTRA = "number";
	public static final String COUNT_EXTRA = "count";
	
	// This is a bit yuck, but the alternative is to use a bound service
	// and do my own threading. It's required because if the LoginActivity
	// gets destroyed due to screen rotation it may miss the finished 
	// broadcast so it also needs to check this field on resume.
	private static boolean downloading;
	
	public static synchronized boolean isDownloading() {
		return downloading;
	}
	
	private static synchronized void setDownloading(boolean newDownloading) {
		downloading = newDownloading;
	}
	
	public SurveyDownloadService() {
		super("SurveyDownloadService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (SurveyDownloadService.isDownloading()) {
			return;
		}
		SurveyDownloadService.setDownloading(true);
		new FieldDataService(this).downloadSurveys(this);
		SurveyDownloadService.setDownloading(false);
		finished();
		
	}
	
	private void finished() {
		Intent finishedIntent = new Intent(FINISHED_ACTION);
		LocalBroadcastManager.getInstance(this).sendBroadcast(finishedIntent);
		
		Log.i("SurveyDownloadService", "Finished download");
	}

	public void surveysDownloaded(int number, int count) {
		
		Intent progressIntent = new Intent(PROGRESS_ACTION);
		progressIntent.putExtra(NUMBER_EXTRA, number);
		progressIntent.putExtra(COUNT_EXTRA, count);
		
		LocalBroadcastManager.getInstance(this).sendBroadcast(progressIntent);
		
		Log.i("SurveyDownloadService", "update...");
	}
	
}
