package com.android.ffmpeg.scanner;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.android.ffmpeg.imageslides.listcompiled.ListRowItem;

public final class MediaScannerClient implements MediaScannerConnectionClient {

	private String _sFilename;

	private String _sMimetype;

	private MediaScannerConnection _connection;

	private Context _context;

	private Cursor _cursor;

	private int _nFileLength;

	public static int nCOUNT = 1;

	public static boolean COMPLETE_SCAN;

	ArrayList<ListRowItem> _rowItemsTest;

	public MediaScannerClient(Context ctx, File file, String mimetype) {
		this._sFilename = file.getAbsolutePath();
		_context = ctx;
		_connection = new MediaScannerConnection(ctx, this);
		_connection.connect();
	}

	public MediaScannerClient(Context ctx, File file, int fileLength,
			ArrayList<ListRowItem> _rowItemsTest) {
		this._sFilename = file.getAbsolutePath();
		this._rowItemsTest = _rowItemsTest;
		_nFileLength = fileLength;
		_context = ctx;
		_connection = new MediaScannerConnection(ctx, this);
		_connection.connect();
	}

	@Override
	public void onMediaScannerConnected() {
		_connection.scanFile(_sFilename, _sMimetype);
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		_connection.disconnect();
		final String[] parameters = { MediaStore.Video.Media._ID,
				MediaStore.Video.Media.DATA,
				MediaStore.Video.Media.DISPLAY_NAME,
				MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DURATION };
		_cursor = MediaStore.Video.query(_context.getContentResolver(), uri,
				parameters);
		_cursor.moveToFirst();

		Log.i("MediaScannerConnection",
				" data ::"
						+ _cursor.getString(_cursor
								.getColumnIndex(MediaStore.Video.Media.DATA))
						+ " | size :: "
						+ _cursor.getString(_cursor
								.getColumnIndex(MediaStore.Video.Media.SIZE))
						+ " | name :: "
						+ _cursor.getString(_cursor
								.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
						+ " | duration :: "
						+ _cursor.getString(_cursor
								.getColumnIndex(MediaStore.Video.Media.DURATION)));

		if (COMPLETE_SCAN) {
			Bitmap thumb = ThumbnailUtils.createVideoThumbnail(_cursor
					.getString(_cursor
							.getColumnIndex(MediaStore.Video.Media.DATA)),
					MediaStore.Images.Thumbnails.MINI_KIND);
			_rowItemsTest
					.add(new ListRowItem(
							thumb,
							_cursor.getString(_cursor
									.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)),
							"Size ::"
									+ _cursor.getString(_cursor
											.getColumnIndex(MediaStore.Video.Media.SIZE))
									+ " bytes"));
			if (nCOUNT == _nFileLength)
				for (ListRowItem item : _rowItemsTest)
					Log.i("MediaScannerConnection",
							" completed complete scann ...........  "
									+ item.getTitle() + " | " + item.getDesc());

			nCOUNT++;
		}
	}
}