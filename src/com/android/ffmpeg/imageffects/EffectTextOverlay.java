package com.android.ffmpeg.imageffects;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.ffmpeg.MainActivity;
import com.android.ffmpeg.R;
import com.android.ffmpeg.imageffects.filters.FiltersImage;
import com.android.ffmpeg.imageffects.view.SlidingUpPanelLayout;
import com.android.ffmpeg.imageffects.view.SlidingUpPanelLayout.PanelSlideListener;

@SuppressLint("NewApi")
public class EffectTextOverlay extends Fragment {

	private static final String TAG = "SlidePanel";

	static final int SELECT_PICTURE = 1;

	private static final String ARG_PAGE = null;

	private Bitmap _originalBitmap, _editBitmap;

	private ImageView _imageView;
	private Spinner _spinner;

	// private ImageView _imageView;

	private SlidingUpPanelLayout mLayout;

	private EditText _txtBox, _textSize;

	private int _tch = 1;

	private int _mode = 0;

	private int _vRed, _vGreen, _vBlue = 0;

	private Paint _paint;

	private DecimalFormat _df;

	private SeekBar _valueRed, _valueBlue, _valueGreen = null;

	public static EffectTextOverlay create(int page) {
		Bundle args = new Bundle();
		args.putInt(ARG_PAGE, page);
		EffectTextOverlay fragment = new EffectTextOverlay();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(false);
		setMenuVisibility(false);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		_imageView = (ImageView) getView().findViewById(R.id.img);
		_txtBox = (EditText) getView().findViewById(R.id.et_txt);
		_textSize = (EditText) getView().findViewById(R.id.et_txt_size);
		_spinner = (Spinner) getView().findViewById(R.id.sp_color);

		String colors[] = { "Red", "Green", "Blue" };

		ArrayAdapter adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_selectable_list_item, colors);
		_spinner.setAdapter(adapter);

		_paint = new Paint();
		_paint.setStyle(Paint.Style.FILL);
		_paint.setColor(Color.BLUE);
		_paint.setTextSize(25);
		_df = new DecimalFormat("#.##");

		_imageView.setImageResource(R.drawable.android_bck_grd);

		_imageView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				Bitmap bm;

				int color = Color.BLACK;

				float x = event.getRawX();
				float y = event.getRawY();
				x = x - v.getLeft();
				y = y - v.getTop();

				int alpha = 0;
				int size = 25;
				int txtSize = 10;
				String overLayText = _txtBox.getText().toString();
				String sizeText = _textSize.getText().toString();
				if (!sizeText.isEmpty()) {
					txtSize = Integer.parseInt(sizeText);
				}
				String selectColor = _spinner.getSelectedItem().toString();
				if (selectColor.equalsIgnoreCase("red"))
					color = Color.RED;
				if (selectColor.equalsIgnoreCase("green"))
					color = Color.GREEN;
				if (selectColor.equalsIgnoreCase("blue"))
					color = Color.BLUE;

				String xo = String.valueOf(x);
				String yo = String.valueOf(y);

				Log.i("Value of X------>", xo);
				Log.i("Value of Y------>", yo);

				if (_originalBitmap == null) {
					Toast.makeText(getActivity(), "Please Select the Image",
							Toast.LENGTH_LONG).show();
				} else {

					bm = applyWaterMarkEffect(_originalBitmap, overLayText, x,
							y, color, alpha, txtSize, false);
					_editBitmap = bm;
					_imageView.setImageBitmap(_editBitmap);
				}

