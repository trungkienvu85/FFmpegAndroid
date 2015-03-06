package com.android.ffmpeg.googleplus.ytutils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.Video;

public class UtilsYTPlayListAsync extends AsyncTask<String, Object, Video> {

	private static YouTube _youtube;

	private Context _context;

	private AsyncResponse _delegateAsyncResponse;

	public UtilsYTPlayListAsync(YouTube youtube, Context context) {
		_youtube = youtube;
		_context = context;
	}

	@Override
	protected Video doInBackground(String... nextToken) {

		YouTube.Channels.List channelRequest;
		try {

			channelRequest = _youtube.channels().list("contentDetails");
			channelRequest.setMine(true);
			channelRequest
					.setFields("items/contentDetails,nextPageToken,pageInfo");
			ChannelListResponse channelResult = channelRequest.execute();

			List<Channel> channelsList = channelResult.getItems();

			if (channelsList != null) {

				String sUploadPlaylistId = channelsList.get(0)
						.getContentDetails().getRelatedPlaylists().getUploads();

				ArrayList<PlaylistItem> playlistItemList = new ArrayList<PlaylistItem>();

				YouTube.PlaylistItems.List playlistItemRequest = _youtube
						.playlistItems().list("id,contentDetails,snippet");
				playlistItemRequest.setPlaylistId(sUploadPlaylistId);
				playlistItemRequest
						.setFields("items(contentDetails/videoId,snippet/title,snippet/publishedAt),nextPageToken,pageInfo");

				String sNextToken = "";
				do {
					playlistItemRequest.setPageToken(nextToken[0]);
					PlaylistItemListResponse playlistItemResult = playlistItemRequest
							.execute();
					playlistItemList.addAll(playlistItemResult.getItems());
					Log.i("YouTubePlayList",
							"| Size " + playlistItemList.size()
									+ "| Current Page Token :: " + nextToken[0]
									+ " | ");

					sNextToken = playlistItemResult.getNextPageToken();
					publishProgress(playlistItemList, sNextToken);
					break;

				} while (sNextToken != null);

				setVideoListItems(playlistItemList.size(),
						playlistItemList.iterator());
			}
		} catch (UserRecoverableAuthIOException userRecoverableException) {
			UtilsYTUploadAsync.requestAuth(_context, userRecoverableException);
		} catch (GoogleJsonResponseException e) {
			e.printStackTrace();

		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;

	}

	@Override
	protected void onProgressUpdate(Object... playlistEntries) {

		if (playlistEntries[0] instanceof ArrayList) {
			for (int i = 0; i < ((ArrayList<?>) playlistEntries[0]).size(); i++) {
				Object item = ((ArrayList<?>) playlistEntries[0]).get(i);
				if (item instanceof PlaylistItem) {
					PlaylistItem playListItem = (PlaylistItem) item;
					_delegateAsyncResponse.onPassMyProgress(playListItem
							.getSnippet().getTitle(), playListItem
							.getContentDetails().getVideoId(), playListItem
							.getSnippet().getPublishedAt().toString());

				}
			}
		}
		_delegateAsyncResponse.onPassLastPageToken((String) playlistEntries[1]);

	}

	@Override
	protected final void onPostExecute(Video returnedVideo) {
		super.onPostExecute(returnedVideo);
		_delegateAsyncResponse.onTaskComplete();
	}

	private void setVideoListItems(int size,
			Iterator<PlaylistItem> playlistEntries) {

		System.out
				.println("=============================================================");
		System.out.println("\t\tTotal Videos Uploaded: " + size);
		System.out
				.println("=============================================================\n");

		while (playlistEntries.hasNext()) {
			PlaylistItem playlistItem = playlistEntries.next();
			System.out.println(" video name  = "
					+ playlistItem.getSnippet().getTitle());
			System.out.println(" video id    = "
					+ playlistItem.getContentDetails().getVideoId());
			System.out.println(" upload date = "
					+ playlistItem.getSnippet().getPublishedAt());
			System.out
					.println("\n-------------------------------------------------------------\n");
		}
	}

	public interface AsyncResponse {
		void onPassMyProgress(String sVideoName, String sVideoId,
				String sVideoPublishedAt);

		void onPassLastPageToken(String sLastPageToken);

		void onTaskComplete();
	}

	public void getAsyncResponse(AsyncResponse onMyAsyncResponse) {
		_delegateAsyncResponse = onMyAsyncResponse;
	}

}
