package com.android.ffmpeg.imageslides.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.ffmpeg.android.Clip;
import org.ffmpeg.android.ShellUtils;
import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.util.Log;

public class UtilsConcatVideo {

	public Clip concatClips(ArrayList<Clip> inVideoList)
			throws InterruptedException, IOException {

		_oCombinedClip = new Clip(UtilsFfmpeg.FILE_TEMP.getAbsolutePath()
				+ File.separator + "combinedFadedClip.mp4");

		try {
			UtilsFfmpeg.FFMPEG_CONTROLLER.concatAndTrimFilesMP4Stream(
					inVideoList, _oCombinedClip, false, false,
					new ShellUtils.ShellCallback() {

						@Override
						public void shellOut(String shellLine) {
							// Log.i(Constants.CONCAT_TAGS, shellLine);
						}

						@Override
						public void processComplete(int exitValue) {

							if (exitValue < 0) {
								Log.i(Consts.CONCAT_TAGS,
										"concat non-zero exit: " + exitValue);
								_bCompiled = false;
							} else if (new File(_oCombinedClip.path).exists()) {
								Log.i(Consts.CONCAT_TAGS,
										"completed concatination ....... "
												+ exitValue);
								_bCompiled = true;
							}
						}
					});
			if (_bCompiled) {
				_oCombinedClip = processVideo(_oCombinedClip);
				return _oCombinedClip;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Clip processVideo(Clip inProcess) {

		final Clip outProcessed = new Clip(
				UtilsFfmpeg.FILE_TEMP.getAbsolutePath() + File.separator
						+ "video_processed.mp4");
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
								Log.d(Consts.CONCAT_TAGS,
										"Compilation error...	 processVideo failed");
							} else if (new File(outProcessed.path).exists()) {
								Log.d(Consts.CONCAT_TAGS,
										"Success processing concatinated video.... ");
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

	private Clip _oCombinedClip;
	private boolean _bCompiled;
	private boolean _bFiltered;

	double fadeLen = 1;

}
