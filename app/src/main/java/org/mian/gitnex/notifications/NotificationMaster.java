package org.mian.gitnex.notifications;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

/**
 * Author opyale
 */

public class NotificationMaster {
	public NotificationMaster() {

	}

	public static void hireWorker(Context context) {

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
