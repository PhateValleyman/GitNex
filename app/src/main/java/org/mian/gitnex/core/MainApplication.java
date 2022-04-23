package org.mian.gitnex.core;

import android.app.Application;
import android.content.Context;
import org.acra.ACRA;
import org.acra.BuildConfig;
import org.acra.ReportField;
import org.acra.config.CoreConfigurationBuilder;
import org.acra.config.LimiterConfigurationBuilder;
import org.acra.config.MailSenderConfigurationBuilder;
import org.acra.config.NotificationConfigurationBuilder;
import org.acra.data.StringFormat;
import org.mian.gitnex.R;
import org.mian.gitnex.database.models.UserAccount;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.FontsOverride;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.contexts.AccountContext;
import org.mian.gitnex.notifications.Notifications;
import java.nio.charset.StandardCharsets;

/**
 * @author opyale
 */

public class MainApplication extends Application {

	private TinyDB tinyDB;
	public AccountContext currentAccount;

	@Override
	public void onCreate() {

		super.onCreate();

		Context appCtx = getApplicationContext();
		tinyDB = TinyDB.getInstance(appCtx);

		currentAccount = AccountContext.fromId(tinyDB.getInt("currentActiveAccountId", 0), appCtx);

		tinyDB.putBoolean("biometricLifeCycle", false);

		FontsOverride.setDefaultFont(this);

		Notifications.createChannels(appCtx);
	}

	@Override
	protected void attachBaseContext(Context context) {

		super.attachBaseContext(context);

		tinyDB = TinyDB.getInstance(context);

		if(tinyDB.getBoolean("crashReportingEnabled", true)) {

			CoreConfigurationBuilder ACRABuilder = new CoreConfigurationBuilder(this);

			ACRABuilder.withBuildConfigClass(BuildConfig.class).withReportContent(ReportField.ANDROID_VERSION, ReportField.PHONE_MODEL,
				ReportField.STACK_TRACE, ReportField.AVAILABLE_MEM_SIZE, ReportField.BRAND).setReportFormat(StringFormat.KEY_VALUE_LIST);
			ACRABuilder.getPluginConfigurationBuilder(NotificationConfigurationBuilder.class).withResTitle(R.string.crashTitle)
				.withResIcon(R.drawable.gitnex_transparent).withResChannelName(R.string.setCrashReports).withResText(R.string.crashMessage);
			ACRABuilder.getPluginConfigurationBuilder(MailSenderConfigurationBuilder.class).withMailTo(getResources().getString(R.string.appEmail))
				.withSubject(getResources().getString(R.string.crashReportEmailSubject, AppUtil
					.getAppBuildNo(context)))
				.withReportAsFile(true)
				.withEnabled(true);
			ACRABuilder.getPluginConfigurationBuilder(LimiterConfigurationBuilder.class).setEnabled(true);

			ACRA.init(this, ACRABuilder);
		}
	}

	public boolean switchToAccount(UserAccount userAccount, boolean tmp) {
		if(!tmp || tinyDB.getInt("currentActiveAccountId") != userAccount.getAccountId()) {
			currentAccount = new AccountContext(userAccount);
			if(!tmp) tinyDB.putInt("currentActiveAccountId", userAccount.getAccountId());
			return true;
		}

		return false;
	}
}
