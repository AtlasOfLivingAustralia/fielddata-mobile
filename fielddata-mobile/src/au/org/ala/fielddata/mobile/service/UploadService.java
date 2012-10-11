package au.org.ala.fielddata.mobile.service;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import au.org.ala.fielddata.mobile.R;
import au.org.ala.fielddata.mobile.ViewSavedRecordsActivity;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.dao.RecordDAO;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.User;
import au.org.ala.fielddata.mobile.pref.Preferences;

/**
 * Uploads Records to the Field Data server.
 */
public class UploadService extends Service {

	public static final String UPLOADED = "Upload";
	public static final String UPLOAD_FAILED = "UploadFailed";
	
	public static final String RECORD_IDS_EXTRA = "RecordIds";
	
	private int SUCCESS = 0;
	private int FAILED_INVALID = 1;
	private int FAILED_SERVER = 2;

	private static final String START_ID = "startId";
	
	
	private UploadServiceHandler serviceHandler;
	private Looper serviceLooper;
	private UploadWakeup networkStatusReceiver;
	
	private List<Bundle> deferredWorkQueue;
		
	private final class UploadServiceHandler extends Handler {
		public UploadServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			Log.i("UploadService", "Handling message: "+msg.getData());
			Bundle msgData = msg.getData();
			if (canUpload()) {
				Log.i("UploadService", "Able to upload!");
				int startId = msgData.getInt(START_ID);
				int[] recordIds = msgData.getIntArray(RECORD_IDS_EXTRA);
				uploadRecords(recordIds);
				Log.d("UploadService", "Stopping with id: "+startId);
				stopSelf(startId);
			}
			else {
				Log.i("UploadService", "Unable to upload, requeuing message");
				notifyQueued();
				synchronized(deferredWorkQueue) {
					deferredWorkQueue.add(msgData);
				}
			}
		}
		
		
	}
	
	class UploadWakeup extends BroadcastReceiver {
		
		public void onReceive(Context context, Intent intent) {
			Log.i("UploadService", "Network status changed: "+intent+" Context: "+context);
			synchronized(deferredWorkQueue) {
				for (int i=0; i<deferredWorkQueue.size(); i++) {
					Message msg = serviceHandler.obtainMessage();
					msg.setData(deferredWorkQueue.get(i));
					serviceHandler.sendMessage(msg);
				}
				deferredWorkQueue.clear();
			}
			
			
		}
	}
	
	@Override
	public Binder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.i("UploadService", "Upload Service Started.");
		
		deferredWorkQueue = new ArrayList<Bundle>();
		
		HandlerThread thread = new HandlerThread("UploadThread", Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		
		serviceLooper = thread.getLooper();
		serviceHandler = new UploadServiceHandler(serviceLooper);
		
		networkStatusReceiver = new UploadWakeup();
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(networkStatusReceiver, filter);
	}
	
	@Override
	public void onDestroy() {
		Log.i("UploadService", "Upload Service Stopped.");
		serviceLooper.quit();
		unregisterReceiver(networkStatusReceiver);
	}
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int[] recordIds = intent.getIntArrayExtra(RECORD_IDS_EXTRA);
		
		Log.i("UploadService", "Upload requested for "+ (recordIds == null ? "all" : recordIds.length) +" records, startId="+startId);
		
		Bundle messageArgs = new Bundle();
		messageArgs.putInt(START_ID, startId);
		messageArgs.putIntArray(RECORD_IDS_EXTRA, recordIds);
		Message message = serviceHandler.obtainMessage();
		message.setData(messageArgs);
		serviceHandler.sendMessage(message);
		
		return START_REDELIVER_INTENT;
	}
	
	/**
	 * Uploads the records identified by the supplied array of ids.
	 * @param recordIds the ids of the records to upload.
	 * @return the action to broadcast after the upload is complete
	 * (SUCCESS, FAILED_INVALID or FAILED_SERVER).
	 */
	private void uploadRecords(int[] recordIds) {
		RecordDAO recordDao = new RecordDAO(this);
		List<Record> records = new ArrayList<Record>();
		if (recordIds == null) {
			records.addAll(recordDao.loadAll(Record.class));
		}
		else {
			for (int id : recordIds) {
				Record record = recordDao.loadIfExists(Record.class, id);
				if (record != null) {
					records.add(record);
				}
			}
		}
		int successCount = 0;
		int invalidCount = 0;
		int failedCount = 0;
		for (Record record : records) {
			int result = upload(record);
			if (result == SUCCESS) {
				successCount++;
				recordDao.delete(Record.class, record.getId());
			}
			else if (result == FAILED_INVALID) {
				invalidCount++;
			}
			else if (result == FAILED_SERVER) {
				failedCount++;
			}
		}
		String action = null;
		if (failedCount > 0) {
			action = UPLOAD_FAILED;
			notifyFailed(failedCount);
		}
		else {
			action = UPLOADED;
			notifiySuccess(successCount);
		}
		Intent broadcastIntent = new Intent(action);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
		
	}
	
	private boolean canUpload() {
		
		Preferences prefs = new Preferences(this);
		ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		
		
		boolean needsWifi = prefs.getUploadOverWifiOnly();
		Log.d("UploadService", "Can upload, WIFI only is: "+needsWifi);
		
		NetworkInfo networkInfo;
		if (needsWifi) {
			networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		}
		else {
			networkInfo = connectivityManager.getActiveNetworkInfo();
		}
		return networkInfo != null && networkInfo.isConnected();
	}
	
	/**
	 * Uploads a single record to the server.
	 * @param record the Record to upload.
	 * @return true if the upload succeeded, false otherwise.
	 */
	private int upload(Record record) {
	
		int resultCode = SUCCESS;
		FieldDataServiceClient service = new FieldDataServiceClient(getApplicationContext());
		
		List<Record> tmp = new ArrayList<Record>();
		tmp.add(record);
		try {
			
			if (record.isValid()) {
				service.sync(tmp);
				resultCode = SUCCESS;
			}
			else {
				Log.w("UploadService", "Not uploading due to validation error");
				resultCode = FAILED_INVALID;
			}
		}
		catch (Exception e) {
			Log.e("UploadService", "Error calling the field data service: ", e);
			resultCode = FAILED_SERVER;
		}
		return resultCode;
	}
	
	private void notifiySuccess(int numSuceeded) {
		Preferences prefs = new Preferences(this);
		GenericDAO<User> userDao = new GenericDAO<User>(this);
		List<User> user = userDao.loadAll(User.class);
		
		String reviewUrl = String.format(prefs.getReviewUrl(), user.get(0).server_id);
		Uri records = Uri.parse(reviewUrl);
		Intent viewRecords = new Intent(Intent.ACTION_VIEW, records);
		PendingIntent intent = PendingIntent.getActivity(this, START_NOT_STICKY, viewRecords, PendingIntent.FLAG_CANCEL_CURRENT);
		
		String message = numSuceeded == 1 ? " record uploaded" : " records uploaded";
		
		notify(numSuceeded + message, reviewUrl, intent, true);
		
	}
	
	private void notifyFailed(int numFailed) {
		Intent savedActivity = new Intent(this, ViewSavedRecordsActivity.class);
		PendingIntent intent = PendingIntent.getActivity(this, START_NOT_STICKY, savedActivity, PendingIntent.FLAG_CANCEL_CURRENT);
		
		notify(numFailed + " records failed to upload", "Touch to view saved records", intent, false);
	}
	
	private void notifyQueued() {
		Intent savedActivity = new Intent(this, ViewSavedRecordsActivity.class);
		PendingIntent intent = PendingIntent.getActivity(this, START_NOT_STICKY, savedActivity, PendingIntent.FLAG_CANCEL_CURRENT);
		
		notify("Upload pending - no network", "Records will be uploaded when network service is available.", intent, true);
	}
	
	private void notify(String title, String subject, PendingIntent intent, boolean success) {
		
		NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle(title)
		       .setContentText(subject)
		       .setTicker(title)
		       .setAutoCancel(true)
		       .setSmallIcon(R.drawable.ala_notification)
			  .setContentIntent(intent);
		
		Notification notification = builder.getNotification();
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notificationManager.notify(success ? SUCCESS : FAILED_SERVER, notification);
		Log.i("UploadService", "sending notification: "+title);
	}
	
	

	

}
