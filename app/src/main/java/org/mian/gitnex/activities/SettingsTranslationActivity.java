package org.mian.gitnex.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import org.mian.gitnex.R;
import org.mian.gitnex.databinding.ActivitySettingsTranslationBinding;
import org.mian.gitnex.helpers.Toasty;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.TreeMap;

/**
 * Author M M Arif
 */

public class SettingsTranslationActivity extends BaseActivity {

	private View.OnClickListener onClickListener;

	private static int langSelectedChoice = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		LinkedHashMap<String, String> langs = new LinkedHashMap<>();
		langs.put("", getString(R.string.settingsLanguageSystem));
		// key is "a" to sort it in the correct order
		for(String n : new String[]{"en", "ar", "zh", "cs", "fi", "fr", "de", "it", "lv", "fa", "pl", "pt", "ru", "sr", "es", "tr", "uk"}) {
			langs.put(n, getLanguageDisplayName(n));
		}

		ActivitySettingsTranslationBinding activitySettingsTranslationBinding = ActivitySettingsTranslationBinding.inflate(getLayoutInflater());
		setContentView(activitySettingsTranslationBinding.getRoot());

		ImageView closeActivity = activitySettingsTranslationBinding.close;

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		final TextView tvLanguageSelected = activitySettingsTranslationBinding.tvLanguageSelected; // setter for en, fr
		TextView helpTranslate = activitySettingsTranslationBinding.helpTranslate;

		LinearLayout langFrame = activitySettingsTranslationBinding.langFrame;

		helpTranslate.setOnClickListener(v12 -> {

			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.addCategory(Intent.CATEGORY_BROWSABLE);
			intent.setData(Uri.parse(getResources().getString(R.string.crowdInLink)));
			startActivity(intent);

		});

		tvLanguageSelected.setText(tinyDB.getString("localeStr"));

		langSelectedChoice = tinyDB.getInt("langId");

		// language dialog
		langFrame.setOnClickListener(view -> {

			AlertDialog.Builder lBuilder = new AlertDialog.Builder(SettingsTranslationActivity.this);

			lBuilder.setTitle(R.string.settingsLanguageSelectorDialogTitle);
			lBuilder.setCancelable(langSelectedChoice != -1);

			lBuilder.setSingleChoiceItems(langs.values().toArray(new String[0]), langSelectedChoice, (dialogInterface, i) -> {

				String selectedLanguage = langs.keySet().toArray(new String[0])[i];
				String langCode = selectedLanguage;
				if(selectedLanguage.equals("a")) {

					langCode = "en";
				}
				tinyDB.putString("localeStr", langs.get(selectedLanguage));
				tinyDB.putInt("langId", i);

				tinyDB.putString("locale", langCode);

				tinyDB.putBoolean("refreshParent", true);
				this.overridePendingTransition(0, 0);
				dialogInterface.dismiss();
				Toasty.success(appCtx, getResources().getString(R.string.settingsSave));
				this.recreate();
			});

			lBuilder.setNeutralButton(getString(R.string.cancelButton), null);

			AlertDialog lDialog = lBuilder.create();
			lDialog.show();
		});
	}

	private void initCloseListener() {

		onClickListener = view -> finish();
	}

	private static String getLanguageDisplayName(String langCode) {
		Locale english = new Locale("en");
		Locale translated = new Locale(langCode);
		return String.format("%s (%s)", translated.getDisplayName(translated), translated.getDisplayName(english));
	}

}
