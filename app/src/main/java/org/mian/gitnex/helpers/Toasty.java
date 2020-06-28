package org.mian.gitnex.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.mian.gitnex.R;

/**
 * Author M M Arif
 */

public class Toasty {

	public static void info(Context context, String message) {

		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(context.getResources().getLayout(R.layout.custom_toast), null);

		TextView text = view.findViewById(R.id.toastText);
		text.setText(message);

		Toast toast = new Toast(context);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(view);
		toast.show();

	}

	public static void error(Context context, String message) {

		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(context.getResources().getLayout(R.layout.custom_toast_error), null);

		TextView text = view.findViewById(R.id.toastText);
		text.setText(message);

		Toast toast = new Toast(context);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(view);
		toast.show();

	}

}
