package com.android.ffmpeg.filebrowser;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BrowserSquareImageView extends ImageView {
	public BrowserSquareImageView(Context context) {
		super(context);
	}

	public BrowserSquareImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BrowserSquareImageView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
	}
}
