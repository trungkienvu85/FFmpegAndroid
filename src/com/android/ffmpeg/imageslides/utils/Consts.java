package com.android.ffmpeg.imageslides.utils;

import java.util.HashMap;

public class Consts {

	protected final static String FFMPEG_TAGS = "ffmpeg_utils";

	protected final static String CONCAT_TAGS = "concat_video_utils";

	protected final static String FADE_TAGS = "image_fade_utils";

	protected final static String SLIDE_TAGS = "image_slide_utils";

	protected final static String MIRROR_EFFECT = "mirror_effect";

	protected final static String TRANSPOSE_VIDEO = "transpose_effect";

	protected final static String FAST_SLOW_MOTION = "speed_effect";

	protected final static int NINETY_COUNTER_CLOCKWISE_AND_VERTICAL_FLIP = 0;

	protected final static int NINETY_CLOCKWISE = 1;

	protected final static int NINETY_COUNTER_CLOCKWISE = 2;

	protected final static int NINETY_CLOCKWISE_AND_VERTICAL_FLIP = 3;

	protected final static HashMap<String, String> VIDEO_FILTERS = new HashMap<String, String>();

	static {
		VIDEO_FILTERS.put(TRANSPOSE_VIDEO, "transpose=");
		VIDEO_FILTERS.put(FAST_SLOW_MOTION, "setpts=(1/<speed>)*PTS");
		VIDEO_FILTERS
				.put(MIRROR_EFFECT,
						"crop=iw/2:ih:0:0,split[tmp],pad=2*iw[left]; [tmp]hflip[right]; [left][right] overlay=W/2");
	}
}
