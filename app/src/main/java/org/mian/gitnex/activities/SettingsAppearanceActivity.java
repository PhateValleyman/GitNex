package org.mian.gitnex.activities;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

		String lightMinute = String.valueOf(tinyDB.getInt("lightThemeTimeMinute"));
		if(lightMinute.length() == 1) lightMinute = "0" + lightMinute;

		String darkMinute = String.valueOf(tinyDB.getInt("darkThemeTimeMinute"));
		if(darkMinute.length() == 1) darkMinute = "0" + darkMinute;

		activitySettingsAppearanceBinding.lightThemeSelectedTime.setText(ctx.getResources().getString(R.string.settingsThemeTimeSelectedHint, String.valueOf(tinyDB.getInt("lightThemeTimeHour")),
			lightMinute));
		activitySettingsAppearanceBinding.darkThemeSelectedTime.setText(ctx.getResources().getString(R.string.settingsThemeTimeSelectedHint, String.valueOf(tinyDB.getInt("darkThemeTimeHour")),
			darkMinute));
		activitySettingsAppearanceBinding.tvDateTimeSelected.setText(tinyDB.getString("timeStr"));
		activitySettingsAppearanceBinding.customFontSelected.setText(tinyDB.getString("customFontStr", "Manrope"));
		activitySettingsAppearanceBinding.themeSelected.setText(tinyDB.getString("themeStr", "Dark"));

		if(tinyDB.getString("themeStr").startsWith("Auto")) {
			darkTimeFrame.setVisibility(View.VISIBLE);
			lightTimeFrame.setVisibility(View.VISIBLE);
		}
		else {
			darkTimeFrame.setVisibility(View.GONE);
			lightTimeFrame.setVisibility(View.GONE);
		}

		timeSelectedChoice = tinyDB.getInt("timeId");
		customFontSelectedChoice = tinyDB.getInt("customFontId", 1);
		themeSelectedChoice = tinyDB.getInt("themeId");

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
				activitySettingsAppearanceBinding.themeSelected.setText(themeList[i]);
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
	        timePicker.show(getSupportFragmentManager(), "timePicker");
		});

		darkTimeFrame.setOnClickListener(view -> {
			DarkTimePicker timePicker = new DarkTimePicker();
	        timePicker.show(getSupportFragmentManager(), "timePicker");
		});

		// custom font dialog
		customFontFrame.setOnClickListener(view -> {

			AlertDialog.Builder cfBuilder = new AlertDialog.Builder(SettingsAppearanceActivity.this);

			cfBuilder.setTitle(R.string.settingsCustomFontSelectorDialogTitle);
			cfBuilder.setCancelable(customFontSelectedChoice != -1);

			cfBuilder.setSingleChoiceItems(customFontList, customFontSelectedChoice, (dialogInterfaceCustomFont, i) -> {

				customFontSelectedChoice = i;
				activitySettingsAppearanceBinding.customFontSelected.setText(customFontList[i]);
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
				activitySettingsAppearanceBinding.tvDateTimeSelected.setText(timeList[i]);
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

		TinyDB db = TinyDB.getInstance(getContext());

		@NotNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int hour = db.getInt("lightThemeTimeHour");
			int minute = db.getInt("lightThemeTimeMinute");

			return new TimePickerDialog(getActivity(), this, hour, minute, true);
		}

		@Override
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

		TinyDB db = TinyDB.getInstance(getContext());

		@NotNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			int hour = db.getInt("darkThemeTimeHour");
			int minute = db.getInt("darkThemeTimeMinute");

			return new TimePickerDialog(getActivity(), this, hour, minute, true);
		}

		@Override
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
