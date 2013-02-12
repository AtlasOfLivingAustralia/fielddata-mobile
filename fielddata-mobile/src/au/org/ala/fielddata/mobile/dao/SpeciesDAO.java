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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import au.org.ala.fielddata.mobile.model.Species;

public class SpeciesDAO extends GenericDAO<Species> {

	public static final String SURVEY_SPECIES_TABLE = "SURVEY_SPECIES";
	public static final String SPECIES_TABLE = "SPECIES";
	public static final String SPECIES_GROUP_TABLE = "SPECIES_GROUP";
	
	
	// Column names for the species table
	public static final String SERVER_ID_COLUMN_NAME = "server_id";
	public static final String CREATED_COLUMN_NAME = "created";
	public static final String UPDATED_COLUMN_NAME = "updated";
	public static final String LSID_COLUMN_NAME = "lsid";
	public static final String SCIENTIFIC_NAME_COLUMN_NAME = "scientific_name";
	public static final String COMMON_NAME_COLUMN_NAME = "column_name";
	public static final String IMAGE_URL_COLUMN_NAME = "image_url";
	public static final String SPECIES_GROUP_COLUMN_NAME = "species_group_id";
	
	
	public static final String SPECIES_TABLE_COLUMNS = 
			"_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
			SERVER_ID_COLUMN_NAME + " INTEGER, " +
		    CREATED_COLUMN_NAME + " INTEGER, " +
		    UPDATED_COLUMN_NAME + " INTEGER, " +
		    LSID_COLUMN_NAME+" TEXT, " +
		    SCIENTIFIC_NAME_COLUMN_NAME + " TEXT, "+
		    COMMON_NAME_COLUMN_NAME + " TEXT, "+
		    IMAGE_URL_COLUMN_NAME + " TEXT, " +
		    SPECIES_GROUP_COLUMN_NAME + " INTEGER";
	
	// Shared column indexes (select *)
	public static final int ID_COLUMN_IDX = 0;
	public static final int SERVER_ID_COLUMN_IDX = 1;
	public static final int CREATED_COLUMN_IDX = 2;
	public static final int UPDATED_COLUMN_IDX = 3;
	
	// Column indexes for the SPECIES TABLE (select *)
	public static final int LSID_COLUMN_IDX = 4;
	public static final int SCIENTIFIC_NAME_COLUMN_IDX = 5;
	public static final int COMMON_NAME_COLUMN_IDX = 6;
	public static final int IMAGE_URL_COLUMN_IDX = 7;
	public static final int SPECIES_GROUP_COLUMN_IDX = 8;
		
	
	public static final String SPECIES_TABLE_DDL = "CREATE TABLE "+SPECIES_TABLE+
				" ("+ SPECIES_TABLE_COLUMNS+ ")";
	
	public static final String SURVEY_SPECIES_TABLE_DDL = "CREATE TABLE "+SURVEY_SPECIES_TABLE+
			" (survey_id INTEGER, species_id INTEGER)";
	
	public static final String SPECIES_GROUP_DDL = "CREATE TABLE "+SPECIES_GROUP_TABLE+
			" (_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
			"  name TEXT, " +
			"  parent_group_id INTEGER)";
	
	public SpeciesDAO(Context ctx) {
		super(ctx);
	}

	protected Species map(SQLiteDatabase db, Cursor result, Class<Species> modelClass) {
		
		Species species = new Species();
		species.setId(result.getInt(ID_COLUMN_IDX));
		species.server_id = result.getInt(SERVER_ID_COLUMN_IDX);
		species.created = result.getLong(CREATED_COLUMN_IDX);
		species.updated = result.getLong(UPDATED_COLUMN_IDX);
		species.setLsid(result.getString(LSID_COLUMN_IDX));
		species.scientificName = result.getString(SCIENTIFIC_NAME_COLUMN_IDX);
		species.commonName = result.getString(COMMON_NAME_COLUMN_IDX);
		species.setImageFileName(result.getString(IMAGE_URL_COLUMN_IDX));
		species.setTaxonGroupId(result.getInt(SPECIES_GROUP_COLUMN_IDX));
		
		return species;
	}
	
	public Integer save(Species species, SQLiteDatabase db) {
		
		long now = System.currentTimeMillis();
		
		ContentValues values = new ContentValues();
		boolean update = map(species, now, values);                                                                                                                                                 
	
		Integer id;
		if (!update) {
			id = (int)db.insertOrThrow(SPECIES_TABLE, null, values);
			species.setId(id);
		}
		else {
			id = species.getId();
			String whereClause = "_id=?";
			String[] params = new String[] { Integer.toString(id)};
			int numRows = db.update(SPECIES_TABLE, values, whereClause, params);
			if (numRows != 1) {
				throw new DatabaseException("Update failed for record with id="+id+", table="+SPECIES_TABLE);
			}
		}
		return id;
	}

	private boolean map(Species species, long now, ContentValues values) {
		Integer id = species.getId();
		boolean update = id != null;
	
		values.put(SERVER_ID_COLUMN_NAME, species.server_id);
		values.put(UPDATED_COLUMN_NAME, now);
		if (!update) {
			species.created = now;
			values.put(CREATED_COLUMN_NAME, now);
		}
		values.put(LSID_COLUMN_NAME, species.getLsid());
		values.put(SCIENTIFIC_NAME_COLUMN_NAME, species.scientificName);
		values.put(COMMON_NAME_COLUMN_NAME, species.commonName);
		values.put(IMAGE_URL_COLUMN_NAME, species.getImageFileName());
		values.put(SPECIES_GROUP_COLUMN_NAME, species.getTaxonGroupId());
		
		return update;
	}
	
	public Cursor speciesForSurvey(int surveyId) {
		throw new UnsupportedOperationException();
	}
	
}
