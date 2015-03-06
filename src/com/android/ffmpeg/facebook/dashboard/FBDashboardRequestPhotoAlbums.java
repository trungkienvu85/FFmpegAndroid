package com.android.ffmpeg.facebook.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.android.ffmpeg.Consts;
import com.android.ffmpeg.facebook.dashboard.usergallery.FBAlbum;
import com.android.ffmpeg.facebook.dashboard.usergallery.FBPhoto;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Response;
import com.facebook.Session;

public class FBDashboardRequestPhotoAlbums {

	public static List<FBAlbum> FB_ALBUMS;

	protected static void makeRequest(Session session) {
		Bundle params = new Bundle();
		params.putString(Consts.FB_FIELDS_PARAM, Consts.FB_PHOTO_ALBUM_FIELDS);

		FB_ALBUMS = new ArrayList<FBAlbum>();

		new Request(session, "me", params, HttpMethod.GET, new Callback() {

			@Override
			public void onCompleted(Response response) {

				if (response.getGraphObject() != null) {
					JSONObject json = response.getGraphObject()
							.getInnerJSONObject();

					try {
						JSONArray jsonFBAlbums = json.getJSONObject("albums")
								.getJSONArray("data");

						for (int i = 0; i < jsonFBAlbums.length(); i++) {

							JSONObject jsonAlbum = jsonFBAlbums
									.getJSONObject(i);

							if (!jsonAlbum.has("photos"))
								continue;

							FBAlbum fbAlbum = new FBAlbum();
							fbAlbum.setName(jsonAlbum.getString("name"));
							String coverImageId = jsonAlbum
									.getString("cover_photo");
							fbAlbum.setCount(jsonAlbum.getInt("count"));

							JSONArray jsonPhotos = jsonAlbum.getJSONObject(
									"photos").getJSONArray("data");

							for (int j = 0; j < jsonPhotos.length(); j++) {
								JSONObject jsonFBPhoto = jsonPhotos
										.getJSONObject(j);
								FBPhoto fbPhoto = new FBPhoto();
								fbPhoto.setId(jsonFBPhoto.getString("id"));
								fbPhoto.setUrl(jsonFBPhoto.getString("picture"));
								fbPhoto.setSource(jsonFBPhoto
										.getString("source"));
								if (fbPhoto.getId().equals(coverImageId)) {
									fbAlbum.setCoverPhoto(fbPhoto.getUrl());
								}
								fbAlbum.getPhotos().add(fbPhoto);
							}

							FB_ALBUMS.add(fbAlbum);
						}

					} catch (JSONException e) {
					}
				}
			}
		}).executeAsync();

	}

}
