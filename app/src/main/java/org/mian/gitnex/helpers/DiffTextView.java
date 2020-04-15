package org.mian.gitnex.helpers;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Author opyale
 */

public class DiffTextView extends androidx.appcompat.widget.AppCompatTextView {

	private int initialBackgroundColor = -1;
	private int currentBackgroundColor = -1;

	public DiffTextView(Context context) {

		super(context);
	}

	public DiffTextView(Context context, AttributeSet attrs) {

		super(context, attrs);
	}

	public DiffTextView(Context context, AttributeSet attrs, int defStyleAttr) {

		super(context, attrs, defStyleAttr);
	}

	@Override
	public void setBackgroundColor(int color) {

		if(initialBackgroundColor == -1) {
			initialBackgroundColor = color;
		}

		currentBackgroundColor = color;

		super.setBackgroundColor(color);
	}

	public int getInitialBackgroundColor() {

		return initialBackgroundColor;
	}

	public int getCurrentBackgroundColor() {

		return currentBackgroundColor;
	}

}
