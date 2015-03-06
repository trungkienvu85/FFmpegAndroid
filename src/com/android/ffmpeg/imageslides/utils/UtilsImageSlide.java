package com.android.ffmpeg.imageslides.utils;

import java.io.File;
import java.util.ArrayList;

import org.ffmpeg.android.Clip;
import org.ffmpeg.android.ShellUtils;
import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.util.Log;

public class UtilsImageSlide {

	public Clip convertImageToVideo(int index, Clip clipIn, int frameDuration) {

		_inClipList = new ArrayList<Clip>();
		_inClipList.clear();
		_inClipList.add(clipIn);
		_outClip = new Clip(UtilsFfmpeg.FILE_TEMP.getAbsolutePath()
				+ File.separator + "compiled" + index + ".mp4");
		try {
			UtilsFfmpeg.FFMPEG_CONTROLLER.createSlideshowFromImagesAndAudio(
					_inClipList, null, _outClip, frameDuration,
					new ShellUtils.ShellCallback() {
						@Override
						public void shellOut(String shellLine) {
							// Log.d(Constants.SLIDE_TAGS, shellLine);
						}

						@Override
						public void processComplete(int exitValue) {
							if (exitValue != 0)
								Log.d(Consts.SLIDE_TAGS,
										"Compilation error. FFmpeg failed");
							else if (new File(_outClip.path).exists())
								Log.d(Consts.SLIDE_TAGS,
										"Success creating Slide "
												+ _outClip.path);
						}
					});

			if (new File(_outClip.path).exists()) {

				_outClip = processVideo(_outClip, index);

				return _outClip;

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	private Clip processVideo(Clip inProcess, int index) {
		Log.d(Consts.SLIDE_TAGS, "processing .......... " + _outClip.path);

		final Clip outProcessed = new Clip(
				UtilsFfmpeg.FILE_TEMP.getAbsolutePath() + File.separator
						+ "video_inprocess" + index + ".mp4");
		outProcessed.videoFilter = "scale=trunc(iw/2)*2:trunc(ih/2)*2";
		outProcessed.videoFps = "30";

		try {
			UtilsFfmpeg.FFMPEG_CONTROLLER.processVideo(inProcess, outProcessed,
					true, new ShellCallback() {
						@Override
						public void shellOut(String shellLine) {
							// Log.d(TAG + "Shell", "" + shellLine);
						}

						@Override
						public void processComplete(int exitValue) {
							if (exitValue != 0) {
								_bFiltered = false;
								Log.d(Consts.SLIDE_TAGS,
										"Compilation error...	 processVideo failed");
							} else if (new File(outProcessed.path).exists()) {
								Log.d(Consts.SLIDE_TAGS,
										"Success processing video frame & resolution .... "
												+ _outClip.path);
								_bFiltered = true;
							}
						}

					});
			if (_bFiltered)
				return outProcessed;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private ArrayList<Clip> _inClipList;
	private boolean _bFiltered;
	private Clip _outClip;

}
