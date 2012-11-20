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
package au.org.ala.fielddata.mobile.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import au.org.ala.fielddata.mobile.Utils;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Species;
import au.org.ala.fielddata.mobile.model.Survey;
import au.org.ala.fielddata.mobile.model.User;

/**
 * Responsible for creating and configuring the database used by the
 * application.
 * As a first cut, all objects are stored in a table equal to their class 
 * name in a serialized form (json) along side a little bit of metadata.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "FieldData.db";
	private static final int SCHEMA_VERSION = 2;

	private static final String[] TABLES = { Survey.class.getSimpleName(),
			Species.class.getSimpleName(),
			User.class.getSimpleName()};

	private static DatabaseHelper instance;
	
	public synchronized static DatabaseHelper getInstance(Context ctx) {
		if (instance == null) {
			instance = new DatabaseHelper(ctx.getApplicationContext());
		}
		return instance;
	}
	
	private DatabaseHelper(Context ctx) {
		super(ctx, DATABASE_NAME, null, SCHEMA_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		try {
			db.beginTransaction();

			for (String table : TABLES) {
				db.execSQL("CREATE TABLE "
						+ table
						+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, server_id INTEGER, " +
							"created INTEGER, updated INTEGER, last_sync INTEGER" +
							"name TEXT, json TEXT)");
				
				
			}
			createRecordTable(db);
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		version2(db);
	}
	
	private void version2(SQLiteDatabase db) {
		if (Utils.DEBUG) {
			Log.i("DatabaseHelper", "Upgrading to version 2 of the schema");
		}
		db.execSQL("DROP TABLE "+Record.class.getSimpleName());
		createRecordTable(db);
	}
	

	private void createRecordTable(SQLiteDatabase db) {
		
		db.execSQL(RecordDAO.RECORD_TABLE_DDL);
		db.execSQL(RecordDAO.ATTRIBUTE_VALUE_TABLE_DDL);
		db.execSQL(DraftRecordDAO.DRAFT_RECORD_TABLE_DDL);
		db.execSQL(DraftRecordDAO.DRAFT_ATTRIBUTE_TABLE_DDL);
	}
	
}
