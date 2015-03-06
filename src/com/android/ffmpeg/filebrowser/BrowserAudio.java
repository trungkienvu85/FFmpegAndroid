package com.android.ffmpeg.filebrowser;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ffmpeg.R;
import com.android.ffmpeg.imageslides.properties.PropAudio;

public class BrowserAudio extends Fragment implements OnItemClickListener,
		OnItemLongClickListener {
	private static String ARG_PAGE;

	private int _count;

	private String[] _sAudioNameArray, _sAudioLengthArray;

	private String[] _pathArray;

	private ListView _listView;

	private ImageAdapter _audioAdapter;

	private ArrayList<PropAudio> _propAudioList;

	private Cursor _cursor;

	private OnAudioDataPass _dataPasser;

	public static BrowserAudio create(int page) {
		Bundle args = new Bundle();
		args.putInt(ARG_PAGE, page);
		BrowserAudio fragment = new BrowserAudio();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
				"mp3");
		String[] selectionArgsMp3 = new String[] { mimeType };

		_cursor = getActivity()
				.getApplicationContext()
				.getContentResolver()
				.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						_sColumnArray, selectionMimeType, selectionArgsMp3,
						sOrderBy);

		_count = _cursor.getCount();

		_pathArray = new String[_count];
		_sAudioLengthArray = new String[_count];
		_sAudioNameArray = new String[_count];
		_propAudioList = new ArrayList<PropAudio>();
		_audioAdapter = new ImageAdapter();

		for (int i = 0; i < _count; i++) {
			_cursor.moveToPosition(i);
			int dataColumnIndex = _cursor
					.getColumnIndex(MediaStore.Audio.Media.DATA);
			_sAudioNameArray[i] = _cursor.getString(_cursor
					.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
			_sAudioLengthArray[i] = _cursor.getString(_cursor
					.getColumnIndex(MediaStore.Audio.Media.DURATION));

			_pathArray[i] = _cursor.getString(dataColumnIndex);
		}

		_listView = (ListView) getView().findViewById(R.id.list);
		final Button selectChecked = (Button) getView().findViewById(
				R.id.btn_select_checked);

		_listView.setAdapter(_audioAdapter);
		_listView.setOnItemClickListener(this);
		_listView.setOnItemLongClickListener(this);
		_listView.setItemsCanFocus(false);

		selectChecked.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!_sAudioUrl.equalsIgnoreCase(""))
					passAudioData(_sAudioUrl, _oAudioProperties);
			}
		});

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			_dataPasser = (OnAudioDataPass) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnAudioDataPassListener");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater
				.inflate(R.layout.audio_list_main, container, false);
		return view;
	}

	public class ImageAdapter extends BaseAdapter {

		public ImageAdapter() {
			_inflater = (LayoutInflater) getActivity().getApplicationContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return _count;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			if (convertView == null) {
				convertView = _inflater.inflate(R.layout.audio_row_item, null);
				holder = new ViewHolder(convertView);
				convertView.setTag(R.id.holder, holder);
			} else {
				holder = (ViewHolder) convertView.getTag(R.id.holder);
			}

			holder.caption.setText(_sAudioNameArray[position].substring(0,
					_sAudioNameArray[position].lastIndexOf(".")));

			final ListView lv = (ListView) parent;
			holder.layout.setChecked(lv.isItemChecked(position));
			return convertView;
		}

		private LayoutInflater _inflater;

	}

	private static class ViewHolder {
		public ViewHolder(View root) {
			caption = (TextView) root.findViewById(R.id.itemCaption);
			layout = (BrowserPhotoCheckableLayout) root
					.findViewById(R.id.layout);
		}

		public TextView caption;
		public BrowserPhotoCheckableLayout layout;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

		int columnIndex = _cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

		if (_cursor != null) {
			_cursor.moveToPosition(position);
			_sAudioUrl = _cursor.getString(columnIndex);
			String name = _cursor.getString(_cursor
					.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));

			_oAudioProperties = new PropAudio(name.substring(0,
					name.lastIndexOf(".")), _cursor.getString(_cursor
					.getColumnIndex(MediaStore.Audio.Media.DURATION)),
					_sAudioUrl.substring(_sAudioUrl.lastIndexOf(".") + 1),
					_cursor.getString(_cursor
							.getColumnIndex(MediaStore.Audio.Media.DATA)));

		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View v, int position,
			long id) {
		int columnIndex = _cursor
				.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

		if (_cursor != null) {
			_cursor.moveToPosition(position);
			Toast toast = Toast.makeText(getActivity().getApplicationContext(),
					_cursor.getString(columnIndex), Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
			toast.show();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			File file = new File(_cursor.getString(columnIndex));
			intent.setDataAndType(Uri.fromFile(file), "audio/*");
			startActivity(intent);
		}
		return false;
	}

	public interface OnAudioDataPass {
		public void onAudioDataPass(String data, PropAudio propAudio);

	}

	public void passAudioData(String data, PropAudio propAudio) {
		_dataPasser.onAudioDataPass(data, propAudio);

	}

	final String[] _sColumnArray = { MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.MIME_TYPE,
			MediaStore.Audio.Media.DISPLAY_NAME };

	final String sOrderBy = MediaStore.Audio.Media.DURATION;

	// final String sSelection = MediaStore.Video.Media.DATA + " like'%/test%'"
	// + "=?";

	String selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE
			+ "=? OR " + Audio.Media.DATA + " like ? OR "
			+ MediaStore.Video.Media.DATA + " like'%/test%'" + "=?";

	private String _sAudioUrl = "";

	private PropAudio _oAudioProperties;

}
