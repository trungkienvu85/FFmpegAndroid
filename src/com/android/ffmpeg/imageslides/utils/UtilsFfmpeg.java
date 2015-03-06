package com.android.ffmpeg.imageslides.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Random;

import org.ffmpeg.android.Clip;
import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.android.ffmpeg.imageslides.properties.PropAudio;
import com.android.ffmpeg.imageslides.properties.PropImage;
import com.android.ffmpeg.scanner.MediaScannerClient;

public class UtilsFfmpeg {

	private FileInputStream _fileInputStream;

	private FileOutputStream _fileOutputStream;

	private boolean _bVideoCompleted;

	private String _sPath;

	private Clip _combinedFadedClip;

	private ArrayList<Clip> _imageClipList;

	private ArrayList<Clip> _videoClipList;

	private ArrayList<Clip> _fadedClipList;

	protected static Context CONTEXT;

	protected static File FILE_TEMP;

	protected static FfmpegController FFMPEG_CONTROLLER = null;

	public UtilsFfmpeg(Context context) {

		CONTEXT = context.getApplicationContext();
		FILE_TEMP = CONTEXT.getCacheDir();

		_imageClipList = new ArrayList<>();
		_videoClipList = new ArrayList<>();
		_fadedClipList = new ArrayList<>();

		_sPath = Environment.getExternalStorageDirectory().getAbsolutePath()
				+ File.separator + "test" + File.separator + "video";
		try {
			FFMPEG_CONTROLLER = new FfmpegController(CONTEXT, FILE_TEMP);
		} catch (IOException e) {
			e.printStackTrace();
		}
		initUtils();
	}

	private void initUtils() {
		_oImageToVideo = new UtilsImageSlide();
		_oImageFadeEffect = new UtilsImageFade();
		_oConcatClipUtils = new UtilsConcatVideo();
		_oAudioUtils = new UtilsAudioWav();
		_oFilterTestUtils = new UtilsVideoFilterTest();
	}

	public boolean startImagetoVideo(ArrayList<PropImage> imagePropList,
			ArrayList<PropAudio> audioPropList, boolean bTrim, int nDuration,
			int nframeDuration) {

		Log.i("TAG", "Frame Duratiom ::" + nframeDuration + " Video ::"
				+ nDuration);

		// Clip inClip = new Clip(FILE_TEMP.getAbsolutePath() + File.separator
		// + "video-" + "xTranspose.mp4");
		//
		// try {
		// copyFile(new File(_sPath, "compiled1600.mp4"),
		// new File(FILE_TEMP.getAbsolutePath() + File.separator
		// + "video-" + "xTranspose.mp4"));
		// inClip = _oFilterTestUtils.addTranspose(inClip);
		// if (inClip != null) {
		// _bVideoCompleted = true;
		// n = generator.nextInt(n);
		// copyFile(new File(inClip.path), new File(_sPath,
		// "/moviexTranspose" + n + ".mp4"));
		//
		// MediaScannerClient.COMPLETE_SCAN = false;
		// new MediaScannerClient(CONTEXT, new File(_sPath,
		// "/moviexTranspose" + n + ".mp4"), null);
		// }
		// } catch (IOException e) {
		// e.printStackTrace();
		// }

		copyClipstoCache(imagePropList);
		if (_combinedFadedClip != null)
			combineAudioandVideo(audioPropList, bTrim, nDuration);

		return _bVideoCompleted;
	}

