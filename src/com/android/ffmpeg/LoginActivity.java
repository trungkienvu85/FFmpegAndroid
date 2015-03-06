package com.android.ffmpeg;

import android.app.Activity;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.view.Window;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.plus.Plus;

public class LoginActivity extends Activity implements
		LoginSplash.LoginButtonCallback, ConnectionCallbacks,
		OnConnectionFailedListener {

	private boolean _bIsResumed = false;

	private boolean _bUserSkippedLogin = false;

	private int _nSignInProgress;

	private int _nSignInError;

	private UiLifecycleHelper _uiHelper;

	private PendingIntent _signInIntent;

	private GoogleApiClient _googleApiClient;

	private Fragment[] _fragments = new Fragment[Consts.FRAGMENT_COUNT];

	Intent _intent;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private GoogleApiClient buildGoogleApiClient() {
		return new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
				.addOnConnectionFailedListener(this)
				.addApi(Plus.API, Plus.PlusOptions.builder().build())
				.addScope(Plus.SCOPE_PLUS_LOGIN).build();
	}

	Fragment frag;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);

		setContentView(R.layout.splash_main);

		_intent = new Intent(getApplicationContext(),
				com.android.ffmpeg.MainActivity.class);

		_fragments[Consts.SPLASH] = LoginSplash.create();

		if (savedInstanceState != null) {
			_bUserSkippedLogin = savedInstanceState
					.getBoolean(Consts.USER_SKIPPED_LOGIN_KEY);
			_nSignInProgress = savedInstanceState.getInt(Consts.SAVED_PROGRESS,
					Consts.STATE_DEFAULT);
		}
		getFragmentManager().beginTransaction()
				.replace(R.id.splash_main_container, _fragments[Consts.SPLASH])
				.commit();

		_uiHelper = new UiLifecycleHelper(this, callback);
		_uiHelper.onCreate(savedInstanceState);

		_googleApiClient = buildGoogleApiClient();

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(Consts.SAVED_PROGRESS, _nSignInProgress);
		outState.putBoolean(Consts.USER_SKIPPED_LOGIN_KEY, _bUserSkippedLogin);
		_uiHelper.onSaveInstanceState(outState);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		_uiHelper.onActivityResult(requestCode, resultCode, data);

		if (requestCode == Consts.RC_SIGN_IN)
			if (resultCode == RESULT_OK) {
				_nSignInProgress = Consts.STATE_SIGN_IN;
				// getFragmentManager()
				// .beginTransaction()
				// .replace(R.id.splash_main_container,
				// _fragments[Consts.GOOLGE]).commit();
			} else
				_nSignInProgress = Consts.STATE_DEFAULT;

		if (!_googleApiClient.isConnecting()) {
			_googleApiClient.connect();
		}
	}

	@Override
	public void onSkipLoginPressed() {

		_bUserSkippedLogin = true;
		_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(_intent);
		overridePendingTransition(R.animator.activity_open_translate,
				R.animator.activity_close_scale);

		finish();
	}

	@Override
	public void onGoogleSigninPressed() {
		if (!_googleApiClient.isConnecting()) {
			resolveSignInError();
		}
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (_bIsResumed) {
			if (state.equals(SessionState.OPENED)) {
				_intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(_intent);
				overridePendingTransition(R.animator.activity_open_translate,
						R.animator.activity_close_scale);

				finish();

			} else if (state.isClosed()) {
				getFragmentManager()
						.beginTransaction()
						.replace(R.id.splash_main_container,
								_fragments[Consts.SPLASH]).commit();
			}
		}

	}

	private void resolveSignInError() {
		if (_signInIntent != null) {

			try {
				_nSignInProgress = Consts.STATE_IN_PROGRESS;
				startIntentSenderForResult(_signInIntent.getIntentSender(),
						Consts.RC_SIGN_IN, null, 0, 0, 0);

			} catch (SendIntentException e) {
				_nSignInProgress = Consts.STATE_SIGN_IN;
				_googleApiClient.connect();
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {

		if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
		} else if (_nSignInProgress != Consts.STATE_IN_PROGRESS) {
			_signInIntent = result.getResolution();
			_nSignInError = result.getErrorCode();

			if (_nSignInProgress == Consts.STATE_SIGN_IN) {
				resolveSignInError();
			}
		}
	}

	@Override
	public void onConnected(Bundle arg0) {
		_nSignInProgress = Consts.STATE_DEFAULT;

		// getFragmentManager().beginTransaction()
		// .replace(R.id.splash_main_container, _fragments[Consts.GOOLGE])
		// .commit();
	}

	@Override
	public void onConnectionSuspended(int arg0) {
		_googleApiClient.connect();

	}

	@Override
	protected void onStart() {
		super.onStart();
		_googleApiClient.connect();
	}

	@Override
	public void onResume() {
		super.onResume();
		_uiHelper.onResume();
		_bIsResumed = true;

		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		_uiHelper.onPause();
		_bIsResumed = false;

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_uiHelper.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (_googleApiClient.isConnected())
			_googleApiClient.disconnect();

	}
}
