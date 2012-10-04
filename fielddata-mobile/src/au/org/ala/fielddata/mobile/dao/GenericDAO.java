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
import android.util.Log;
import au.org.ala.fielddata.mobile.model.Persistent;
import au.org.ala.fielddata.mobile.service.dto.Mapper;

import com.google.gson.Gson;

public class GenericDAO<T extends Persistent> {

	protected DatabaseHelper helper;
	protected Context context;
	
	public static final String SELECT_BY_ID = "SELECT json FROM %s WHERE _id=?";

	public GenericDAO(Context ctx) {
		helper = DatabaseHelper.getInstance(ctx);
		context = ctx;
	}

	public T findByServerId(Class<T> modelClass, Integer id) {
		return findByColumn(modelClass, "server_id", Integer.toString(id), true);
	}

	public T load(Class<T> modelClass, Integer id) {
		return findByColumn(modelClass, "_id", Integer.toString(id), false);
	}
	
	public T loadIfExists(Class<T> modelClass, Integer id) {
		return findByColumn(modelClass, "_id", Integer.toString(id), true);
	}

	private T findByColumn(Class<T> modelClass, String column, String value, boolean allowNoResults) {
		synchronized(helper) {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor result = null;
		T modelObject = null;
		try {
			db = helper.getReadableDatabase();
			db.beginTransaction();
			result = db.query(true, modelClass.getSimpleName(), null, column + " = ?",
					new String[] { value }, null, null, null, null);

			
			if (result.getCount() != 1) {
				
				if (!allowNoResults) {
					Log.e("GenericDAO", "Expected 1 "+modelClass.getSimpleName()+", found: "+result.getCount());
					throw new DatabaseException("Expected 1 result, found: " + result.getCount());
				}
			}
			else {
				result.moveToFirst();
				String json = result.getString(5);
				Log.v("GenericDAO", "loading: "+json);
				Gson gson = Mapper.getGson(context);
				modelObject = (T) gson.fromJson(json, modelClass);
				modelObject.setId(result.getInt(0));
			}
			db.setTransactionSuccessful();

		} finally {
			if (result != null) {
				result.close();
			}
			if (db != null) {
				db.endTransaction();
				helper.close();
			}
		}

		return modelObject;
		}
	}

	public Integer save(T modelObject) {
		synchronized(helper) {
		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			db.beginTransaction();

			modelObject.setId(save(modelObject, db));

			db.setTransactionSuccessful();
		} finally {
			if (db != null) {
				db.endTransaction();
				helper.close();
			}
		}
		return modelObject.getId(); 
		}
	}

	public Integer save(T modelObject, SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		long now = System.currentTimeMillis();
		values.put("updated", now);
		modelObject.updated = now;
		Gson gson = Mapper.getGson(context);
		String value = gson.toJson(modelObject);
		Log.v("GenericDAO", "saving: "+value);
		values.put("json", value);
		values.put("server_id", modelObject.server_id);

		if (modelObject.getId() != null) {
			db.update(modelObject.getClass().getSimpleName(), values, "_id=?",
					new String[] { Integer.toString(modelObject.getId()) });
		} else {
			values.put("created", now);
			modelObject.created = now;
			long id = db.insertOrThrow(modelObject.getClass().getSimpleName(), null, values);
			if (id == -1) {
				throw new RuntimeException("Error saving object: " + modelObject);
			}
			modelObject.setId((int) id);
		}
		return modelObject.getId();
	}

	public List<T> loadAll(Class<T> modelClass) {
		synchronized(helper) {
		List<T> results = new ArrayList<T>();
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor result = null;
		T modelObject = null;
		try {
			db.beginTransaction();
			result = db.query(false, modelClass.getSimpleName(), null, null, null, null, null,
					null, null);

			if (result.getCount() > 0) {

				result.moveToFirst();
				while (!result.isAfterLast()) {
					String json = result.getString(5);
					Log.v("GenericDAO", "value="+json);
					Gson gson = Mapper.getGson(context);
					modelObject = (T) gson.fromJson(json, modelClass);
					modelObject.setId(result.getInt(0));
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
				helper.close();
			}
		}

		return results;
		}
	}

	public int count(Class<T> modelClass) {
		synchronized(helper) {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor result = null;

		int count = 0;
		try {
			db.beginTransaction();

			result = db.rawQuery(
					String.format("SELECT count(*) from %s", modelClass.getSimpleName()), null);
			if (result.getCount() != 1) {
				throw new DatabaseException("Error performing query");
			}
			result.moveToFirst();
			count = result.getInt(0);
			
			db.setTransactionSuccessful();
		} finally {
			if (result != null) {
				result.close();
			}
			if (db != null) {
				db.endTransaction();
				helper.close();
			}
		}
		return count;
		}
	}

	public void deleteAll(Class<T> modelClass) {
		synchronized(helper) {
		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			db.delete(modelClass.getSimpleName(), null, null);
		}
		finally {
			if (db != null) {
				helper.close();
			}
		}
		}
	}

	public void delete(Class<T> modelClass, Integer id) {
		synchronized(helper) {
		SQLiteDatabase db = helper.getWritableDatabase();
		
		try {
			db.delete(modelClass.getSimpleName(), "_id=?", new String[] {Integer.toString(id)});
		}
		finally {
			if (db != null) {
				helper.close();
			}
		}
		}
	}
}
