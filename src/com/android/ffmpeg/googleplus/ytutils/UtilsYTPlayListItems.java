package com.android.ffmpeg.googleplus.ytutils;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;

import com.android.ffmpeg.R;
import com.android.ffmpeg.googleplus.dashboard.DashBoardGoogleActivity;
import com.android.ffmpeg.googleplus.dashboard.widgets.adapter.ListViewAdapter;
import com.android.ffmpeg.googleplus.dashboard.widgets.listener.EndlessScrollListener;
import com.android.ffmpeg.googleplus.dashboard.widgets.view.YtListViewWithLoadingIndicator;
import com.android.ffmpeg.googleplus.ytutils.UtilsYTPlayListAsync.AsyncResponse;
import com.android.ffmpeg.socialmedia.dashboard.adapters.DashboardBaseListElement;
import com.facebook.model.OpenGraphAction;
import com.google.android.gms.plus.PlusShare;

public class UtilsYTPlayListItems extends Fragment {

	private static int PAGE_COUNT = 1;

	private boolean _bLoadingComplete;

	private ArrayList<DashboardBaseListElement> _listElements;

	private YtListViewWithLoadingIndicator _listView;

	private UtilsYTPlayListAsync _youTubePlayListAsync;

	private ListViewAdapter _listViewAdapter;

	private String _sPlayListLastToken = "";

	public static UtilsYTPlayListItems create() {
		UtilsYTPlayListItems youTubePlayListItems = new UtilsYTPlayListItems();
		return youTubePlayListItems;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().getActionBar().hide();

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		_listElements = new ArrayList<DashboardBaseListElement>();
		_listViewAdapter = new ListViewAdapter(getActivity());

		_listView = (YtListViewWithLoadingIndicator) getView().findViewById(
				R.id.selection_list);
		_listView.setLoadingView(getActivity().getLayoutInflater().inflate(
				R.layout.actionbar_indeterminate_progress, null));
		_listView.setAdapter(_listViewAdapter);
		_listView.setOnScrollListener(new EndlessScrollListener() {

			@Override
			protected boolean hasMoreDataToLoad() {
				if (_sPlayListLastToken != null && _bLoadingComplete)
					return true;
				else
					return false;
			}

			@Override
			protected void loadMoreData(int page) {
				getYouTubePLaylist();
			}

		});
		getYouTubePLaylist();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_youtube_playlist,
				container, false);
		return view;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode >= 0
				&& requestCode < _listElements.size()) {
			_listElements.get(requestCode).onActivityResult(data);
		}
	}

	private void getYouTubePLaylist() {
		_bLoadingComplete = false;
		_listView.showLoadingView();
		_youTubePlayListAsync = new UtilsYTPlayListAsync(
				DashBoardGoogleActivity._youtube, getActivity());
		_youTubePlayListAsync.getAsyncResponse(new AsyncResponse() {
			@Override
			public void onPassMyProgress(String sVideoName, String sVideoId,
					String sVideoPublishedAt) {
				_listElements.add(new YouTubePlayListElement(sVideoName,
						sVideoId, sVideoPublishedAt, _listElements.size()));
				_listViewAdapter.updateDummyList(_listElements);
			}

			@Override
			public void onPassLastPageToken(String sLastPageToken) {
				_sPlayListLastToken = sLastPageToken;
				_listView.hideLoadingView();

			}

			@Override
			public void onTaskComplete() {
				PAGE_COUNT++;
				if (PAGE_COUNT <= 3 && _sPlayListLastToken != null) {
					getYouTubePLaylist();
				} else {
					PAGE_COUNT = 1;
					_bLoadingComplete = true;
				}

			}

		});
		_youTubePlayListAsync.execute(_sPlayListLastToken);

	}

	public class YouTubePlayListElement extends DashboardBaseListElement {

		public YouTubePlayListElement(String sVideoName, String sVideoId,
				String sVideoPublishedAt, int requestCode) {
			super(sVideoId, sVideoName, sVideoPublishedAt, requestCode);
		}

		@Override
		public void onActivityResult(Intent data) {
		}

		@Override
		public OnClickListener getOnClickListener() {
			return new OnClickListener() {
				@Override
				public void onClick(View view) {
					// getYouTubePLaylist();
					Uri selectedImage = Uri.parse(getIcon());
					Intent shareIntent = new PlusShare.Builder(getActivity())
							.addCallToAction(
									"WATCH",
									Uri.parse("https://www.youtube.com/watch?v="
											+ getVideoID()), getVideoID())
							.setContentUrl(
									Uri.parse("https://www.youtube.com/watch?v="
											+ getVideoID()))
							.setContentDeepLinkId(
									getVideoID(),
									getText1(),
									"Upload Android Youtube Api V3 on "
											+ getText2(), selectedImage)
							.setText("Android!").getIntent();
					startActivityForResult(shareIntent, getRequestCode());
				}
			};
		}

		public String getMimeType(String url) {
			String type = null;
			String extension = MimeTypeMap.getFileExtensionFromUrl(url);
			if (extension != null) {
				MimeTypeMap mime = MimeTypeMap.getSingleton();
				type = mime.getMimeTypeFromExtension(extension);
			}
			return type;
		}

		@Override
		public void populateOGAction(OpenGraphAction action) {
		}
	}

	public static final String YT_TAG = "YouTubePlayList";
}