	private void copyClipstoCache(ArrayList<PropImage> imageProp) {
		_imageClipList.clear();
		for (PropImage image : imageProp) {
			_imageClipList.add(new Clip(FILE_TEMP.getAbsolutePath()
					+ File.separator + "image-" + imageProp.indexOf(image)
					+ "." + image.getMime()));

			try {
				copyFile(new File(image.getUrl()),
						new File(FILE_TEMP.getAbsolutePath() + File.separator
								+ "image-" + imageProp.indexOf(image) + "."
								+ image.getMime()));

			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		convertClipstoVideo(_imageClipList);
	}

	private void convertClipstoVideo(ArrayList<Clip> clipList) {
		_videoClipList.clear();
		for (Clip clip : clipList) {
			_videoClipList.add(_oImageToVideo.convertImageToVideo(
					clipList.indexOf(clip), clip, 5));
		}

		startFadeToClip(_videoClipList);
	}

	private void startFadeToClip(ArrayList<Clip> _videoClipList) {
		_fadedClipList.clear();
		for (Clip inClip : _videoClipList) {
			_fadedClipList.add(_oImageFadeEffect.addFadeEffect(inClip,
					_videoClipList.indexOf(inClip)));
		}
		concatinateClips(_fadedClipList);
	}

	private void concatinateClips(ArrayList<Clip> _videoClipList) {
		n = generator.nextInt(n);
		try {
			_combinedFadedClip = _oConcatClipUtils.concatClips(_videoClipList);

		} catch (InterruptedException | IOException e1) {
			e1.printStackTrace();
		}

	}

	private void combineAudioandVideo(ArrayList<PropAudio> audioPropList,
			boolean bTrim, int nDuration) {

		String audiopath = audioPropList.get(0).getUrl();

		if (bTrim)
			audiopath = _oAudioUtils.trimAudioFile(audioPropList.get(0)
					.getUrl(), nDuration);

		final Clip clipOut = new Clip(FILE_TEMP.getAbsolutePath()
				+ File.separator + "finalmovie" + 984 + ".mp4");

		final Clip audioClip = new Clip(audiopath);

		try {
			FFMPEG_CONTROLLER.combineAudioAndVideo(_combinedFadedClip,
					audioClip, clipOut, new ShellCallback() {

						@Override
						public void shellOut(String shellLine) {
							Log.i(Consts.FFMPEG_TAGS + "WAV", shellLine);
						}

						@Override
						public void processComplete(int exitValue) {
							if (exitValue != 0)
								_bVideoCompleted = false;
							else if (new File(clipOut.path).exists())
								_bVideoCompleted = true;
						}
					});
			if (_bVideoCompleted) {
				n = generator.nextInt(n);
				copyFile(new File(clipOut.path), new File(_sPath, "/movie" + n
						+ ".mp4"));
				MediaScannerClient.COMPLETE_SCAN = false;
				new MediaScannerClient(CONTEXT, new File(_sPath, "/movie" + n
						+ ".mp4"), null);

			} else
				Log.i(Consts.FFMPEG_TAGS, " video making failed.........");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void trimVideo(Clip inClipTrim, int nDuration) {

		// finalClip.videoCodec = "libx264";
		// finalClip.audioCodec = "libmp3lame";
		inClipTrim.duration = nDuration;
		final File fileOut = new File(FILE_TEMP.getAbsoluteFile(), "1"
				+ "-trim.mp4");
		try {
			FFMPEG_CONTROLLER.trim(inClipTrim, true,
					fileOut.getCanonicalPath(), new ShellCallback() {

						@Override
						public void shellOut(String shellLine) {
							Log.i(Consts.FFMPEG_TAGS, shellLine);
						}

						@Override
						public void processComplete(int exitValue) {
							if (exitValue != 0) {
								_bVideoCompleted = false;
								Log.i(Consts.FFMPEG_TAGS,
										" video making failed.........");
							} else if (fileOut.exists()) {
								_bVideoCompleted = true;
								try {
									n = generator.nextInt(n);
									copyFile(fileOut, new File(_sPath, "/movie"
											+ n + ".mp4"));
									MediaScannerClient.COMPLETE_SCAN = false;
									new MediaScannerClient(CONTEXT, new File(
											_sPath, "/movie" + n + ".mp4"),
											null);
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					});
		} catch (Exception e1) {
			e1.printStackTrace();
		}
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

	Random generator = new Random();

	int n = 10000;

	private UtilsImageFade _oImageFadeEffect;
	private UtilsImageSlide _oImageToVideo;
	private UtilsConcatVideo _oConcatClipUtils;
	private UtilsAudioWav _oAudioUtils;
	private UtilsVideoFilterTest _oFilterTestUtils;

}
