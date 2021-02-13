package org.mian.gitnex.notifications;

import android.content.Context;
import android.os.Build;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import org.mian.gitnex.helpers.StaticGlobalVariables;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Version;
import java.util.concurrent.TimeUnit;

/**
 * Author opyale
 */

public class Notifications {

	private static int notificationsSupported = -1;

	private static void checkVersion(TinyDB tinyDB) {

		String currentVersion = tinyDB.getString("giteaVersion");

		if(tinyDB.getBoolean("loggedInMode") && !currentVersion.isEmpty()) {

			notificationsSupported = new Version(currentVersion).higherOrEqual("1.12.3") ? 1 : 0;
		}
	}

	public static void stopWorker(Context context) {

		WorkManager.getInstance(context).cancelAllWorkByTag(context.getPackageName());
	}

	public static void startWorker(Context context) {

		TinyDB tinyDB = TinyDB.getInstance(context);

		if(tinyDB.getBoolean("notificationsEnabled", true)) {

			if(notificationsSupported == -1) checkVersion(tinyDB);

			if(notificationsSupported == 1) {

				Constraints.Builder constraints = new Constraints.Builder()
					.setRequiredNetworkType(NetworkType.CONNECTED)
					.setRequiresBatteryNotLow(false)
					.setRequiresStorageNotLow(false)
					.setRequiresCharging(false);

				if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

					constraints.setRequiresDeviceIdle(false);
				}

				int pollingDelayMinutes = Math.max(tinyDB.getInt("pollingDelayMinutes", StaticGlobalVariables.defaultPollingDelay), 15);

				PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(NotificationsWorker.class, pollingDelayMinutes, TimeUnit.MINUTES)
					.setConstraints(constraints.build())
					.addTag(context.getPackageName())
					.build();

				WorkManager.getInstance(context).enqueueUniquePeriodicWork(context.getPackageName(), ExistingPeriodicWorkPolicy.KEEP, periodicWorkRequest);

			}
		}
	}
}