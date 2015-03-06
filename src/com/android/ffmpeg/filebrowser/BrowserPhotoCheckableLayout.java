/**
 * 
 */
package com.android.ffmpeg.filebrowser;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.RelativeLayout;

public class BrowserPhotoCheckableLayout extends RelativeLayout implements
		Checkable {

	public static interface OnCheckedChangeListener {
		public void onCheckedChanged(BrowserPhotoCheckableLayout layout,
				boolean isChecked);
	}

	public BrowserPhotoCheckableLayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		initialise(attrs);
	}

	public BrowserPhotoCheckableLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialise(attrs);
	}

	public BrowserPhotoCheckableLayout(Context context, int checkableId) {
		super(context);
		initialise(null);
	}

	public boolean isChecked() {
		return _isChecked;
	}

	public void setChecked(boolean isChecked) {
		this._isChecked = isChecked;
		for (Checkable c : _checkableViews) {
			c.setChecked(isChecked);
		}

		if (_onCheckedChangeListener != null) {
			_onCheckedChangeListener.onCheckedChanged(this, isChecked);
		}
	}

	public void toggle() {
		this._isChecked = !this._isChecked;
		for (Checkable c : _checkableViews) {
			c.toggle();
		}
	}

	public void setOnCheckedChangeListener(
			OnCheckedChangeListener onCheckedChangeListener) {
		this._onCheckedChangeListener = onCheckedChangeListener;
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		final int childCount = this.getChildCount();
		for (int i = 0; i < childCount; ++i) {
			findCheckableChildren(this.getChildAt(i));
		}
	}

	private void initialise(AttributeSet attrs) {
		this._isChecked = false;
		this._checkableViews = new ArrayList<Checkable>(5);
	}

	private void findCheckableChildren(View v) {
		if (v instanceof Checkable) {
			this._checkableViews.add((Checkable) v);
		}

		if (v instanceof ViewGroup) {
			final ViewGroup vg = (ViewGroup) v;
			final int childCount = vg.getChildCount();
			for (int i = 0; i < childCount; ++i) {
				findCheckableChildren(vg.getChildAt(i));
			}
		}
	}

	private boolean _isChecked;
	private List<Checkable> _checkableViews;
	private OnCheckedChangeListener _onCheckedChangeListener;
}
