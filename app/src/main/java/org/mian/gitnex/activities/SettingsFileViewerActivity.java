package org.mian.gitnex.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.NumberPicker;
import androidx.appcompat.app.AlertDialog;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsFileviewerBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.Toasty;

/**
 * Author M M Arif
 */

public class SettingsFileViewerActivity extends BaseActivity {

	private static final String[] fileViewerSourceCodeThemesList = {"Sublime", "Arduino Light", "Github", "Far ", "Ir Black", "Android Studio"};
	private static int fileViewerSourceCodeThemesSelectedChoice = 0;

	@SuppressLint("DefaultLocale")
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		ActivitySettingsFileviewerBinding binding = ActivitySettingsFileviewerBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		binding.close.setOnClickListener(view -> finish());

		if(fileViewerSourceCodeThemesSelectedChoice == 0) {
			fileViewerSourceCodeThemesSelectedChoice = tinyDB.getInt("fileviewerThemeId");
		}

		binding.sourceCodeThemeSelected.setText(tinyDB.getString("fileviewerSourceCodeThemeStr", fileViewerSourceCodeThemesList[0]));
		binding.maxViewerSizeSelected.setText(String.format("%d MB", tinyDB.getInt("maxFileViewerSize", Constants.defaultFileViewerSize)));
		binding.switchPdfMode.setChecked(tinyDB.getBoolean("enablePdfMode"));

		// fileviewer source code theme selection dialog
		binding.sourceCodeThemeFrame.setOnClickListener(view -> {

			AlertDialog.Builder fvtsBuilder = new AlertDialog.Builder(SettingsFileViewerActivity.this);

			fvtsBuilder.setTitle(R.string.fileViewerSourceCodeThemeSelectorDialogTitle);
			fvtsBuilder.setCancelable(fileViewerSourceCodeThemesSelectedChoice != -1);

			fvtsBuilder.setSingleChoiceItems(fileViewerSourceCodeThemesList, fileViewerSourceCodeThemesSelectedChoice, (dialogInterfaceTheme, i) -> {

				fileViewerSourceCodeThemesSelectedChoice = i;
				binding.sourceCodeThemeSelected.setText(fileViewerSourceCodeThemesList[i]);
				tinyDB.putString("fileviewerSourceCodeThemeStr", fileViewerSourceCodeThemesList[i]);
				tinyDB.putInt("fileviewerSourceCodeThemeId", i);

				dialogInterfaceTheme.dismiss();
				Toasty.success(appCtx, getResources().getString(R.string.settingsSave));

			});

			AlertDialog alertDialog = fvtsBuilder.create();
			alertDialog.show();

		});

		// pdf night mode switcher
		binding.switchPdfMode.setOnCheckedChangeListener((buttonView, isChecked) -> {

			tinyDB.putBoolean("enablePdfMode", isChecked);
			tinyDB.putString("enablePdfModeInit", "yes");
			Toasty.success(appCtx, getResources().getString(R.string.settingsSave));

		});

		binding.maxViewerSizeFrame.setOnClickListener(v -> {

			NumberPicker numberPicker = new NumberPicker(ctx);
			numberPicker.setMinValue(Constants.minimumFileViewerSize);
			numberPicker.setMaxValue(Constants.maximumFileViewerSize);
			numberPicker.setValue(tinyDB.getInt("maxFileViewerSize", Constants.defaultFileViewerSize));
			numberPicker.setWrapSelectorWheel(true);

			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			builder.setTitle(getString(R.string.fileViewerDialogHeaderText));
			builder.setMessage(getString(R.string.fileViewerDialogDescriptionText));

			builder.setCancelable(true);
			builder.setPositiveButton(getString(R.string.okButton), (dialog, which) -> {

				tinyDB.putInt("maxFileViewerSize", numberPicker.getValue());

				binding.maxViewerSizeSelected.setText(String.format("%d MB", numberPicker.getValue()));
				Toasty.info(ctx, getResources().getString(R.string.settingsSave));

			});

			builder.setNeutralButton(R.string.cancelButton, (dialog, which) -> dialog.dismiss());
			builder.setView(numberPicker);
			builder.create().show();

		});

	}

}
