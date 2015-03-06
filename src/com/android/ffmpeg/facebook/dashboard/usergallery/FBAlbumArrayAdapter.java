package com.android.ffmpeg.facebook.dashboard.usergallery;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ffmpeg.R;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class FBAlbumArrayAdapter extends ArrayAdapter<FBAlbum> {

	private Context _context;

	private LayoutInflater _inflater;

	public FBAlbumArrayAdapter(Context context, int textViewResourceId,
			List<FBAlbum> objects) {
		super(context, textViewResourceId, objects);
		_context = context;
		_inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		AlbumHolder holder;

		if (convertView == null) {
			convertView = _inflater.inflate(R.layout.test_view_album_list_item,
					parent, false);
			holder = new AlbumHolder();
			holder.coverPhoto = (ImageView) convertView
					.findViewById(R.id.imageView_cover_photo);
			holder.name = (TextView) convertView
					.findViewById(R.id.textView_name);
			holder.photoCount = (TextView) convertView
					.findViewById(R.id.textView_count);
			convertView.setTag(holder);
		} else {
			holder = (AlbumHolder) convertView.getTag();
		}

		FBAlbum album = getItem(position);
		if (album.getCoverPhoto() != null)
			UrlImageViewHelper.setUrlDrawable(holder.coverPhoto,
					album.getCoverPhoto());

		holder.name.setText(album.getName());

		if (album.getCount() == 1)
			holder.photoCount.setText(album.getCount() + " "
					+ _context.getString(R.string.photo));
		else
			holder.photoCount.setText(album.getCount() + " "
					+ _context.getString(R.string.photos));

		return convertView;
	}

	static class AlbumHolder {
		ImageView coverPhoto;
		TextView name;
		TextView photoCount;
	}
}