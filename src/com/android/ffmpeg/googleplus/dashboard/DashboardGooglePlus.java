package com.android.ffmpeg.googleplus.dashboard;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ffmpeg.Consts;
import com.android.ffmpeg.R;
import com.android.ffmpeg.fadingactionbar.FadingActionBarHelper;
import com.android.ffmpeg.socialmedia.dashboard.adapters.ActionListAdapter;
import com.android.ffmpeg.socialmedia.dashboard.adapters.DashboardBaseListElement;
import com.android.ffmpeg.socialmedia.dashboard.view.ViewFragmentVideoShare;
import com.android.ffmpeg.socialmedia.dashboard.view.ViewFragmentVideoShare.LoadedImage;
import com.android.ffmpeg.socialmedia.dashboard.view.ViewFragmentVideoShare.OnMyDialogResult;
import com.facebook.model.OpenGraphAction;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.People.LoadPeopleResult;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusShare;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

public class DashboardGooglePlus extends Fragment implements
		ResultCallback<People.LoadPeopleResult> {

	private GoogleApiClient _googleApiClient;

	// private ListView _circlesListView;

	private Button _uploadVideo;

	private ListView _listView;

	private TextView _userName;

	private TextView _userEmail;

	private ImageView _profileImage;

	private ImageView _photoThumbnail;

	private ArrayAdapter<String> _circlesAdapter;

	private ArrayList<String> _circlesList;

	private int _nSignInProgress;

	private OnGoogleActivityDataPass _googleDataPasser;

	private FadingActionBarHelper _oFadingHelper;

	private Bundle _arguments;

	private ActionBar _actionBar;

	private ArrayList<DashboardBaseListElement> _listElements;

	private String _sDataPath = Environment.getExternalStorageDirectory()
			+ File.separator + "test/video/sample_1001kbit.mp4";

	private String _sVideoName;

	public static DashboardGooglePlus create() {
		DashboardGooglePlus fragment = new DashboardGooglePlus();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_actionBar = getActivity().getActionBar();
		_actionBar.show();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		_listElements = new ArrayList<DashboardBaseListElement>();
		_listElements.add(new VideoUploadElement(0));
		_listElements.add(new VideoShareElement(1));

		_listView = (ListView) getView().findViewById(R.id.selection_list);
		_userName = (TextView) getView().findViewById(R.id.gplus_username);
		_userEmail = (TextView) getView().findViewById(R.id.gplus_email);
		_photoThumbnail = (ImageView) getView().findViewById(
				R.id.selected_image);
		_uploadVideo = (Button) getView().findViewById(R.id.gplus_upload);
		_profileImage = (ImageView) getView().findViewById(R.id.profile_image);
		_profileImage.setVisibility(View.INVISIBLE);

		_googleApiClient = getGoogleApiClient();
		_circlesList = new ArrayList<String>();
		_circlesAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.circle_member, _circlesList);
		_uploadVideo.setEnabled(false);

		getProfileInfo();

		_listView.setAdapter(new ActionListAdapter(getActivity(),
				R.id.selection_list, _listElements));

		_uploadVideo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				executeUpload();
			}
		});
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		_arguments = getArguments();

		int actionBarBg = _arguments != null ? _arguments
				.getInt(ARG_ACTION_BG_RES) : R.drawable.act_background_trans;
		_oFadingHelper = new FadingActionBarHelper()
				.actionBarBackground(actionBarBg)
				.headerLayout(R.layout.header_light)
				.contentLayout(R.layout.dashboard_google_plus)
				.lightActionBar(actionBarBg == R.drawable.act_background_trans);
		_oFadingHelper.initActionBar(activity);
		try {
			_googleDataPasser = (OnGoogleActivityDataPass) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ "must implemetnt googleApi listener");
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = _oFadingHelper.createView(inflater);

		if (_arguments != null) {
			ImageView headerImage = (ImageView) view
					.findViewById(R.id.image_header);
			headerImage.setImageResource(_arguments.getInt(ARG_IMAGE_RES));
		}

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(Consts.SAVED_PROGRESS, _nSignInProgress);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode >= 0
				&& requestCode < _listElements.size()) {
			_listElements.get(requestCode).onActivityResult(data);
		}
	}

	private void getProfileInfo() {

		Person currentUser = Plus.PeopleApi.getCurrentPerson(_googleApiClient);

		_userName.setText(currentUser.getDisplayName());
		_userEmail.setText(Plus.AccountApi.getAccountName(_googleApiClient));
		_actionBar.setTitle(_userEmail.getText().toString());
		String profilePixUrl = currentUser.getImage().getUrl();
		profilePixUrl = profilePixUrl.substring(0, profilePixUrl.length() - 2) + 400;

		new LoadProfileImage(_profileImage).execute(profilePixUrl);
		Plus.PeopleApi.loadVisible(_googleApiClient, null).setResultCallback(
				this);

	}

	@Override
	public void onResult(LoadPeopleResult peopleData) {
		if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
			_circlesList.clear();
			PersonBuffer personBuffer = peopleData.getPersonBuffer();
			try {
				int count = personBuffer.getCount();

				for (int i = 0; i < count; i++) {
					_circlesList.add(personBuffer.get(i).getDisplayName());
				}
			} finally {
				personBuffer.close();
			}

			_circlesAdapter.notifyDataSetChanged();
		} else {
			Log.e("TAG",
					"Error requesting visible circles: "
							+ peopleData.getStatus());
		}
	}

	private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;

		public LoadProfileImage(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) {
			_profileImage.setVisibility(View.VISIBLE);
			bmImage.setImageBitmap(result);
		}
	}

	private class VideoUploadElement extends DashboardBaseListElement {

		ViewFragmentVideoShare showDialog;

		public VideoUploadElement(int requestCode) {
			super(null, "Upload Videos", null, requestCode);
		}

		@Override
		public void onActivityResult(Intent data) {
			System.out.println("onActivityResult LIST");
		}

		@Override
		public View.OnClickListener getOnClickListener() {
			return new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					showDialog = ViewFragmentVideoShare.create();

					getFragmentManager().beginTransaction()
							.add(showDialog, "DIALOG_VIDEO_GALLERY").commit();
					showDialog.setDialogResult(new OnMyDialogResult() {
						@Override
						public void finish(ArrayList<LoadedImage> _photosList,
								int position) {
							setPhotoThumbnail(_photosList.get(position)
									.getBitmap());
							setPhotoText(_photosList.get(position).getName());
							_sDataPath = _photosList.get(position).getURL();
						}
					});
				}
			};
		}

		@Override
		public void populateOGAction(OpenGraphAction action) {

		}

		private void setPhotoThumbnail(Bitmap bitmap) {
			_photoThumbnail.setImageBitmap(bitmap);
		}

		private void setPhotoText(String name) {
			_sVideoName = name.substring(0, name.lastIndexOf("."));
			setText2(_sVideoName + "/"
					+ getResources().getString(R.string.action_photo_ready));
			_uploadVideo.setEnabled(true);
		}
	}

	private class VideoShareElement extends DashboardBaseListElement {

		public VideoShareElement(int requestCode) {
			super(null, "Share Videos", null, requestCode);
		}

		@Override
		public void onActivityResult(Intent data) {
			Uri selectedImage = data.getData();
			ContentResolver cr = getActivity().getContentResolver();
			String mime = cr.getType(selectedImage);

			Intent shareIntent = new PlusShare.Builder(getActivity())
					.setText("hello everyone!").addStream(selectedImage)
					.setType(mime).getIntent();
			startActivityForResult(shareIntent, REQ_START_SHARE);

		}

		@Override
		public View.OnClickListener getOnClickListener() {
			return new View.OnClickListener() {
				@Override
				public void onClick(View view) {

					if (isGooglePlusInstalled())
						getYoutubePlayList();
					else
						Toast.makeText(getActivity().getApplicationContext(), "install google +", Toast.LENGTH_LONG).show();
						
					/*
					 * showDialog = FBVideoShare.create();
					 * 
					 * getFragmentManager().beginTransaction() .add(showDialog,
					 * "DIALOG_VIDEO_GALLERY").commit();
					 * showDialog.setDialogResult(new OnMyDialogResult() {
					 * 
					 * @Override public void finish(ArrayList<LoadedImage>
					 * _photosList, int position) { if (isGooglePlusInstalled())
					 * { setPhotoText(_photosList.get(position) .getName());
					 * _sDataPath = _photosList.get(position).getURL();
					 * 
					 * Uri selectedImage = Uri.parse(_sDataPath);
					 * Log.i("Google+", "STREAM :: " + selectedImage);
					 * Log.i("Google+", "MIME :: " + getMimeType(_sDataPath));
					 * Intent shareIntent = new PlusShare.Builder(
					 * getActivity()) .setText(
					 * "Welcome to the Google+ platform.")
					 * .addStream(selectedImage)
					 * .setType(getMimeType(_sDataPath)) .getIntent(); //
					 * startActivityForResult(shareIntent, // getRequestCode());
					 * getYoutubePlayList();
					 * 
					 * } else Toast.makeText(getActivity(),
					 * "GooglePlusNotInstalled ", Toast.LENGTH_LONG).show(); }
					 * });
					 */
				}
			};
		}

		@Override
		public void populateOGAction(OpenGraphAction action) {

		}
	}

	private boolean isGooglePlusInstalled() {
		try {
			getActivity().getPackageManager().getApplicationInfo(
					"com.google.android.apps.plus", 0);
			return true;
		} catch (PackageManager.NameNotFoundException e) {
			
			final String appPackageName = "com.google.android.apps.plus"; // getPackageName() from Context or Activity object
			try {
			    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
			} catch (android.content.ActivityNotFoundException anfe) {
			    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
			}
			return false;
		}
	}

	protected interface OnGoogleActivityDataPass {
		public GoogleApiClient onPassGoogleApiClient();

		public void onSignOut();

		public void onExecuteUpload(String sUrl, String sVideoName);

		public void onGetYoutubePlayList();
	}

	public GoogleApiClient getGoogleApiClient() {
		return _googleDataPasser.onPassGoogleApiClient();
	}

	public void executeUpload() {
		_googleDataPasser.onExecuteUpload(_sDataPath, _sVideoName);
	}

	public void getYoutubePlayList() {
		_googleDataPasser.onGetYoutubePlayList();
	}

	public void signOut() {
		_googleDataPasser.onSignOut();
	}

	public static final String ARG_IMAGE_RES = "image_source";

	public static final String ARG_ACTION_BG_RES = "image_action_bs_res";

	private static final int REQ_START_SHARE = 2;

}
