package com.android.ffmpeg.imageslides.listcompiled;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ffmpeg.R;

public class ListVideoAdapter extends BaseAdapter {
	Context context;
	private List<ListRowItem> _rowItemList;

	public ListVideoAdapter(Context context, List<ListRowItem> items) {
		this.context = context;
		this._rowItemList = items;
	}

	private class ViewHolder {
		ImageView imageView;
		TextView txtTitle;
		TextView txtDesc;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.video_row_item_test, null);
			holder = new ViewHolder();
			holder.txtDesc = (TextView) convertView.findViewById(R.id.desc);
			holder.txtTitle = (TextView) convertView.findViewById(R.id.title);
			holder.imageView = (ImageView) convertView.findViewById(R.id.icon);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		ListRowItem rowItem = (ListRowItem) getItem(position);

		holder.txtDesc.setText(rowItem.getDesc());
		holder.txtTitle.setText(rowItem.getTitle());
		// holder.imageView.setImageResource(rowItem.getImageId());
		holder.imageView.setImageBitmap(rowItem.getImageId());
		return convertView;
	}

	@Override
	public int getCount() {
		return _rowItemList.size();
	}

	@Override
	public Object getItem(int position) {
		return _rowItemList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return _rowItemList.indexOf(getItem(position));
	}
}
