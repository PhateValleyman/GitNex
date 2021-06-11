package org.mian.gitnex.activities;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.google.android.material.switchmaterial.SwitchMaterial;
import org.jetbrains.annotations.NotNull;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsAppearanceBinding;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;

/**
 * Author M M Arif
 */

public class SettingsAppearanceActivity extends BaseActivity {

	private View.OnClickListener onClickListener;

	private static String[] timeList;
	private static int timeSelectedChoice = 0;

	private static String[] customFontList;
	private static int customFontSelectedChoice = 0;

	private static String[] themeList;
	private static int themeSelectedChoice = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivitySettingsAppearanceBinding activitySettingsAppearanceBinding = ActivitySettingsAppearanceBinding.inflate(getLayoutInflater());
		setContentView(activitySettingsAppearanceBinding.getRoot());

		ImageView closeActivity = activitySettingsAppearanceBinding.close;

		final TextView tvDateTimeSelected = activitySettingsAppearanceBinding.tvDateTimeSelected; // setter for time
		final TextView customFontSelected = activitySettingsAppearanceBinding.customFontSelected; // setter for custom font
		final TextView themeSelected = activitySettingsAppearanceBinding.themeSelected; // setter for theme

		LinearLayout timeFrame = activitySettingsAppearanceBinding.timeFrame;
		LinearLayout customFontFrame = activitySettingsAppearanceBinding.customFontFrame;
		LinearLayout themeFrame = activitySettingsAppearanceBinding.themeSelectionFrame;
		LinearLayout lightTimeFrame = activitySettingsAppearanceBinding.lightThemeTimeSelectionFrame;
		LinearLayout darkTimeFrame = activitySettingsAppearanceBinding.darkThemeTimeSelectionFrame;

		SwitchMaterial counterBadgesSwitch = activitySettingsAppearanceBinding.switchCounterBadge;

		timeList = getResources().getStringArray(R.array.timeFormats);
		customFontList = getResources().getStringArray(R.array.fonts);
		themeList = getResources().getStringArray(R.array.themes);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		if(!tinyDB.getString("timeStr").isEmpty()) {

			tvDateTimeSelected.setText(tinyDB.getString("timeStr"));
		}

		if(!tinyDB.getString("customFontStr").isEmpty()) {

			customFontSelected.setText(tinyDB.getString("customFontStr"));
		}

		if(!tinyDB.getString("themeStr").isEmpty()) {

			themeSelected.setText(tinyDB.getString("themeStr"));
		}

		if(timeSelectedChoice == 0) {

			timeSelectedChoice = tinyDB.getInt("timeId");
		}

		if(customFontSelectedChoice == 0) {

			customFontSelectedChoice = tinyDB.getInt("customFontId", 1);
		}

		if(themeSelectedChoice == 0) {

			themeSelectedChoice = tinyDB.getInt("themeId");
		}

		counterBadgesSwitch.setChecked(tinyDB.getBoolean("enableCounterBadges"));

