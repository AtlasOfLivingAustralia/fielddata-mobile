package au.org.ala.fielddata.mobile.dao;

import android.content.Context;

public class DraftRecordDAO extends RecordDAO {
	
	public static final String DRAFT_RECORD_TABLE = "DRAFT_RECORD";
	public static final String DRAFT_ATTRIBUTE_VALUE_TABLE = "DRAFT_ATTRIBUTE_VALUE";
	
	public static final String DRAFT_RECORD_TABLE_DDL = "CREATE TABLE "+DRAFT_RECORD_TABLE+
	" (_id INTEGER PRIMARY KEY, "+ RECORD_COLUMNS +")";
	
	public static final String DRAFT_ATTRIBUTE_TABLE_DDL = 
		"CREATE TABLE "+DRAFT_ATTRIBUTE_VALUE_TABLE+ ATTRIBUTE_VALUE_COLUMNS;
	
	public DraftRecordDAO(Context ctx) {
		super(ctx);
		
		recordTable = DRAFT_RECORD_TABLE;
		attributeValueTable = DRAFT_ATTRIBUTE_VALUE_TABLE;
	}
}
