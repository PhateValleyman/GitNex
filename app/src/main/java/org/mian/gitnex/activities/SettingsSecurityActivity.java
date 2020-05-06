package org.mian.gitnex.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import androidx.appcompat.app.AlertDialog;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.ssl.MemorizingTrustManager;
import org.mian.gitnex.util.TinyDB;

/**
 * Author M M Arif
 */

public class SettingsSecurityActivity extends BaseActivity {

	private Context appCtx;
	private View.OnClickListener onClickListener;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_settings_security;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();

		TinyDB tinyDb = new TinyDB(appCtx);

		ImageView closeActivity = findViewById(R.id.close);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		LinearLayout certsFrame = findViewById(R.id.certsFrame);
		LinearLayout pollingDelayFrame = findViewById(R.id.pollingDelayFrame);

		// certs deletion
		certsFrame.setOnClickListener(v1 -> {

			AlertDialog.Builder builder = new AlertDialog.Builder(SettingsSecurityActivity.this);

			builder.setTitle(getResources().getString(R.string.settingsCertsPopupTitle));
			builder.setMessage(getResources().getString(R.string.settingsCertsPopupMessage));
			builder.setPositiveButton(R.string.menuDeleteText, (dialog, which) -> {

				appCtx.getSharedPreferences(MemorizingTrustManager.KEYSTORE_NAME, Context.MODE_PRIVATE).edit().remove(MemorizingTrustManager.KEYSTORE_KEY).apply();

				tinyDb.putBoolean("loggedInMode", false);
				tinyDb.remove("basicAuthPassword");
				tinyDb.putBoolean("basicAuthFlag", false);
				//tinyDb.clear();

				Intent loginActivityIntent = new Intent().setClass(appCtx, LoginActivity.class);
				loginActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				appCtx.startActivity(loginActivityIntent);

			});

			builder.setNeutralButton(R.string.cancelButton, (dialog, which) -> dialog.dismiss());
			builder.create().show();

		});

		// polling delay
		pollingDelayFrame.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				NumberPicker numberPicker = new NumberPicker(appCtx);
				numberPicker.setMinValue(20);
				numberPicker.setMaxValue(500);

				if(tinyDb.getInt("pollingDelaySeconds") >= 20) {
					numberPicker.setValue(tinyDb.getInt("pollingDelaySeconds"));
				}
				else {
					numberPicker.setValue(50);
				}

				numberPicker.setWrapSelectorWheel(true);

				AlertDialog.Builder builder = new AlertDialog.Builder(appCtx);
				builder.setTitle("Select polling delay");
				builder.setMessage("Choose your polling delay in seconds.");

				builder.setCancelable(true);
				builder.setPositiveButton("SELECT", (dialog, which) -> {
					tinyDb.putInt("pollingDelaySeconds", numberPicker.getValue());
					Toasty.info(appCtx, getResources().getString(R.string.settingsSave));
				});

				builder.setNegativeButton(R.string.cancelButton, (dialog, which) -> dialog.dismiss());
				builder.setView(numberPicker);
				builder.create().show();

			}
		});

	}

	private void initCloseListener() {
		onClickListener = view -> {
			finish();
		};
	}

}
