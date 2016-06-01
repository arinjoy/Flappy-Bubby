package arinjoy.biswas.com.dailyselfie;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

public class SelfiesFileHelper
{
	// application directory to store the selifes
	private static final String APP_DIR = "DailySelfie";

	// utility class to store selfie details
	public static class SelfieFileInfo {
		String selfieName;
		File photoFile;
		public SelfieFileInfo() {
		}
	}

	/**
	 * Create/Open a file to save a selfie based on the current time stamp
	 * @return Information about the selfie
	 * @throws IOException
	 */
	public static SelfieFileInfo createImageFile() throws IOException {
		// Create an image file name
		Date currentTime = new Date();
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(currentTime);
		String imageFileName = "JPEG_" + timeStamp + ".jpg";
		File folder = new File(getSelfiesFolderPath());

		if(!folder.exists()){
			folder.mkdirs();
		}

		File myFile = new File(folder.getAbsolutePath(), imageFileName);
		myFile.createNewFile();

		SelfieFileInfo info = new SelfieFileInfo();

		// set the selfie name as the current time stamp
		info.selfieName = new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(currentTime);
		info.photoFile = myFile;

		return info;
	}

	/**
	 * Get the file path for the selfies container folder
	 * @return
	 */
	public static String getSelfiesFolderPath() {
		File folder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + APP_DIR +"/");
		return folder.getAbsolutePath();
	}


	/**
	 * Delete selfies recursively at file/folder location
	 * @param path
	 * @return
	 */
	public static boolean deleteSelfieFileRecursive(String path) {
		File folder = new File(path);
		try{
			if(folder.isDirectory()) {
				for(File child : folder.listFiles()) {
					deleteSelfieFileRecursive(child.getAbsolutePath());
				}
			}
			else {
				folder.delete();
			}
			return true;
		}
		catch(Exception e){
			return false;
		}

	}

	/**
	 * Get the scaled bitmap from an image file
	 * @param picturePath the location of the file
	 * @param width desired width of the scaled bitmap. 0 for no scaling
	 * @param height desired height of the scaled bitmap. 0 for no scaling
	 * @return
	 */
	public static Bitmap getScaledBitmap(String picturePath, int width, int height)
	{
		// Get the dimensions of the bitmap
	    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
	    bmOptions.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(picturePath, bmOptions);
	    int photoW = bmOptions.outWidth;
	    int photoH = bmOptions.outHeight;
	
	    // Determine how much to scale down the image
	    int scaleFactor;
	    
	    if(width > 0 && height > 0) {
	    	scaleFactor = Math.min(photoW/width, photoH/height);
	    }
	    else {
	    	scaleFactor = 1;
	    }
	
	    // Decode the image file into a Bitmap
	    bmOptions.inJustDecodeBounds = false;
	    bmOptions.inSampleSize = scaleFactor;
	    bmOptions.inPurgeable = true;
	
	    return BitmapFactory.decodeFile(picturePath, bmOptions);
	}


}
