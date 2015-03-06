package com.android.ffmpeg.googleplus.ytutils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.ffmpeg.R;
import com.android.ffmpeg.googleplus.dashboard.DashBoardGoogleActivity;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.media.MediaHttpUploader;
import com.google.api.client.googleapis.media.MediaHttpUploaderProgressListener;
import com.google.api.client.http.InputStreamContent;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatus;

public class UtilsYTUploadAsync extends AsyncTask<String, Integer, Video> {

	private YouTube _youtube;

	private Context _context;

	private NotificationManager _notifyManager;

	private NotificationCompat.Builder _builder;

	public UtilsYTUploadAsync(YouTube youtube, Context context) {
		_youtube = youtube;
		_context = context;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		_notifyManager = (NotificationManager) _context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		_builder = new NotificationCompat.Builder(_context)
				.setContentTitle("youtube_upload")
				.setContentText("youtube_upload_started")
				.setSmallIcon(R.drawable.ic_launcher);

		_notifyManager.notify(UPLOAD_NOTIFICATION_ID, _builder.build());
	}

	@Override
	protected Video doInBackground(final String... videoArg) {
		try {
			Video videoObjectDefiningMetadata = new Video();

			VideoStatus status = new VideoStatus();
			status.setPrivacyStatus("public");
			videoObjectDefiningMetadata.setStatus(status);

			VideoSnippet snippet = new VideoSnippet();

			Calendar cal = Calendar.getInstance();
			snippet.setTitle("Android/Java Test Upload on " + cal.getTime());
			snippet.setDescription("Video uploaded via YouTube Data API V3 using the Java library "
					+ "on " + cal.getTime());

			List<String> tags = new ArrayList<String>();
			tags.add("test");
			tags.add("example");
			tags.add("android");
			tags.add("YouTube Data API V3");
			tags.add("erase me");
			snippet.setTags(tags);

			videoObjectDefiningMetadata.setSnippet(snippet);

			InputStreamContent mediaContent = new InputStreamContent(
					VIDEO_FILE_FORMAT, new BufferedInputStream(
							new FileInputStream(videoArg[0])));

			YouTube.Videos.Insert videoInsert = _youtube.videos().insert(
					"snippet,statistics,status", videoObjectDefiningMetadata,
					mediaContent);

			MediaHttpUploader uploader = videoInsert.getMediaHttpUploader();
			uploader.setDirectUploadEnabled(false);

			MediaHttpUploaderProgressListener progressListener = new MediaHttpUploaderProgressListener() {
				public void progressChanged(MediaHttpUploader uploader)
						throws IOException {
					switch (uploader.getUploadState()) {
					case INITIATION_STARTED:
						_builder.setContentText("INITIATION_STARTED")
								.setTicker("INITIATION_STARTED")
								.setProgress(100,
										(int) uploader.getNumBytesUploaded(),
										false);
						_notifyManager.notify(UPLOAD_NOTIFICATION_ID,
								_builder.build());
						break;
					case INITIATION_COMPLETE:
						_builder.setContentText("INITIATION_COMPLETE")
								.setTicker("INITIATION_COMPLETE")
								.setProgress(100,
										(int) uploader.getNumBytesUploaded(),
										false);
						_notifyManager.notify(UPLOAD_NOTIFICATION_ID,
								_builder.build());
						break;
					case MEDIA_IN_PROGRESS:
						publishProgress((int) (uploader.getProgress() * 100),
								(int) uploader.getNumBytesUploaded());
						_builder.setContentTitle(
								videoArg[1] + " Uploading.. "
										+ (int) (uploader.getProgress() * 100)
										+ "%")
								.setContentText("MEDIA_IN_PROGRESS")
								.setProgress(100,
										(int) uploader.getNumBytesUploaded(),
										false);
						_notifyManager.notify(UPLOAD_NOTIFICATION_ID,
								_builder.build());
						break;
					case MEDIA_COMPLETE:
						_builder.setContentTitle(
								"Completed upload :: " + videoArg[1])
								.setTicker("MEDIA_COMPLETE")
								.setContentText("MEDIA_COMPLETE")
								.setProgress(0, 0, false);
						_notifyManager.notify(UPLOAD_NOTIFICATION_ID,
								_builder.build());

						break;
					case NOT_STARTED:
						_builder.setContentText("NOT_STARTED").setProgress(100,
								(int) uploader.getNumBytesUploaded(), false);
						_notifyManager.notify(UPLOAD_NOTIFICATION_ID,
								_builder.build());

						break;
					}
				}
			};
			uploader.setProgressListener(progressListener);

			Video returnedVideo = videoInsert.execute();

			System.out
					.println("\n================== Returned Video ==================\n");
			System.out.println("  - Id: " + returnedVideo.getId());
			System.out.println("  - Title: "
					+ returnedVideo.getSnippet().getTitle());
			System.out.println("  - Tags: "
					+ returnedVideo.getSnippet().getTags());
			System.out.println("  - Privacy Status: "
					+ returnedVideo.getStatus().getPrivacyStatus());
			System.out.println("  - Video Count: "
					+ returnedVideo.getStatistics().getViewCount());

		} catch (GoogleJsonResponseException googleJsonResponseException) {
			googleJsonResponseException.printStackTrace();
		} catch (UserRecoverableAuthIOException userRecoverableException) {
			requestAuth(_context, userRecoverableException);
		} catch (IOException iOException) {
			iOException.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		_builder.setContentTitle(" Uploading.. " + values[0])
				.setContentText("MEDIA_IN_PROGRESS")
				.setProgress(100, values[1], false);
		_notifyManager.notify(UPLOAD_NOTIFICATION_ID, _builder.build());
	}

	@Override
	protected final void onPostExecute(Video returnedVideo) {
		super.onPostExecute(returnedVideo);
	}

	public static void requestAuth(Context context,
			UserRecoverableAuthIOException userRecoverableException) {
		LocalBroadcastManager manager = LocalBroadcastManager
				.getInstance(context);
		Intent authIntent = userRecoverableException.getIntent();
		Intent runReqAuthIntent = new Intent(
				DashBoardGoogleActivity.REQUEST_AUTHORIZATION_INTENT);
		runReqAuthIntent.putExtra(
				DashBoardGoogleActivity.REQUEST_AUTHORIZATION_INTENT_PARAM,
				authIntent);
		manager.sendBroadcast(runReqAuthIntent);
		Log.d(DashBoardGoogleActivity.TAG_YTB, String.format(
				"Sent broadcast %s",
				DashBoardGoogleActivity.REQUEST_AUTHORIZATION_INTENT));
	}

	private static final String VIDEO_FILE_FORMAT = "video/*";

	private static int UPLOAD_NOTIFICATION_ID = 1001;

}
