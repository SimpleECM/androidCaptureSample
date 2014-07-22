package com.simpleecm.imageprocessing.sample.core;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.simpleecm.imageprocessing.context.SECMContext;
import com.simpleecm.imageprocessing.core.SECMImage;
import com.simpleecm.imageprocessing.core.SECMImage.SECMImageRotation;
import com.simpleecm.imageprocessing.core.SECMQuadrangle;
import com.simpleecm.imageprocessing.operation.SECMBrightnessAndContrastOperation;
import com.simpleecm.imageprocessing.operation.SECMConvertToBlackAndWhiteOperation;
import com.simpleecm.imageprocessing.operation.SECMConvertToGrayscaleOperation;
import com.simpleecm.imageprocessing.operation.SECMDewarpOperation;
import com.simpleecm.imageprocessing.operation.SECMImageOperation;
import com.simpleecm.imageprocessing.operation.SECMImageOperationManager.OnImageOperationListener;
import com.simpleecm.imageprocessing.operation.SECMRotationOperation;

public class MainActivity extends Activity implements OnClickListener,
		OnCheckedChangeListener, OnSeekBarChangeListener {

	private static final String TAG = "Simple ECM Core Sample";
	private static final int REQUEST_CODE_CAMERA = 1;
	private static final int REQUEST_CODE_GALLERY = 2;
	private static final String KEY_PATH = "file_path";
	private static final int FILTER_NORMAL = 0;
	private static final int FILTER_BLACK_WHITE = 1;
	private static final int FILTER_GRAYSCALE = 2;

	private int mImageFilter = FILTER_NORMAL;
	private File mPhotoFile;
	private Button btnRotateLeft, btnRotateRight, btnNormal, btnBlackWhite,
			btnGrayScale;
	private ToggleButton btnBrightnessContrast, btnRotateAngle, btnDewarp;
	private ImageButton btnInfo;
	private DewarpingImageView imgPhoto;
	private LinearLayout brightnessContrastSeekBarsContainer,
			angleSeekBarContainer;
	private SeekBar seekBrigthness, seekContrast, seekAngle;
	private TextView txtAngle;
	private View parentContainer, buttonBar;
	private SECMContext mSecmContext;
	private float mBrightness = 0, mContrast = 1;
	private boolean isDewarpingEnabled = false;
	private int mFixedAngleValue, mAngleValue;
	private SECMQuadrangle mQuadrangle;
	private boolean isCropped = false;
	private SECMImageRotation mImageRotation = SECMImageRotation.SECM_IMAGE_ROTATION_0_DEGREES;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initViews();
		SECMContext.createInstance();
		mSecmContext = SECMContext.getInstance();
		if (savedInstanceState != null) {
			String path = savedInstanceState.getString(KEY_PATH);
			if (path != null && !path.isEmpty()) {
				mPhotoFile = new File(path);
				// Add OnGlobalLayoutListener for getting the ImageView
				// dimensions when it is ready
				imgPhoto.getViewTreeObserver().addOnGlobalLayoutListener(
						new OnGlobalLayoutListener() {

							@SuppressWarnings("deprecation")
							@SuppressLint("NewApi")
							@Override
							public void onGlobalLayout() {
								if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
									imgPhoto.getViewTreeObserver()
											.removeOnGlobalLayoutListener(this);
								} else {
									imgPhoto.getViewTreeObserver()
											.removeGlobalOnLayoutListener(this);
								}
								displayImage();
							}
						});
			}
		}

	}

	private void initViews() {
		imgPhoto = (DewarpingImageView) findViewById(R.id.imgPicture);
		btnRotateLeft = (Button) findViewById(R.id.btnRotateLeft);
		btnRotateRight = (Button) findViewById(R.id.btnRotateRight);
		btnNormal = (Button) findViewById(R.id.btnNormal);
		btnBlackWhite = (Button) findViewById(R.id.btnBlackWhite);
		btnGrayScale = (Button) findViewById(R.id.btnGrayScale);
		btnBrightnessContrast = (ToggleButton) findViewById(R.id.btnBrightnessContrast);
		btnRotateAngle = (ToggleButton) findViewById(R.id.btnRotateAngle);
		btnDewarp = (ToggleButton) findViewById(R.id.btnDewarp);
		btnInfo = (ImageButton) findViewById(R.id.btnInfo);
		brightnessContrastSeekBarsContainer = (LinearLayout) findViewById(R.id.brightnessContrastSeekBars);
		angleSeekBarContainer = (LinearLayout) findViewById(R.id.rotateAngleSeekBars);
		seekBrigthness = (SeekBar) findViewById(R.id.seekBrightness);
		seekContrast = (SeekBar) findViewById(R.id.seekContrast);
		seekAngle = (SeekBar) findViewById(R.id.seekAngle);
		txtAngle = (TextView) findViewById(R.id.txtAngle);
		buttonBar = findViewById(R.id.buttonBar);
		parentContainer = findViewById(android.R.id.content);

		btnRotateLeft.setOnClickListener(this);
		btnRotateRight.setOnClickListener(this);
		btnNormal.setOnClickListener(this);
		btnBlackWhite.setOnClickListener(this);
		btnGrayScale.setOnClickListener(this);
		btnBrightnessContrast.setOnCheckedChangeListener(this);
		btnRotateAngle.setOnCheckedChangeListener(this);
		btnDewarp.setOnCheckedChangeListener(this);
		seekBrigthness.setOnSeekBarChangeListener(this);
		seekContrast.setOnSeekBarChangeListener(this);
		seekAngle.setOnSeekBarChangeListener(this);
		btnInfo.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// If dewarping is enabled, inflate the crop menu. Otherwise, inflate
		// the main one.
		if (isDewarpingEnabled) {
			getMenuInflater().inflate(R.menu.menu_crop, menu);
		} else {
			getMenuInflater().inflate(R.menu.menu_main, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemTakePhoto:
			openCamera();
			break;
		case R.id.menuItePickPhoto:
			openGallery();
			break;
		case R.id.menuItemReset:
			reset();
			break;
		case R.id.menuItemCropPhoto:
			crop();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mPhotoFile != null) {
			outState.putString(KEY_PATH, mPhotoFile.getAbsolutePath());
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnRotateLeft:
			rotateLeft();
			break;
		case R.id.btnRotateRight:
			rotateRight();
			break;
		case R.id.btnNormal:
			convertToNormal();
			break;
		case R.id.btnBlackWhite:
			convertToBlackAndWhite();
			break;
		case R.id.btnGrayScale:
			convertToGrayScale();
			break;
		case R.id.btnInfo:
			showInfoDialog();
			break;

		default:
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.btnBrightnessContrast:
			btnRotateAngle.setChecked(false);
			showOrHideView(angleSeekBarContainer, false);
			showOrHideView(brightnessContrastSeekBarsContainer, isChecked);
			break;
		case R.id.btnRotateAngle:
			btnBrightnessContrast.setChecked(false);
			showOrHideView(brightnessContrastSeekBarsContainer, false);
			showOrHideView(angleSeekBarContainer, isChecked);
			break;
		case R.id.btnDewarp:
			isDewarpingEnabled = isChecked;
			setDewarpingEnable(isChecked);
			invalidateOptionsMenu();
			break;

		default:
			break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		switch (seekBar.getId()) {
		case R.id.seekAngle:
			StringBuilder builder = new StringBuilder();
			builder.append(progress);
			builder.append("°");
			txtAngle.setText(builder.toString());
			break;

		default:
			break;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// Do nothing
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		switch (seekBar.getId()) {
		case R.id.seekBrightness:
			mBrightness = (seekBar.getProgress() / 50.0f) - 1;
			adjustBrightnessContrast();
			break;
		case R.id.seekContrast:
			mContrast = (seekBar.getProgress() / 50.0f);
			adjustBrightnessContrast();
			break;
		case R.id.seekAngle:
			rotateAngle(seekBar.getProgress());
			break;

		default:
			break;
		}

	}

	private void setDewarpingEnable(boolean isEnabled) {
		// Enable/disable the cropping points
		loadOrigEdges();
		imgPhoto.setRecognized(isEnabled);
		imgPhoto.reset();
	}

	private void showOrHideView(View view, boolean show) {
		if (show) {
			view.setVisibility(View.VISIBLE);
		} else {
			view.setVisibility(View.GONE);
		}
	}

	private void openCamera() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// Ensure that there's a camera activity to handle the intent
		if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
			try {
				mPhotoFile = createImageFile(".tmp");
			} catch (IOException e) {
				e.printStackTrace();
			}
			// Continue only if the File was successfully created
			if (mPhotoFile != null) {
				takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
						Uri.fromFile(mPhotoFile));
				startActivityForResult(takePictureIntent, REQUEST_CODE_CAMERA);
			}
		}
	}

	public void openGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, REQUEST_CODE_GALLERY);
	}

	// Creates a file where the picture taken will be placed.
	private File createImageFile(String fileExtension) throws IOException {
		File storageDir = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
				"MyImages");

		if (!storageDir.exists()) {
			if (!storageDir.mkdir()) {
				Log.d(TAG, "was not able to create it");
			}
		}
		if (!storageDir.isDirectory()) {
			Log.d(TAG, "Don't think there is a dir there.");
		}

		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.getDefault()).format(new Date());
		String imageFileName = "TEMP_" + timeStamp + "_image";

		File image = File.createTempFile(imageFileName, fileExtension,
				storageDir);

		return image;
	}

	// Return the path for the given Uri
	public String getPath(Uri uri) {
		String result = "";
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(uri, projection, null, null,
				null);
		if (cursor.moveToFirst()) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			result = cursor.getString(column_index);
		}
		return result;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			// When returns from the Gallery
			if (requestCode == REQUEST_CODE_GALLERY) {
				Uri selectedImageUri = data.getData();
				String path = getPath(selectedImageUri);
				mPhotoFile = new File(path);
			}
			// When returns from the Gallery or Camera
			if (requestCode == REQUEST_CODE_GALLERY
					|| requestCode == REQUEST_CODE_CAMERA) {
				displayImage();
			}
		}
	}

	private void displayImage() {
		int height = parentContainer.getHeight() - getActionBar().getHeight()
				- buttonBar.getHeight();
		int width = parentContainer.getWidth();
		Bitmap bitmap = ImageUtil.getFixedBitmap(width, height, mPhotoFile);
		bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
		if (bitmap != null) {
			mSecmContext.loadSourceImage(bitmap);
			mSecmContext.setBitmap(bitmap);
			// Set the bitmap to show on the view.
			imgPhoto.setImageBitmap(bitmap);
			// Add listener to notify when ImageView has change its dimensions
			imgPhoto.getViewTreeObserver().addOnGlobalLayoutListener(
					new OnGlobalLayoutListener() {

						@SuppressWarnings("deprecation")
						@SuppressLint("NewApi")
						@Override
						public void onGlobalLayout() {
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
								imgPhoto.getViewTreeObserver()
										.removeOnGlobalLayoutListener(this);
							} else {
								imgPhoto.getViewTreeObserver()
										.removeGlobalOnLayoutListener(this);
							}
							imgPhoto.init(mSecmContext.getBitmap());
							loadOrigEdges();
						}
					});
			// Enable the buttons to perform image operations
			btnRotateLeft.setEnabled(true);
			btnRotateRight.setEnabled(true);
			btnRotateAngle.setEnabled(true);
			btnNormal.setEnabled(true);
			btnGrayScale.setEnabled(true);
			btnBlackWhite.setEnabled(true);
			btnBrightnessContrast.setEnabled(true);
			btnDewarp.setEnabled(true);
			// Load initial edges to crop the image
		} else {
			// Disable everything if bitmap is null
			imgPhoto.setImageDrawable(null);
			btnRotateLeft.setEnabled(false);
			btnRotateRight.setEnabled(false);
			btnRotateAngle.setEnabled(false);
			btnNormal.setEnabled(false);
			btnGrayScale.setEnabled(false);
			btnBlackWhite.setEnabled(false);
			btnBrightnessContrast.setEnabled(false);
			btnDewarp.setEnabled(false);
			setDewarpingEnable(false);
		}
		// Set unchecked as initial state for toggle buttons
		btnBrightnessContrast.setChecked(false);
		btnRotateAngle.setChecked(false);
		btnDewarp.setChecked(false);
	}

	private void loadOrigEdges() {
		PointF topLeft = new PointF(30, 30);
		PointF topRight = new PointF(imgPhoto.getWidth() - 30,
				imgPhoto.getHeight() - 30);
		PointF bottomLeft = new PointF(30, imgPhoto.getHeight() - 30);
		PointF bottomRigth = new PointF(imgPhoto.getWidth() - 30, 30);

		imgPhoto.setTopLeft(topLeft);
		imgPhoto.setTopRight(topRight);
		imgPhoto.setBottomLeft(bottomLeft);
		imgPhoto.setBottomRight(bottomRigth);
		imgPhoto.setRecognized(false);
		imgPhoto.reset();
	}

	private void showInfoDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = LayoutInflater.from(this);
		builder.setTitle(R.string.infoDialogTitle);
		builder.setView(inflater.inflate(R.layout.dialog_info, null));
		builder.show();
	}

	// -----------------------------------
	// IMAGE OPERATIONS
	// -----------------------------------

	private void executeOperations() {
		mImageRotation = getImageRotation();
		mSecmContext.loadSourceImage(mSecmContext.getSource().getBitmap());

		SECMImageOperation rotateOperation = new SECMRotationOperation(
				mSecmContext.getResult(), mImageRotation);
		SECMRotationOperation rotationOperation = new SECMRotationOperation(
				mSecmContext.getResult(), mAngleValue);
		SECMImageOperation brigthnessContrastOperation = new SECMBrightnessAndContrastOperation(
				mSecmContext.getResult(), mBrightness, mContrast);
		SECMImageOperation filterOperation = null;
		OnImageOperationListener listener = new OnImageOperationListener() {

			@Override
			public void onFinished() {
				changeImage(mSecmContext.getResult());
			}
		};
		switch (mImageFilter) {
		case FILTER_BLACK_WHITE:
			filterOperation = new SECMConvertToBlackAndWhiteOperation(
					mSecmContext.getResult());
			break;
		case FILTER_GRAYSCALE:
			filterOperation = new SECMConvertToGrayscaleOperation(
					mSecmContext.getResult());
			break;

		default:
			break;
		}
		if (isCropped) {
			SECMDewarpOperation dewarpOperation = new SECMDewarpOperation(
					mSecmContext.getResult(), mQuadrangle);
			if (filterOperation == null) {
				mSecmContext.applyOperations(listener, dewarpOperation,
						rotationOperation, rotateOperation,
						brigthnessContrastOperation);
			} else {
				mSecmContext.applyOperations(listener, dewarpOperation,
						rotationOperation, filterOperation, rotateOperation,
						brigthnessContrastOperation);
			}
		} else {
			if (filterOperation == null) {
				mSecmContext.applyOperations(listener, rotationOperation,
						rotateOperation, brigthnessContrastOperation);
			} else {
				mSecmContext.applyOperations(listener, filterOperation,
						rotationOperation, rotateOperation,
						brigthnessContrastOperation);
			}
		}
	}

	private void changeImage(final SECMImage image) {
		if (image == null) {
			return;
		}
		imgPhoto.setImageBitmap(null);
		mSecmContext.clearBitmap();
		Bitmap bitmap = image.getBitmap().copy(Config.RGB_565, false);
		mSecmContext.setBitmap(bitmap);
		imgPhoto.setImageBitmap(bitmap);
	}

	private SECMImageRotation getImageRotation() {
		SECMImageRotation imageRotation = SECMImageRotation.SECM_IMAGE_ROTATION_0_DEGREES;
		if (mFixedAngleValue < 0) {
			mFixedAngleValue += 360;
		}
		if (mFixedAngleValue == 360) {
			mFixedAngleValue = 0;
		}
		if (mFixedAngleValue == 90) {
			imageRotation = SECMImageRotation.SECM_IMAGE_ROTATION_90_DEGREES;
		} else if (mFixedAngleValue == 180) {
			imageRotation = SECMImageRotation.SECM_IMAGE_ROTATION_180_DEGREES;
		} else if (mFixedAngleValue == 270) {
			imageRotation = SECMImageRotation.SECM_IMAGE_ROTATION_270_DEGREES;
		}
		return imageRotation;
	}

	private void rotateLeft() {
		mFixedAngleValue -= 90;
		executeOperations();
	}

	private void rotateRight() {
		mFixedAngleValue += 90;
		executeOperations();
	}

	private void rotateAngle(int angle) {
		mAngleValue = angle;
		executeOperations();
	}

	private void adjustBrightnessContrast() {
		executeOperations();
	}

	private void convertToBlackAndWhite() {
		mImageFilter = FILTER_BLACK_WHITE;
		executeOperations();
	}

	private void convertToGrayScale() {
		mImageFilter = FILTER_GRAYSCALE;
		executeOperations();
	}

	private void convertToNormal() {
		mImageFilter = FILTER_NORMAL;
		executeOperations();
	}

	private void crop() {
		mQuadrangle = new SECMQuadrangle(imgPhoto.getTopLeft(),
				imgPhoto.getTopRight(), imgPhoto.getBottomLeft(),
				imgPhoto.getBottomRight());
		isCropped = true;
		btnDewarp.setChecked(false);
		executeOperations();
	}

	private void reset() {
		Bitmap bitmap = mSecmContext.getSource().getBitmap();
		imgPhoto.setImageBitmap(bitmap);
		mSecmContext.loadSourceImage(bitmap);
		mSecmContext.setBitmap(bitmap);
		mFixedAngleValue = 0;
		seekBrigthness.setProgress(50);
		seekContrast.setProgress(50);
		mBrightness = 0;
		mContrast = 1;
		mImageFilter = FILTER_NORMAL;
		mQuadrangle = null;
		isCropped = false;
	}
}
