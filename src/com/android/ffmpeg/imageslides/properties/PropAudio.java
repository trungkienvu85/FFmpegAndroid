package com.android.ffmpeg.imageslides.properties;

import java.io.Serializable;

public class PropAudio implements Serializable {

	private static final long serialVersionUID = 1L;

	private String _sName;

	private String _sLength;

	private String _sType;

	private String _sUrl;

	public PropAudio(String name, String length, String type, String url) {
		_sName = name;
		_sLength = length;
		_sType = type;
		_sUrl = url;
	}

	public PropAudio() {

	}

	public String getname() {
		return _sName;
	}

	public String getlength() {
		return _sLength;
	}

	public String gettype() {
		return _sType;
	}

	public String getUrl() {
		return _sUrl;
	}

}