				return true;
			}
		});

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);

		initButtonListeners();
		initSlidePanel();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_textoverlay, container,
				false);
		return v;
	}

	private void initSlidePanel() {

		// setActionBar((Toolbar) findViewById(R.id.main_toolbar));

		mLayout = (SlidingUpPanelLayout) getView().findViewById(
				R.id.sliding_layout);
		mLayout.setPanelSlideListener(new PanelSlideListener() {
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
				mLayout.collapsePanel();
			}
		});
		mLayout.setAnchorPoint(0.5f);
		mLayout.expandPanel(0.5f);

	}

	private void initButtonListeners() {

		final Button btnPickImage = (Button) getView().findViewById(
				R.id.btn_pick_image);
		final Button btnClearrAll = (Button) getView().findViewById(
				R.id.btn_clear_all);
		final Button btnSaveImage = (Button) getView().findViewById(
				R.id.btn_save_image);

		btnClearrAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (_editBitmap != null)
					clearBitmap();
				_txtBox.setText("");
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

		/*
		 * btnApplyGrascale.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View arg0) { // TODO Auto-generated
		 * method stub if (_editBitmap != null) { _editBitmap = ImageFilters
		 * .applyGreyscaleEffect(_originalBitmap);
		 * _imageView.setImageBitmap(_editBitmap); } } });
		 */

	}

	private void initSeekBarsListeners() {
		final int depth = 50;
		_valueRed.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (_editBitmap != null) {
					_vRed = progress;
					_editBitmap = FiltersImage.applySepiaToningEffect(
							_originalBitmap, depth, _vRed / 10, _vGreen / 10,
							_vBlue / 10);
					_imageView.setImageBitmap(_editBitmap);
				}
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		_valueBlue.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				_vBlue = progress;
				if (_editBitmap != null) {
					_editBitmap = FiltersImage.applySepiaToningEffect(
							_originalBitmap, depth, _vRed / 10, _vGreen / 10,
							_vBlue / 10);
					_imageView.setImageBitmap(_editBitmap);
				}
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
		_valueGreen.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (_editBitmap != null) {

					_vGreen = progress;
					_editBitmap = FiltersImage.applySepiaToningEffect(
							_originalBitmap, depth, _vRed / 10, _vGreen / 10,
							_vBlue / 10);
					_imageView.setImageBitmap(_editBitmap);

				}
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}
		});
	}

	private void clearBitmap() {
		if (_editBitmap != null) {
			_editBitmap.recycle();
			_editBitmap = null;
		}
	}

	private Bitmap createImage(float x0, float y0, float x1, float y1,
			String usertext) {
		if (_editBitmap != null) {
			Canvas canvas = new Canvas(_editBitmap);
			int viewTop = getView().findViewById(Window.ID_ANDROID_CONTENT)
					.getTop();
			_mode = 0;
			if (_mode == 0) {

				getActualCoordinates(x0, y0);

				canvas.drawText("my text" + usertext, x0, y0, _paint);
			} else if (_mode == 1) {
				canvas.drawLine(x0, y0, x1, y1, _paint);
			} else if (_mode == 2) {
				canvas.drawRect(x0, y0, x1, y1, _paint);
			}

			_imageView.setImageBitmap(_editBitmap);
			_tch = 1;
			return _editBitmap;
		} else
			return null;

	}

	private void getImageCoordinates(float x0, float y0) {
		int topParam = _imageView.getPaddingTop();
		int rightParam = _imageView.getPaddingRight();
		int maxTopParam = topParam + _imageView.getMaxHeight();
		int maxRightParam = rightParam + _imageView.getMaxWidth();
		if (x0 > topParam && y0 < maxTopParam) {
			// the x coordinate is in your image... do the same to Y
			// Toast.makeText(getApplicationContext(), "inside ",
			// Toast.LENGTH_LONG).show();
		}

	}

	private void getActualCoordinates(float x0, float y0) {
		Drawable drawable = _imageView.getDrawable();
		Rect imageBounds = drawable.getBounds();

		// original height and width of the bitmap
		int intrinsicHeight = drawable.getIntrinsicHeight();
		int intrinsicWidth = drawable.getIntrinsicWidth();

		// height and width of the visible (scaled) image
		int scaledHeight = imageBounds.height();
		int scaledWidth = imageBounds.width();

		// Find the ratio of the original image to the scaled image
		// Should normally be equal unless a disproportionate scaling
		// (e.g. fitXY) is used.
		float heightRatio = intrinsicHeight / scaledHeight;
		float widthRatio = intrinsicWidth / scaledWidth;

		// do whatever magic to get your touch point
		// MotionEvent event;

		// get the distance from the left and top of the image bounds
		int scaledImageOffsetX = (int) x0 - imageBounds.left;
		int scaledImageOffsetY = (int) y0 - imageBounds.top;

		// scale these distances according to the ratio of your scaling
		// For example, if the original image is 1.5x the size of the scaled
		// image, and your offset is (10, 20), your original image offset
		// values should be (15, 30).
		float originalImageOffsetX = scaledImageOffsetX * widthRatio;
		float originalImageOffsetY = scaledImageOffsetY * heightRatio;
		Toast.makeText(
				getActivity().getApplicationContext(),
				"points :: " + originalImageOffsetX + ","
						+ originalImageOffsetY, Toast.LENGTH_LONG).show();
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
				_imageView.setImageBitmap(_originalBitmap);

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

	public Bitmap applyWaterMarkEffect(Bitmap src, String watermark, float x,
			float y, int color, int alpha, int size, boolean underline) {
		int w = src.getWidth();
		int h = src.getHeight();
		Bitmap result = Bitmap.createBitmap(w, h, src.getConfig());

		Canvas canvas = new Canvas(result);
		canvas.drawBitmap(src, 0, 0, null);

		Paint paint = new Paint();
		paint.setAlpha(alpha);
		paint.setTextSize(size);
		paint.setAntiAlias(true);
		paint.setUnderlineText(underline);
		paint.setColor(color);
		canvas.drawText(watermark, x, y, paint);

		return result;
	}

	/**
	 * 
	 * 
	 * implementation of text over, override interface implementation
	 * 
	 * 
	 */
	// @Override
	// public void onSetTextOver(float x0, float y0, float x1, float y1,
	// String usertext) {
	// // Toast.makeText(getApplicationContext(),
	// // "Text Coordinates :: " + x0 + ", " + y0, Toast.LENGTH_SHORT)
	// // .show();
	// createImage(x0, y0, x1, y1, usertext);
	// }
	//
	// @Override
	// public void onBackPressed() {
	// if (mLayout != null && mLayout.isPanelExpanded()
	// || mLayout.isPanelAnchored()) {
	// mLayout.collapsePanel();
	// } else {
	// super.onBackPressed();
	// }
	// }

}
