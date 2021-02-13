package org.mian.gitnex.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Author M M Arif
 */

public class Images {

	public static Bitmap scaleImage(byte[] imageData, int size_limit) {

		Bitmap original = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

		if(original.getHeight() > size_limit && original.getWidth() <= original.getHeight()) {

			double reduction_percentage = (double) size_limit / original.getHeight();

			Bitmap scaled = Bitmap.createScaledBitmap(original, (int) (reduction_percentage * original.getWidth()), size_limit, false);
			original.recycle();

			return scaled;

		}
		else if(original.getWidth() > size_limit && original.getHeight() < original.getWidth()) {

			double reduction_percentage = (double) size_limit / original.getWidth();

			Bitmap scaled = Bitmap.createScaledBitmap(original, size_limit, (int) (reduction_percentage * original.getHeight()), false);
			original.recycle();

			return scaled;

		}

		// Image size does not exceed bounds.
		return original;

	}

}
