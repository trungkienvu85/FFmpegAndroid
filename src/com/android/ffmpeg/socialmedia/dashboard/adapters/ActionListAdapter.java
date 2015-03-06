package com.android.ffmpeg.socialmedia.dashboard.adapters;

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

public class ActionListAdapter extends ArrayAdapter<DashboardBaseListElement> {

	private List<DashboardBaseListElement> _listElements;

	private Context _context;

	public ActionListAdapter(Context context, int resourceId,
			List<DashboardBaseListElement> listElements) {
		super(context, resourceId, listElements);
		_context = context;
		_listElements = listElements;

		for (int i = 0; i < listElements.size(); i++) {
			listElements.get(i).setAdapter(this);
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) _context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.fb_listitem, null);
		}

		DashboardBaseListElement listElement = _listElements.get(position);
		if (listElement != null) {
			view.setOnClickListener(listElement.getOnClickListener());
			ImageView icon = (ImageView) view.findViewById(R.id.icon);
			TextView text1 = (TextView) view.findViewById(R.id.text1);
			TextView text2 = (TextView) view.findViewById(R.id.text2);
			if (icon != null) {
				UrlImageViewHelper.setUrlDrawable(icon, listElement.getIcon());
			}
			if (text1 != null) {
				text1.setText(listElement.getText1());
			}
			if (text2 != null) {
				if (listElement.getText2() != null) {
					text2.setVisibility(View.VISIBLE);
					text2.setText(listElement.getText2());
				} else {
					text2.setVisibility(View.GONE);
				}
			}
		}
		return view;
	}
}
