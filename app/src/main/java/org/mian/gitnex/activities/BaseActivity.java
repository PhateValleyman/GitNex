package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.acra.ACRA;
import org.acra.BuildConfig;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraNotification;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.LimiterConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.data.StringFormat;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.FontsOverride;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.notifications.NotificationsMaster;
import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.STACK_TRACE;

/**
 * Author M M Arif
 */

@AcraNotification(resIcon = R.drawable.gitnex_transparent,
		resTitle = R.string.crashTitle,
		resChannelName = R.string.setCrashReports,
		resText = R.string.crashMessage)
@AcraCore(reportContent = { ANDROID_VERSION, PHONE_MODEL, STACK_TRACE })

public abstract class BaseActivity extends AppCompatActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		Context appCtx = getApplicationContext();
		final TinyDB tinyDb = TinyDB.getInstance(appCtx);

		switch(tinyDb.getInt("themeId")) {

			case 1:

				setTheme(R.style.AppThemeLight);
				break;
			case 2:

				if(TimeHelper.timeBetweenHours(18, 6)) { // 6pm to 6am

					setTheme(R.style.AppTheme);
				}
				else {

					setTheme(R.style.AppThemeLight);
				}
				break;
			case 3:

				setTheme(R.style.AppThemeRetro);
				break;
			case 4:

				if(TimeHelper.timeBetweenHours(18, 6)) { // 6pm to 6am

					setTheme(R.style.AppTheme);
				}
				else {

					setTheme(R.style.AppThemeRetro);
				}
				break;
			default:

				setTheme(R.style.AppTheme);
				break;
		}

		String appLocale = tinyDb.getString("locale");
		AppUtil.setAppLocale(getResources(), appLocale);

		super.onCreate(savedInstanceState);
		setContentView(getLayoutResourceId());

		switch(tinyDb.getInt("customFontId", -1)) {

			case 0:

				FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/roboto.ttf");
				FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/roboto.ttf");
				FontsOverride.setDefaultFont(this, "SERIF", "fonts/roboto.ttf");
				FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/roboto.ttf");
				break;
			case 2:

				FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/sourcecodeproregular.ttf");
				FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/sourcecodeproregular.ttf");
				FontsOverride.setDefaultFont(this, "SERIF", "fonts/sourcecodeproregular.ttf");
				FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/sourcecodeproregular.ttf");
				break;
			default:

				FontsOverride.setDefaultFont(this, "DEFAULT", "fonts/manroperegular.ttf");
				FontsOverride.setDefaultFont(this, "MONOSPACE", "fonts/manroperegular.ttf");
				FontsOverride.setDefaultFont(this, "SERIF", "fonts/manroperegular.ttf");
				FontsOverride.setDefaultFont(this, "SANS_SERIF", "fonts/manroperegular.ttf");
				break;
		}

        if(tinyDb.getInt("pollingDelayMinutes") == 0) {

            tinyDb.putInt("pollingDelayMinutes", 15);
        }

        NotificationsMaster.hireWorker(appCtx);

        // enabling counter badges by default
        if(tinyDb.getString("enableCounterBadgesInit").isEmpty()) {

            tinyDb.putBoolean("enableCounterBadges", true);
            tinyDb.putString("enableCounterBadgesInit", "yes");
        }

		// enable crash reports by default
		if(tinyDb.getString("crashReportingEnabledInit").isEmpty()) {

			tinyDb.putBoolean("crashReportingEnabled", true);
			tinyDb.putString("crashReportingEnabledInit", "yes");
		}

		// default cache setter
		if(tinyDb.getString("cacheSizeStr").isEmpty()) {

			tinyDb.putString("cacheSizeStr", getResources().getString(R.string.cacheSizeDataSelectionSelectedText));
		}
		if(tinyDb.getString("cacheSizeImagesStr").isEmpty()) {

			tinyDb.putString("cacheSizeImagesStr", getResources().getString(R.string.cacheSizeImagesSelectionSelectedText));
		}

		// enable comment drafts by default
		if(tinyDb.getString("draftsCommentsDeletionEnabledInit").isEmpty()) {

			tinyDb.putBoolean("draftsCommentsDeletionEnabled", true);
			tinyDb.putString("draftsCommentsDeletionEnabledInit", "yes");
		}

		if (tinyDb.getBoolean("crashReportingEnabled")) {

			CoreConfigurationBuilder ACRABuilder = new CoreConfigurationBuilder(this);
			ACRABuilder.setBuildConfigClass(BuildConfig.class).setReportFormat(StringFormat.KEY_VALUE_LIST);
			ACRABuilder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class).setReportAsFile(true).setMailTo(getResources().getString(R.string.appEmail)).setSubject(getResources().getString(R.string.crashReportEmailSubject, AppUtil.getAppBuildNo(getApplicationContext()))).setEnabled(true);
			ACRABuilder.getPluginConfigurationBuilder(LimiterConfigurationBuilder.class).setEnabled(true);
			ACRA.init(getApplication(), ACRABuilder);
		}
	}

	protected abstract int getLayoutResourceId();

}


