package com.android.ffmpeg.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.Toast;

import com.android.ffmpeg.R;
import com.android.ffmpeg.service.ServiceBoundDep.MyLocalBinder;

public class ServiceActDep extends Activity {

	ServiceBoundDep myService;

	Messenger myServiceMesenger;

	boolean isBound = false;

	private ServiceConnection myConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder service) {

			MyLocalBinder binder = (MyLocalBinder) service;
			myService = binder.getService();
			isBound = true;
			Toast.makeText(getApplicationContext(), "onServiceConnected ",
					Toast.LENGTH_SHORT).show();
		}

		public void onServiceDisconnected(ComponentName arg0) {
			isBound = false;
		}

	};

	private ServiceConnection myConnectionRemote = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			myServiceMesenger = new Messenger(service);
			Toast.makeText(getApplicationContext(),
					"onServiceConnected Remote", Toast.LENGTH_LONG).show();
			isBound = true;
		}

		public void onServiceDisconnected(ComponentName className) {
			myService = null;
			isBound = false;
			Toast.makeText(getApplicationContext(), "onServiceDisconnected ",
					Toast.LENGTH_SHORT).show();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_service);

		Intent intent = new Intent(this, ServiceRemote.class);
		bindService(intent, myConnectionRemote, Context.BIND_AUTO_CREATE);
	}

	public void sendMessage(View view) {
		if (!isBound) {
			Toast.makeText(getApplicationContext(), "not bounded..... ",
					Toast.LENGTH_SHORT).show();
			return;
		}

		Message msg = Message.obtain();

		Bundle bundle = new Bundle();
		bundle.putString("MyString", "Message Received");

		msg.setData(bundle);

		try {
			myServiceMesenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	public void showTime(View view) {

		if (!isBound) {
			Toast.makeText(getApplicationContext(), "not bounded..... ",
					Toast.LENGTH_SHORT).show();
			return;
		}

		Message msg = Message.obtain();

		Bundle bundle = new Bundle();
		bundle.putString("MyString", "Message Received");

		msg.setData(bundle);

		try {
			myServiceMesenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		Intent intent = new Intent(this, ServiceRemote.class);
		startService(intent);
	}
}
