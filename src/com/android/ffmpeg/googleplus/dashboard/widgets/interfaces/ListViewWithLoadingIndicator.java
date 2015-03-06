package com.android.ffmpeg.googleplus.dashboard.widgets.interfaces;

import android.view.View;

public interface ListViewWithLoadingIndicator {
	void showLoadingView();

	void hideLoadingView();

	boolean isLoadingViewVisible();

	View getLoadingView();

	void setLoadingView(View loadingView);
}
