package com.android.ffmpeg.googleplus.dashboard.widgets.adapter;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.ffmpeg.R;
import com.android.ffmpeg.ThreadPreconditions;
import com.android.ffmpeg.socialmedia.dashboard.adapters.DashboardBaseListElement;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

public class ListViewAdapter extends BaseAdapter {
	private Context _context;

	private List<DashboardBaseListElement> _dummyItems = Collections
			.emptyList();

	public ListViewAdapter(Context context) {
		_context = context;
	}

	public void updateDummyList(List<DashboardBaseListElement> dummyItems) {
		ThreadPreconditions.checkOnMainThread();
		_dummyItems = dummyItems;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return _dummyItems.size();
	}

	@Override
	public DashboardBaseListElement getItem(int position) {
		return _dummyItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;
		DummyView viewHolder;

		if (convertView == null) {
			LayoutInflater li = (LayoutInflater) _context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = li.inflate(R.layout.dummy_item, null);
			v.setOnClickListener(getItem(position).getOnClickListener());
			viewHolder = new DummyView(v);
			v.setTag(viewHolder);

		} else {
			viewHolder = (DummyView) v.getTag();
		}

		viewHolder.dummyTextView1.setText(getItem(position).getText1());
		viewHolder.dummyTextView2.setText(getItem(position).getText2());
		UrlImageViewHelper.setUrlDrawable(viewHolder.dummyView,
				getItem(position).getIcon());
		return v;
	}

	private static class DummyView {

		public final ImageView dummyView;

		public final TextView dummyTextView1;

		public final TextView dummyTextView2;

		public DummyView(View base) {
			dummyTextView1 = (TextView) base
					.findViewById(R.id.dummy_item_text1);
			dummyTextView2 = (TextView) base
					.findViewById(R.id.dummy_item_text2);
			dummyView = (ImageView) base.findViewById(R.id.dummy_item_image);
		}
	}

}
