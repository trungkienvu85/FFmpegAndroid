package com.android.ffmpeg.facebook.dashboard;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ffmpeg.Consts;
import com.android.ffmpeg.R;
import com.android.ffmpeg.facebook.dashboard.usergallery.FBPhotoGalleryDialog;
import com.android.ffmpeg.fadingactionbar.FadingActionBarHelper;
import com.android.ffmpeg.socialmedia.dashboard.adapters.ActionListAdapter;
import com.android.ffmpeg.socialmedia.dashboard.adapters.DashboardBaseListElement;
import com.android.ffmpeg.socialmedia.dashboard.view.ViewFragmentVideoShare;
import com.android.ffmpeg.socialmedia.dashboard.view.ViewFragmentVideoShare.LoadedImage;
import com.android.ffmpeg.socialmedia.dashboard.view.ViewFragmentVideoShare.OnMyDialogResult;
import com.facebook.AppEventsLogger;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionDefaultAudience;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.model.OpenGraphAction;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class FBDashboard extends Fragment {

	private UiLifecycleHelper _uiHelper;

	private ListView _listView;

	private Button _allowPostButton;

	private FadingActionBarHelper _oFadingHelper;

	private Bundle _arguments;

	private ImageView _profileImage;

	private TextView _fbUserName;

	private TextView _fbUserEmail;

	private ActionBar _actionBar;

	private ArrayList<DashboardBaseListElement> _listElements;

	private ImageView _photoThumbnail;

	private Session.StatusCallback _sessionCallback = new Session.StatusCallback() {
		@Override
		public void call(final Session session, final SessionState state,
				final Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	public static FBDashboard create() {
		FBDashboard fragment = new FBDashboard();
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_actionBar = getActivity().getActionBar();
		_actionBar.show();
		_uiHelper = new UiLifecycleHelper(getActivity(), _sessionCallback);
		_uiHelper.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		_listElements = new ArrayList<DashboardBaseListElement>();
		_listElements.add(new FBGalleryElement(0));
		_listElements.add(new PhotoListElement(1));
		_listElements.add(new VideoShareElement(2));

		if (savedInstanceState != null) {
			for (DashboardBaseListElement listElement : _listElements)
				listElement.restoreState(savedInstanceState);
		}

		_listView = (ListView) getView().findViewById(R.id.selection_list);
		_profileImage = (ImageView) getView().findViewById(R.id.profile_image);
		_fbUserName = (TextView) getView().findViewById(R.id.fb_username);
		_fbUserEmail = (TextView) getView().findViewById(R.id.fb_email);
		_allowPostButton = (Button) getView().findViewById(R.id.fb_share);
		_photoThumbnail = (ImageView) getView().findViewById(
				R.id.selected_image);

		_profileImage.setVisibility(View.INVISIBLE);
		// _allowPostButton.setEnabled(false);

		_allowPostButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				// publishFeedDialog();
				uploadVideos();
				// publishStory();
				// handleAnnounce(true);
			}
		});
		_listView.setAdapter(new ActionListAdapter(getActivity(),
				R.id.selection_list, _listElements));

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
				.contentLayout(R.layout.dashboard_facebook)
				.lightActionBar(actionBarBg == R.drawable.act_background_trans);
		_oFadingHelper.initActionBar(activity);
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
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		for (DashboardBaseListElement listElement : _listElements) {
			listElement.onSaveInstanceState(bundle);
		}
		_uiHelper.onSaveInstanceState(bundle);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == Activity.RESULT_OK && requestCode >= 0
				&& requestCode < _listElements.size()) {
			_listElements.get(requestCode).onActivityResult(data);
		} else {
			Session.getActiveSession().onActivityResult(getActivity(),
					requestCode, resultCode, data);
			_uiHelper.onActivityResult(requestCode, resultCode, data,
					nativeDialogCallback);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Session session = Session.getActiveSession();
		if (session != null && (session.isOpened() || session.isClosed())) {
			onSessionStateChange(session, session.getState(), null);
		}
		_uiHelper.onResume();
		AppEventsLogger.activateApp(getActivity().getApplicationContext());
	}

	@Override
	public void onPause() {
		super.onPause();
		_uiHelper.onPause();
		AppEventsLogger.deactivateApp(getActivity().getApplicationContext());

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		_uiHelper.onDestroy();
	}

	private FacebookDialog.Callback nativeDialogCallback = new FacebookDialog.Callback() {
		@Override
		public void onComplete(FacebookDialog.PendingCall pendingCall,
				Bundle data) {
			Log.i("TAG_FB", "nativeDialogCallback");

			boolean resetSelections = true;
			if (FacebookDialog.getNativeDialogDidComplete(data)) {
				if (FacebookDialog.COMPLETION_GESTURE_CANCEL
						.equals(FacebookDialog
								.getNativeDialogCompletionGesture(data))) {
					// Leave selections alone if user canceled.
					resetSelections = false;
					showCancelResponse();
					Log.i("TAG_FB", "getNativeDialogCompletionGesture");

				} else {
					Log.i("TAG_FB", "showSuccessResponse");

					showSuccessResponse(FacebookDialog
							.getNativeDialogPostId(data));
				}
			}

			if (resetSelections) {
				Log.i("TAG_FB", "resetSelections..........");

			}
		}

		@Override
		public void onError(FacebookDialog.PendingCall pendingCall,
				Exception error, Bundle data) {
			new AlertDialog.Builder(getActivity())
					.setPositiveButton(R.string.error_dialog_button_text, null)
					.setTitle(R.string.error_dialog_title)
					.setMessage(error.getLocalizedMessage()).show();
		}
	};

	private void handleGraphApiAnnounce() {
		Session session = Session.getActiveSession();

		List<String> permissions = session.getPermissions();
		if (!permissions.contains(Consts.PERMISSION)) {
			requestPublishPermissions(session);
			return;
		}

		AsyncTask<Void, Void, List<Response>> task = new AsyncTask<Void, Void, List<Response>>() {

			@Override
			protected List<Response> doInBackground(Void... voids) {
				return null;
			}

			@Override
			protected void onPostExecute(List<Response> responses) {
				// We only care about the last response, or the first one with
				// an error.
				Response finalResponse = null;
				for (Response response : responses) {
					finalResponse = response;
					if (response != null && response.getError() != null) {
						break;
					}
				}
				onPostActionResponse(finalResponse);
			}
		};

		task.execute();
	}

	private void onPostActionResponse(Response response) {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
		if (getActivity() == null) {
			// if the user removes the app from the website, then a request will
			// have caused the session to close (since the token is no longer
			// valid),
			// which means the splash fragment will be shown rather than this
			// one,
			// causing activity to be null. If the activity is null, then we
			// cannot
			// show any dialogs, so we return.
			return;
		}

		PostResponse postResponse = response
				.getGraphObjectAs(PostResponse.class);

		if (postResponse != null && postResponse.getId() != null) {
			showSuccessResponse(postResponse.getId());
			// init(null);
		} else {
			// handleError(response.getError());
		}
	}

	private interface PostResponse extends GraphObject {
		String getId();
	}

	private ProgressDialog progressDialog;
	private Uri photoUri;
	private boolean pendingPublishReauthorization = false;

	private void requestPublishPermissions(Session session) {
		if (session != null) {
			Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
					getActivity(), Consts.PERMISSION).setDefaultAudience(
					SessionDefaultAudience.FRIENDS).setRequestCode(
					Consts.REAUTH_ACTIVITY_CODE);
			session.requestNewPublishPermissions(newPermissionsRequest);
		}
	}

	private void requestUPloadPermissions(Session session) {
		if (session != null) {
			Log.i("TAG_FB", "requestUPloadPermissions ");
			pendingPublishReauthorization = true;
			Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(
					getActivity(), Consts.PERMISSION_UPLOAD)
					.setDefaultAudience(SessionDefaultAudience.FRIENDS)
					.setRequestCode(Consts.REAUTH_ACTIVITY_CODE);
			session.requestNewReadPermissions(newPermissionsRequest);

		}
	}

	private void onSessionStateChange(final Session session,
			SessionState state, Exception exception) {
		if (session != null && session.isOpened()) {
			makeMeRequest(session);
			FBDashboardRequestPhotoAlbums.makeRequest(session);
			if (state.equals(SessionState.OPENED_TOKEN_UPDATED)) {
				if (pendingPublishReauthorization) {
					Log.i("TAG_FB", "pendingPublishReauthorization ");
					pendingPublishReauthorization = false;
					// uploadVideos();
				}
			} else {
				makeMeRequest(session);
			}
		}
	}

	private void makeMeRequest(final Session session) {
		Request request = Request.newMeRequest(session,
				new Request.GraphUserCallback() {
					@Override
					public void onCompleted(GraphUser user, Response response) {
						if (session == Session.getActiveSession()) {
							if (user != null) {
								try {
									_fbUserName.setText(user.getName());
									_fbUserEmail.setText((String) user
											.getProperty("email"));
									URL imgUrl = new URL(
											"https://graph.facebook.com/"
													+ user.getId()
													+ "/picture?type=large");
									InputStream in;
									in = (InputStream) imgUrl.getContent();
									Bitmap bitmap = BitmapFactory
											.decodeStream(in);
									if (bitmap != null) {
										_profileImage
												.setVisibility(View.VISIBLE);
										UrlImageViewHelper
												.setUrlDrawable(
														_profileImage,
														"https://graph.facebook.com/"
																+ user.getId()
																+ "/picture?type=large");
									}

								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
						if (response.getError() != null) {
							// handleError(response.getError());
						}
					}
				});
		request.executeAsync();
	}

	private String _sName = "Facebook SDK for Android";
	private String _sCaption = "Build great social apps and get more installs";
	private String _sDescription = "The Facebook SDK for Android makes it easier and faster to develop Facebook integrated Android apps";
	private String _sPicture = "https://raw.github.com/fbsamples/ios-3.x-howtos/master/Images/iossdk_logo.png";
	private String _sLink = "https://developers.facebook.com/android";

	private void publishStory() {
		Session session = Session.getActiveSession();

		if (session != null) {

			List<String> permissions = session.getPermissions();
			if (!permissions.contains(Consts.PERMISSION)) {
				requestPublishPermissions(session);
				return;
			}

			Bundle postParams = new Bundle();
			postParams.putString("name", _sName);
			postParams.putString("caption", _sCaption);
			postParams.putString("description", _sDescription);
			postParams.putString("link", _sLink);
			postParams.putString("picture", _sPicture);

			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
					JSONObject graphResponse = response.getGraphObject()
							.getInnerJSONObject();
					String postId = null;
					try {
						postId = graphResponse.getString("id");
					} catch (JSONException e) {
						// Log.i(TAG, "JSON error " + e.getMessage());
					}
					FacebookRequestError error = response.getError();
					if (error != null) {
						Toast.makeText(getActivity().getApplicationContext(),
								error.getErrorMessage(), Toast.LENGTH_SHORT)
								.show();
					} else {
						showSuccessResponse(postId);
					}
				}
			};

			Request request = new Request(session, "me/feed", postParams,
					HttpMethod.POST, callback);
			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();
		}

	}

	private void publishFeedDialog() {
		Session session = Session.getActiveSession();
		if (session != null) {

			List<String> permissions = session.getPermissions();
			if (!permissions.contains(Consts.PERMISSION)) {
				requestPublishPermissions(session);
				Log.i("TAG_FB", "requestPublishPermissions");

				return;
			}

			if (FacebookDialog.canPresentShareDialog(getActivity()
					.getApplicationContext(),
					FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
				FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(
						getActivity()).setLink(_sLink).setName(_sName)
						.setCaption(_sCaption).setPicture(_sPicture)
						.setDescription(_sDescription).build();
				_uiHelper.trackPendingDialogCall(shareDialog.present());
				Log.i("TAG_FB", "requestPublishPermissions");

			} else {
				Log.i("TAG_FB", "requestPublishPermissions");

				Bundle params = new Bundle();
				params.putString("name", _sName);
				params.putString("caption", _sCaption);
				params.putString("description", _sDescription);
				params.putString("link", _sDescription);
				params.putString("picture", _sLink);
				WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(
						getActivity(), Session.getActiveSession(), params))
						.setOnCompleteListener(new OnCompleteListener() {

							@Override
							public void onComplete(Bundle values,
									FacebookException error) {
								if (error == null) {
									final String postId = values
											.getString("post_id");
									if (postId != null) {
										Toast.makeText(getActivity(),
												"Posted story, id: " + postId,
												Toast.LENGTH_SHORT).show();
									} else {
										Toast.makeText(
												getActivity()
														.getApplicationContext(),
												"Publish cancelled",
												Toast.LENGTH_SHORT).show();
									}
								} else if (error instanceof FacebookOperationCanceledException) {
									Toast.makeText(
											getActivity()
													.getApplicationContext(),
											"Publish cancelled",
											Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(
											getActivity()
													.getApplicationContext(),
											"Error posting story",
											Toast.LENGTH_SHORT).show();
								}
							}

						}).build();
				feedDialog.show();
			}
		}
	}

	private String _sDataPath = Environment.getExternalStorageDirectory()
			+ File.separator + "test/video/sample_1001kbit.mp4";

	private void uploadVideos() {
		byte[] data = null;

		String dataMsg = "My First upld.....";
		Session session = Session.getActiveSession();
		InputStream is = null;
		Bundle postParams = new Bundle();

		if (session != null) {
			List<String> permissions = session.getPermissions();
			if (!permissions.contains(Consts.PERMISSION_UPLOAD)) {
				requestUPloadPermissions(session);
				for (String perms : session.getDeclinedPermissions())
					Log.i("TAG_FB", "Permission declined ::" + perms);
				return;
			}
			Log.i("TAG_FB", "Permission GAINED");

			try {
				is = new FileInputStream(_sDataPath);
				data = readBytes(is);
				postParams.putString("message", dataMsg);
				postParams.putByteArray("video", data);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			Request.Callback callback = new Request.Callback() {
				public void onCompleted(Response response) {
					JSONObject graphResponse = response.getGraphObject()
							.getInnerJSONObject();
					String postId = null;
					try {
						postId = graphResponse.getString("id");
					} catch (JSONException e) {
						// Log.i(TAG, "JSON error " + e.getMessage());
					}
					FacebookRequestError error = response.getError();
					if (error != null) {
						Toast.makeText(getActivity().getApplicationContext(),
								error.getErrorMessage(), Toast.LENGTH_SHORT)
								.show();
						showCancelResponse();
					} else {
						showSuccessResponse(postId);
					}
				}
			};

			Request request = new Request(session, "me/videos", postParams,
					HttpMethod.POST, callback);
			RequestAsyncTask task = new RequestAsyncTask(request);
			task.execute();

			// File file = new File(dataPath);
			// Request audioRequest;
			// try {
			// audioRequest = Request.newUploadVideoRequest(session, file,
			// callback);
			// audioRequest.executeAsync();
			//
			// } catch (FileNotFoundException e) {
			// e.printStackTrace();
			// }

		}
	}

	public byte[] readBytes(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];

		int len = 0;
		Log.i("TAG_FB", "Uploading Video");

		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);

		}

		return byteBuffer.toByteArray();
	}

	private void showSuccessResponse(String postId) {
		String dialogBody;
		if (postId != null) {
			dialogBody = String.format(
					getString(R.string.result_dialog_text_with_id), postId);
		} else {
			dialogBody = getString(R.string.result_dialog_text_default);
		}
		showResultDialog(dialogBody);
	}

	private void showCancelResponse() {
		showResultDialog(getString(R.string.result_dialog_text_canceled));
	}

	private void showResultDialog(String dialogBody) {
		new AlertDialog.Builder(getActivity())
				.setPositiveButton(R.string.result_dialog_button_text, null)
				.setTitle(R.string.result_dialog_title).setMessage(dialogBody)
				.show();
	}

	private class FBGalleryElement extends DashboardBaseListElement {

		public FBGalleryElement(int requestCode) {
			super(null, "View Gallery", null, requestCode);

		}

		@Override
		public View.OnClickListener getOnClickListener() {
			return new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					getFragmentManager()
							.beginTransaction()
							.add(FBPhotoGalleryDialog.create(),
									"DIALOG_GALLERY").commit();
				}
			};
		}

		@Override
		public void populateOGAction(OpenGraphAction action) {
			// TODO Auto-generated method stub

		}

	}

	private class VideoShareElement extends DashboardBaseListElement {

		ViewFragmentVideoShare showDialog;

		public VideoShareElement(int requestCode) {
			super(null, "Share Videos", null, requestCode);
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
							Toast.makeText(
									getActivity(),
									"result ::"
											+ _photosList.get(position)
													.getName()
											+ " "
											+ _photosList.get(position)
													.getURL(),
									Toast.LENGTH_LONG).show();
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
			setText2(name + "/"
					+ getResources().getString(R.string.action_photo_ready));
		}
	}

	private class PhotoListElement extends DashboardBaseListElement {

		private static final int GALLERY = 1;
		private static final String PHOTO_URI_KEY = "photo_uri";
		private static final String TEMP_URI_KEY = "temp_uri";
		private Uri _tempUri = null;

		public PhotoListElement(int requestCode) {
			super(null, "Select Photo", null, requestCode);

			photoUri = null;
		}

		@Override
		public View.OnClickListener getOnClickListener() {
			return new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					showPhotoChoice();
				}
			};
		}

		@Override
		public void onActivityResult(Intent data) {
			if (_tempUri != null) {
				photoUri = _tempUri;
			} else if (data != null) {
				photoUri = data.getData();
			}
			setPhotoThumbnail();
			setPhotoText();

		}

		@Override
		public void populateOGAction(OpenGraphAction action) {
		}

		@Override
		public void onSaveInstanceState(Bundle bundle) {
			if (photoUri != null) {
				bundle.putParcelable(PHOTO_URI_KEY, photoUri);
			}
			if (_tempUri != null) {
				bundle.putParcelable(TEMP_URI_KEY, _tempUri);
			}
		}

		@Override
		public boolean restoreState(Bundle savedState) {
			photoUri = savedState.getParcelable(PHOTO_URI_KEY);
			_tempUri = savedState.getParcelable(TEMP_URI_KEY);
			setPhotoText();
			return true;
		}

		private void showPhotoChoice() {

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

			builder.setCancelable(true).setItems(
					new CharSequence[] { "camera", "gallery" },
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface,
								int i) {

							if (i == GALLERY) {
								startGalleryActivity();
							}
						}
					});
			builder.show();
		}

		private void setPhotoText() {
			if (photoUri == null) {
				setText2(photoUri
						+ "/"
						+ getResources().getString(
								R.string.action_photo_default));
			} else {
				_allowPostButton.setEnabled(true);
				setText2(photoUri + "/"
						+ getResources().getString(R.string.action_photo_ready));
			}

		}

		private void setPhotoThumbnail() {
			_photoThumbnail.setImageURI(photoUri);
		}

		private void startGalleryActivity() {
			_tempUri = null;
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("image/*");
			String selectPicture = getResources().getString(
					R.string.select_picture);
			startActivityForResult(Intent.createChooser(intent, selectPicture),
					getRequestCode());
		}

	}

	public static final String ARG_IMAGE_RES = "image_source";

	public static final String ARG_ACTION_BG_RES = "image_action_bs_res";

}
