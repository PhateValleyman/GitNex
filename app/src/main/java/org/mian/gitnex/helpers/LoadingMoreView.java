package org.mian.gitnex.helpers;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import org.mian.gitnex.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

		int padding = AppUtil.getPixelsFromDensity(getContext(), 20);

		super.setPadding(padding, padding, padding, padding);
		super.setGravity(Gravity.CENTER);
		super.setOrientation(HORIZONTAL);

		TypedValue textColorPrimary = new TypedValue();
		TypedValue textColorSecondary = new TypedValue();

		getContext().getTheme().resolveAttribute(R.attr.primaryTextColor, textColorPrimary, true);
		getContext().getTheme().resolveAttribute(R.attr.hintColor, textColorSecondary, true);

		LinearLayout linearLayout = new LinearLayout(getContext());
		linearLayout.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		linearLayout.setPadding(0, 0, AppUtil.getPixelsFromDensity(getContext(), 25), 0);
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

		int size = AppUtil.getPixelsFromDensity(getContext(), 30);

		super.addView(linearLayout);
		super.addView(progressBar, size, size);

		Thread thread = new Thread(() -> {

			List<Float> alphas = new ArrayList<>();
			Collections.addAll(alphas, 1.0f, 0.0f);

			while(true) {

				try {

					AlphaAnimation anim = new AlphaAnimation(alphas.get(0), alphas.get(1));
					anim.setDuration(1000);

					textView.startAnimation(anim);
					textView2.startAnimation(anim);

					Thread.sleep(1000);

					textView.setText(headers[new Random().nextInt(headers.length)]);
					textView2.setText(tips[new Random().nextInt(tips.length)]);

					Collections.reverse(alphas);

					Thread.sleep(1000);

				} catch (InterruptedException ignored) {}
			}
		});

		thread.start();

	}

}
