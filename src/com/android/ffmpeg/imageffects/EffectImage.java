package com.android.ffmpeg.imageffects;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.android.ffmpeg.MainActivity;
import com.android.ffmpeg.R;
import com.android.ffmpeg.imageffects.filters.FiltersImage;
import com.android.ffmpeg.imageffects.view.SlidingUpPanelLayout;
import com.android.ffmpeg.imageffects.view.SlidingUpPanelLayout.PanelSlideListener;
import com.android.ffmpeg.imageffects.view.TouchImageView;
import com.android.ffmpeg.imageffects.view.TouchImageView.OnTouchImageViewListener;

public class EffectImage extends Fragment {

	private static final String TAG = "SlidePanel";

	static final int SELECT_PICTURE = 1;

	private static final String ARG_PAGE = null;

	private Bitmap _originalBitmap, _editBitmap;

	private TouchImageView _imageView;

	private SlidingUpPanelLayout _oSlideUpLayout;

	private EditText _txtEditBox;

	private int _nRed, _nGreen, _nBlue = 0;

	private Paint _paint;

	private SeekBar _valueRedSeekBar, _valueBlueSeekBar,
			_valueGreenSeekBar = null;

	public static EffectImage create(int page) {
		Bundle args = new Bundle();
		args.putInt(ARG_PAGE, page);
		EffectImage fragment = new EffectImage();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(false);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		_imageView = (TouchImageView) getView().findViewById(R.id.img);
		_valueRedSeekBar = (SeekBar) getView().findViewById(R.id.red_seekbar);
		_valueGreenSeekBar = (SeekBar) getView().findViewById(
				R.id.green_seekbar);
		_valueBlueSeekBar = (SeekBar) getView().findViewById(R.id.blue_seekbar);
		_txtEditBox = (EditText) getView().findViewById(R.id.et_txt);

		_paint = new Paint();
		_paint.setStyle(Paint.Style.FILL);
		_paint.setColor(Color.BLUE);
		_paint.setTextSize(25);

		_imageView.setImageResource(R.drawable.android_bck_grd);
		_imageView.setOnTouchImageViewListener(new OnTouchImageViewListener() {

			@Override
			public void onMove() {
				// PointF point = _imageView.getScrollPosition();
				// RectF rect = _imageView.getZoomedRect();
				// float currentZoom = _imageView.getCurrentZoom();
				// boolean isZoomed = _imageView.isZoomed();

			}
		});

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);

		initSeekBarsListeners();
		initButtonListeners();
		initSlidePanel();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_image_effects, container,
				false);
		return v;
	}

	private void initSlidePanel() {

		// setActionBar((Toolbar) findViewById(R.id.main_toolbar));

		_oSlideUpLayout = (SlidingUpPanelLayout) getView().findViewById(
				R.id.sliding_layout);
		_oSlideUpLayout.setPanelSlideListener(new PanelSlideListener() {
			@Override
			public void onPanelSlide(View panel, float slideOffset) {
				Log.i(TAG, "onPanelSlide, offset " + slideOffset);
			}

			@Override
			public void onPanelExpanded(View panel) {
				Log.i(TAG, "onPanelExpanded");
			}

			@Override
			public void onPanelCollapsed(View panel) {
				Log.i(TAG, "onPanelCollapsed");
			}

			@Override
			public void onPanelAnchored(View panel) {
				Log.i(TAG, "onPanelAnchored");
			}

			@Override
			public void onPanelHidden(View panel) {
				Log.i(TAG, "onPanelHidden");
			}
		});

		// TextView t = (TextView) findViewById(R.id.main);
		_imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_oSlideUpLayout.collapsePanel();
			}
		});
		_oSlideUpLayout.setAnchorPoint(0.5f);
		_oSlideUpLayout.expandPanel(0.5f);

	}

	private void initButtonListeners() {

		final Button btnPickImage = (Button) getView().findViewById(
				R.id.btn_pick_image);
		final Button btnClearrAll = (Button) getView().findViewById(
				R.id.btn_clear_all);
		final Button btnSaveImage = (Button) getView().findViewById(
				R.id.btn_save_image);
		final Button btnApplyGrascale = (Button) getView().findViewById(
				R.id.btn_grascale);

		btnClearrAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (_editBitmap != null)
					clearBitmap();
				_txtEditBox.setText("");
				_imageView.setImageResource(R.drawable.android_bck_grd);

			}
		});

		btnPickImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("image/*");
				startActivityForResult(
						Intent.createChooser(intent, "Select Picture"),
						SELECT_PICTURE);

			}
		});

		btnSaveImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (_editBitmap != null)
					saveImage(_editBitmap);
			}
		});
		btnApplyGrascale.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (_editBitmap != null) {
					_editBitmap = FiltersImage
							.applyGreyscaleEffect(_originalBitmap);
					_imageView.setImageBitmap(_editBitmap);
				}
			}
		});

	}

	private void initSeekBarsListeners() {
		final int depth = 50;
		_valueRedSeekBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						_nRed = progress;

					}

					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					public void onStopTrackingTouch(SeekBar seekBar) {
						if (_editBitmap != null) {
							_editBitmap = FiltersImage.applySepiaToningEffect(
									_originalBitmap, depth, _nRed / 10,
									_nGreen / 10, _nBlue / 10);
							_imageView.setImageBitmap(_editBitmap);
						}
					}
				});
		_valueBlueSeekBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						_nBlue = progress;

					}

					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					public void onStopTrackingTouch(SeekBar seekBar) {
						if (_editBitmap != null) {
							_editBitmap = FiltersImage.applySepiaToningEffect(
									_originalBitmap, depth, _nRed / 10,
									_nGreen / 10, _nBlue / 10);
							_imageView.setImageBitmap(_editBitmap);
						}
					}
				});
		_valueGreenSeekBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						_nGreen = progress;

					}

					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					public void onStopTrackingTouch(SeekBar seekBar) {
						if (_editBitmap != null) {

							_editBitmap = FiltersImage.applySepiaToningEffect(
									_originalBitmap, depth, _nRed / 10,
									_nGreen / 10, _nBlue / 10);
							_imageView.setImageBitmap(_editBitmap);

						}
					}
				});
	}

	private void clearBitmap() {
		if (_editBitmap != null) {
			_editBitmap.recycle();
			_editBitmap = null;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		getActivity();
		if (resultCode == MainActivity.RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				clearBitmap();
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };

				Cursor cursor = getActivity().getContentResolver().query(
						selectedImage, filePathColumn, null, null, null);
				cursor.moveToFirst();

				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				String filePath = cursor.getString(columnIndex);
				cursor.close();
				_originalBitmap = BitmapFactory.decodeFile(filePath);
				_editBitmap = _originalBitmap.copy(Bitmap.Config.RGB_565, true);
				_imageView.setImageBitmap(_editBitmap);

			}
		}
	}

	private void saveImage(Bitmap img) {
		String RootDir = Environment.getExternalStorageDirectory()
				+ File.separator + "TextImage";
		File myDir = new File(RootDir);
		myDir.mkdirs();
		Random generator = new Random();
		int n = 10000;
		n = generator.nextInt(n);
		String fname = "Image-" + n + ".jpg";
		File file = new File(myDir, fname);
		if (file.exists())
			file.delete();
		try {
			FileOutputStream out = new FileOutputStream(file);

			img.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		Toast.makeText(getActivity().getApplicationContext(),
				"Image saved to 'TextImage' folder", Toast.LENGTH_LONG).show();
	}

}
