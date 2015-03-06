package com.android.ffmpeg.imageslides.listcompiled;

import android.graphics.Bitmap;

public class ListRowItem {
	private Bitmap _thumbnailBitmap;
	private String _sTitle;
	private String _sDesc;

	public ListRowItem(Bitmap imageId, String title, String desc) {
		this._thumbnailBitmap = imageId;
		this._sTitle = title;
		this._sDesc = desc;
	}

	public Bitmap getImageId() {
		return _thumbnailBitmap;
	}

	public void setImageId(Bitmap imageId) {
		this._thumbnailBitmap = imageId;
	}

	public String getDesc() {
		return _sDesc;
	}

	public void setDesc(String desc) {
		this._sDesc = desc;
	}

	public String getTitle() {
		return _sTitle;
	}

	public void setTitle(String title) {
		this._sTitle = title;
	}

	@Override
	public String toString() {
		return _sTitle + "\n" + _sDesc;
	}
}