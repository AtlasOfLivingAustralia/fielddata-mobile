package au.org.ala.fielddata.mobile.service;

import java.util.ArrayList;
import java.util.List;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.validation.RecordValidator;
import au.org.ala.fielddata.mobile.validation.RecordValidator.RecordValidationResult;

/**
 * Uploads Records to the Field Data server.
 */
public class UploadService extends IntentService {

	public UploadService() {
		super("Upload Service");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		
		Log.i("UploadService", "Uploading records...");
		
		GenericDAO<Record> recordDao = new GenericDAO<Record>(getApplicationContext());
		
		List<Record> records = recordDao.loadAll(Record.class);
		
		boolean success = true;
		for (Record record : records) {
			boolean result = upload(record);
			if (result) {
				recordDao.delete(Record.class, record.getId());
			}
			success = success && result;
		}
		if (success) {
			Toast.makeText(getApplicationContext(), "Uploaded succeeded!", Toast.LENGTH_SHORT).show();
		}
		stopSelf();
		
	}
	
	/**
	 * Uploads a single record to the server.
	 * @param record the Record to upload.
	 * @return true if the upload succeeded, false otherwise.
	 */
	private boolean upload(Record record) {
	
		boolean success = false;
		GenericDAO<Survey> surveyDao = new GenericDAO<Survey>(getApplicationContext());
		Survey survey = surveyDao.findByServerId(Survey.class, record.survey_id);
		FieldDataService service = new FieldDataService(getApplicationContext());
		
		RecordValidator validator = new RecordValidator();
		List<Record> tmp = new ArrayList<Record>();
		tmp.add(record);
		try {
			RecordValidationResult result = validator.validateRecord(survey, record);
			if (result.valid()) {
				service.sync(tmp);
				success = true;
				
			}
			else {
				Log.w("UploadService", "Not uploading due to validation error");
				Toast.makeText(getApplicationContext(), "Record invalid!", Toast.LENGTH_SHORT).show();
			}
		}
		catch (Exception e) {
			Log.e("UploadService", "Error calling the field data service: ", e);
			Toast.makeText(getApplicationContext(), "Upload failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
		}
		return success;
	}
	
	

}
