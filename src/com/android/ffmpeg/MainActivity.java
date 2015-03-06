package com.android.ffmpeg;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.StrictMode;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;

import com.android.ffmpeg.filebrowser.BrowserAudio;
import com.android.ffmpeg.filebrowser.BrowserAudio.OnAudioDataPass;
import com.android.ffmpeg.filebrowser.BrowserPhoto;
import com.android.ffmpeg.filebrowser.BrowserPhoto.OnPhotoDataPass;
import com.android.ffmpeg.imageffects.EffectImage;
import com.android.ffmpeg.imageslides.listcompiled.ListRowItem;
import com.android.ffmpeg.imageslides.listcompiled.ListVideos;
import com.android.ffmpeg.imageslides.properties.PropAudio;
import com.android.ffmpeg.imageslides.properties.PropImage;
import com.android.ffmpeg.scanner.MediaScannerClient;
import com.android.ffmpeg.service.ServiceRemote;
import com.android.ffmpeg.slidemenu.SlideMenu;
import com.android.ffmpeg.slidemenu.SlideMenuInterface.OnSlideMenuItemClickListener;
import com.android.ffmpeg.videostories.VideoStories;
import com.android.ffmpeg.videostories.VideoStories.OnUpdateFields;

public class MainActivity extends Activity implements OnPhotoDataPass,
		OnAudioDataPass, OnUpdateFields, OnSlideMenuItemClickListener {

	private ArrayList<String> _audioFileList;

	private ArrayList<String> _imageFileList;

	private ArrayList<PropAudio> _propAudioList;

	private ArrayList<PropImage> _propVideoList;

	private SavedStates _oSavedState;

	private SlideMenu _oSlidemenu;

	private ImageView _appImageView;

	private File _file;

	private Intent _myServiceIntent;

	private Messenger _myServiceMesenger;

	private boolean _bIsBound = false;

	private Fragment[] _fragments = new Fragment[Consts.FRAGMENT_COUNT];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_ACTION_BAR);

		setContentView(R.layout.activity_main);

		initFragments();

		if (savedInstanceState == null) {
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.listFragment,
							_fragments[Consts.FRAGMENT_PHOTO]).commit();
			final String sFileDirectory = Environment
					.getExternalStorageDirectory().getAbsolutePath();
			_file = new File(sFileDirectory, "test" + File.separator + "video");
			if (!_file.exists())
				_file.mkdirs();
			// scanFolder(_file);

		} else {
			getActionBar().setTitle(
					getResources().getString(R.string.menu_photos));
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.listFragment,
							_fragments[Consts.FRAGMENT_PHOTO]).commit();
		}

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		_appImageView = (ImageView) findViewById(android.R.id.home);
		_oSlidemenu = (SlideMenu) findViewById(R.id.slideMenu);
		_oSlidemenu.init(this, R.menu.slide, this, 333);
		_oSlidemenu.setHeaderImage(getResources().getDrawable(
				R.drawable.ic_launcher));

		_oSavedState = new SavedStates();
		_audioFileList = new ArrayList<String>();
		_imageFileList = new ArrayList<String>();

		_propAudioList = new ArrayList<PropAudio>();
		_propVideoList = new ArrayList<PropImage>();

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(true);

		_myServiceIntent = new Intent(this, ServiceRemote.class);
		bindService(_myServiceIntent, _myConnection, Context.BIND_AUTO_CREATE);

	}

	private void initFragments() {
		_fragments[Consts.LIST_VIDEOS] = ListVideos.create(0);
		_fragments[Consts.FRAGMENT_PHOTO] = BrowserPhoto.create(0);
		_fragments[Consts.FRAGMENT_IMAGE_EFFECT] = EffectImage.create(0);
		_fragments[Consts.FRAGMENT_VIDEO_STORIES] = VideoStories.create(0);
		_fragments[Consts.FRAGMENT_AUDIO] = BrowserAudio.create(0);

	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		_oSavedState.setAudioProperties(_propAudioList);
		_oSavedState.setVideoProperties(_propVideoList);
		savedInstanceState.putParcelable(Consts.AUDIO_VIDEO_PROPERTIES,
				_oSavedState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		_oSavedState = (SavedStates) savedInstanceState
				.getParcelable(Consts.AUDIO_VIDEO_PROPERTIES);
		_propAudioList = _oSavedState.getAudioProperties();
		_propVideoList = _oSavedState.getVideoProperties();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			_appImageView.animate().rotation(90);
			_oSlidemenu.show();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	public void onSlideMenuItemClick(int menuItem) {
		switch (menuItem) {

		case R.id.item_one:
			getActionBar().setTitle(
					getResources().getString(R.string.my_stories));
			getFragmentManager().beginTransaction()
					.replace(R.id.listFragment, _fragments[Consts.LIST_VIDEOS])
					.commit();
			break;
		case R.id.item_two:
			getActionBar().setTitle(
					getResources().getString(R.string.menu_photos));
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.listFragment,
							_fragments[Consts.FRAGMENT_PHOTO]).commit();

			break;
		case R.id.item_three:
			getActionBar().setTitle(
					getResources().getString(R.string.menu_photo_story));
			getFragmentManager()
					.beginTransaction()

					.replace(R.id.listFragment,
							_fragments[Consts.FRAGMENT_VIDEO_STORIES]).commit();
			break;
		case R.id.item_four:
			getActionBar().setTitle(
					getResources().getString(R.string.menu_image_effect));
			getFragmentManager()
					.beginTransaction()
					.replace(R.id.listFragment,
							_fragments[Consts.FRAGMENT_IMAGE_EFFECT]).commit();
			break;
		case R.id.item_five:
			Intent intentFB = new Intent(getApplicationContext(),
					com.android.ffmpeg.facebook.dashboard.FBActivity.class);
			intentFB.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intentFB.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intentFB);
			break;
		case R.id.item_six:
			Intent intentGplus = new Intent(
					getApplicationContext(),
					com.android.ffmpeg.googleplus.dashboard.DashBoardGoogleActivity.class);
			intentGplus.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intentGplus.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intentGplus);
			break;

		}
		killAsyncTasks();

	}

	public void onEncode(final int nDuration, final int nFrameDuration,
			final boolean bTrim) {

		if (!_propAudioList.isEmpty() && !_propVideoList.isEmpty()) {

			if (!_bIsBound)
				return;
			else {

				Message msg = Message.obtain();
				Bundle bundle = new Bundle();

				_oSavedState.setAudioProperties(_propAudioList);
				_oSavedState.setVideoProperties(_propVideoList);

				bundle.putParcelable(Consts.AUDIO_VIDEO_PROPERTIES,
						_oSavedState);
				bundle.putBoolean(Consts.TRIM_PROPERTY, bTrim);
				bundle.putInt(Consts.VIDEO_DURATION, nDuration);
				bundle.putInt(Consts.FRAME_DURATION, nFrameDuration);

				msg.setData(bundle);
				_myServiceIntent.putExtras(bundle);
				try {
					_myServiceMesenger.send(msg);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				startService(_myServiceIntent);
				new AlertDialog.Builder(this)
						.setPositiveButton(R.string.error_dialog_button_text,
								null).setMessage(R.string.result_dialog_msg)
						.show();
			}
		} else
			new AlertDialog.Builder(this)
					.setPositiveButton(R.string.error_dialog_button_text, null)
					.setMessage(R.string.result_dialog_msg_condition).show();
	}

	private ServiceConnection _myConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			_myServiceMesenger = new Messenger(service);
			_bIsBound = true;
		}

		public void onServiceDisconnected(ComponentName className) {
			_myServiceMesenger = null;
			_bIsBound = false;
		}
	};

	@Override
	public int passImageUpdateFields() {
		if (_imageFileList.size() != 0)
			return _imageFileList.size();
		else
			return 0;
	}

	@Override
	public ArrayList<PropAudio> passAudioUpdateFields() {
		if (_propAudioList.size() != 0)
			return _propAudioList;
		else
			return null;
	}

	@Override
	public void onPhotoDataPass(ArrayList<String> data,
			ArrayList<PropImage> videoProp) {

		_imageFileList.clear();
		_propVideoList.clear();
		_imageFileList.addAll(data);
		_propVideoList.addAll(videoProp);

		getActionBar().setTitle(
				getResources().getString(R.string.menu_video_story));
		getFragmentManager()
				.beginTransaction()
				.setCustomAnimations(R.animator.slide_right_in,
						R.animator.slide_right_out)
				.replace(R.id.listFragment, _fragments[Consts.FRAGMENT_AUDIO])
				.commit();

	}

	@Override
	public void onAudioDataPass(String data, PropAudio propAudio) {

		_propAudioList.clear();
		_audioFileList.clear();
		_audioFileList.add(data);
		_propAudioList.add(propAudio);

		getFragmentManager().popBackStack();
		getFragmentManager()
				.beginTransaction()
				.setCustomAnimations(R.animator.slide_left_in,
						R.animator.slide_right_out)
				.replace(R.id.listFragment,
						_fragments[Consts.FRAGMENT_VIDEO_STORIES]).commit();
	}

	public void scanFolder(File dir) {

		MediaScannerClient.nCOUNT = 1;
		MediaScannerClient.COMPLETE_SCAN = true;

		String sPattern = ".mp4";
		_rowItemsTest = new ArrayList<ListRowItem>();

		File[] listFile = dir.listFiles();
		_filePath = new String[listFile.length];
		if (listFile != null)
			for (int i = 0; i < listFile.length; i++)
				if (listFile[i].isDirectory()) {
					scanFolder(listFile[i]);
				} else {
					if (listFile[i].getName().endsWith(sPattern)) {
						_filePath[i] = listFile[i].getAbsolutePath();
						new MediaScannerClient(this, listFile[i],
								listFile.length, _rowItemsTest);

					}
				}

	}

	public Bitmap decodeUri(Uri uri, final int requiredSize)
			throws FileNotFoundException {
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(getContentResolver().openInputStream(uri),
				null, o);

		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;

		while (true) {
			if (width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
				break;
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}

		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		return BitmapFactory.decodeStream(
				getContentResolver().openInputStream(uri), null, o2);
	}

	private ArrayList<ListRowItem> _rowItemsTest;

	private String[] _filePath;

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (_bIsBound)
			unbindService(_myConnection);

	}

	@Override
	public void onResume() {
		super.onResume();
		if (!_bIsBound)
			bindService(_myServiceIntent, _myConnection,
					Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onPause() {
		super.onPause();

	}

	private void killAsyncTasks() {
		if (_fragments[Consts.FRAGMENT_PHOTO].isVisible()) {
			((BrowserPhoto) _fragments[Consts.FRAGMENT_PHOTO])
					.forceCancellTask();
		}
	}
}