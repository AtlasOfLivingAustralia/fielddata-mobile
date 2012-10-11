package au.org.ala.fielddata.mobile.dao;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Record.AttributeValue;
import au.org.ala.fielddata.mobile.model.Record.PropertyAttributeValue;

public class RecordDAO extends GenericDAO<Record> {

	public static final String ATTRIBUTE_VALUE_TABLE = "ATTRIBUTE_VALUE";
	
	// Shared column indexes (select *)
	public static final int ID_COLUMN = 0;
	public static final int SERVER_ID_COLUMN = 1;
	public static final int CREATED_COLUMN = 2;
	public static final int UPDATED_COLUMN = 3;
	
	// Column indexes for the RECORD TABLE (select *)
	public static final int UUID_COLUMN = 4;
	public static final int NUMBER_COLUMN = 5;
	public static final int WHEN_COLUMN = 6;
	public static final int NOTES_COLUMN = 7;
	public static final int LATITUDE_COLUMN = 8;
	public static final int LONGITUDE_COLUMN = 9;
	public static final int ACCURACY_COLUMN = 10;
	public static final int POINT_TIME_COLUMN = 11;
	public static final int POINT_SOURCE_COLUMN = 12;
	public static final int LOCATION_ID_COLUMN = 13;
	public static final int SURVEY_ID_COLUMN = 14;
	public static final int TAXON_ID_COLUMN = 15;
	public static final int STATUS_COLUMN = 16;
	public static final int SCIENTIFIC_NAME_COLUMN = 17;
	
	// Column indexes for the ATTRIBUTE_VALUE table (select *)
	public static final int RECORD_ID_COLUMN = 4;
	public static final int ATTRIBUTE_ID_COLUMN = 5;
	public static final int ATTRIBUTE_VALUE_COLUMN = 6;
	public static final int TYPE_COLUMN = 7;
	
	private static final int TYPE_TEXT = 0;
	private static final int TYPE_URI = 1;
	
	
	public static final String RECORD_TABLE_DDL = "CREATE TABLE RECORD "+
	"(_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
	"server_id INTEGER, " +
    "created INTEGER, " +
    "updated INTEGER, " +
    "uuid TEXT, " +
    "number INTEGER, "+
    "when_millis INTEGER, "+
    "notes TEXT, "+
    "latitude REAL, " +
    "longitude REAL, " +
    "accuracy REAL, " +
    "point_millis INTEGER, " +
    "point_source TEXT, " +
    "location_id INTEGER, " +
    "survey_id INTEGER, "+
    "taxon_id INTEGER, "+
    "status INTEGER, " +
    "scientific_name TEXT)";
	
	
	public static final String ATTRIBUTE_VALUE_TABLE_DDL = "CREATE TABLE "+ATTRIBUTE_VALUE_TABLE+" "+
	"(_id INTEGER PRIMARY KEY AUTOINCREMENT, "+
	"server_id INTEGER, "+
	"created INTEGER, " +
    "updated INTEGER, " +
    "record_id INTEGER, " +
    "attribute_id INTEGER, "+
    "value TEXT, " +
    "type INTEGER)";
	
	public RecordDAO(Context ctx) {
		super(ctx);
	}
	
