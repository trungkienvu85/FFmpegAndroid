package com.android.ffmpeg.socialmedia.dashboard.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ffmpeg.R;
import com.android.ffmpeg.socialmedia.dashboard.view.ViewFragmentVideoShare.LoadedImage;

public class VideoArrayAdapter extends ArrayAdapter<LoadedImage> {
	private LayoutInflater _inflater;

	private ArrayList<LoadedImage> _videos = new ArrayList<LoadedImage>();

	public VideoArrayAdapter(Context context, int textViewResourceId,
			ArrayList<LoadedImage> objects) {
		super(context, textViewResourceId, objects);
		_videos = objects;
		_inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = _inflater.inflate(R.layout.gallery_image, null);
			holder.imageView = (ImageView) convertView
					.findViewById(R.id.thumbImage);
			holder.textView = (TextView) convertView.findViewById(R.id.text);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		holder.imageView.setImageBitmap(_videos.get(position).getBitmap());
		holder.textView.setText(_videos.get(position).getName());

		return convertView;
	}

	static class ViewHolder {
		ImageView imageView;
		TextView textView;
		int id;
	}
}
