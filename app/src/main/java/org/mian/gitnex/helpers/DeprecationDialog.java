package org.mian.gitnex.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import org.mian.gitnex.R;

/**
 * Author opyale
 */

public class DeprecationDialog extends AlertDialog.Builder {

	private Context context;
	private String message = "";

	public DeprecationDialog(@NonNull Context context) {

		super(context);
		this.context = context;

	}

	public DeprecationDialog(@NonNull Context context, int themeResId) {

		super(context, themeResId);
		this.context = context;

	}

	@NonNull
	@SuppressLint("InflateParams")
	@Override
	public AlertDialog create() {

		setPositiveButton(context.getResources().getString(R.string.okButton), (dialog, which) -> dialog.dismiss());

		View view = LayoutInflater.from(context).inflate(R.layout.layout_deprecation_dialog, null);

		TextView textView = view.findViewById(R.id.customMessage);
		textView.setText(message);

		setView(view);
		return super.create();

	}

	public void setMessage(String message) {

		this.message = message;
	}

}
