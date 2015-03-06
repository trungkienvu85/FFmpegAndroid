package com.android.ffmpeg.facebook.dashboard.usergallery;

import java.util.List;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;

import com.android.ffmpeg.R;
import com.android.ffmpeg.facebook.dashboard.FBDashboardImageDownloader;
import com.android.ffmpeg.facebook.dashboard.FBDashboardImageDownloader.ImageLoaderListener;
import com.android.ffmpeg.facebook.dashboard.FBDashboardRequestPhotoAlbums;

public class FBPhotoGalleryDialog extends DialogFragment {

	public static final String PHOTO_ID = "photoId";

	public static final String PHOTO_URL = "photoUrl";

	public static String TAG = FBPhotoGalleryDialog.class.getSimpleName();

	private ListView _albumsList;

	private GridView _photosGrid;

	private List<FBPhoto> _photosList;

	public static final int SELECT_PHOTO = 99;

	private FBDashboardImageDownloader _oFBImageDownloader;

	public static FBPhotoGalleryDialog create() {
		FBPhotoGalleryDialog fragment = new FBPhotoGalleryDialog();
		return fragment;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		_albumsList = (ListView) getView().findViewById(R.id.listView_albums);
		_photosGrid = (GridView) getView().findViewById(R.id.gridView_photos);
		initGalleryView(FBDashboardRequestPhotoAlbums.FB_ALBUMS);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().setTitle("Gallery");

		View view = inflater.inflate(R.layout.test_activity_fb_photo_picker,
				container, false);
		// View v = inflater.inflate(R.layout.YOUR_LAYOUT_ID, null);

		return view;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);
	}

	private void initGalleryView(final List<FBAlbum> resultFbAlbums) {
		_albumsList.setAdapter(new FBAlbumArrayAdapter(getActivity()
				.getApplicationContext(), 0, resultFbAlbums));
		_albumsList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				_photosList = resultFbAlbums.get(position).getPhotos();
				_photosGrid.setAdapter(new FBPhotoArrayAdapter(getActivity()
						.getApplicationContext(), 0, _photosList));
				// TODO: check for API Level
				// before animating
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1)
					_albumsList.animate().x(-_albumsList.getWidth());
				else
					_albumsList.setVisibility(View.GONE);

			}

		});

		_photosGrid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent resultData = new Intent(Intent.ACTION_VIEW);

				// resultData.putExtra(
				// PHOTO_ID,
				// _photosList.get(
				// position)
				// .getId());
				// resultData.putExtra(
				// PHOTO_URL,
				// _photosList.get(
				// position)
				// .getSource());
				// Uri uri =
				// Uri.parse(_photosList
				// .get(position)
				// .getSource());
				// resultData.setDataAndType(uri,
				
				// "image/*");
				// startActivity(resultData);
				_oFBImageDownloader = new FBDashboardImageDownloader(
						_photosList.get(position).getSource(), getActivity(),
						new ImageLoaderListener() {
							@Override
							public void onImageDownloaded(Bitmap bmp) {

							}
						});
				// _oFBImageDownloader.execute();
			}

		});
	}

}
