package com.android.ffmpeg.scanner;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

public class MediaScannerBroadcast {

	public static void scan(Context context) {
		final String sPath = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		final File file = new File(sPath, "test/video");
		if (file.exists()) {
			int currentapiVersion = android.os.Build.VERSION.SDK_INT;
			if (currentapiVersion >= android.os.Build.VERSION_CODES.KITKAT) {
				try {
					Runtime.getRuntime().exec(
							"am broadcast -a android.intent.action.MEDIA_MOUNTED -d file://"
									+ Environment.getExternalStorageDirectory()
									+ "test/video");
				} catch (IOException e) {
					e.printStackTrace();
				}
				((Activity) context).sendBroadcast(new Intent(
						Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri
								.parse("file://"
										+ Environment
												.getExternalStorageDirectory()
										+ "test/video")));
			} else {
				((Activity) context).sendBroadcast(new Intent(
						Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
								+ Environment.getExternalStorageDirectory()
								+ "test/video")));
			}
		}
	}

}
