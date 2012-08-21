package au.org.ala.fielddata.mobile.test;

import android.test.AndroidTestCase;
import au.org.ala.fielddata.mobile.dao.GenericDAO;
import au.org.ala.fielddata.mobile.model.Record;

public class GenericDaoTest extends AndroidTestCase {

	
	public void testSaveAndLoadById() throws Exception {
		GenericDAO<Record> recordDao = new GenericDAO<Record>(getContext());
		
		Record record = new Record();
		
		record.latitude = -36.885845;
		record.longitude = 149.912548;
		record.notes = "test from android";
		
		record.id = recordDao.save(record);
		
		
		Record record2 = recordDao.load(Record.class, record.id);
		
		assertEquals(record.latitude, record2.latitude);
		assertEquals(record.longitude, record2.longitude);
		assertEquals(record.notes, record2.notes);
		
	}
}
