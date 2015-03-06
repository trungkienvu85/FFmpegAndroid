package com.android.ffmpeg.imageslides.listcompiled;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ffmpeg.R;
import com.android.ffmpeg.scanner.MediaScannerClient;

public class ListVideos extends Fragment implements OnItemClickListener {

	private static String ARG_PAGE;

	private GridView _sdcardImagesView;

	private VideoAdapter _imageAdapter;

	private File _file;

	private Cursor _cursor;

	private int _nScreenWidth;

	private int _nScreenHeight;

	private String[] _sFilePathArray;

	private ArrayList<ListRowItem> _rowItemList;

	public static ListVideos create(int page) {
		Bundle args = new Bundle();
		args.putInt(ARG_PAGE, page);
		ListVideos fragment = new ListVideos();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		final String f = Environment.getExternalStorageDirectory()
				.getAbsolutePath();

		_file = new File(f, "test" + File.separator + "video");

		if (_file.exists()) {
			setupViews();
			loadImages();
		} else {
			_file.mkdirs();
			Toast toast = Toast.makeText(getActivity().getApplicationContext(),
					"video list empty", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();

		}

	}

	public void scanFolder(File dir) {
		MediaScannerClient.nCOUNT = 1;
		MediaScannerClient.COMPLETE_SCAN = true;
		String pattern = ".mp4";
		_rowItemList = new ArrayList<ListRowItem>();

		File[] listFile = dir.listFiles();
		_sFilePathArray = new String[listFile.length];
		if (listFile != null) {
			for (int i = 0; i < listFile.length; i++) {
				if (listFile[i].isDirectory()) {
					scanFolder(listFile[i]);
				} else {
					if (listFile[i].getName().endsWith(pattern)) {
						_sFilePathArray[i] = listFile[i].getAbsolutePath();
						new MediaScannerClient(getActivity(), listFile[i],
								listFile.length, _rowItemList);

					}
				}
			}
		}

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);
		_nScreenWidth = displaymetrics.widthPixels;
		_nScreenHeight = displaymetrics.heightPixels;

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// final GridView grid = _sdcardImagesView;
		// final int count = grid.getChildCount();
		// ImageView v = null;
		// for (int i = 0; i < count; i++) {
		// v = (ImageView) grid.getChildAt(i);
		// ((BitmapDrawable) v.getDrawable()).setCallback(null);
		// }
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.gridview_image, container, false);
		return view;
	}

	private void setupViews() {

		getView().findViewById(R.id.btn_select_image).setVisibility(View.GONE);
		_sdcardImagesView = (GridView) getView().findViewById(R.id.gridview);
		_sdcardImagesView.setNumColumns(_nScreenWidth / 150);
		_sdcardImagesView.setClipToPadding(false);
		_sdcardImagesView.setOnItemClickListener(this);
		_imageAdapter = new VideoAdapter(getActivity());
		_sdcardImagesView.setAdapter(_imageAdapter);
	}

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

	private void addImage(LoadedImage... value) {
		for (LoadedImage image : value) {
			_imageAdapter.addVideoThumbnail(image);
			_imageAdapter.notifyDataSetChanged();
		}
	}

	class LoadVideosFromSDCard extends AsyncTask<Object, LoadedImage, Object> {

		@Override
		protected Object doInBackground(Object... params) {
			Bitmap bitmap = null;
			String sBitmapName;

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
				sBitmapName = _cursor
						.getString(_cursor
								.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
				publishProgress(new LoadedImage(bitmap, sBitmapName));
			}

			return null;
		}

		@Override
		public void onProgressUpdate(LoadedImage... value) {
			addImage(value);
		}

		@Override
		protected void onPostExecute(Object result) {
			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					setRefreshActionButtonState(false);
				}
			});
		}
	}

	class VideoAdapter extends BaseAdapter {
		private LayoutInflater _inflater;

		private ArrayList<LoadedImage> _videos = new ArrayList<LoadedImage>();

		public VideoAdapter(Context context) {

			_inflater = (LayoutInflater) getActivity().getApplicationContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void addVideoThumbnail(LoadedImage photo) {
			_videos.add(photo);
		}

		public int getCount() {
			return _videos.size();
		}

		public Object getItem(int position) {
			return _videos.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = _inflater.inflate(R.layout.gallery_image, null);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.thumbImage);
				holder.textView = (TextView) convertView
						.findViewById(R.id.text);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();

			}

			holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			holder.imageView.setImageBitmap(_videos.get(position).getBitmap());
			holder.textView.setText(_videos
					.get(position)
					.getName()
					.substring(0,
							_videos.get(position).getName().lastIndexOf(".")));
			return convertView;
		}

	}

	private static class LoadedImage {

		private Bitmap _bitmap;

		private String _sName;

		LoadedImage(Bitmap bitmap, String sName) {
			_bitmap = bitmap;
			_sName = sName;
		}

		public Bitmap getBitmap() {
			return _bitmap;
		}

		public String getName() {
			return _sName;
		}
	}

	static class ViewHolder {
		ImageView imageView;
		TextView textView;
		int id;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		int columnIndex = 0;

		_cursor = getActivity()
				.getApplicationContext()
				.getContentResolver()
				.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
						sColumnsArray, sSelection, null, sOrderBy);

		if (_cursor != null) {
			columnIndex = _cursor
					.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
			_cursor.moveToPosition(position);

			Intent tostart = new Intent(Intent.ACTION_VIEW);
			tostart.setDataAndType(Uri.parse(_cursor.getString(columnIndex)),
					"video/*");
			startActivity(tostart);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.main, menu);
		this._optionsMenu = menu;
		setRefreshActionButtonState(true);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_refresh:
			setRefreshActionButtonState(true);
			scanFolder(_file);
			setupViews();
			loadImages();
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
				if (refreshing)
					refreshItem
							.setActionView(R.layout.actionbar_indeterminate_progress);
				else
					refreshItem.setActionView(null);

			}
		}
	}

	final String[] sColumnsArray = { MediaStore.Video.Media.DATA,
			MediaStore.Video.Media._ID, MediaStore.Video.Thumbnails._ID,
			MediaStore.Video.Media.DISPLAY_NAME };

	final String sOrderBy = MediaStore.Images.Media._ID;

	final String sSelection = MediaStore.Images.Media.DATA + " like'%/test%'";

}
