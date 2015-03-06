package com.android.ffmpeg;

import java.util.Arrays;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.widget.LoginButton;
import com.google.android.gms.common.SignInButton;

public class LoginSplash extends Fragment {

	private LoginButton _loginButton;

	private SignInButton _signInButton;

	private TextView _skipLoginTextView;

	private LoginButtonCallback _loginCallback;

	public static LoginSplash create() {
		LoginSplash fragment = new LoginSplash();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		_loginButton = (LoginButton) getView().findViewById(R.id.login_button);
		_loginButton.setReadPermissions(Arrays.asList(Consts.FACEBOOK_PERMS));

		_signInButton = (SignInButton) getView().findViewById(
				R.id.sign_in_button);

		_signInButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onGoogleSigninPressed();
			}
		});

		_skipLoginTextView = (TextView) getView().findViewById(
				R.id.skip_login_button);
		_skipLoginTextView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onSkipLoginPressed();
			}
		});
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			_loginCallback = (LoginButtonCallback) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement 	loginCallback");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.splash, container, false);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	public interface LoginButtonCallback {
		void onSkipLoginPressed();

		void onGoogleSigninPressed();
	}

	public void onSkipLoginPressed() {
		_loginCallback.onSkipLoginPressed();
	}

	public void onGoogleSigninPressed() {
		_loginCallback.onGoogleSigninPressed();
	}

}
