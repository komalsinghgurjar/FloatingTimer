package gurjar.singh.komal.exam.prep.timer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Define a SQLiteOpenHelper subclass to manage database creation and version management
public class MyDatabaseHelper extends SQLiteOpenHelper {
	// Database and table info
	private static final String DATABASE_NAME = "config.db";
	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_CONFIG = "config";
	private static final String COLUMN_KEY = "key";
	private static final String COLUMN_VALUE = "value";
	
	// Constructor
	public MyDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	// Create table
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLE_CONFIG + " (" +
		COLUMN_KEY + " TEXT PRIMARY KEY, " +
		COLUMN_VALUE + " INTEGER)");
	}
	
	// Upgrade database (if needed)
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Upgrade logic
	}
	
	
	
	public void storeInt(String key, int value) {
    SQLiteDatabase db = getWritableDatabase();
    ContentValues values = new ContentValues();
    values.put(COLUMN_KEY, key);
    values.put(COLUMN_VALUE, value);
    db.insertWithOnConflict(TABLE_CONFIG, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    db.close();
}


public int getInt(String key, int defaultValue) {
    SQLiteDatabase db = getReadableDatabase();
    Cursor cursor = db.query(TABLE_CONFIG, new String[]{COLUMN_VALUE}, COLUMN_KEY + "=?",
            new String[]{key}, null, null, null);
    int value = defaultValue;
    if (cursor != null && cursor.moveToFirst()) {
        value = cursor.getInt(0);
        cursor.close();
    }
    db.close();
    return value;
}

	// Store boolean value in database
	public void storeBoolean(String key, boolean value) {
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(COLUMN_KEY, key);
		values.put(COLUMN_VALUE, value ? 1 : 0);
		db.insertWithOnConflict(TABLE_CONFIG, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		db.close();
	}
	
	// Retrieve boolean value from database
	public boolean getBoolean(String key, boolean defaultValue) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(TABLE_CONFIG, new String[]{COLUMN_VALUE}, COLUMN_KEY + "=?",
		new String[]{key}, null, null, null);
		boolean value = defaultValue;
		if (cursor != null && cursor.moveToFirst()) {
			value = cursor.getInt(0) == 1;
			cursor.close();
		}
		db.close();
		return value;
	}
}