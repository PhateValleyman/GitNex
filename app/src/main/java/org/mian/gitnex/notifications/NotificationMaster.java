package org.mian.gitnex.notifications;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.models.GiteaVersion;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author opyale
 */

public class NotificationMaster {
	private String notificationsSupported;

	public NotificationMaster() {

	}

	private void checkVersion(Context context) {

		Call<GiteaVersion> versionCall = RetrofitClient.getInstance("", context).getApiInterface().getGiteaVersion();

		versionCall.enqueue(new Callback<GiteaVersion>() {

			@Override
			public void onResponse(@NonNull Call<GiteaVersion> call, @NonNull Response<GiteaVersion> response) {

			}

			@Override
			public void onFailure(@NonNull Call<GiteaVersion> call,@NonNull Throwable t) {

			}
		});

	}

	public void hireWorker(Context context) {

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
