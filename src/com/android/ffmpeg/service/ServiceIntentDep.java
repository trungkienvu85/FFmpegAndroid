package com.android.ffmpeg.service;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.util.Log;

public class ServiceIntentDep extends IntentService {

	private static final String TAG = "ServiceExample";

	@Override
	protected void onHandleIntent(Intent arg0) {
		Log.i(TAG, "Intent Service started");

	}

	public ServiceIntentDep() {
		super("MyIntentService");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return Service.START_STICKY;
	}
}