package com.android.ffmpeg.facebook.dashboard.usergallery;

import java.util.ArrayList;
import java.util.List;

public class FBAlbum {

	private String _sName;

	private String _sCoverPhoto;

	private int _nCount;

	private List<FBPhoto> _photosList = new ArrayList<FBPhoto>();

	public String getName() {
		return _sName;
	}

	public void setName(String name) {
		this._sName = name;
	}

	public String getCoverPhoto() {
		return _sCoverPhoto;
	}

	public void setCoverPhoto(String coverPhoto) {
		this._sCoverPhoto = coverPhoto;
	}

	public int getCount() {
		return _nCount;
	}

	public void setCount(int count) {
		this._nCount = count;
	}

	public List<FBPhoto> getPhotos() {
		return _photosList;
	}

	public void setPhotos(List<FBPhoto> photos) {
		this._photosList = photos;
	}
}