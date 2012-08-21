/*******************************************************************************
 * Copyright (C) 2010 Atlas of Living Australia
 * All Rights Reserved.
 *  
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *  
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 ******************************************************************************/
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
