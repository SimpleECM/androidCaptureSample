package com.simpleecm.imageprocessing.sample.wizard;

import android.app.Application;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		/*
		SECMLicense.verifyLicense("App name",
				getResources().openRawResource(R.raw.dct_license), getResources()
						.openRawResource(R.raw.dct_hash));*/
	}
}
