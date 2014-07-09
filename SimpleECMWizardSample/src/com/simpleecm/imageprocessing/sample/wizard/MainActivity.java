package com.simpleecm.imageprocessing.sample.wizard;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.simpleecm.imageprocessing.utilities.SECMCaptureDocumentWizardBuilder;
import com.simpleecm.imageprocessing.utilities.SECMCaptureDocumentWizardBuilder.OnWizardFinishListener;
import com.simpleecm.imageprocessing.utilities.SECMDocumentBuilder;

public class MainActivity extends Activity implements OnClickListener,
		OnWizardFinishListener {

	private static final String TAG = "Simple ECM Wizard Sample";

	private Button btnOpenWizard;
	private SECMCaptureDocumentWizardBuilder mWizardBuilder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnOpenWizard = (Button) findViewById(R.id.btnOpenWizard);
		btnOpenWizard.setOnClickListener(this);

		mWizardBuilder = SECMCaptureDocumentWizardBuilder.getInstance(this);
		mWizardBuilder.setOnWizardFinishListener(this);
		customizeWizard();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnOpenWizard:
			openWizard();
			break;
		default:
			break;
		}
	}

	private void openWizard() {
		Log.i(TAG, "Wizard opened");
		mWizardBuilder.start();
	}

	private void customizeWizard() {
		/*
		 * Wizard resources can be customize the corresponding method in
		 * SECMCaptureDocumentWizardBuilder instance. There are some values that
		 * are set by default, like camera icons, grid color, texts of Buttons
		 * and TextViews, etc. In order to customize activity titles, they could
		 * be set in manifest with the "label" attribute.
		 */
		mWizardBuilder
				.setPositiveButtonBackground(R.drawable.btn_gray_background);
		mWizardBuilder
				.setNegativeButtonBackground(R.drawable.btn_red_background);
		mWizardBuilder.setActionBarBackgroundColorResource(R.color.gray_enabled);
		mWizardBuilder.setToolBarBackgroundColorResource(R.color.gray_enabled);
		mWizardBuilder.setDewarpQuadrangleColorResource(android.R.color.white);
		mWizardBuilder.setGridColorResource(android.R.color.white);
		mWizardBuilder
				.setEmptyDocumentListLabelResource(R.string.emptyDocumentListLabel);
	}

	/*
	 * This method is execute when Done button is pressed in Document List
	 * Activity. The builder object could be used to build and export images of
	 * the document to PDF, JPG or PNG files. Note: The exported files are
	 * placed in internal storage.
	 */
	@Override
	public void onWizardSucceed(SECMDocumentBuilder builder) {
		Log.i(TAG, "Wizard suceeded");

	}

	/*
	 * This method is executed when wizard is closed by pressing Exit or Exit
	 * and remove document in the confirmation dialog. Also, if the document has
	 * no images, this method is executed by pressing Back, Home or Done button.
	 */
	@Override
	public void onWizardCanceled(SECMDocumentBuilder builder, boolean wantToDelete) {
		Log.i(TAG, "Wizard canceled");
		if (wantToDelete) {
			builder.deleteAll();
		}
	}
}
