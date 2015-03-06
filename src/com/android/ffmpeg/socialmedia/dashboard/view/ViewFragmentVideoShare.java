package com.android.ffmpeg.socialmedia.dashboard.view;

import java.util.ArrayList;

import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;

import com.android.ffmpeg.R;
import com.android.ffmpeg.socialmedia.dashboard.adapters.VideoArrayAdapter;

public class ViewFragmentVideoShare extends DialogFragment implements
		OnItemClickListener {

	private GridView _photosGrid;

	private ListView _albumsList;

	private ArrayList<LoadedImage> _photosList = new ArrayList<LoadedImage>();;

	private VideoArrayAdapter _videoArrayAdapter;

	private OnMyDialogResult mDialogResult;

	public static ViewFragmentVideoShare create() {
		ViewFragmentVideoShare fragment = new ViewFragmentVideoShare();
		return fragment;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		_albumsList = (ListView) getView().findViewById(R.id.listView_albums);
		_albumsList.setVisibility(View.GONE);

		_photosGrid = (GridView) getView().findViewById(R.id.gridView_photos);
		_videoArrayAdapter = new VideoArrayAdapter(getActivity()
				.getApplicationContext(), 0, _photosList);
		_photosGrid.setOnItemClickListener(this);
		_photosGrid.setAdapter(_videoArrayAdapter);

		loadImages();
	}

	@SuppressWarnings("deprecation")
	private void loadImages() {
		final Object data = getActivity().getLastNonConfigurationInstance();
		if (data == null) {
			new LoadVideosFromSDCard().execute();
		} else {
			final LoadedImage[] photos = (LoadedImage[]) data;
			if (photos.length == 0) {
				new LoadVideosFromSDCard().execute();
			}
			for (LoadedImage photo : photos) {
				addImage(photo);
			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
		View view = inflater.inflate(R.layout.test_activity_fb_photo_picker,
				container, false);

		return view;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);
	}

	class LoadVideosFromSDCard extends AsyncTask<Object, LoadedImage, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			Bitmap bitmap = null;

			_cursor = getActivity()
					.getApplicationContext()
					.getContentResolver()
					.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
							sColumnsArray, sSelection, null, sOrderBy);

			int columnIndex = _cursor
					.getColumnIndex(MediaStore.Video.Media._ID);
			int size = _cursor.getCount();
			int imageID = 0;

			for (int i = 0; i < size; i++) {
				_cursor.moveToPosition(i);
				imageID = _cursor.getInt(columnIndex);
				bitmap = MediaStore.Video.Thumbnails.getThumbnail(getActivity()
						.getApplicationContext().getContentResolver(), imageID,
						MediaStore.Video.Thumbnails.MICRO_KIND, null);
				publishProgress(new LoadedImage(
						bitmap,
						_cursor.getString(_cursor
								.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)),
						_cursor.getString(_cursor
								.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))));
			}

			return null;
		}

		@Override
		public void onProgressUpdate(LoadedImage... value) {
			addImage(value);
		}

		@Override
		protected void onPostExecute(Object result) {
		}
	}

	private void addImage(LoadedImage... value) {
		for (LoadedImage image : value) {
			_photosList.add(image);
			_videoArrayAdapter.notifyDataSetChanged();

		}
	}

	public static class LoadedImage {

		private Bitmap _bitmap;

		private String _sName;

		private String _sUrl;

		LoadedImage(Bitmap bitmap, String sName, String sUrl) {
			_bitmap = bitmap;
			_sName = sName;
			_sUrl = sUrl;
		}

		public Bitmap getBitmap() {
			return _bitmap;
		}

		public String getName() {
			return _sName;
		}

		public String getURL() {
			return _sUrl;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		mDialogResult.finish(_photosList, position);
		this.dismiss();

	}

	public interface OnMyDialogResult {
		void finish(ArrayList<LoadedImage> _photosList, int position);
	}

	public void setDialogResult(OnMyDialogResult dialogResult) {
		mDialogResult = dialogResult;
	}

	private Cursor _cursor;

	final String[] sColumnsArray = { MediaStore.Video.Media.DATA,
			MediaStore.Video.Media._ID, MediaStore.Video.Thumbnails._ID,
			MediaStore.Video.Media.DISPLAY_NAME };

	final String sOrderBy = MediaStore.Images.Media._ID;

	final String sSelection = MediaStore.Images.Media.DATA + " like'%/test%'";

}
