package com.android.ffmpeg.googleplus.dashboard.widgets.listener;

import com.android.ffmpeg.googleplus.ytutils.UtilsYTPlayListItems;

import android.util.Log;
import android.widget.AbsListView;

public abstract class EndlessScrollListener implements
		AbsListView.OnScrollListener {

	public static final int DEFAULT_VISIBLE_THRESHOLD = 5;

	private int _nVisibleThreshold;

	private int _nCurrentPage;

	private boolean _bLoading;

	public EndlessScrollListener() {
		this(DEFAULT_VISIBLE_THRESHOLD);
	}

	public EndlessScrollListener(int visibleThreshold) {
		_nVisibleThreshold = visibleThreshold;
		_nCurrentPage = 0;
		_bLoading = true;
	}

	@Override
	public void onScrollStateChanged(AbsListView absListView, int i) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// ListView listView = (ListView) view;
		// int nHeaderViewsCount = listView.getHeaderViewsCount();
		// int nFooterViewsCount = listView.getFooterViewsCount();
		// int nLiquidTotalItemCount = totalItemCount - nHeaderViewsCount
		// - nFooterViewsCount;

		// if (_bLoading) {
		// if (nLiquidTotalItemCount >= _nPreviousTotal) {
		// _bLoading = false;
		// _nPreviousTotal = nLiquidTotalItemCount;
		// _nCurrentPage++;
		// }
		// }

		Log.i(UtilsYTPlayListItems.YT_TAG, "firstVisibleItem::"
				+ firstVisibleItem + " | visibleItemCount::" + visibleItemCount
				+ " |VisibleThreshold:: " + _nVisibleThreshold
				+ " |totalItemCount::" + totalItemCount);
		if (hasMoreDataToLoad()
				&& (firstVisibleItem + visibleItemCount) >= totalItemCount) {
			_nCurrentPage++;
			_bLoading = true;
			loadMoreData(_nCurrentPage);
		}

	}

	protected abstract boolean hasMoreDataToLoad();

	protected abstract void loadMoreData(int page);

	public boolean isLoading() {
		return _bLoading;
	}
}
