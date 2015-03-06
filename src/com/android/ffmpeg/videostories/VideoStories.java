package com.android.ffmpeg.videostories;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ffmpeg.MainActivity;
import com.android.ffmpeg.R;
import com.android.ffmpeg.dialogpopups.DialogPopUp;
import com.android.ffmpeg.imageslides.properties.PropAudio;

public class VideoStories extends Fragment implements OnClickListener {

	private static String ARG_PAGE;

	private boolean _bTrim;

	private OnUpdateFields _listener;

	// private Uri _fileUri;

	private int _nframeDuration;

	private Point _positionPoint;

	private Point _positionPointyImage;

	private int[] _nLocationXYArray;

	private TextView _txtVideoLengthView;

	private TextView _audioLengthView;

	private SeekBar _frameDurationSeekBar;

	private TextView _audioNameView;

	private TextView _imageCountView;

	private HashMap<String, String> _valueToSaveMap;

	public Dialog _dialogDurationSeekbar;

	private Button _confirmDialog;

	private boolean _bDialogShowing;

	private ViewTreeObserver _viewTreeObsvr;

	public static VideoStories create(int page) {
		Bundle args = new Bundle();
		args.putInt(ARG_PAGE, page);
		VideoStories fragment = new VideoStories();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = (View) inflater.inflate(R.layout.fragment_write_stories,
				container, false);

		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(false);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			_listener = (OnUpdateFields) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnUpdateFieldsListener");
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		_valueToSaveMap.put(NO_IMAGES, _imageCountView.getText().toString());
		_valueToSaveMap.put(VIDEO_LENGTH, _txtVideoLengthView.getText()
				.toString());
		_valueToSaveMap.put(AUDIO_NAME, _audioNameView.getText().toString());
		_valueToSaveMap
				.put(AUDIO_LENGTH, _audioLengthView.getText().toString());

		outState.putSerializable(VIDEOS_PROPERTY_VALUE,
				(Serializable) _valueToSaveMap);

	}

	@SuppressWarnings("unchecked")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// final Button selectAudio = (Button) getView().findViewById(
		// R.id.btn_select_audio);
		final Button encode = (Button) getView().findViewById(R.id.encode);
		// final ImageView camera = (ImageView) getView()
		// .findViewById(R.id.camera);

		_audioNameView = (TextView) getView().findViewById(R.id.txt_audio_name);
		_imageCountView = (TextView) getView().findViewById(
				R.id.txtv_images_selected);
		_txtVideoLengthView = (TextView) getView().findViewById(
				R.id.text_videolength);
		_audioLengthView = (TextView) getView().findViewById(
				R.id.txt_audio_length);
		_frameDurationSeekBar = (SeekBar) getView().findViewById(
				R.id.edit_frame_duration);
		_nframeDuration = 0;

		if (savedInstanceState != null) {
			_valueToSaveMap = (HashMap<String, String>) savedInstanceState
					.getSerializable(VIDEOS_PROPERTY_VALUE);
			_audioNameView.setText(_valueToSaveMap.get(AUDIO_NAME));
			_audioLengthView.setText(_valueToSaveMap.get(AUDIO_LENGTH));
			_txtVideoLengthView.setText(_valueToSaveMap.get(VIDEO_LENGTH));
			_imageCountView.setText(_valueToSaveMap.get(NO_IMAGES));
		} else {
			_valueToSaveMap = new HashMap<String, String>();

		}

		// selectAudio.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View arg0) {
		// getActivity()
		// .getFragmentManager()
		// .beginTransaction()
		// .addToBackStack(null)
		// .setCustomAnimations(R.animator.slide_right_in,
		// R.animator.slide_left_out,
		// R.animator.slide_left_in,
		// R.animator.slide_right_out)
		// .replace(R.id.listFragment, BrowserAudio.create(0))
		// .commit();
		// }
		// });

		// camera.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// intent.putExtra(MediaStore.EXTRA_OUTPUT, _fileUri);
		// startActivityForResult(intent,
		// CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
		// }
		// });

