package com.android.ffmpeg.googleplus.dashboard.widgets.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.android.ffmpeg.googleplus.dashboard.widgets.helper.ListViewWithLoadingIndicatorHelper;
import com.android.ffmpeg.googleplus.dashboard.widgets.interfaces.ListViewWithLoadingIndicator;

public class YtListViewWithLoadingIndicator extends ListView implements
		ListViewWithLoadingIndicator {

	private ListViewWithLoadingIndicatorHelper _listViewWithLoadingIndicatorHelper;

	public YtListViewWithLoadingIndicator(Context context) {
		super(context);
	}

	public YtListViewWithLoadingIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public YtListViewWithLoadingIndicator(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this._listViewWithLoadingIndicatorHelper = new ListViewWithLoadingIndicatorHelper(
				this);
	}

	@Override
	public View getLoadingView() {
		return _listViewWithLoadingIndicatorHelper.getLoadingView();
	}

	@Override
	public void setLoadingView(View loadingView) {
		_listViewWithLoadingIndicatorHelper.setLoadingView(loadingView);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		_listViewWithLoadingIndicatorHelper.beforeSetAdapter(adapter);
		super.setAdapter(adapter);
		_listViewWithLoadingIndicatorHelper.afterSetAdapter(adapter);
	}

	@Override
	public void showLoadingView() {
		_listViewWithLoadingIndicatorHelper.showLoadingView();
	}

	@Override
	public void hideLoadingView() {
		_listViewWithLoadingIndicatorHelper.hideLoadingView();
	}

	@Override
	public boolean isLoadingViewVisible() {
		return _listViewWithLoadingIndicatorHelper.isLoadingViewVisible();
	}
}
