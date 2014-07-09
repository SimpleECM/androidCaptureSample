package com.simpleecm.imageprocessing.sample.core;

import android.app.Application;

public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		/*
		SECMLicense.verifyLicense(
				getResources().openRawResource(R.raw.license), getResources()
						.openRawResource(R.raw.hash), getResources()
						.openRawResource(R.raw.pub));
		*/
	}
}
