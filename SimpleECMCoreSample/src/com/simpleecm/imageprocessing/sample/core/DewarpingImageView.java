package com.simpleecm.imageprocessing.sample.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.simpleecm.imageprocessing.model.SECMDocumentImage;
import com.simpleecm.imageprocessing.utilities.SECMDocumentCaptureUtils;

public class DewarpingImageView extends ImageView {

	private static final int NONE = -1;
	private static final int TOUCH_TOP_LEFT = 0;
	private static final int TOUCH_TOP_RIGHT = 1;
	private static final int TOUCH_BOT_LEFT = 2;
	private static final int TOUCH_BOT_RIGHT = 3;
	private static final float RECT_SIZE = 25.0f;
	private static final float CORNER_RADIUS = 10.0f;
	private static final float ZOOM_RADIUS = 50.0f;
	private static final float ZOOM_START = 80.0f;
	private static final float LINE_WIDTH = 2.0f;
	private static final float ANGLE_STROKE_WIDTH = 7.0f;
	private static final float ZOOM_ANIMATION_FACTOR = 10.0f;	

	int mCurrentTouch = NONE;

	private Bitmap mBitmap;
	private BitmapShader mShader;
	private Paint mZoomPaint;
	private Paint mCornerPaint;
	private Paint mLinePaint;

	private PointF mZoomPos = new PointF(0, 0);

	private PointF mTopLeft;
	private PointF mTopRight;
	private PointF mBottomLeft;
	private PointF mBottomRight;

	private PointF mOrigTopLeft;
	private PointF mOrigTopRight;
	private PointF mOrigBottomLeft;
	private PointF mOrigBottomRight;

	private RectF mTopLeftTouchArea;
	private RectF mTopRightTouchArea;
	private RectF mBottomLeftTouchArea;
	private RectF mBottomRightTouchArea;
	private RectF mRect;
	private float mZoomX;
	private boolean mMoveLeft;
	private boolean mMoveRight;

	private int mSampleSize;
	private DrawResultListener mListener;
	private int mOrigWidth;
	private int mOrigHeight;
	private boolean mRecognized;

	private int mMaxZoomX;

