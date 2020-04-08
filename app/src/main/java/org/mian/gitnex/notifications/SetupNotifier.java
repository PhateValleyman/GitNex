package org.mian.gitnex.notifications;

import android.content.Context;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

/**
 * Author opyale
 */

public class SetupNotifier {
	public SetupNotifier() {

	}

	public static void setup(Context context) {

		if(!WorkManager.getInstance(context).getWorkInfosByTagLiveData("gitnex-notifications").hasActiveObservers()) {
			Constraints constraints = new Constraints.Builder()
					.setRequiredNetworkType(NetworkType.CONNECTED)
					.setRequiresBatteryNotLow(false)
					.setRequiresDeviceIdle(false)
					.setRequiresStorageNotLow(false)
					.setRequiresCharging(false)
					.build();

			OneTimeWorkRequest notificationRequest = new OneTimeWorkRequest.Builder(NotifierWorker.class)
					.setConstraints(constraints)
					.addTag("gitnex-notifications")
					.build();

			WorkManager.getInstance(context).enqueue(notificationRequest);
		}

	}
}
