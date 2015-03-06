package com.android.ffmpeg.facebook.dashboard.usergallery;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.android.ffmpeg.R;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class FBPhotoArrayAdapter extends ArrayAdapter<FBPhoto> {
	private LayoutInflater _inflater;

	public FBPhotoArrayAdapter(Context context, int textViewResourceId,
			List<FBPhoto> objects) {
		super(context, textViewResourceId, objects);
		_inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PhotoHolder holder;

		if (convertView == null) {
			convertView = _inflater.inflate(R.layout.test_view_photo_grid_item,
					parent, false);
			holder = new PhotoHolder();
			holder.photo = (ImageView) convertView
					.findViewById(R.id.imageView_photo);
			convertView.setTag(holder);
		} else {
			holder = (PhotoHolder) convertView.getTag();
		}

		FBPhoto photo = getItem(position);
		UrlImageViewHelper.setUrlDrawable(holder.photo, photo.getUrl());

		return convertView;
	}

	static class PhotoHolder {
		ImageView photo;
	}
}