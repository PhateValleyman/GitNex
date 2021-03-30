package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import org.acra.ACRA;
import org.acra.BuildConfig;
import org.acra.ReportField;
import org.acra.annotation.AcraCore;
import org.acra.annotation.AcraNotification;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.LimiterConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.data.StringFormat;
import org.mian.gitnex.R;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.TimeHelper;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.notifications.Notifications;

/**
 * Author M M Arif
 */

@AcraNotification(resIcon = R.drawable.gitnex_transparent,
	resTitle = R.string.crashTitle,
	resChannelName = R.string.setCrashReports,
	resText = R.string.crashMessage)
@AcraCore(reportContent = {
	ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL,
	ReportField.STACK_TRACE, ReportField.AVAILABLE_MEM_SIZE, ReportField.BRAND })

public abstract class BaseActivity extends AppCompatActivity {

	protected TinyDB tinyDB;

	protected Context ctx = this;
	protected Context appCtx;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		this.appCtx = getApplicationContext();
		this.tinyDB = TinyDB.getInstance(appCtx);

		switch(tinyDB.getInt("themeId")) {

			case 1:

				tinyDB.putString("currentTheme", "light");
				setTheme(R.style.AppThemeLight);
				break;
			case 2:

				if(TimeHelper.timeBetweenHours(18, 6)) { // 6pm to 6am

					tinyDB.putString("currentTheme", "dark");
					setTheme(R.style.AppTheme);
				}
				else {

					tinyDB.putString("currentTheme", "light");
					setTheme(R.style.AppThemeLight);
				}
				break;
			case 3:

				tinyDB.putString("currentTheme", "light");
				setTheme(R.style.AppThemeRetro);
				break;
			case 4:
				if(TimeHelper.timeBetweenHours(18, 6)) { // 6pm to 6am

					tinyDB.putString("currentTheme", "dark");
					setTheme(R.style.AppTheme);
				}
				else {

					tinyDB.putString("currentTheme", "light");
					setTheme(R.style.AppThemeRetro);
				}
				break;
			case 5:

				tinyDB.putString("currentTheme", "dark");
				setTheme(R.style.AppThemePitchBlack);
				break;
			default:

				tinyDB.putString("currentTheme", "dark");
				setTheme(R.style.AppTheme);

		}

		AppUtil.setAppLocale(getResources(), tinyDB.getString("locale"));

		if(tinyDB.getBoolean("crashReportingEnabled")) {

			CoreConfigurationBuilder ACRABuilder = new CoreConfigurationBuilder(this);

			ACRABuilder.setBuildConfigClass(BuildConfig.class).setReportFormat(StringFormat.KEY_VALUE_LIST);
			ACRABuilder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class).setReportAsFile(true).setMailTo(getResources().getString(R.string.appEmail)).setSubject(getResources().getString(R.string.crashReportEmailSubject, AppUtil.getAppBuildNo(getApplicationContext()))).setEnabled(true);
			ACRABuilder.getPluginConfigurationBuilder(LimiterConfigurationBuilder.class).setEnabled(true);

			ACRA.init(getApplication(), ACRABuilder);
		}

		Notifications.createChannels(appCtx);
		Notifications.startWorker(appCtx);
	}

}


