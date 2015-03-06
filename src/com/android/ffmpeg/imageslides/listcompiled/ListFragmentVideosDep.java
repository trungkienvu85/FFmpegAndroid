package com.android.ffmpeg.imageslides.listcompiled;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.android.ffmpeg.R;
import com.android.ffmpeg.scanner.MediaScannerClient;

public class ListFragmentVideosDep extends Fragment implements
		OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

	private static final String ARG_PAGE = null;

	private ListView _listView;

	private List<ListRowItem> _rowItems;

	private Cursor _videoCursor;

	private ListVideoAdapter _videoAdapter;

	private ProgressDialog _progress;

	public static ListFragmentVideosDep create(int page) {
		Bundle args = new Bundle();
		args.putInt(ARG_PAGE, page);
		ListFragmentVideosDep fragment = new ListFragmentVideosDep();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	File file;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final String f = Environment.getExternalStorageDirectory()
				.getAbsolutePath();

		file = new File(f, "test" + File.separator + "video");
		MediaScannerClient.nCOUNT = 1;
		MediaScannerClient.COMPLETE_SCAN = true;
		Uri.fromFile(file);
		if (file.exists()) {
			// walkdir(file);
			getVideoList();
			initView();
		} else {
			file.mkdirs();

		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		this._optionsMenu = menu;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			break;
		case R.id.menu_refresh:
			if (file.exists()) {
				setRefreshActionButtonState(true);

			} else
				Toast.makeText(getActivity(), "path null !! ",
						Toast.LENGTH_LONG).show();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private Menu _optionsMenu;

	public void setRefreshActionButtonState(final boolean refreshing) {
		if (_optionsMenu != null) {
			final MenuItem refreshItem = _optionsMenu
					.findItem(R.id.menu_refresh);
			if (refreshItem != null) {
				if (refreshing) {
					refreshItem
							.setActionView(R.layout.actionbar_indeterminate_progress);
				} else {
					refreshItem.setActionView(null);
				}
			}

		}
	}

	public void initView() {
		_listView = (ListView) getView().findViewById(R.id.list);
		_videoAdapter = new ListVideoAdapter(getActivity(), _rowItems);
		_listView.setAdapter(_videoAdapter);
		_listView.setOnItemClickListener(this);
	}

	private void getVideoList() {
		System.gc();
		_rowItems = new ArrayList<ListRowItem>();

		String[] parameters = { MediaStore.Video.Media._ID,
				MediaStore.Video.Media.DATA,
				MediaStore.Video.Media.DISPLAY_NAME,
				MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DURATION };
		String selection = MediaStore.Video.Media.DATA + " like'%/test/video%'";
		String[] selectionArgs = new String[] { "%test%" };

		_videoCursor = getActivity().getContentResolver().query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, parameters,
				selection, null, MediaStore.Video.Media.DATE_TAKEN + " DESC");
		_videoCursor.getCount();
		for (int i = 0; i < _videoCursor.getCount(); i++) {
			Log.i("MediaScanner", " started adding ........");

			_videoCursor.moveToPosition(i);
			_videoCursor.getString(_videoCursor
					.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
			Bitmap thumb = ThumbnailUtils.createVideoThumbnail(_videoCursor
					.getString(_videoCursor
							.getColumnIndex(MediaStore.Video.Media.DATA)),
					MediaStore.Images.Thumbnails.MINI_KIND);

			ListRowItem item = new ListRowItem(
					thumb,
					_videoCursor.getString(_videoCursor
							.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)),
					"Size ::"
							+ _videoCursor.getString(_videoCursor
									.getColumnIndex(MediaStore.Video.Media.SIZE))
							+ " bytes");

			_rowItems.add(item);
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_list_compiled, container,
				false);
		return v;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {

		Toast toast = Toast.makeText(getActivity().getApplicationContext(),
				"Item " + (position + 1) + ": " + _rowItems.get(position),
				Toast.LENGTH_SHORT);
		toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.show();

		_videoCursor.moveToPosition(position);
		Intent tostart = new Intent(Intent.ACTION_VIEW);
		tostart.setDataAndType(Uri.parse(_videoCursor.getString(_videoCursor
				.getColumnIndex(MediaStore.Video.Media.DATA))), "video/*");

		Log.i("url", _videoCursor.getString(_videoCursor
				.getColumnIndex(MediaStore.Video.Media.DATA)));
		startActivity(tostart);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle args) {
		Toast.makeText(getActivity(), "onCreateLoader !!", Toast.LENGTH_LONG)
				.show();

		String[] parameters = { MediaStore.Video.Media._ID,
				MediaStore.Video.Media.DATA,
				MediaStore.Video.Media.DISPLAY_NAME,
				MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DURATION };
		String selection = MediaStore.Video.Media.DATA + " like'%/test/video%'";
		return new CursorLoader(getActivity(),
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, parameters,
				selection, null, MediaStore.Video.Media.DATE_TAKEN + " DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> Loader, Cursor data) {
		// TODO Auto-generated method stub

		_videoCursor = data;

	}

	@Override
	public void onLoaderReset(Loader<Cursor> Loader) {
		// TODO Auto-generated method stub

	}

	int position = 0;
	Cursor cursor;

	class GetFilesFromSd extends AsyncTask<String, String, String> {
		String f; // "video/*"

		@Override
		protected String doInBackground(String... arg0) {
			final String f2 = Environment.getExternalStorageDirectory()
					.getAbsolutePath();

			final File file = new File(f2, "test/video");
			f = Environment.getExternalStorageDirectory().getAbsolutePath()
					+ "/test/video";
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			_progress.dismiss();
			getVideoList();
			initView();

		}

		@Override
		protected void onPreExecute() {
			_progress = new ProgressDialog(getActivity());
			_progress.setMessage(" scanning  ....");
			_progress.setIndeterminate(true);
			_progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			_progress.show();
		}

		@Override
		protected void onProgressUpdate(String... values) {
		}
	}

	;

	String[] filePath;

	private ArrayList<ListRowItem> _rowItemsTest;

	public void walkdir(File dir) {
		String pattern = ".mp4";
		_rowItemsTest = new ArrayList<ListRowItem>();

		File[] listFile = dir.listFiles();
		filePath = new String[listFile.length];
		if (listFile != null) {
			for (int i = 0; i < listFile.length; i++) {
				if (listFile[i].isDirectory()) {
					walkdir(listFile[i]);
				} else {
					if (listFile[i].getName().endsWith(pattern)) {
						filePath[i] = listFile[i].getAbsolutePath();
						new MediaScannerClient(getActivity(), listFile[i],
								listFile.length, _rowItemsTest);

					}
				}
			}
		}

	}

}
