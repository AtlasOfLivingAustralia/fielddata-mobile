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

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import au.org.ala.fielddata.mobile.model.Persistent;

import com.google.gson.Gson;

public class GenericDAO<T extends Persistent> {

	protected DatabaseHelper helper;

	public static final String SELECT_BY_ID = "SELECT json FROM %s WHERE _id=?";

	public GenericDAO(Context ctx) {
		helper = DatabaseHelper.getInstance(ctx);

	}

	public T findByServerId(Class<T> modelClass, Integer id) {
		return findByColumn(modelClass, "server_id", Integer.toString(id));
	}
	
	public T load(Class<T> modelClass, Integer id)  {
		return findByColumn(modelClass, "_id", Integer.toString(id));
	}
	
	private T findByColumn(Class<T> modelClass, String column, String value) {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor result = null;
		T modelObject = null;
		try {
			db = helper.getReadableDatabase();
			db.beginTransaction();
			result = db.query(true, modelClass.getSimpleName(), null,
					column+" = ?", new String[] { value }, null,
					null, null, null);

			if (result.getCount() != 1) {
				throw new DatabaseException("Expected 1 result, found: "
						+ result.getCount());
			}
			result.moveToFirst();
			String json = result.getString(5);
			db.setTransactionSuccessful();
			Gson gson = new Gson();
			modelObject = (T) gson.fromJson(json, modelClass);

		} finally {
			if (result != null) {
				result.close();
			}
			if (db != null) {
				db.endTransaction();
				db.close();
			}
		}

		return modelObject;
	}

	public Integer save(T modelObject) {
		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			db.beginTransaction();
			
			modelObject.id = save(modelObject, db);

			db.setTransactionSuccessful();
		} finally {
			if (db != null) {
				db.endTransaction();
				db.close();
			}
		}
		return modelObject.id;
	}
	
	public Integer save(T modelObject, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		long now = System.currentTimeMillis();
		values.put("updated", now);
		modelObject.updated = now;
		values.put("json", modelObject.asJson());
		values.put("server_id", modelObject.server_id);

		if (modelObject.id != null) {
			db.update(modelObject.getClass().getSimpleName(), values,
					"_id=?",
					new String[] { Integer.toString(modelObject.id) });
		} else {
			values.put("created", now);
			modelObject.created = now;
			long id = db.insertOrThrow(modelObject.getClass()
					.getSimpleName(), null, values);
			if (id == -1) {
				throw new RuntimeException("Error saving object: "
						+ modelObject);
			}
			modelObject.id = (int) id;
		}
			return modelObject.id;
	}

	public List<T> loadAll(Class<T> modelClass)  {

		List<T> results = new ArrayList<T>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor result = null;
		T modelObject = null;
		try {
			db.beginTransaction();
			result = db.query(false, modelClass.getSimpleName(), null, null,
					null, null, null, null, null);

			if (result.getCount() > 0) {

				result.moveToFirst();
				while (!result.isAfterLast()) {
					String json = result.getString(5);
					Gson gson = new Gson();
					modelObject = (T) gson.fromJson(json, modelClass);
					modelObject.id = result.getInt(0);
					results.add(modelObject);
					result.moveToNext();

				}
			}
			db.setTransactionSuccessful();

		} finally {
			if (result != null) {
				result.close();
			}
			if (db != null) {
				db.endTransaction();
				db.close();
			}
		}

		return results;
	}
	
	public int count(Class<T> modelClass) {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor result = null;
		
		int count = 0;
		try {
			db.beginTransaction();
			
			result = db.rawQuery(String.format("SELECT count(*) from %s", modelClass.getSimpleName()), null);
		    if (result.getCount() != 1) {
		    	throw new DatabaseException("Error performing query");
		    }
		    result.moveToFirst();
		    count = result.getInt(0);
		    result.close();
			db.setTransactionSuccessful();
		} finally {
			if (result != null) {
				result.close();
			}
			if (db != null) {
				db.endTransaction();
				db.close();
			}
		}
		return count;
	}

}
