package au.org.ala.fielddata.mobile.dao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import au.org.ala.fielddata.mobile.R;
import au.org.ala.fielddata.mobile.model.Record;
import au.org.ala.fielddata.mobile.model.Species;
import au.org.ala.fielddata.mobile.model.Survey;

/**
 * Responsible for creating and configuring the database used by the
 * application.
 * As a first cut, all objects are stored in a table equal to their class 
 * name in a serialized form (json) along side a little bit of metadata.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "FieldData.db";
	private static final int SCHEMA_VERSION = 1;

	private static final String[] TABLES = { Survey.class.getSimpleName(),
			Record.class.getSimpleName(), Species.class.getSimpleName() };

	private Context ctx;

	private static DatabaseHelper instance;
	
	public synchronized static DatabaseHelper getInstance(Context ctx) {
		if (instance == null) {
			instance = new DatabaseHelper(ctx.getApplicationContext());
		}
		return instance;
	}
	
	private DatabaseHelper(Context ctx) {
		super(ctx, DATABASE_NAME, null, SCHEMA_VERSION);
		this.ctx = ctx;
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
			populateSpecies(db);

			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		throw new RuntimeException("Upgrade not supported.");
	}

	private void populateSpecies(SQLiteDatabase database) {
		GenericDAO<Species> dao = new GenericDAO<Species>(ctx);
		Species species = new Species("Nassella neesiana", "Chilean Needle Grass",
				R.drawable.nassella_neesiana_thumb);
		species.server_id = 11831;
		dao.save(species, database);
		
		species = new Species("Parthenium hysterophorus", "Parthenium Weed",
				R.drawable.parthenium_hysterophorus_thumb);
		species.server_id = 11830;
		dao.save(species, database);
		
	}
	
}
