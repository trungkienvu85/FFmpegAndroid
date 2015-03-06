package com.android.ffmpeg.googleplus.dashboard.widgets.helper;

import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.android.ffmpeg.googleplus.dashboard.widgets.interfaces.ListViewWithLoadingIndicator;

/**
 * Created by sachin on 2/6/2015.
 */
public class ListViewWithLoadingIndicatorHelper implements
		ListViewWithLoadingIndicator {

	private ListView _listView;

	private View _loadingView;

	private boolean _loadingViewAttached;

	private View _dummyView;

	public ListViewWithLoadingIndicatorHelper(ListView listView) {
		this._listView = listView;
	}

	public void beforeSetAdapter(ListAdapter listAdapter) {
		_dummyView = new View(_listView.getContext());
		_listView.addFooterView(_dummyView, null, false);
	}

	public void afterSetAdapter(ListAdapter listAdapter) {
		_listView.removeFooterView(_dummyView);
		_dummyView = null;
	}

	@Override
	public void showLoadingView() {
		if (_loadingView != null && !_loadingViewAttached) {
			_listView.addFooterView(_loadingView, null, false);
			_loadingViewAttached = true;
		}

	}

	@Override
	public void hideLoadingView() {
		if (_loadingView != null && _loadingViewAttached) {
			_listView.removeFooterView(_loadingView);
			_loadingViewAttached = false;
		}

	}

	@Override
	public boolean isLoadingViewVisible() {
		return _loadingViewAttached;
	}

	@Override
	public View getLoadingView() {
		return _loadingView;
	}

	@Override
	public void setLoadingView(View loadingView) {
		this._loadingView = loadingView;
	}
}