	public Integer save(Record record, SQLiteDatabase db) {
		
		Integer id = record.getId();
		boolean update = id != null;
		ContentValues values = new ContentValues();
		long now = System.currentTimeMillis();
		
		values.put("uuid", record.uuid);
		//values.put("server_id", record.server_id); // Since we delete after upload we don't have to worry about server_id
		
		values.put("updated", now);
		
		Location location = record.getLocation();
		if (location != null) {
			values.put("latitude", location.getLatitude());
			values.put("longitude", location.getLongitude());
			values.put("point_source", location.getProvider());
			values.put("accuracy", location.getAccuracy());
			values.put("point_millis", location.getTime());
			Log.d("RecordDAO", "Putting: "+location.getTime());
		}
		else if (update) {
			values.put("latitude", (Double)null);
			values.put("longitude", (Double)null);
			values.put("point_source", (String)null);
			values.put("accuracy", (Float)null);
			values.put("point_millis", (Long)null);
		}
		values.put("when_millis", record.when);
		values.put("notes", record.notes);
		values.put("survey_id", record.survey_id);
		values.put("number", record.number);
		values.put("taxon_id", record.taxon_id);
		values.put("status", record.isValid() ? 0 : 1);
		values.put("scientific_name", record.scientificName);
		record.updated = now;                                                                                                                                                 
	
		
		if (!update) {
			record.created = now;
			values.put("created", now);
		
			id = (int)db.insertOrThrow(Record.class.getSimpleName(), null, values);
	
			record.setId(id);
		}
		else {
			String whereClause = "_id=?";
			String[] params = new String[] { id.toString()};
			db.update(Record.class.getSimpleName(), values, whereClause, params);
		
			// Since the number of attributes is fairly small we can probably 
			// get away with re-writing the attributes.
			whereClause = "record_id=?";
			db.delete(ATTRIBUTE_VALUE_TABLE, whereClause, params);
		}
		
		InsertHelper insertHelper = new InsertHelper(db, ATTRIBUTE_VALUE_TABLE);
		for (AttributeValue attrValue : record.getAttributeValues()) {
			// PropertyAttributeValues have already been inserted as columns of the RECORD table.
			if (!(attrValue instanceof PropertyAttributeValue)) {
				// Don't insert values that are empty, this is to prevent problems
				// when we upload. (e.g. numbers evaluate to NaN, URIs give a 
				// broken image link etc.)
				String value = attrValue.nullSafeValue();
				if (value.length() > 0) {
					insertHelper.prepareForInsert();
					insertHelper.bind(CREATED_COLUMN+1, now);
					insertHelper.bind(UPDATED_COLUMN+1, now);
					//insertHelper.bind(SERVER_ID_COLUMN+1, attrValue.server_id);
					insertHelper.bind(ATTRIBUTE_ID_COLUMN+1, attrValue.attribute_id);
					insertHelper.bind(RECORD_ID_COLUMN+1, id);
					insertHelper.bind(ATTRIBUTE_VALUE_COLUMN+1, value);
					insertHelper.bind(TYPE_COLUMN+1, attrValue.isUri() ? TYPE_URI : TYPE_TEXT);
					Log.d("RecordDAO", "Inserting value: "+attrValue.nullSafeValue()+" for record: "+id);
					long attr_value_id = insertHelper.execute();
					attrValue.id = (int)attr_value_id;
				}
			}
		}
		
		
		return id;
	}
	
	protected Record map(SQLiteDatabase db, Cursor result, Class<Record> modelClass) {
		Record record = new Record();
		record.setId(result.getInt(ID_COLUMN));
		record.server_id = result.getInt(SERVER_ID_COLUMN);
		record.created = result.getInt(CREATED_COLUMN);
		record.updated = result.getInt(UPDATED_COLUMN);
		record.uuid = result.getString(UUID_COLUMN);
		record.number = result.getInt(NUMBER_COLUMN);
		record.when = result.getLong(WHEN_COLUMN);
		record.notes = result.getString(NOTES_COLUMN);
		
		String locationSource = result.getString(POINT_SOURCE_COLUMN);
		if (locationSource != null) {
			Location location = new Location(locationSource);
			location.setLatitude(result.getDouble(LATITUDE_COLUMN));
			location.setLongitude(result.getDouble(LONGITUDE_COLUMN));
			location.setAccuracy(result.getFloat(ACCURACY_COLUMN));
			location.setTime(result.getLong(POINT_TIME_COLUMN));
			record.setLocation(location);
		}
		record.location = result.getInt(LOCATION_ID_COLUMN);
		record.survey_id = result.getInt(SURVEY_ID_COLUMN);
		record.taxon_id = result.getInt(TAXON_ID_COLUMN);
		int status = result.getInt(STATUS_COLUMN);
		record.setValid(status == 0);
		record.scientificName = result.getString(SCIENTIFIC_NAME_COLUMN);
		
		Cursor values = db.query(false, ATTRIBUTE_VALUE_TABLE, new String[]{"_id", "attribute_id", "value", "type"}, "record_id = ?", new String[] {Integer.toString(record.getId())}, null, null, null, null);
		try {
			values.moveToFirst();
			List<AttributeValue> attrValues = record.getAttributeValues();
			while (!values.isAfterLast()) {
				
				AttributeValue value = new AttributeValue(values.getInt(1), values.getString(2), values.getInt(3) == TYPE_URI);
				attrValues.add(value);
				Log.d("RecordDAO", "Loaded: "+value.attribute_id+", value: "+value.nullSafeValue());
				values.moveToNext();
			}
		}
		finally {
			values.close();
		}
		return record;
	}

}
