package au.org.ala.fielddata.mobile.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import android.test.AndroidTestCase;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.service.LoginResponse;
import au.org.ala.fielddata.mobile.service.LoginService;
import au.org.ala.fielddata.mobile.service.FieldDataService;

/**
 * The class <code>RecordServiceTest</code> contains tests for the class {@link
 * <code>RecordService</code>}
 */
public class RecordServiceTest extends AndroidTestCase {

	private LoginResponse login;
	
	
	protected void setUp() {
		login = new LoginService(getContext()).login();
	}

	/**
	 * Run the void sync(List<Record>) method test
	 */
	public void testSync() throws Exception
	{
		// add test code here
		FieldDataService fixture = new FieldDataService(getContext());
		List<Record> records = new ArrayList<Record>();
		Record r = new Record();
		records.add(r);
		fixture.sync(records);
		
	}
	
	public void testDownloadSurveys() {
		FieldDataService fixture = new FieldDataService(getContext());
		List<Survey> surveys = fixture.downloadSurveys();
		System.out.println("Surveys: "+surveys);
		
	}
}
