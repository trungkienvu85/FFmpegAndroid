package com.android.ffmpeg.imageslides.utils;

import java.io.File;
import java.util.ArrayList;

import org.ffmpeg.android.Clip;
import org.ffmpeg.android.ShellUtils.ShellCallback;
import org.ffmpeg.android.filters.FadeVideoFilter;

import android.util.Log;

public class UtilsImageFade {

	private ArrayList<Clip> _fadedClipList;

	private int _nEndFrame;

	private int _nStartFrame;

	private boolean _bCompiled;

	private Clip _outClip;

	public ArrayList<Clip> startFadeEffect(ArrayList<Clip> clipList) {
		_fadedClipList = new ArrayList<>();
		for (Clip clip : clipList) {
			_fadedClipList.add(addFadeEffect(clip, clipList.indexOf(clip)));
		}
		return _fadedClipList;
	}

	public Clip addFadeEffect(Clip inClip, final int index) {

		_nStartFrame = 0;
		_nEndFrame = 30;

		final FadeVideoFilter filter = new FadeVideoFilter("in", _nStartFrame,
				_nEndFrame);

		_outClip = new Clip(UtilsFfmpeg.FILE_TEMP.getAbsolutePath()
				+ File.separator + "videofadein" + index + ".mp4");
		_outClip.videoFilter = filter.getFilterString();

		try {

			UtilsFfmpeg.FFMPEG_CONTROLLER.processVideo(inClip, _outClip, true,
					new ShellCallback() {

						@Override
						public void shellOut(String shellLine) {
						}

						@Override
						public void processComplete(int exitValue) {
							if (exitValue != 0) {
								_bCompiled = false;
								Log.d(Consts.FADE_TAGS,
										"Compilation error...	 Fade failed");
							} else if (new File(_outClip.path).exists()) {
								Log.d(Consts.FADE_TAGS,
										"Success adding fade in effect .."
												+ _outClip.path);
								_bCompiled = true;
							}
						}
					});

			if (_bCompiled) {
				return fadeOutEffect(new Clip(_outClip.path), index);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Clip fadeOutEffect(Clip inFade, int index) {

		_nStartFrame = 120;
		_nEndFrame = 30;

		FadeVideoFilter filter = new FadeVideoFilter("out", _nStartFrame,
				_nEndFrame);

		final Clip outFinalClip = new Clip(
				UtilsFfmpeg.FILE_TEMP.getAbsolutePath() + File.separator
						+ "videofadeout" + index + ".mp4");
		outFinalClip.videoFilter = filter.getFilterString();
		try {
			UtilsFfmpeg.FFMPEG_CONTROLLER.processVideo(inFade, outFinalClip,
					true, new ShellCallback() {

						@Override
						public void shellOut(String shellLine) {
							// Log.d(TAG + "Shell", "" + shellLine);
						}

						@Override
						public void processComplete(int exitValue) {
							if (exitValue != 0)
								Log.d(Consts.FADE_TAGS,
										"Compilation error...	 fadeOutEffect failed");
							else if (new File(outFinalClip.path).exists()) {
								Log.d(Consts.FADE_TAGS,
										"Success adding fade out effect:"
												+ outFinalClip.path);
								_bCompiled = true;
							}
						}
					});

			if (_bCompiled) {
				return outFinalClip;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
