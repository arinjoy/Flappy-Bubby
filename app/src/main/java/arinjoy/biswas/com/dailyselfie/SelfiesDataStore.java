package arinjoy.biswas.com.dailyselfie;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SelfiesDataStore
{
	public static final String KEY_ROWID = "_id";
	public static final String KEY_NAME = "name";
	public static final String KEY_PICTURE_PATH = "picturePath";

	private static final String DATABASE_NAME = "Daily-Selfies-DB";
	private static final String DATABASE_TABLE = "selfies";
	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_CREATE = "create table selfies (_id integer primary key autoincrement, "
			+ "name text not null, picturePath text not null);";

	private final Context mContext;

	private DatabaseHelper mDBHelper;
	private SQLiteDatabase mDB;

	public SelfiesDataStore(Context ctx) {
		mContext = ctx;
		mDBHelper = new DatabaseHelper(mContext);
	}

	public void open() throws SQLException {
		mDB = mDBHelper.getWritableDatabase();
	}

	public void close() {
		mDBHelper.close();
	}

	/**
	 * Insert a selfie into the database
	 * @param name
	 * @param picturePath
	 * @return
	 */
	public long insertSelfie(String name, String picturePath) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_NAME, name);
		initialValues.put(KEY_PICTURE_PATH, picturePath);
		return mDB.insert(DATABASE_TABLE, null, initialValues);
	}

	/**
	 * delee a selfie from teh database
	 * @param rowId
	 * @return
	 */
	public boolean deleteSelfie(long rowId) {
		return mDB.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	// not used
	public boolean updateSelfie(long rowId, String name, String picturePath) {
		ContentValues args = new ContentValues();
		args.put(KEY_NAME, name);
		args.put(KEY_PICTURE_PATH, picturePath);
		return mDB.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Delete all the selfies
	 * @return
	 */
	public boolean deleteAllSelfies() {
		return mDB.delete(DATABASE_TABLE, null, null) > 0;
	}

	/**
	 * Get all the selfies
	 * @return
	 */
	public Cursor getAllSelfies() {
		String orderBy =  KEY_ROWID + " DESC";
		return mDB.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_NAME, KEY_PICTURE_PATH }, null, null, null, null, orderBy);
	}

	public Cursor getSelfie(long rowId) throws SQLException {
		Cursor mCursor = mDB.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_NAME, KEY_PICTURE_PATH }, KEY_ROWID + "=" + rowId, null, null, null, null, null);
		
		if(mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	// not used
	public Cursor getSelfieFilePath(long rowId) throws SQLException {
		Cursor mCursor = mDB.query(true, DATABASE_TABLE, new String[] {KEY_PICTURE_PATH }, KEY_ROWID + "=" + rowId, null, null, null, null, null);

		if(mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	/**
	 * DatabaseHelper Class
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL(DATABASE_CREATE);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS selfies");
			onCreate(db);
		}
	}
}