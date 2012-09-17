package au.org.ala.fielddata.mobile.service;

import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.validation.RecordValidator;
import au.org.ala.fielddata.mobile.validation.RecordValidator.RecordValidationResult;

/**
 * Uploads Records to the Field Data server.
 */
public class UploadService extends IntentService {

	public static final String UPLOADED = "Upload";
	public static final String UPLOAD_FAILED = "UploadFailed";
	
	public static final String RECORD_IDS_EXTRA = "RecordIds";
	
	private int SUCCESS = 0;
	private int FAILED_INVALID = 1;
	private int FAILED_SERVER = 2;
	
	public UploadService() {
		super("Upload Service");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		
		Log.i("UploadService", "Uploading records...");
		
		int[] recordIds = intent.getIntArrayExtra(RECORD_IDS_EXTRA);
		
		GenericDAO<Record> recordDao = new GenericDAO<Record>(this);
		List<Record> records = new ArrayList<Record>();
		if (recordIds == null) {
			records.addAll(recordDao.loadAll(Record.class));
		}
		else {
			for (int id : recordIds) {
				records.add(recordDao.load(Record.class, id));
			}
		}
		boolean success = true;
		for (Record record : records) {
			int result = upload(record);
			if (result == SUCCESS) {
				recordDao.delete(Record.class, record.getId());
			}
			success = success && result == SUCCESS;
		}
		String action = UPLOADED;
		if (!success) {
			action = UPLOAD_FAILED;
		}
		
		intent = new Intent(action);
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
		stopSelf();
	}
	
	/**
	 * Uploads a single record to the server.
	 * @param record the Record to upload.
	 * @return true if the upload succeeded, false otherwise.
	 */
	private int upload(Record record) {
	
		int resultCode = SUCCESS;
		GenericDAO<Survey> surveyDao = new GenericDAO<Survey>(this);
		Survey survey = surveyDao.findByServerId(Survey.class, record.survey_id);
		FieldDataServiceClient service = new FieldDataServiceClient(getApplicationContext());
		
		RecordValidator validator = new RecordValidator();
		List<Record> tmp = new ArrayList<Record>();
		tmp.add(record);
		try {
			RecordValidationResult result = validator.validateRecord(survey, record);
			if (result.valid()) {
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
	
	

}
