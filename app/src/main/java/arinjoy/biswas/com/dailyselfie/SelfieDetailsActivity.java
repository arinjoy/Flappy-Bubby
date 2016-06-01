package arinjoy.biswas.com.dailyselfie;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

public class SelfieDetailsActivity extends Activity
{
	private static final String TAG = "Lab-Daily-Selfie-detail";

	private SelfiesDataStore mSelfiesDataStore;
	private int mSelfieId;
	private String mSelfieFilePath;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		setContentView(R.layout.selfie_details_view);

		ImageView imageView = (ImageView) findViewById(R.id.selfie_details_image_view);

		mSelfieFilePath = getIntent().getStringExtra("selfieFilePath");
		imageView.setImageBitmap(SelfiesFileHelper.getScaledBitmap(mSelfieFilePath, 0, 0));

		// set the title bar of the selfie
		getActionBar().setTitle(getIntent().getStringExtra("selfieName"));

		// receive the selfie id from sending intent
		mSelfieId = getIntent().getIntExtra("selfieRowId", -1);

		//open up and connect the database
		mSelfiesDataStore = new SelfiesDataStore(getApplicationContext());
		mSelfiesDataStore.open();

	}


	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.selfie_details_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		//if deleting this selfie
		if (id == R.id.delete_selfie) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.dialog_delete_selfie));
			builder.setPositiveButton(getString(R.string.dialog_yes), dialogDeleteSelfieClickListener);
			builder.setNegativeButton(getString(R.string.dialog_no), dialogDeleteSelfieClickListener).show();

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	DialogInterface.OnClickListener dialogDeleteSelfieClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which){
				case DialogInterface.BUTTON_POSITIVE:
					//Yes button clicked
					if(SelfiesFileHelper.deleteSelfieFileRecursive(mSelfieFilePath)) {
						mSelfiesDataStore.deleteSelfie(mSelfieId);

						Toast.makeText(getApplicationContext(), "Selfie deleted", Toast.LENGTH_SHORT).show();

						// selfie deleted. go back to main activity
						finish();
					}
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					// no action performed
					break;
			}
		}
	};
}