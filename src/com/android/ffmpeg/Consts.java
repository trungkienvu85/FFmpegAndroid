package com.android.ffmpeg;

import android.net.Uri;

public final class Consts {

	public static final String AUDIO_VIDEO_PROPERTIES = "audioVideoProp";

	public static final String PROFILE_FACEBOOK = "isProfileView";

	public static final String TRIM_PROPERTY = "trimVideo";

	public static final String VIDEO_DURATION = "durationVideo";

	public static final String FRAME_DURATION = "frameVideo";

	public static final String[] FACEBOOK_PERMS = new String[] {
			"user_about_me", "user_photos", "email" };

	public static final Uri M_FACEBOOK_URL = Uri.parse("http://m.facebook.com");

	public static final int SELECT_PHOTO = 99;

	public static final String FB_FIELDS_PARAM = "fields";

	public static final String FB_PHOTO_ALBUM_FIELDS = "albums.fields(id,name,photos.fields(id,icon,picture,source,name,height,width),count,cover_photo)";

	public static final int USER_GENERATED_MIN_SIZE = 480;

	public static final String PERMISSION = "publish_actions";

	public static final String PERMISSION_UPLOAD = "video_upload";

	public static final int REAUTH_ACTIVITY_CODE = 100;

	public static final int STATE_DEFAULT = 0;

	public static final int STATE_SIGN_IN = 1;

	public static final int STATE_IN_PROGRESS = 2;

	public static final int RC_SIGN_IN = 0;

	public static final int DIALOG_PLAY_SERVICES_ERROR = 0;

	public static final String SAVED_PROGRESS = "sign_in_progress";

	public static final int FRAGMENT_GOOGLE_FB_COUNT = 2;

	public static final int FRAGMENT_GOOGLE_FB_LOGOUT = 0;

	public static final int FRAGMENT_GOOGLE_FB_ACTIVE = 1;

	public static final int FRAGMENT_GOOGLE_ACTIVE_YTPLAYLIST = 2;

	protected static final String USER_SKIPPED_LOGIN_KEY = "user_skipped_login";

	protected static final int FRAGMENT_COUNT = 5;

	protected static final int LIST_VIDEOS = 0;

	protected static final int SPLASH = 0;

	protected static final int FRAGMENT_PHOTO = 1;

	protected static final int FRAGMENT_VIDEO_STORIES = 2;

	protected static final int FRAGMENT_IMAGE_EFFECT = 3;

	protected static final int FRAGMENT_AUDIO = 4;

	private Consts() {
		throw new AssertionError();
	}
}