package com.android.ffmpeg.googleplus.dashboard;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.android.ffmpeg.Consts;
import com.android.ffmpeg.R;
import com.android.ffmpeg.googleplus.dashboard.DashboardGooglePlus.OnGoogleActivityDataPass;
import com.android.ffmpeg.googleplus.ytutils.UtilsYTPlayListItems;
import com.android.ffmpeg.googleplus.ytutils.UtilsYTUploadAsync;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpBackOffIOExceptionHandler;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.youtube.YouTube;

public class DashBoardGoogleActivity extends Activity implements
		ConnectionCallbacks, OnGoogleActivityDataPass,
		OnConnectionFailedListener {

	private int _nSignInProgress;

	private int _nSignInError;

	private GoogleApiClient _googleApiClient;

	private GoogleAccountCredential _credential;

	private PendingIntent _signInIntent;

	private Fragment[] _fragments = new Fragment[Consts.FRAGMENT_GOOGLE_FB_COUNT + 1];

	private List<String> _scopeList = Arrays.asList(
			"https://www.googleapis.com/auth/youtube.upload",
			"https://www.googleapis.com/auth/youtube.readonly");

	public static YouTube _youtube;

	private static final JsonFactory JSON_FACTORY = new JacksonFactory();

	private static final HttpTransport HTTP_TRANSPORT = AndroidHttp
			.newCompatibleTransport();

	private UploadBroadcastReceiver broadcastReceiver;

	private GoogleApiClient buildGoogleApiClient() {
		return new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Plus.API, Plus.PlusOptions.builder().build())
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);

		_googleApiClient = buildGoogleApiClient();

		_fragments[Consts.FRAGMENT_GOOGLE_FB_LOGOUT] = new FragmentLogOut();
		_fragments[Consts.FRAGMENT_GOOGLE_FB_ACTIVE] = DashboardGooglePlus
				.create();
		_fragments[Consts.FRAGMENT_GOOGLE_ACTIVE_YTPLAYLIST] = UtilsYTPlayListItems
				.create();

		getFragmentManager()
				.beginTransaction()
				.replace(R.id.social_media_list_frag,
						_fragments[Consts.FRAGMENT_GOOGLE_FB_LOGOUT]).commit();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case Consts.RC_SIGN_IN:
			if (resultCode == RESULT_OK)
				_nSignInProgress = Consts.STATE_SIGN_IN;
			else
				_nSignInProgress = Consts.STATE_DEFAULT;
			break;
		case REQUEST_GOOGLE_PLAY_SERVICES:
			if (resultCode == Activity.RESULT_OK)
				haveGooglePlayServices();
			else
				checkGooglePlayServicesAvailable();
			break;
		case REQUEST_AUTHORIZATION:
			if (resultCode == Activity.RESULT_OK)
				chooseAccount();
			break;
		case REQUEST_ACCOUNT_PICKER:
			if (resultCode == Activity.RESULT_OK && data != null
					&& data.getExtras() != null) {
				String accountName = data.getExtras().getString(
						AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					_credential.setSelectedAccountName(accountName);
					SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString(PREF_ACCOUNT_NAME, accountName);
					editor.commit();
				}
			}
			break;
		}
		if (!_googleApiClient.isConnecting())
			_googleApiClient.connect();
	}

	@Override
	public void onStart() {
		super.onStart();
		_googleApiClient.connect();

	}

	@Override
	public void onPause() {
		super.onPause();
		if (broadcastReceiver != null) {
			LocalBroadcastManager.getInstance(this).unregisterReceiver(
					broadcastReceiver);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (broadcastReceiver == null)
			broadcastReceiver = new UploadBroadcastReceiver();

		IntentFilter intentFilter = new IntentFilter(
				REQUEST_AUTHORIZATION_INTENT);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				broadcastReceiver, intentFilter);

	}

	@Override
	protected void onStop() {
		super.onStop();
		if (_googleApiClient.isConnected())
			_googleApiClient.disconnect();

	}

	@Override
	public GoogleApiClient onPassGoogleApiClient() {
		return _googleApiClient;
	}

	@Override
	public void onSignOut() {
		signedOut();
	}

	@Override
	public void onConnected(Bundle arg0) {

		// fix on authuntication received

		getFragmentManager()
				.beginTransaction()
				.replace(R.id.social_media_list_frag,
						_fragments[Consts.FRAGMENT_GOOGLE_FB_ACTIVE]).commit();

		SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);

		_nSignInProgress = Consts.STATE_DEFAULT;
		_credential = GoogleAccountCredential.usingOAuth2(this, _scopeList);
		_credential.setSelectedAccountName(settings.getString(
				PREF_ACCOUNT_NAME, null));

		haveGooglePlayServices();

	}

	@Override
	public void onConnectionSuspended(int arg0) {
		_googleApiClient.connect();

	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {

		if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
			Log.i("TAG_GOOGLE", "ConnectionResult.API_UNAVAILABLE");
		} else if (_nSignInProgress != Consts.STATE_IN_PROGRESS) {
			_signInIntent = result.getResolution();
			_nSignInError = result.getErrorCode();

			if (_nSignInProgress == Consts.STATE_SIGN_IN) {
				resolveSignInError();

			}
		}
	}

	public class FragmentLogOut extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.google_signin, container,
					false);
			return view;
		}

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			getActionBar().hide();
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			final SignInButton _signInButton = (SignInButton) getView()
					.findViewById(R.id.sign_in_button);
			_signInButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (!_googleApiClient.isConnecting()) {
						resolveSignInError();
					}
				}
			});
		}

	}

	private void resolveSignInError() {
		if (_signInIntent != null)
			try {

				_nSignInProgress = Consts.STATE_IN_PROGRESS;
				startIntentSenderForResult(_signInIntent.getIntentSender(),
						Consts.RC_SIGN_IN, null, 0, 0, 0);

			} catch (SendIntentException e) {
				_nSignInProgress = Consts.STATE_SIGN_IN;
				_googleApiClient.connect();
			}
		else if (GooglePlayServicesUtil.isUserRecoverableError(_nSignInError)) {
			GooglePlayServicesUtil.getErrorDialog(_nSignInError, this,
					Consts.RC_SIGN_IN, new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							_nSignInProgress = Consts.STATE_DEFAULT;
						}
					}).show();
		}
	}

	private void signedOut() {
		Plus.AccountApi.clearDefaultAccount(_googleApiClient);
		Plus.AccountApi.revokeAccessAndDisconnect(_googleApiClient)
				.setResultCallback(new ResultCallback<Status>() {

					@Override
					public void onResult(Status status) {
						_googleApiClient.disconnect();
						_googleApiClient.connect();
						_nSignInProgress = Consts.STATE_DEFAULT;

					}

				});

	}

	private void haveGooglePlayServices() {
		if (_credential.getSelectedAccountName() == null) {
			chooseAccount();
		}

	}

	private void chooseAccount() {
		startActivityForResult(_credential.newChooseAccountIntent(),
				REQUEST_ACCOUNT_PICKER);
	}

	private boolean checkGooglePlayServicesAvailable() {
		final int connectionStatusCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(this);
		if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
			showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
			return false;
		}
		return true;
	}

	void showGooglePlayServicesAvailabilityErrorDialog(
			final int connectionStatusCode) {
		runOnUiThread(new Runnable() {
			public void run() {
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(
						connectionStatusCode, DashBoardGoogleActivity.this,
						REQUEST_GOOGLE_PLAY_SERVICES);
				dialog.show();
			}
		});
	}

	@Override
	public void onGetYoutubePlayList() {
		_youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				_credential).setApplicationName(
				getResources().getString(R.string.app_name)).build();

		getFragmentManager()
				.beginTransaction()
				.replace(R.id.social_media_list_frag,
						_fragments[Consts.FRAGMENT_GOOGLE_ACTIVE_YTPLAYLIST])
				.commit();

	}

	@Override
	public void onExecuteUpload(String sUrl, String sVideoName) {

		_youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				new HttpRequestInitializer() {
					@Override
					public void initialize(HttpRequest request)
							throws IOException {
						_credential.initialize(request);
						request.setIOExceptionHandler(new HttpBackOffIOExceptionHandler(
								new ExponentialBackOff()));
					}
				}).setApplicationName(
				getResources().getString(R.string.app_name)).build();

		new UtilsYTUploadAsync(_youtube, this).execute(sUrl, sVideoName);
	}

	public class UploadBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(REQUEST_AUTHORIZATION_INTENT)) {
				Intent toRun = intent
						.getParcelableExtra(REQUEST_AUTHORIZATION_INTENT_PARAM);
				startActivityForResult(toRun, REQUEST_AUTHORIZATION);
			}

		}
	}

	private static final int REQUEST_AUTHORIZATION = 2;

	private static final int REQUEST_ACCOUNT_PICKER = 3;

	private static final int REQUEST_GOOGLE_PLAY_SERVICES = 1;

	public static final String TAG_YTB = "Youtube_auth";

	private static final String PREF_ACCOUNT_NAME = "accountName";

	public static final String REQUEST_AUTHORIZATION_INTENT = "com.android.ffmpeg.youtube.REQUEST_AUTH";

	public static final String REQUEST_AUTHORIZATION_INTENT_PARAM = "com.android.ffmpeg.youtube.REQUEST_AUTH.param";

}
