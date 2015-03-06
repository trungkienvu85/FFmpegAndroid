package com.android.ffmpeg.service;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

import com.android.ffmpeg.Consts;
import com.android.ffmpeg.MainActivity;
import com.android.ffmpeg.R;
import com.android.ffmpeg.SavedStates;
import com.android.ffmpeg.imageslides.utils.UtilsFfmpeg;

public class ServiceRemote extends Service {

	final Messenger myMessenger = new Messenger(new IncomingHandler());

	public static final int NOTIFICATION_ID = 1;

	private NotificationManager _notificationManager;

	private SavedStates _oSavedState;

	private UtilsFfmpeg _oFfmpegUtils;

	private Bundle _servicedataBundle;

	private WakeLock _cpuWakeLock;

	private WakeLock _cpuWakeLockPartial;

	private String _sMessage = "";

	public static String PARAM_OUT_MSG = "MESG_SENT";

	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			_oFfmpegUtils = new UtilsFfmpeg(getApplicationContext());

			_servicedataBundle = msg.getData();
			_servicedataBundle.setClassLoader(SavedStates.class
					.getClassLoader());
			_oSavedState = (SavedStates) _servicedataBundle
					.getParcelable(Consts.AUDIO_VIDEO_PROPERTIES);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return myMessenger.getBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return false;
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, int startId) {
		sendNotification("started movie making ");

		new Thread(new Runnable() {
			public void run() {

				synchronized (this) {

					PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
					_cpuWakeLockPartial = pm.newWakeLock(
							PowerManager.PARTIAL_WAKE_LOCK,
							"TAG_PARTIAL_WAKE_LOCK");
					_cpuWakeLockPartial.acquire();

					Log.i("TAG", "started service");

					if (_oFfmpegUtils.startImagetoVideo(
							_oSavedState.getVideoProperties(),
							_oSavedState.getAudioProperties(),
							_servicedataBundle.getBoolean(Consts.TRIM_PROPERTY),
							_servicedataBundle.getInt(Consts.VIDEO_DURATION),
							_servicedataBundle.getInt(Consts.FRAME_DURATION))) {
						_sMessage = "sucess";
					} else
						_sMessage = "failed";

					_cpuWakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
							| PowerManager.ACQUIRE_CAUSES_WAKEUP
							| PowerManager.ON_AFTER_RELEASE,
							"TAG_CPU_WAKE_LOCK");
					_cpuWakeLock.acquire();

					Intent serviceBroadcast = new Intent();
					serviceBroadcast
							.setAction("com.android.ffmpeg.service.CUSTOM_INTENT");
					serviceBroadcast.putExtra(PARAM_OUT_MSG, "Video making "
							+ _sMessage);
					sendBroadcast(serviceBroadcast);
					stopSelf();
					trimCache(getApplicationContext());
					_cpuWakeLockPartial.release();
					_cpuWakeLock.release();

				}
			}
		}).start();

		return Service.START_STICKY;
	}

	private void sendMessage(String msg) {

		if (!msg.equals(null) && !msg.equals("")) {
			Message msgObj = handler.obtainMessage();
			Bundle b = new Bundle();
			b.putString("message", msg);
			msgObj.setData(b);
			handler.sendMessage(msgObj);
		}
	}

	Message msg = new Message();

	Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {

			String sResponse = msg.getData().getString("message");

			if ((null != sResponse))
				Toast.makeText(getApplicationContext(), sResponse,
						Toast.LENGTH_LONG).show();
			return false;
		}
	});

	private void sendNotification(String msg) {
		_notificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, MainActivity.class), 0);

		Notification.Builder builder = new Notification.Builder(this)

		.setVibrate(new long[] { 100, 100, 100, 100, 100 }).setTicker(msg)
				.setSmallIcon(R.drawable.notification_icon)
				.setContentIntent(contentIntent).setAutoCancel(true)
				.setContentTitle("Image to Video Converter")
				.setStyle(new Notification.BigTextStyle().bigText(msg))
				.setContentText(getCurrentTime());
		_notificationManager.notify(NOTIFICATION_ID, builder.build());
	}

	public String getCurrentTime() {
		SimpleDateFormat dateformat = new SimpleDateFormat(
				"HH:mm:ss MM/dd/yyyy", Locale.US);
		return (dateformat.format(new Date()));
	}

	public void trimCache(Context context) {
		sendMessage("cache cleared ......");
		try {
			File dir = context.getCacheDir();
			if (dir != null && dir.isDirectory())
				deleteDir(dir);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static boolean deleteDir(File dir) {
		if (dir != null && dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		return dir.delete();
	}

	private void testDelete() {
		for (int i = 2; i < 10; i++)
			try {
				Thread.sleep(2500);
				Log.i("ServiceRemote", "FfmpegUtils::\n" + "SavedState:: Video"
						+ _oSavedState.getVideoProperties().get(0).getUrl()
						+ "|| Audio"
						+ _oSavedState.getAudioProperties().get(0).getname()
						+ "|| count::" + i);

			} catch (InterruptedException e) {

			}

	}
}
