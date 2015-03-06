package com.android.ffmpeg.service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import com.android.ffmpeg.MainActivity;
import com.android.ffmpeg.R;

public class ServiceBroadcastReceiver extends BroadcastReceiver {

	public static final int NOTIFICATION_ID = 1;

	private NotificationManager _notificationManager;

	@Override
	public void onReceive(Context context, Intent intent) {
		String sMsg = intent.getExtras().getString(ServiceRemote.PARAM_OUT_MSG);

		sendNotification(sMsg, context);
	}

	private void sendNotification(String msg, Context context) {
		_notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				new Intent(context, MainActivity.class), 0);

		Uri uri = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		Notification.Builder builder = new Notification.Builder(context)

		.setVibrate(new long[] { 100, 100, 100, 100, 100 }).setTicker(msg)
				.setSmallIcon(R.drawable.notification_icon).setSound(uri)
				.setContentIntent(contentIntent).setAutoCancel(true)
				.setContentTitle("Image to Video Converter")
				.setStyle(new Notification.BigTextStyle().bigText(msg))
				.setContentText(msg + getCurrentTime());
		_notificationManager.notify(NOTIFICATION_ID, builder.build());
	}

	public String getCurrentTime() {
		SimpleDateFormat dateformat = new SimpleDateFormat(
				"HH:mm:ss MM/dd/yyyy", Locale.US);
		return (dateformat.format(new Date()));
	}

}
