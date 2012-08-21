package au.org.ala.fielddata.mobile.test;

import android.test.AndroidTestCase;
import au.org.ala.fielddata.mobile.service.LoginService;

/**
 * The class <code>RecordServiceTest</code> contains tests for the class {@link
 * <code>RecordService</code>}
 */
public class LoginServiceTest extends AndroidTestCase {


	/**
	 * Run the void sync(List<Record>) method test
	 */
	public void testSync() throws Exception
	{
		// add test code here
		LoginService fixture = new LoginService(getContext());
		fixture.login();
		
	}
}
