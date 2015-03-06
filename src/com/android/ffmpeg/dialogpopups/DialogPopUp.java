package com.android.ffmpeg.dialogpopups;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.ffmpeg.R;

public class DialogPopUp {

	public static void showPopup(final Activity context, Point p,
			String sMessage) {

		LinearLayout viewGroup = (LinearLayout) context
				.findViewById(R.id.popup);
		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = layoutInflater.inflate(R.layout.popup_layout, viewGroup);

		final PopupWindow popup = new PopupWindow(context);
		popup.setContentView(layout);
		popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		popup.setFocusable(true);

		int OFFSET_X = 30;
		int OFFSET_Y = 30;

		popup.setBackgroundDrawable(new ColorDrawable(
				android.graphics.Color.TRANSPARENT));

		popup.showAtLocation(layout, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y
				+ OFFSET_Y);

		TextView close = (TextView) layout.findViewById(R.id.textView1);
		close.setText(sMessage);
	}

}
