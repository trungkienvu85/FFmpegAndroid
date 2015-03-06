package com.android.ffmpeg.imageslides.utils;

import java.io.File;

import org.ffmpeg.android.Clip;
import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.util.Log;

public class UtilsVideoFilterTest {
	Clip _outClip;

	public Clip addTranspose(Clip inClip) {

		_outClip = new Clip(UtilsFfmpeg.FILE_TEMP.getAbsolutePath()
				+ File.separator + "videofadeTranspose" + "x" + ".mp4");
		_outClip.videoFilter = Consts.VIDEO_FILTERS
				.get(Consts.MIRROR_EFFECT);
		try {

			UtilsFfmpeg.FFMPEG_CONTROLLER.processVideo(inClip, _outClip, true,
					new ShellCallback() {

						@Override
						public void shellOut(String shellLine) {
							Log.d(Consts.FFMPEG_TAGS + "_TEST",
									"Compilation error...	 addTranspose failed ");
						}

						@Override
						public void processComplete(int exitValue) {
							if (exitValue != 0) {
								Log.d(Consts.FFMPEG_TAGS + "_TEST",
										"Compilation error...	 addTranspose failed ");
							} else if (new File(_outClip.path).exists()) {
								Log.d(Consts.FFMPEG_TAGS + "_TEST",
										"Success addTranspose .."
												+ _outClip.path);

							}
						}

					});
			if (new File(_outClip.path).exists())
				return _outClip;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
