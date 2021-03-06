package com.simpleecm.imageprocessing.sample.core;

import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageUtil {

	public static Bitmap decodeSampledBitmapFromFile(String path, int reqWidth, int reqHeight) {
	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(path, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set false
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeFile(path, options);
	}
	
	public static Bitmap getFixedBitmap(int reqWidth, int reqHeight, File imgFile) {
		Bitmap bmp = ImageUtil.decodeSampledBitmapFromFile(imgFile.getPath(), reqWidth, reqHeight);
        return bmp;
	}
	
	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        // Calculate ratios of height and width to requested height and width
	        final int heightRatio = Math.round((float) height / (float) reqHeight);
	        final int widthRatio = Math.round((float) width / (float) reqWidth);
	
	        // Choose the smallest ratio as inSampleSize value, this will guarantee
	        // a final image with both dimensions larger than or equal to the
	        // requested height and width.
	        inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
	    }
	
	    return inSampleSize;
	}
}
