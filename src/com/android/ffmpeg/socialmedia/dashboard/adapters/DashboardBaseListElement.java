package com.android.ffmpeg.socialmedia.dashboard.adapters;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;

import com.facebook.model.OpenGraphAction;

public abstract class DashboardBaseListElement {

	private String _sThumbnailUrl;

	private String _sVideoID;

	private String _sText1;

	protected String _sText2;

	private BaseAdapter _adapter;

	private int _nRequestCode;

	public DashboardBaseListElement(String sVideoId, String sVideoName,
			String sVideoPublishedAt, int nRequestCode) {
		_sThumbnailUrl = "http://img.youtube.com/vi/" + sVideoId
				+ "/default.jpg";
		_sVideoID = sVideoId;
		_sText1 = sVideoName;
		_sText2 = sVideoPublishedAt;
		_nRequestCode = nRequestCode;
	}

	public void setAdapter(BaseAdapter adapter) {
		this._adapter = adapter;
	}

	public String getVideoID() {
		return _sVideoID;
	}

	public String getIcon() {
		return _sThumbnailUrl;
	}

	public String getText1() {
		return _sText1;
	}

	public String getText2() {
		return _sText2;
	}

	public int getRequestCode() {
		return _nRequestCode;
	}

	public void setText1(String text1) {
		this._sText1 = text1;
		if (_adapter != null) {
			_adapter.notifyDataSetChanged();
		}
	}

	public void setText2(String text2) {
		this._sText2 = text2;
		if (_adapter != null) {
			_adapter.notifyDataSetChanged();
		}

	}

	public void onActivityResult(Intent data) {
	}

	public void onSaveInstanceState(Bundle bundle) {
	}

	public boolean restoreState(Bundle savedState) {
		return false;
	}

	public void notifyDataChanged() {
		_adapter.notifyDataSetChanged();
	}

	public abstract View.OnClickListener getOnClickListener();

	public abstract void populateOGAction(OpenGraphAction action);

}
