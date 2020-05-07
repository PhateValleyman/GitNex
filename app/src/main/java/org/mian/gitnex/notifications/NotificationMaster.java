package org.mian.gitnex.notifications;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import org.mian.gitnex.helpers.VersionCheck;
import org.mian.gitnex.util.TinyDB;

/**
 * Author opyale
 */

public class NotificationMaster {
	private static int notificationsSupported = -1;

	public NotificationMaster() {

	}

	private static void checkVersion(Context context) {

		TinyDB tinyDB = new TinyDB(context);
		String currentVersion = tinyDB.getString("giteaVersion");

		if(tinyDB.getBoolean("loggedInMode") && !currentVersion.isEmpty()) {
			notificationsSupported = VersionCheck.compareVersion("1.12.0", currentVersion) >= 1 ? 1 : 0;
		}

	}

	public static void hireWorker(Context context) {

		if(notificationsSupported == -1) {
			checkVersion(context);
		}

		if(notificationsSupported == 1) {

			WorkManager.getInstance(context).cancelAllWorkByTag("gitnex-notifications");

			Constraints constraints = new Constraints.Builder()
					.setRequiredNetworkType(NetworkType.CONNECTED)
					.setRequiresBatteryNotLow(false)
					.setRequiresDeviceIdle(false)
					.setRequiresStorageNotLow(false)
					.setRequiresCharging(false)
					.build();

			OneTimeWorkRequest notificationRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
					.setConstraints(constraints)
					.addTag("gitnex-notifications")
					.build();

			WorkManager.getInstance(context).enqueue(notificationRequest);

		}

	}

}
