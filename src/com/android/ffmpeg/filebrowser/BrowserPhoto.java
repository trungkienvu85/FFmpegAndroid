package com.android.ffmpeg.filebrowser;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ffmpeg.R;
import com.android.ffmpeg.imageslides.properties.PropImage;

public class BrowserPhoto extends Fragment implements OnItemClickListener,
		OnClickListener {

	private static String ARG_PAGE;

	private int nSize;

	private int _nScreenWidth;

	// private int _nScreenHeight;

	private GridView _sdCardImagesView;

	private ImageAdapter _imageAdapter;

	private Button _selectImagesButton;

	private ArrayList<String> _imageUrlList;

	private ArrayList<PropImage> _videoPropList;

	private boolean _bClear = false;

	private boolean[] thumbnailsselection;

	private boolean _bSelectionChecked = false;

	private OnPhotoDataPass _dataPasser;

	private LoadImagesFromSDCard _loadImagesFromSDCard;

	public static BrowserPhoto create(int page) {
		Bundle args = new Bundle();
		args.putInt(ARG_PAGE, page);
		BrowserPhoto fragment = new BrowserPhoto();
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

		vibrator = (Vibrator) getActivity().getSystemService(
				Context.VIBRATOR_SERVICE);
		_loadImagesFromSDCard = new LoadImagesFromSDCard();
		setupViews();
		loadImages();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);
		_nScreenWidth = displaymetrics.widthPixels;
		// _nScreenHeight = displaymetrics.heightPixels;
		try {
			_dataPasser = (OnPhotoDataPass) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnVideoDataPassListener");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// final GridView grid = _sdCardImages;
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
		_selectImagesButton = (Button) getView().findViewById(
				R.id.btn_select_image);
		_selectImagesButton.setOnClickListener(this);
		_sdCardImagesView = (GridView) getView().findViewById(R.id.gridview);
		_sdCardImagesView.setNumColumns(_nScreenWidth / 150);
		_sdCardImagesView.setClipToPadding(false);
		_sdCardImagesView.setOnItemClickListener(this);
		_imageAdapter = new ImageAdapter(getActivity().getApplicationContext());
		_sdCardImagesView.setAdapter(_imageAdapter);
	}

	@SuppressWarnings("deprecation")
	private void loadImages() {
		final Object data = getActivity().getLastNonConfigurationInstance();
		if (data == null) {
			_loadImagesFromSDCard.execute();
		} else {
			final LoadedImage[] photos = (LoadedImage[]) data;

			if (photos.length == 0)
				_loadImagesFromSDCard.execute();

			for (LoadedImage photo : photos)
				addImage(photo);
		}

	}

	private void addImage(LoadedImage... value) {
		for (LoadedImage image : value) {
			_imageAdapter.addPhoto(image);
			_imageAdapter.notifyDataSetChanged();
		}
	}

	class LoadImagesFromSDCard extends AsyncTask<Object, LoadedImage, Object> {

		@Override
		protected Object doInBackground(Object... params) {

			if (!isCancelled()) {
				Bitmap bitmap = null;

				String sBitmapName;

				Cursor cursor = getActivity()
						.getApplicationContext()
						.getContentResolver()
						.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								sColumnsArray, sSelection, null, sOrderBy);

				int columnIndex = cursor
						.getColumnIndex(MediaStore.Images.Media._ID);

				nSize = cursor.getCount();
				thumbnailsselection = new boolean[nSize];

				int imageID = 0;
				for (int i = 0; i < nSize; i++) {
					cursor.moveToPosition(i);

					imageID = cursor.getInt(columnIndex);

					bitmap = MediaStore.Images.Thumbnails.getThumbnail(
							getActivity().getApplicationContext()
									.getContentResolver(), imageID,
							MediaStore.Images.Thumbnails.MICRO_KIND, null);
					sBitmapName = cursor
							.getString(cursor
									.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));

					publishProgress(new LoadedImage(bitmap, sBitmapName));

				}
				cursor.close();
			}
			return null;
		}

		@Override
		public void onProgressUpdate(LoadedImage... value) {
			addImage(value);
		}

		@Override
		protected void onPostExecute(Object result) {
			Message message = new Message();
			message.obj = "Stop ..";

			getActivity().runOnUiThread(new Runnable() {
				public void run() {
					setRefreshActionButtonState(false);
					new AlertDialog.Builder(getActivity())
							.setPositiveButton(
									R.string.result_dialog_button_text, null)
							.setTitle(R.string.result_dialog_msg_hint_title)
							.setMessage(
									R.string.result_dialog_msg_hint_createvideo)
							.show();
				}
			});

		}

		@Override
		protected void onCancelled() {
		}
	}

	public void forceCancellTask() {
		_loadImagesFromSDCard.cancel(true);
	}

	class ImageAdapter extends BaseAdapter {

		private ArrayList<LoadedImage> _photos = new ArrayList<LoadedImage>();

		private LayoutInflater _inflater;

		public ImageAdapter(Context context) {
			_inflater = (LayoutInflater) getActivity().getApplicationContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public void addPhoto(LoadedImage photo) {
			_photos.add(photo);
		}

		public int getCount() {
			return _photos.size();
		}

		public Object getItem(int position) {
			return _photos.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			final ViewHolder holder;

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = _inflater.inflate(R.layout.gallery_image, null);
				holder.imageview = (ImageView) convertView
						.findViewById(R.id.thumbImage);
				holder.checkbox = (CheckBox) convertView
						.findViewById(R.id.itemCheckBox);
				holder.textView = (TextView) convertView
						.findViewById(R.id.text);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.checkbox.setId(position);
			holder.imageview.setId(position);
			holder.textView.setId(position);
			if (_bClear) {
				thumbnailsselection = new boolean[nSize];
				holder.checkbox.setVisibility(View.INVISIBLE);
			}

			holder.checkbox.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					CheckBox cb = (CheckBox) v;
					int id = cb.getId();
					if (_bSelectionChecked)
						if (thumbnailsselection[id]) {
							cb.setChecked(false);
							cb.setVisibility(View.INVISIBLE);
							thumbnailsselection[id] = false;
						} else {
							cb.setChecked(true);
							cb.setVisibility(View.VISIBLE);
							thumbnailsselection[id] = true;
						}
				}
			});

			holder.imageview.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					if (_bSelectionChecked) {
						holder.checkbox.setVisibility(View.VISIBLE);
						holder.checkbox.setChecked(true);
						thumbnailsselection[v.getId()] = true;
					} else {

					}
				}
			});

			holder.imageview.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					_bSelectionChecked = true;
					vibrator.vibrate(100);
					holder.checkbox.setVisibility(View.VISIBLE);
					holder.checkbox.setChecked(true);
					return false;
				}
			});

			holder.imageview.setScaleType(ImageView.ScaleType.FIT_CENTER);
			holder.imageview.setImageBitmap(_photos.get(position).getBitmap());
			holder.textView.setText(_photos
					.get(position)
					.getName()
					.substring(0,
							_photos.get(position).getName().lastIndexOf(".")));
			holder.checkbox.setChecked(thumbnailsselection[position]);
			holder.id = position;
			return convertView;
		}
	}

	static class ViewHolder {
		ImageView imageview;
		CheckBox checkbox;
		TextView textView;
		int id;
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

	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		int columnIndex = 0;
		Cursor cursor = getActivity()
				.getApplicationContext()
				.getContentResolver()
				.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						sColumnsArray, sSelection, null, sOrderBy);

		if (cursor != null) {
			columnIndex = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToPosition(position);
			String imagePath = cursor.getString(columnIndex);
			Toast.makeText(getActivity(), "image " + imagePath,
					Toast.LENGTH_SHORT).show();
		}
	}

	Vibrator vibrator;

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.main, menu);
		this._optionsMenu = menu;
		setRefreshActionButtonState(true);
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

	@Override
	public void onClick(View arg0) {
		int columnIndex = 0;

		_bClear = true;
		_imageAdapter.notifyDataSetChanged();
		_videoPropList = new ArrayList<PropImage>();
		_imageUrlList = new ArrayList<String>();

		Cursor cursor = getActivity()
				.getApplicationContext()
				.getContentResolver()
				.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
						sColumnsArray, sSelection, null, sOrderBy);
		for (int position = 0; position < cursor.getCount(); position++) {
			if (thumbnailsselection[position]) {
				columnIndex = cursor
						.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToPosition(position);
				String imagePath = cursor.getString(columnIndex);
				_imageUrlList.add(imagePath);
				int dotposition = imagePath.lastIndexOf(".");
				String format = imagePath.substring(dotposition + 1,
						imagePath.length());

				Log.i("path", format + "| " + imagePath);
				_videoPropList.add(new PropImage(imagePath, format));
			}

		}
		if (!_imageUrlList.isEmpty()) {
			if (_imageUrlList.size() < 6 || _imageUrlList.size() > 12) {
				Toast toast = Toast.makeText(getActivity()
						.getApplicationContext(),
						" min photo :: 6 max photo :: 12", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
						0);
				toast.show();
				passPhotoData(_imageUrlList, _videoPropList);

			} else
				passPhotoData(_imageUrlList, _videoPropList);

		} else {
			Toast toast = Toast.makeText(getActivity().getApplicationContext(),
					"select photo", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
		}
	}

	public interface OnPhotoDataPass {
		public void onPhotoDataPass(ArrayList<String> data,
				ArrayList<PropImage> videoProp);

	}

	public void passPhotoData(ArrayList<String> data,
			ArrayList<PropImage> videoProp) {
		_dataPasser.onPhotoDataPass(data, _videoPropList);
	}

	final String[] sColumnsArray = { MediaStore.Images.Media.DATA,
			MediaStore.Images.Media._ID, MediaStore.Images.Thumbnails._ID,
			MediaStore.Images.Media.DISPLAY_NAME };

	final String sOrderBy = MediaStore.Images.Media._ID;

	final String selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE
			+ "=? OR " + MediaStore.Images.Media.DATA + " like ? OR "
			+ MediaStore.Images.Media.DATA + "=?";

	// final String sSelection = MediaStore.Images.Media.DATA +
	// " like'%/test%'";
	final String sSelection = null;
}
