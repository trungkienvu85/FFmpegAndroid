package com.android.ffmpeg.imageslides.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import net.sourceforge.sox.SoxController;

import org.ffmpeg.android.Clip;
import org.ffmpeg.android.ShellUtils;

import android.util.Log;

public class UtilsAudioWav {

	public String trimWavAudio(String fileOut, int duration) {

		File fileAppRoot = new File("");
		_soxCon = null;
		try {
			_soxCon = new SoxController(UtilsFfmpeg.CONTEXT, fileAppRoot,
					new ShellUtils.ShellCallback() {

						@Override
						public void shellOut(String shellLine) {
							Log.d(Consts.FFMPEG_TAGS, shellLine);

						}

						@Override
						public void processComplete(int exitValue) {
							if (exitValue != 0) {
								System.err.println("sxCon> EXIT=" + exitValue);
								RuntimeException re = new RuntimeException(
										"non-zero exit: " + exitValue);
								re.printStackTrace();
								throw re;
							}
						}
					});

			String wavOutFile;
			try {
				// Log.d(Constants.FFMPEG_TAGS,
				// "Original Length :: " + _soxCon.getLength(fileOut));
				wavOutFile = _soxCon.trimAudio(fileOut, 0, duration);

				if (new File(wavOutFile).exists()) {
					Log.d(Consts.FFMPEG_TAGS,
							"Trim  wav sucess | new length :: "
									+ _soxCon.getLength(wavOutFile));
					return wavOutFile;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;

	}

	public String trimAudioFile(String sAudioUrl, int nDuration) {

		int mp3Idx = 0;

		Clip mediaIn = new Clip(UtilsFfmpeg.FILE_TEMP.getAbsolutePath()
				+ "/audio" + ".mp3");
		mediaIn.duration = nDuration;
		try {
			copyFile(new File(sAudioUrl),
					new File(UtilsFfmpeg.FILE_TEMP.getAbsolutePath(), "/audio"
							+ ".mp3"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			Clip audioOut = UtilsFfmpeg.FFMPEG_CONTROLLER.trimMp3(mediaIn,
					new File(UtilsFfmpeg.FILE_TEMP, mp3Idx + ".mp3")
							.getCanonicalPath(),
					new ShellUtils.ShellCallback() {

						@Override
						public void shellOut(String shellLine) {
							Log.i(Consts.FFMPEG_TAGS, shellLine);
						}

						@Override
						public void processComplete(int exitValue) {
							if (exitValue != 0) {

							}
						}

					});
			if (new File(UtilsFfmpeg.FILE_TEMP, mp3Idx + ".mp3").exists()) {
				Log.d(Consts.FFMPEG_TAGS, "Convert to wav sucess |");

				return audioOut.path;
			} else
				Log.d(Consts.FFMPEG_TAGS, "Convert to wav failed |");

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;

	}

	private void copyFile(File src, File dst) throws IOException {
		_fileInputStream = new FileInputStream(src);
		_fileOutputStream = new FileOutputStream(dst);

		FileChannel inChannel = _fileInputStream.getChannel();
		FileChannel outChannel = _fileOutputStream.getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

	private FileInputStream _fileInputStream;
	private FileOutputStream _fileOutputStream;
	private SoxController _soxCon;

}