		if (getAudioUpdateFields() != null) {
			_audioNameView.setText(getAudioUpdateFields().get(0).getname());
			_audioLengthView
					.setText(Integer.toString(Integer
							.parseInt(getAudioUpdateFields().get(0).getlength()) / 1000));
		}
		if (getImageUpdateFields() != 0)
			_imageCountView.setText(Integer.toString(getImageUpdateFields()));

		_frameDurationSeekBar.incrementProgressBy(1);

		if (_imageCountView.getText().toString().equalsIgnoreCase("0")) {
			_frameDurationSeekBar.setMax(60);
			Log.i("duration", "null..............");
		} else {
			_frameDurationSeekBar.setMax(60 / Integer.parseInt(_imageCountView
					.getText().toString()));
		}
		_frameDurationSeekBar
				.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar arg0) {

					}

					@Override
					public void onStartTrackingTouch(SeekBar arg0) {

					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						_nframeDuration = progress;
						_txtVideoLengthView.setText(Integer
								.toString((calculateVideoDuration(Integer
										.parseInt(_imageCountView.getText()
												.toString()), progress))));
					}
				});

		encode.setOnClickListener(this);

		getMeasurePostion();
	}

	private void getMeasurePostion() {

		_viewTreeObsvr = _frameDurationSeekBar.getViewTreeObserver();
		_viewTreeObsvr.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				ViewTreeObserver obs = _frameDurationSeekBar
						.getViewTreeObserver();
				_nLocationXYArray = new int[2];
				_frameDurationSeekBar.getLocationOnScreen(_nLocationXYArray);
				_positionPoint = new Point();
				_positionPoint.x = _nLocationXYArray[0];
				_positionPoint.y = _nLocationXYArray[1];
				obs.removeOnGlobalLayoutListener(this);
			}
		});

		_viewTreeObsvr = _txtVideoLengthView.getViewTreeObserver();
		_viewTreeObsvr.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				ViewTreeObserver obs = _txtVideoLengthView
						.getViewTreeObserver();
				_nLocationXYArray = new int[2];
				_txtVideoLengthView.getLocationOnScreen(_nLocationXYArray);
				_positionPointyImage = new Point();
				_positionPointyImage.x = _nLocationXYArray[0];
				_positionPointyImage.y = _nLocationXYArray[1];
				obs.removeOnGlobalLayoutListener(this);
			}
		});

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
			if (resultCode == MainActivity.RESULT_OK) {
				Toast.makeText(getActivity().getApplicationContext(),
						"Image saved to:\n" + data.getData(), Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	@Override
	public void onClick(View v) {
		if (meetsCondition())
			((MainActivity) ((Activity) getActivity())).onEncode(
					Integer.parseInt(_txtVideoLengthView.getText().toString()),
					_nframeDuration, _bTrim);
	}

	private int calculateVideoDuration(int noOfImage, int frameDuration) {
		int sec = noOfImage * frameDuration;
		if (sec > 60)
			return 1;
		else
			return sec;
	}

	private boolean meetsCondition() {

		if (!_txtVideoLengthView.getText().toString()
				.equalsIgnoreCase(_audioLengthView.getText().toString()))
			_bTrim = true;
		else
			_bTrim = false;

		if (_nframeDuration == 0) {
			DialogPopUp.showPopup(getActivity(), _positionPoint,
					"frame Duration null");
			return false;
		}
		if (Integer.parseInt(_txtVideoLengthView.getText().toString()) < Integer
				.parseInt(_imageCountView.getText().toString()) * 4) {
			DialogPopUp.showPopup(getActivity(), _positionPointyImage,
					"video length less");
			return false;
		}
		return true;
	}

	public interface OnUpdateFields {
		public int passImageUpdateFields();

		public ArrayList<PropAudio> passAudioUpdateFields();
	}

	public int getImageUpdateFields() {
		return _listener.passImageUpdateFields();
	}

	public ArrayList<PropAudio> getAudioUpdateFields() {
		return _listener.passAudioUpdateFields();
	}

	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

	private static final String VIDEOS_PROPERTY_VALUE = "videoProperties";

	private static final String NO_IMAGES = "noOfImages";

	private static final String VIDEO_LENGTH = "videolength";

	private static final String AUDIO_NAME = "audioName";

	private static final String AUDIO_LENGTH = "audioLength";
}