		// counter badge switcher
		counterBadgesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {

			tinyDB.putBoolean("enableCounterBadges", isChecked);
			Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
		});

		// theme selection dialog
		themeFrame.setOnClickListener(view -> {

			AlertDialog.Builder tsBuilder = new AlertDialog.Builder(SettingsAppearanceActivity.this);

			tsBuilder.setTitle(getResources().getString(R.string.themeSelectorDialogTitle));
			tsBuilder.setCancelable(themeSelectedChoice != -1);

			tsBuilder.setSingleChoiceItems(themeList, themeSelectedChoice, (dialogInterfaceTheme, i) -> {

				themeSelectedChoice = i;
				themeSelected.setText(themeList[i]);
				tinyDB.putString("themeStr", themeList[i]);
				tinyDB.putInt("themeId", i);

				tinyDB.putBoolean("refreshParent", true);
				this.recreate();
				this.overridePendingTransition(0, 0);
				dialogInterfaceTheme.dismiss();
				Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
			});

			AlertDialog cfDialog = tsBuilder.create();
			cfDialog.show();
		});

		lightTimeFrame.setOnClickListener(view -> {
			LightTimePicker timePicker = new LightTimePicker();
			timePicker.setVariables(tinyDB);
	        timePicker.show(getSupportFragmentManager(), "timePicker");
		});

		darkTimeFrame.setOnClickListener(view -> {
			DarkTimePicker timePicker = new DarkTimePicker();
			timePicker.setVariables(tinyDB);
	        timePicker.show(getSupportFragmentManager(), "timePicker");
		});

		// custom font dialog
		customFontFrame.setOnClickListener(view -> {

			AlertDialog.Builder cfBuilder = new AlertDialog.Builder(SettingsAppearanceActivity.this);

			cfBuilder.setTitle(R.string.settingsCustomFontSelectorDialogTitle);
			cfBuilder.setCancelable(customFontSelectedChoice != -1);

			cfBuilder.setSingleChoiceItems(customFontList, customFontSelectedChoice, (dialogInterfaceCustomFont, i) -> {

				customFontSelectedChoice = i;
				customFontSelected.setText(customFontList[i]);
				tinyDB.putString("customFontStr", customFontList[i]);
				tinyDB.putInt("customFontId", i);

				tinyDB.putBoolean("refreshParent", true);
				this.recreate();
				this.overridePendingTransition(0, 0);
				dialogInterfaceCustomFont.dismiss();
				Toasty.success(appCtx, appCtx.getResources().getString(R.string.settingsSave));
			});

			AlertDialog cfDialog = cfBuilder.create();
			cfDialog.show();
		});

		// time and date dialog
		timeFrame.setOnClickListener(view -> {

			AlertDialog.Builder tBuilder = new AlertDialog.Builder(SettingsAppearanceActivity.this);

			tBuilder.setTitle(R.string.settingsTimeSelectorDialogTitle);
			tBuilder.setCancelable(timeSelectedChoice != -1);

			tBuilder.setSingleChoiceItems(timeList, timeSelectedChoice, (dialogInterfaceTime, i) -> {

				timeSelectedChoice = i;
				tvDateTimeSelected.setText(timeList[i]);
				tinyDB.putString("timeStr", timeList[i]);
				tinyDB.putInt("timeId", i);

				switch(i) {
					case 0:
						tinyDB.putString("dateFormat", "pretty");
						break;
					case 1:
						tinyDB.putString("dateFormat", "normal");
						break;
				}

				dialogInterfaceTime.dismiss();
				Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
			});

			AlertDialog tDialog = tBuilder.create();
			tDialog.show();
		});

	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}

	public static class LightTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

		TinyDB db;

		public void setVariables(TinyDB db) {
			this.db = db;
		}

		@NotNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int hour = db.getInt("lightThemeTimeHour");
			int minute = db.getInt("lightThemeTimeMinute");

			return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			db.putInt("lightThemeTimeHour", hourOfDay);
			db.putInt("lightThemeTimeMinute", minute);
			db.putBoolean("refreshParent", true);
			requireActivity().overridePendingTransition(0, 0);
			this.dismiss();
			Toasty.success(requireActivity().getApplicationContext(), requireContext().getResources().getString(R.string.settingsSave));
			requireActivity().recreate();
		}

	}

	public static class DarkTimePicker extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

		TinyDB db;

		public void setVariables(TinyDB db) {
			this.db = db;
		}

		@NotNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int hour = db.getInt("darkThemeTimeHour");
			int minute = db.getInt("darkThemeTimeMinute");

			return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			db.putInt("darkThemeTimeHour", hourOfDay);
			db.putInt("darkThemeTimeMinute", minute);
			db.putBoolean("refreshParent", true);
			requireActivity().overridePendingTransition(0, 0);
			this.dismiss();
			Toasty.success(requireActivity().getApplicationContext(), requireContext().getResources().getString(R.string.settingsSave));
			requireActivity().recreate();
		}

	}

}
