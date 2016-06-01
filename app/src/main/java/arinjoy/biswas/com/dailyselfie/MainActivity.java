package arinjoy.biswas.com.dailyselfie;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends Activity {

    private static final String TAG = "Lab-Daily-Selfie-main";

    // defining the desired intervals for alarm notification
    private static final long ONE_MIN = 1 * 60 * 1000;
    private static final long TWO_MIN = 2 * 60 * 1000;
    private static final long ONE_DAY =  24 * 60 * 60 * 1000;

    static final int REQUEST_TAKE_PHOTO = 1;

    private SelfiesViewAdapter mAdapter;

    private ListView listView;

    // selfies datastore helper class
    private SelfiesDataStore mSelfiesDataStore;

    // to get hold fo the recently processed selfie
    private File currentFile;
    private String currentSelfieName;

    // to maintain the currently selected selfie
    private SelfieRecord selectedSelfie;

    // pending intent for the notification
    private PendingIntent mPendingIntent;
    // alarm manager for notification receiver
    private AlarmManager mAlarmManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.selfiesList_id);

        mSelfiesDataStore = new SelfiesDataStore(getApplicationContext());
        mSelfiesDataStore.open();

        mAdapter = new SelfiesViewAdapter(getApplicationContext(), mSelfiesDataStore.getAllSelfies(), 0);
        mAdapter.reloadData();
        mAdapter.notifyDataSetChanged();
        listView.setAdapter(mAdapter);

        // setup the alarm with default interval (2 min)
        setupAlarm();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                selectedSelfie = (SelfieRecord) mAdapter.getItem(position);

                Intent detailsIntent = new Intent(getApplicationContext(), SelfieDetailsActivity.class);

                // passing selfie information to the detail intent
                detailsIntent.putExtra("selfieName", selectedSelfie.getName());
                detailsIntent.putExtra("selfieFilePath", selectedSelfie.getFilePath());
                detailsIntent.putExtra("selfieRowId", selectedSelfie.getId());

                startActivity(detailsIntent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
        mAdapter.reloadData();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       // inflate the menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // dynamically checks if there are selfies and displays the delete all selfies button
        if (mAdapter.getCursor().getCount()>0){
            menu.findItem(R.id.action_delete_all_selfies).setVisible(true);
        }
        else {
            menu.findItem(R.id.action_delete_all_selfies).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_camera_button) {
            dispatchTakePictureIntent();
            return true;
        }

        // delete all the selfies
        if(id == R.id.action_delete_all_selfies) {
            deleteAllSelfies();
            return true;
        }
        // setup user peference
        if(id==R.id.action_alarm_1_min) {
            resetAlarm(ONE_MIN);
            return true;
        }
        if(id==R.id.action_alarm_2_min) {
            resetAlarm(TWO_MIN);
            return true;
        }
        if(id==R.id.action_alarm_1_day) {
            resetAlarm(ONE_DAY);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    /**
     * Setup a Repeating alarm with default interval of 2 min
     */
    private void setupAlarm() {

        // Retrieve a PendingIntent that will perform a broadcast
        Intent alarmIntent = new Intent(this, SelfiesAlarmReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Setup a repeating alarm based on the user preference
        mAlarmManager = (AlarmManager) getSystemService(getApplicationContext().ALARM_SERVICE);

        // default interval is 2 min
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + TWO_MIN, TWO_MIN, mPendingIntent);
    }

    /**
     * Reset any existing alarm and start a new alarm with desired frequency form the menu
     * @param desiredInterval User preference from the menu
     */
    public void resetAlarm(long desiredInterval) {
        mAlarmManager.cancel(mPendingIntent);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + desiredInterval, desiredInterval, mPendingIntent);
    }

    /**
     * To dispatch and intent for taking picture
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            currentFile = null;
            currentSelfieName = null;
            try {

                SelfiesFileHelper.SelfieFileInfo fileInfo = SelfiesFileHelper.createImageFile();

                currentFile = fileInfo.photoFile;
                currentSelfieName = fileInfo.selfieName;

            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.i(TAG, "Error occured!");
            }
            // Continue only if the File was successfully created
            if (currentFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentFile));

                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            if(currentFile !=null && currentSelfieName !=null) {

                SelfieRecord selfie = new SelfieRecord();

                // get the file path and selfie name that was just processed before
                selfie.setFilePath(currentFile.getAbsolutePath());
                selfie.setName(currentSelfieName);

                // add the selfie to the database
                addSelfie(selfie);
            }

        }
    }

    /**
     * Add the selfie to the list view and database
     * @param newSelfie the newly created selfie to be added
     */
    public void addSelfie(SelfieRecord newSelfie) {
        if(mAdapter != null) {
            mAdapter.addSelfie(newSelfie);
        }
    }

    /**
     * Delete all selfies that were taken by this application
     */
    public void deleteAllSelfies() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_delete_all_selfies));
        builder.setPositiveButton(getString(R.string.dialog_yes), dialogDeleteAllSelfiesClickListener);
        builder.setNegativeButton(getString(R.string.dialog_no), dialogDeleteAllSelfiesClickListener).show();
    }

    /**
     * On click listerner for the dialog that appears upon the delete action on the selfies
     */
    DialogInterface.OnClickListener dialogDeleteAllSelfiesClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    if(mAdapter != null) {
                        mAdapter.deleteAllSelfies();
                        Toast.makeText(getApplicationContext(), "All selfies deleted", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };


}
