package com.android.ffmpeg.facebook.dashboard;

import java.util.Arrays;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.ffmpeg.Consts;
import com.android.ffmpeg.R;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

public class FBActivity extends Activity {

	private Fragment[] _fragments = new Fragment[Consts.FRAGMENT_GOOGLE_FB_COUNT];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fragment);

		_fragments[Consts.FRAGMENT_GOOGLE_FB_LOGOUT] = new FragmentLogOut();
		_fragments[Consts.FRAGMENT_GOOGLE_FB_ACTIVE] = FBDashboard.create();

		getFragmentManager().beginTransaction()
				.replace(R.id.social_media_list_frag, _fragments[0]).commit();

		_uiHelper = new UiLifecycleHelper(this, callback);
		_uiHelper.onCreate(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		_uiHelper.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		_uiHelper.onActivityResult(requestCode, resultCode, data);

	}

	public class FragmentLogOut extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.facebook_login_opt,
					container, false);
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
			LoginButton _loginButton = (LoginButton) getView().findViewById(
					R.id.login_button);
			_loginButton.setReadPermissions(Arrays
					.asList(Consts.FACEBOOK_PERMS));

		}

	}

	private boolean _bIsResumed;
	private UiLifecycleHelper _uiHelper;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (_bIsResumed)
			if (state.equals(SessionState.OPENED)) {
				getFragmentManager()
						.beginTransaction()
						.replace(R.id.social_media_list_frag,
								_fragments[Consts.FRAGMENT_GOOGLE_FB_ACTIVE])
						.commit();
			} else {
				getFragmentManager()
						.beginTransaction()
						.replace(R.id.list,
								_fragments[Consts.FRAGMENT_GOOGLE_FB_LOGOUT])
						.commit();

			}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_uiHelper.onDestroy();

	}

	@Override
	public void onResume() {
		super.onResume();
		_bIsResumed = true;

		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
		}
		_uiHelper.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		_uiHelper.onPause();
		_bIsResumed = false;
	}

}
