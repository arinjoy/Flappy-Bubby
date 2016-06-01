package arinjoy.biswas.com.dailyselfie;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Selfies adapter to maintain the list view. It is tied with database for the sefies
 */
public class SelfiesViewAdapter extends CursorAdapter {

	private static LayoutInflater mLayoutInflater = null;

	private Context mContext;

	private ArrayList<SelfieRecord> mSelfieRecords = new ArrayList<SelfieRecord>();

	private SelfiesDataStore mSelfiesDataStore;


	public SelfiesViewAdapter(Context context, Cursor cursor, int flags) {

		super(context, cursor, flags);

		mContext = context;
		mLayoutInflater = LayoutInflater.from(mContext);

		//open the database connection
		mSelfiesDataStore = new SelfiesDataStore(mContext);
		mSelfiesDataStore.open();
	}

	static class ViewHolder {
		ImageView thumbNail;
		TextView name;
	}


	@Override
	public Cursor swapCursor(Cursor newCursor) {

		// swap the
		mSelfieRecords.clear();

		if(newCursor != null && newCursor.moveToFirst()) {
			do {
				mSelfieRecords.add(getSelfieRecordFromCursor(newCursor));
			} while (newCursor.moveToNext());
		}

		return super.swapCursor(newCursor);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		ViewHolder holder = (ViewHolder) view.getTag();
		holder.name.setText(cursor.getString(cursor.getColumnIndex(SelfiesDataStore.KEY_NAME)));

		// go much to scale down is defined into dimesion files
		int dimenPix = (int)mContext.getResources().getDimension(R.dimen.selfie_row_thumb_width_height);

		File file = new File(cursor.getString(cursor.getColumnIndex(SelfiesDataStore.KEY_PICTURE_PATH)));
		if(file.exists()) {
			holder.thumbNail.setImageBitmap(SelfiesFileHelper
					.getScaledBitmap(file.getAbsolutePath(), dimenPix, dimenPix));
		} else {
			holder.thumbNail.setImageDrawable(mContext.getResources().getDrawable(R.drawable.placeholder_stub));
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		ViewHolder holder = new ViewHolder();

		View newView = mLayoutInflater.inflate(R.layout.selfie_badge_view, parent, false);
		holder.name = (TextView)newView.findViewById(R.id.selfie_name);
		holder.thumbNail = (ImageView)newView.findViewById(R.id.selfie_thumb);

		newView.setTag(holder);

		return newView;
	}


	@Override
	public int getCount() {
		return mSelfieRecords.size();
	}

	@Override
	public Object getItem(int position) {
		return mSelfieRecords.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Refresh Selfies cursor
	 */
	public void reloadData() {
		// swap the current cursor with the fresh new cusrsor for all selfies
		this.swapCursor(mSelfiesDataStore.getAllSelfies());
	}

	/**
	 * Add slefie to the adapter
	 * @param newSelfie
	 */
	public void addSelfie(SelfieRecord newSelfie) {

		mSelfiesDataStore.insertSelfie(newSelfie.getName(), newSelfie.getFilePath());

		// refresh data
		reloadData();
	}


	public void deleteSelfie(SelfieRecord selfie) {
		mSelfiesDataStore.deleteSelfie(selfie.getId());
		reloadData();
	}

	public void deleteAllSelfies() {

		SelfiesFileHelper.deleteSelfieFileRecursive(SelfiesFileHelper.getSelfiesFolderPath());

		mSelfiesDataStore.deleteAllSelfies();

		// refresh data on the view
		reloadData();
	}


	/**
	 * Returns a new Selfie record for the data at the cursor's current position
	 * @param cursor
	 * @return
	 */
	private SelfieRecord getSelfieRecordFromCursor(Cursor cursor) {

		int id = cursor.getInt(cursor.getColumnIndex(SelfiesDataStore.KEY_ROWID));
		String name = cursor.getString(cursor.getColumnIndex(SelfiesDataStore.KEY_NAME));
		String picturePath = cursor.getString(cursor.getColumnIndex(SelfiesDataStore.KEY_PICTURE_PATH));

		SelfieRecord selfie = new SelfieRecord();
		selfie.setId(id);
		selfie.setFilePath(picturePath);
		selfie.setName(name);

		return selfie;
	}


}
