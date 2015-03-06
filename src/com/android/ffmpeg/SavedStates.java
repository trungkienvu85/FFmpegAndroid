package com.android.ffmpeg;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.android.ffmpeg.imageslides.properties.PropAudio;
import com.android.ffmpeg.imageslides.properties.PropImage;

public class SavedStates implements Parcelable {

	private ArrayList<PropAudio> _propAudioList;

	private ArrayList<PropImage> _propVideoList;

	public static final int x = 10;

	public ArrayList<PropAudio> getAudioProperties() {
		return _propAudioList;
	}

	public ArrayList<PropImage> getVideoProperties() {
		return _propVideoList;

	}

	protected void setAudioProperties(ArrayList<PropAudio> _propAudio) {
		this._propAudioList = _propAudio;
	}

	protected void setVideoProperties(ArrayList<PropImage> _propVideo) {
		this._propVideoList = _propVideo;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int arg1) {
		out.writeSerializable(_propAudioList);
		out.writeSerializable(_propVideoList);
	}

	public static final Parcelable.Creator<SavedStates> CREATOR = new Parcelable.Creator<SavedStates>() {
		public SavedStates createFromParcel(Parcel in) {
			return new SavedStates();
		}

		public SavedStates[] newArray(int size) {
			return new SavedStates[size];
		}
	};
}
