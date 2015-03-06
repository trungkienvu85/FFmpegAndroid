package com.android.ffmpeg.imageslides.properties;

import java.io.Serializable;

public class PropImage implements Serializable {

	private static final long serialVersionUID = 1L;

	private String _sMime;

	private String _sUrl;

	public PropImage(String url, String mimeType) {
		_sUrl = url;
		_sMime = mimeType;
	}

	public PropImage() {

	}

	public String getUrl() {
		return _sUrl;
	}

	public String getMime() {
		return _sMime;
	}

}
