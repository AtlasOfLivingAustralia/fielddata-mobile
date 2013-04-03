package au.org.ala.fielddata.mobile.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import au.org.ala.fielddata.mobile.model.Survey;

public class SurveyDAO extends GenericDAO<Survey> {

	public static final String SURVEY_TABLE = "SURVEY";
	public static final String SURVEY_SPECIES_TABLE = "SURVEY_SPECIES";
	
	// Shared column indexes (select *)
	public static final int ID_COLUMN_IDX = 0;
	public static final int SERVER_ID_COLUMN_IDX = 1;
	public static final int CREATED_COLUMN_IDX = 2;
	public static final int UPDATED_COLUMN_IDX = 3;
	
	// Column indexes for the SURVEY TABLE (select *)
	public static final int LSID_COLUMN_IDX = 4;
	public static final int SCIENTIFIC_NAME_COLUMN_IDX = 5;
	public static final int COMMON_NAME_COLUMN_IDX = 6;
	public static final int IMAGE_URL_COLUMN_IDX = 7;
	public static final int SPECIES_GROUP_COLUMN_IDX = 8;
		
	
	public SurveyDAO(Context ctx) {
		super(ctx);
	}
	
	
	public List<Survey> surveysForSpecies(Integer speciesId) {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor result = null;
		List<Survey> surveyList = new ArrayList<Survey>();
		try {
			
			String query = "SELECT * from "+SURVEY_TABLE+" s inner join "+SURVEY_SPECIES_TABLE+
					" ss on s.server_id = ss.survey_id where ss.species_id = ?";
			result = db.rawQuery(query, new String[] {Integer.toString(speciesId)});

			if (result.getCount() == 0) {
				surveyList = loadAll(Survey.class);
				
			} else {
				result.moveToFirst();
				while (!result.isAfterLast()) {
					surveyList.add( map(db, result, Survey.class) );
					result.moveToNext();
				}
			}
		}
		finally {
			if (result != null) {
				result.close();
			}
		}
		return surveyList;
	}
	
}
