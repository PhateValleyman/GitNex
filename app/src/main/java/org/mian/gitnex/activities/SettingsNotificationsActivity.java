package org.mian.gitnex.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.GlobalVariables;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.notifications.NotificationsMaster;

/**
 * Template Author M M Arif
 * Author opyale
 */

public class SettingsNotificationsActivity extends BaseActivity {

	private Context appCtx;
	private Context ctx = this;

	@Override
	protected int getLayoutResourceId() {

		return R.layout.activity_settings_notifications;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		appCtx = getApplicationContext();

		TinyDB tinyDb = new TinyDB(appCtx);

		View.OnClickListener onClickListener = view -> finish();

		ImageView closeActivity = findViewById(R.id.close);
		closeActivity.setOnClickListener(onClickListener);

		Switch enableLightsMode = findViewById(R.id.enableLightsMode);
		Switch enableVibrationMode = findViewById(R.id.enableVibrationMode);

		TextView pollingDelaySelected = findViewById(R.id.pollingDelaySelected);
		CardView chooseColorState = findViewById(R.id.chooseColorState);

		LinearLayout pollingDelayFrame = findViewById(R.id.pollingDelayFrame);
		RelativeLayout chooseColorFrame = findViewById(R.id.chooseColorFrame);

		pollingDelaySelected.setText(String.format(getString(R.string.pollingDelaySelectedText), tinyDb.getInt("pollingDelayMinutes", GlobalVariables.defaultPollingDelay)));
		chooseColorState.setCardBackgroundColor(tinyDb.getInt("notificationsLightColor", Color.GREEN));

		if(tinyDb.getBoolean("notificationsEnableLights", true)) {
			enableLightsMode.setChecked(true);
		}
		else {
			enableLightsMode.setChecked(false);
		}

		if(tinyDb.getBoolean("notificationsEnableVibration", true)) {
			enableVibrationMode.setChecked(true);
		}
		else {
			enableVibrationMode.setChecked(false);
		}

		// polling delay
		pollingDelayFrame.setOnClickListener(v -> {

			NumberPicker numberPicker = new NumberPicker(ctx);
			numberPicker.setMinValue(GlobalVariables.minimumPollingDelay);
			numberPicker.setMaxValue(GlobalVariables.maximumPollingDelay);
			numberPicker.setValue(tinyDb.getInt("pollingDelayMinutes", GlobalVariables.defaultPollingDelay));
			numberPicker.setWrapSelectorWheel(true);

			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder.setTitle(getString(R.string.pollingDelayDialogHeaderText));
			builder.setMessage(getString(R.string.pollingDelayDialogDescriptionText));

			builder.setCancelable(true);
			builder.setPositiveButton(getString(R.string.okButton), (dialog, which) -> {

				tinyDb.putInt("pollingDelayMinutes", numberPicker.getValue());

				NotificationsMaster.fireWorker(ctx);
				NotificationsMaster.hireWorker(ctx);

				pollingDelaySelected.setText(String.format(getString(R.string.pollingDelaySelectedText), numberPicker.getValue()));
				Toasty.info(appCtx, getResources().getString(R.string.settingsSave));

			});

			builder.setNegativeButton(R.string.cancelButton, (dialog, which) -> dialog.dismiss());
			builder.setView(numberPicker);
			builder.create().show();

		});

		// lights switcher
		enableLightsMode.setOnCheckedChangeListener((buttonView, isChecked) -> {

			tinyDb.putBoolean("notificationsEnableLights", isChecked);
			Toasty.info(appCtx, getResources().getString(R.string.settingsSave));

		});

		// lights color chooser
		chooseColorFrame.setOnClickListener(v -> {

			ColorPicker colorPicker = new ColorPicker(SettingsNotificationsActivity.this);
			colorPicker.setColor(tinyDb.getInt("notificationsLightColor", Color.GREEN));
			colorPicker.setCallback(color -> {

				tinyDb.putInt("notificationsLightColor", color);
				chooseColorState.setCardBackgroundColor(color);
				colorPicker.dismiss();
				Toasty.info(appCtx, getResources().getString(R.string.settingsSave));

			});

			colorPicker.show();

		});

		// vibration switcher
		enableVibrationMode.setOnCheckedChangeListener((buttonView, isChecked) -> {

			tinyDb.putBoolean("notificationsEnableVibration", isChecked);
			Toasty.info(appCtx, getResources().getString(R.string.settingsSave));

		});

	}

}
