package com.android.ffmpeg.circularimage;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.android.ffmpeg.R;

public class CircularImageView extends ImageView {
	private int _nBorderWidth;

	private int _nCanvasSize;

	private Bitmap _image;

	private Paint _paint;

	private Paint _paintBorder;

	public CircularImageView(final Context context) {
		this(context, null);
	}

	public CircularImageView(Context context, AttributeSet attrs) {
		this(context, attrs, R.attr.circularImageViewStyle);
	}

	public CircularImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// init paint
		_paint = new Paint();
		_paint.setAntiAlias(true);

		_paintBorder = new Paint();
		_paintBorder.setAntiAlias(true);

		// load the styled attributes and set their properties
		TypedArray attributes = context.obtainStyledAttributes(attrs,
				R.styleable.CircularImageView, defStyle, 0);

		if (attributes.getBoolean(R.styleable.CircularImageView_border, true)) {
			int defaultBorderSize = (int) (4 * getContext().getResources()
					.getDisplayMetrics().density + 0.5f);
			setBorderWidth(attributes.getDimensionPixelOffset(
					R.styleable.CircularImageView_border_width,
					defaultBorderSize));
			setBorderColor(attributes.getColor(
					R.styleable.CircularImageView_border_color, Color.WHITE));
		}

		if (attributes.getBoolean(R.styleable.CircularImageView_shadow, false))
			addShadow();
	}

	public void setBorderWidth(int borderWidth) {
		this._nBorderWidth = borderWidth;
		this.requestLayout();
		this.invalidate();
	}

	public void setBorderColor(int borderColor) {
		if (_paintBorder != null)
			_paintBorder.setColor(borderColor);
		this.invalidate();
	}

	public void addShadow() {
		setLayerType(LAYER_TYPE_SOFTWARE, _paintBorder);
		_paintBorder.setShadowLayer(4.0f, 0.0f, 2.0f, Color.BLACK);
	}

	@Override
	public void onDraw(Canvas canvas) {
		// load the bitmap
		_image = drawableToBitmap(getDrawable());

		// init shader
		if (_image != null) {

			_nCanvasSize = canvas.getWidth();
			if (canvas.getHeight() < _nCanvasSize)
				_nCanvasSize = canvas.getHeight();

			BitmapShader shader = new BitmapShader(Bitmap.createScaledBitmap(
					_image, _nCanvasSize, _nCanvasSize, false),
					Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
			_paint.setShader(shader);

			// circleCenter is the x or y of the view's center
			// radius is the radius in pixels of the cirle to be drawn
			// paint contains the shader that will texture the shape
			int circleCenter = (_nCanvasSize - (_nBorderWidth * 2)) / 2;
			canvas.drawCircle(circleCenter + _nBorderWidth, circleCenter
					+ _nBorderWidth, ((_nCanvasSize - (_nBorderWidth * 2)) / 2)
					+ _nBorderWidth - 4.0f, _paintBorder);
			canvas.drawCircle(circleCenter + _nBorderWidth, circleCenter
					+ _nBorderWidth,
					((_nCanvasSize - (_nBorderWidth * 2)) / 2) - 4.0f, _paint);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = measureWidth(widthMeasureSpec);
		int height = measureHeight(heightMeasureSpec);
		setMeasuredDimension(width, height);
	}

	private int measureWidth(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// The parent has determined an exact size for the child.
			result = specSize;
		} else if (specMode == MeasureSpec.AT_MOST) {
			// The child can be as large as it wants up to the specified size.
			result = specSize;
		} else {
			// The parent has not imposed any constraint on the child.
			result = _nCanvasSize;
		}

		return result;
	}

	private int measureHeight(int measureSpecHeight) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpecHeight);
		int specSize = MeasureSpec.getSize(measureSpecHeight);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be
			result = specSize;
		} else if (specMode == MeasureSpec.AT_MOST) {
			// The child can be as large as it wants up to the specified size.
			result = specSize;
		} else {
			// Measure the text (beware: ascent is a negative number)
			result = _nCanvasSize;
		}

		return (result + 2);
	}

	public Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable == null) {
			return null;
		} else if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}
}