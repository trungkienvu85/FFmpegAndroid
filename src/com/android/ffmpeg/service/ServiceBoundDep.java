package com.android.ffmpeg.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.android.ffmpeg.R;

public class ServiceBoundDep extends Service {

	private final IBinder _myBinder = new MyLocalBinder();

	@Override
	public IBinder onBind(Intent arg0) {
		Toast.makeText(getApplicationContext(), "onBoundService ",
				Toast.LENGTH_SHORT).show();
		return _myBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		final int currentId = startId;

		new Thread(new Runnable() {
			public void run() {

				for (int i = 0; i < 3; i++) {
					long endTime = System.currentTimeMillis() + 10 * 1000;

					while (System.currentTimeMillis() < endTime) {
						synchronized (this) {
							try {
								wait(endTime - System.currentTimeMillis());
							} catch (Exception e) {
							}
						}
					}
					Log.i("MyService", "Service running " + currentId);
					sendNotification("Service running " + currentId);
				}
				stopSelf();
			}
		}).start();

		return Service.START_STICKY;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return false;
	}

	public String getCurrentTime() {
		SimpleDateFormat dateformat = new SimpleDateFormat(
				"HH:mm:ss MM/dd/yyyy", Locale.US);
		return (dateformat.format(new Date()));
	}

	public class MyLocalBinder extends Binder {
		public ServiceBoundDep getService() {
			return ServiceBoundDep.this;
		}
	}

	public static final int NOTIFICATION_ID = 1;

	private NotificationManager _notificationManager;

	private void sendNotification(String msg) {
		_notificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				new Intent(this, ServiceActDep.class), 0);
		// builder.setSound(Uri.parse("uri://sadfasdfasdf.mp3"));

		Notification.Builder builder = new Notification.Builder(this)

		.setVibrate(new long[] { 100, 100, 100, 100, 100 })
				.setTicker("my text").setSmallIcon(R.drawable.notification_icon)
				.setContentIntent(contentIntent).setAutoCancel(true)
				.setContentTitle(msg)
				.setStyle(new Notification.BigTextStyle().bigText(msg))
				.setContentText(getCurrentTime());

		_notificationManager.notify(NOTIFICATION_ID, builder.build());
	}
}
