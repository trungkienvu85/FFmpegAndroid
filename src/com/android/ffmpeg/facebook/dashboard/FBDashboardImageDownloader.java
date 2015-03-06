package com.android.ffmpeg.facebook.dashboard;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class FBDashboardImageDownloader extends AsyncTask<Void, Integer, Void> {

	private String url;
	private Context c;
	private int progress;
	private Bitmap bmp;
	private ImageLoaderListener listener;

	public FBDashboardImageDownloader(String url, Context c,
			ImageLoaderListener listener) {
		this.url = url;
		this.c = c;
		this.listener = listener;
	}

	/*--- we need this interface for keeping the reference to our Bitmap from the MainActivity. 
	 *  Otherwise, bmp would be null in our MainActivity*/
	public interface ImageLoaderListener {

		void onImageDownloaded(Bitmap bmp);

	}

	@Override
	protected void onPreExecute() {

		Toast.makeText(c, "starting download", Toast.LENGTH_SHORT).show();

		super.onPreExecute();
	}

	@Override
	protected Void doInBackground(Void... arg0) {

		bmp = getBitmapFromURL(url);

		while (progress < 100) {

			progress += 1;

			publishProgress(progress);

			/*--- an image download usually happens very fast so you would not notice 
			 * how the ProgressBar jumps from 0 to 100 percent. You can use the method below 
			 * to visually "slow down" the download and see the progress bein updated ---*/

			SystemClock.sleep(200);

		}

		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {

		super.onProgressUpdate(values);
	}

	@Override
	protected void onPostExecute(Void result) {

		if (listener != null) {
			listener.onImageDownloaded(bmp);
		}
		Toast.makeText(c, "download complete", Toast.LENGTH_SHORT).show();

		super.onPostExecute(result);
	}

	public static Bitmap getBitmapFromURL(String link) {
		/*--- this method downloads an Image from the given URL, 
		 *  then decodes and returns a Bitmap object
		 ---*/
		try {
			URL url = new URL(link);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			Bitmap myBitmap = BitmapFactory.decodeStream(input);

			return myBitmap;

		} catch (IOException e) {
			e.printStackTrace();
			Log.e("getBmpFromUrl error: ", e.getMessage().toString());
			return null;
		}
	}


}
