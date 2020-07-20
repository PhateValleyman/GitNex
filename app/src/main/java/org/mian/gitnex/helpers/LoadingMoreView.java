package org.mian.gitnex.helpers;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import org.mian.gitnex.R;
import java.util.Random;

/**
 * Author opyale
 */

public class LoadingMoreView extends LinearLayout {

	public LoadingMoreView(Context context) {

		super(context);
		init();

	}

	public LoadingMoreView(Context context, @Nullable AttributeSet attrs) {

		super(context, attrs);
		init();

	}

	public LoadingMoreView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {

		super(context, attrs, defStyleAttr);
		init();

	}

	public LoadingMoreView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {

		super(context, attrs, defStyleAttr, defStyleRes);
		init();

	}

	private void init() {

		String[] headers = getContext().getResources().getStringArray(R.array.headersLoadingMoreView);
		String[] tips = getContext().getResources().getStringArray(R.array.tipsLoadingMoreView);

		super.setPadding(20, 20, 20, 20);
		super.setGravity(Gravity.CENTER);
		super.setOrientation(HORIZONTAL);

		TypedValue textColorPrimary = new TypedValue();
		TypedValue textColorSecondary = new TypedValue();

		getContext().getTheme().resolveAttribute(R.attr.primaryTextColor, textColorPrimary, true);
		getContext().getTheme().resolveAttribute(R.attr.hintColor, textColorSecondary, true);

		LinearLayout linearLayout = new LinearLayout(getContext());
		linearLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		linearLayout.setPadding(0, 0, 30, 0);
		linearLayout.setOrientation(VERTICAL);

		TextView textView = new TextView(getContext());
		textView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		textView.setTextSize(16);
		textView.setTextColor(textColorPrimary.data);
		textView.setText(headers[new Random().nextInt(headers.length)]);

		TextView textView2 = new TextView(getContext());
		textView2.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		textView2.setTextSize(12);
		textView2.setTextColor(textColorSecondary.data);
		textView2.setText(tips[new Random().nextInt(tips.length)]);

		linearLayout.addView(textView);
		linearLayout.addView(textView2);

		ProgressBar progressBar = new ProgressBar(getContext());
		progressBar.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

		super.addView(linearLayout);
		super.addView(progressBar, 35, 35);

	}

}