	public DewarpingImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setDrawingCacheEnabled(true);
		mRect = new RectF();
		mZoomX = ZOOM_START;
		mMaxZoomX = 0;
	}

	public void updateTouchArea() {
		mTopLeftTouchArea = new RectF(mTopLeft.x - RECT_SIZE, mTopLeft.y
				- RECT_SIZE, mTopLeft.x + RECT_SIZE, mTopLeft.y + RECT_SIZE);
		mTopRightTouchArea = new RectF(mTopRight.x - RECT_SIZE, mTopRight.y
				- RECT_SIZE, mTopRight.x + RECT_SIZE, mTopRight.y + RECT_SIZE);
		mBottomLeftTouchArea = new RectF(mBottomLeft.x - RECT_SIZE,
				mBottomLeft.y - RECT_SIZE, mBottomLeft.x + RECT_SIZE,
				mBottomLeft.y + RECT_SIZE);
		mBottomRightTouchArea = new RectF(mBottomRight.x - RECT_SIZE,
				mBottomRight.y - RECT_SIZE, mBottomRight.x + RECT_SIZE,
				mBottomRight.y + RECT_SIZE);
	}

	public void init(Bitmap bitmap) {
		if (bitmap != null) {
			mZoomPaint = new Paint();
			mCornerPaint = new Paint();
			mLinePaint = new Paint();

			mCornerPaint.setColor(Color.GREEN);
			mCornerPaint.setStyle(Style.FILL);

			mLinePaint.setStrokeWidth(LINE_WIDTH);
			mLinePaint.setColor(Color.GREEN);
			mLinePaint.setStyle(Paint.Style.STROKE);
			mLinePaint.setStrokeWidth(ANGLE_STROKE_WIDTH);
			mLinePaint.setAntiAlias(true);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		try {
			if ((mListener != null) && (!mRecognized)) {
				mListener.onDrawResult(canvas);
			}
			if (mRecognized) {
				drawCorners(canvas);
				drawLines(canvas);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public void getZoomRect() {
		mRect.left = (int) (SECMDocumentCaptureUtils.dpToPx((int) mZoomX,
				getContext()) - SECMDocumentCaptureUtils.dpToPx(
				(int) ZOOM_RADIUS - 5, getContext()));
		mRect.top = (int) (SECMDocumentCaptureUtils.dpToPx((int) ZOOM_START,
				getContext()) - SECMDocumentCaptureUtils.dpToPx(
				(int) ZOOM_RADIUS - 5, getContext()));
		mRect.right = (int) (SECMDocumentCaptureUtils.dpToPx((int) mZoomX,
				getContext()) + SECMDocumentCaptureUtils.dpToPx(
				(int) ZOOM_RADIUS - 5, getContext()));
		mRect.bottom = (int) (SECMDocumentCaptureUtils.dpToPx((int) ZOOM_START,
				getContext()) + SECMDocumentCaptureUtils.dpToPx(
				(int) ZOOM_RADIUS - 5, getContext()));
	}

	public void drawCorners(Canvas canvas) {
		canvas.drawCircle(mTopLeft.x, mTopLeft.y,
				SECMDocumentCaptureUtils.dpToPx((int) CORNER_RADIUS, getContext()),
				mCornerPaint);
		canvas.drawCircle(mBottomLeft.x, mBottomLeft.y,
				SECMDocumentCaptureUtils.dpToPx((int) CORNER_RADIUS, getContext()),
				mCornerPaint);
		canvas.drawCircle(mTopRight.x, mTopRight.y,
				SECMDocumentCaptureUtils.dpToPx((int) CORNER_RADIUS, getContext()),
				mCornerPaint);
		canvas.drawCircle(mBottomRight.x, mBottomRight.y,
				SECMDocumentCaptureUtils.dpToPx((int) CORNER_RADIUS, getContext()),
				mCornerPaint);
	}

	public void drawLines(Canvas canvas) {
		canvas.drawLine(mTopLeft.x, mTopLeft.y, mBottomLeft.x, mBottomLeft.y,
				mLinePaint);
		canvas.drawLine(mBottomLeft.x, mBottomLeft.y, mTopRight.x, mTopRight.y,
				mLinePaint);
		canvas.drawLine(mTopRight.x, mTopRight.y, mBottomRight.x,
				mBottomRight.y, mLinePaint);
		canvas.drawLine(mBottomRight.x, mBottomRight.y, mTopLeft.x, mTopLeft.y,
				mLinePaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		mZoomPos.x = event.getX();
		mZoomPos.y = event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (mRecognized && (mZoomPos.x > 0) && (mZoomPos.y > 0)
					&& (mZoomPos.x < this.getWidth())
					&& (mZoomPos.y < this.getHeight())) {
				if (mTopLeftTouchArea.contains(mZoomPos.x, mZoomPos.y)) {
					mCurrentTouch = TOUCH_TOP_LEFT;
				} else if (mTopRightTouchArea.contains(mZoomPos.x, mZoomPos.y)) {
					mCurrentTouch = TOUCH_TOP_RIGHT;
				} else if (mBottomLeftTouchArea
						.contains(mZoomPos.x, mZoomPos.y)) {
					mCurrentTouch = TOUCH_BOT_LEFT;
				} else if (mBottomRightTouchArea.contains(mZoomPos.x,
						mZoomPos.y)) {
					mCurrentTouch = TOUCH_BOT_RIGHT;
				} else {
					return false;
				}
			}
			break;

		case MotionEvent.ACTION_MOVE:
			if (mRecognized && (mZoomPos.x > 0) && (mZoomPos.y > 0)
					&& (mZoomPos.x < this.getWidth())
					&& (mZoomPos.y < this.getHeight())) {
				switch (mCurrentTouch) {
				case TOUCH_BOT_LEFT:
					mBottomLeft.x = mZoomPos.x;
					mBottomLeft.y = mZoomPos.y;
					break;
				case TOUCH_TOP_LEFT:
					mTopLeft.x = mZoomPos.x;
					mTopLeft.y = mZoomPos.y;
					break;
				case TOUCH_TOP_RIGHT:
					mTopRight.x = mZoomPos.x;
					mTopRight.y = mZoomPos.y;
					break;
				case TOUCH_BOT_RIGHT:
					mBottomRight.x = mZoomPos.x;
					mBottomRight.y = mZoomPos.y;
					break;
				}
				this.updateTouchArea();
				if (mRect.contains(mZoomPos.x, mZoomPos.y)) {
					if (mZoomX == ZOOM_START) {
						mMoveRight = false;
						mMoveLeft = true;
					}
					if ((mZoomX < mMaxZoomX) && (!mMoveRight)) {
						this.mMoveLeft = true;
						this.mMoveRight = false;
					} else {
						this.mMoveRight = true;
						this.mMoveLeft = false;
					}
				}
				this.invalidate();
			}
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (mRecognized && (mZoomPos.x > 0) && (mZoomPos.y > 0)
					&& (mZoomPos.x < this.getWidth())
					&& (mZoomPos.y < this.getHeight())) {
				updateTouchArea();
				mRect = new RectF();
				mZoomX = ZOOM_START;
				this.mMoveLeft = false;
				this.mMoveRight = false;
			}
			this.invalidate();
			break;

		default:
			break;
		}

		return true;
	}

	public double getAngle(float centreX, float centreY, float pointX,
			float pointY) {
		double angle = 0;
		float dx = pointX - centreX;
		float dy = -(pointY - centreY);
		double inRads = Math.atan2(dy, dx);
		if (inRads < 0) {
			inRads = Math.abs(inRads);
		} else {
			inRads = 2 * Math.PI - inRads;
		}
		angle = Math.toDegrees(inRads);
		return angle;
	}

	public void moveZoomLeft(Canvas canvas) {
		int width = canvas.getWidth();
		width = this.getWidth();
		width = this.getMeasuredWidth();
		mMaxZoomX = (int) ((width / getResources().getDisplayMetrics().density)
				-  ZOOM_START);
		int animatioFactor = SECMDocumentCaptureUtils.dpToPx((int) ZOOM_ANIMATION_FACTOR, getContext());
		if ((mZoomX < mMaxZoomX) && (!mMoveRight)) {
			mZoomX += animatioFactor;
			if (mZoomX > mMaxZoomX) {
				mZoomX = mMaxZoomX;
			}
			invalidate();
		} else {
			if ((mZoomX > ZOOM_START) && (!mMoveLeft)) {
				mZoomX -= animatioFactor;
				if (mZoomX < ZOOM_START) {
					mZoomX = ZOOM_START;
				}
				invalidate();
			}
		}
	}

	public PointF getTopLeft() {
		return mTopLeft;
	}

	public PointF getTopRight() {
		return mBottomRight;
	}

	public PointF getBottomLeft() {
		return mBottomLeft;
	}

	public PointF getBottomRight() {
		return mTopRight;
	}

	public void setTopLeft(PointF point) {
		mOrigTopLeft = new PointF(point.x, point.y);
		mTopLeft = point;
	}

	public void setTopRight(PointF point) {
		mOrigTopRight = new PointF(point.x, point.y);
		mTopRight = point;
	}

	public void setBottomLeft(PointF point) {
		mOrigBottomLeft = new PointF(point.x, point.y);
		mBottomLeft = point;
	}

	public void setBottomRight(PointF point) {
		mOrigBottomRight = new PointF(point.x, point.y);
		mBottomRight = point;
	}

	public int getSampleSize() {
		return mSampleSize;
	}

	public void setSampleSize(int mSampleSize) {
		this.mSampleSize = mSampleSize;
	}

	public void setDrawResultListener(final DrawResultListener listener) {
		mListener = listener;
	}

	public interface DrawResultListener {

		public void onDrawResult(final Canvas canvas);
	}

	public int getOrigWidth() {
		return mOrigWidth;
	}

	public void setOrigWidth(int origWidth) {
		mOrigWidth = origWidth;
	}

	public int getOrigHeight() {
		return mOrigHeight;
	}

	public void setOrigHeight(int origHeight) {
		mOrigHeight = origHeight;
	}

	public void setRecognized(boolean recognized) {
		mRecognized = recognized;
	}

	public void reset() {
		mTopLeft.x = mOrigTopLeft.x;
		mTopLeft.y = mOrigTopLeft.y;

		mTopRight.x = mOrigTopRight.x;
		mTopRight.y = mOrigTopRight.y;

		mBottomLeft.x = mOrigBottomLeft.x;
		mBottomLeft.y = mOrigBottomLeft.y;

		mBottomRight.x = mOrigBottomRight.x;
		mBottomRight.y = mOrigBottomRight.y;

		updateTouchArea();
		invalidate();
	}

	public PointF getOrigTopLeft() {
		return mTopLeft;
	}

	public PointF getOrigTopRight() {
		return mTopRight;
	}

	public PointF getOrigBottomLeft() {
		return mBottomLeft;
	}

	public PointF getOrigBottomRight() {
		return mBottomRight;
	}

	public void drawRealTimeEdges(final SECMDocumentImage image) {
		setDrawResultListener(new DrawResultListener() {

			@Override
			public void onDrawResult(Canvas canvas) {
				setTopLeft(new PointF((image.getTopLeftX() * canvas.getWidth())
						/ getOrigWidth(),
						(image.getTopLeftY() * canvas.getHeight())
								/ getOrigHeight()));
				setBottomLeft(new PointF(
						(image.getBottomLeftX() * canvas.getWidth())
								/ getOrigWidth(),
						((image.getBottomLeftY() * canvas.getHeight())
								/ getOrigHeight())));
				setBottomRight(new PointF(
						((image.getTopRightX() * canvas.getWidth())
								/ getOrigWidth()),
						(image.getTopRightY() * canvas.getHeight())
								/ getOrigHeight()));
				setTopRight(new PointF(
						((image.getBottomRightX() * canvas.getWidth())
								/ getOrigWidth()),
						((image.getBottomRightY() * canvas.getHeight())
								/ getOrigHeight())));
				drawCorners(canvas);
				updateTouchArea();
				setRecognized(true);
				invalidate();
			}

		});
	}
}
