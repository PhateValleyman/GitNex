package org.mian.gitnex.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.LoginActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.util.TinyDB;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author opyale
 */

public class NotifierWorker extends Worker {
	private static final int NOTIFICATION_ID = "opyale".length() * 4;

	private Context context;
	private TinyDB tinyDB;

	public NotifierWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);

		this.context = context;
		tinyDB = new TinyDB(context);
	}

	@NonNull
	@Override
	public Result doWork() {

		String instanceUrl = tinyDB.getString("instanceUrl");
		String token = "token " + tinyDB.getString(tinyDB.getString("loginUid") + "-token");

		Call<JsonElement> call = RetrofitClient.getInstance(instanceUrl, context).getApiInterface().checkUnreadNotifications(token);

		call.enqueue(new Callback<JsonElement>() {

			@Override
			public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {

				if(call.isExecuted()) {

					if(response.code() == 200) {

						if(response.body() != null) {

							int previousUnreadNotifications = tinyDB.getInt("previousUnreadNotifications");
							int unreadNotifications = response.body().getAsJsonObject().get("new").getAsInt();

							Log.i("ReceivedNotifications", String.valueOf(unreadNotifications));

							if(previousUnreadNotifications != unreadNotifications) {

								if(unreadNotifications > previousUnreadNotifications) {
									sendNotification(unreadNotifications - previousUnreadNotifications);
								}

								tinyDB.putInt("previousUnreadNotifications", unreadNotifications);
							}

						}

					} else if(response.code() == 204) {

						Log.i("ReceivedNotifications", "0");
						tinyDB.putInt("previousUnreadNotifications", 0);
					} else {

						Log.e("NotifierHttpError", String.valueOf(response.code()));
					}

				}

			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) { t.printStackTrace(); }

		});

		new Thread(() -> {

			sleep();
			SetupNotifier.setup(context);

		}).start();

		return Result.success();

	}

	private void sleep() {

		try {
			Thread.sleep(tinyDB.getInt("pollingDelaySeconds") * 1000);
		}
		catch(InterruptedException e) {
			Log.e("onFailure", e.toString());
		}
	}

	private void sendNotification(int notificationsCount) {

		Intent intent = new Intent(context, LoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "gitnex_notification_channel")
				.setSmallIcon(R.drawable.app_logo_foreground)
				.setContentTitle("You've received new notification(s)")
				.setContentText("You've got " + notificationsCount + " notification(s)!")
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.setContentIntent(pendingIntent)
				.setAutoCancel(true);

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		if(notificationManager != null) {
			notificationManager.notify(NOTIFICATION_ID, builder.build());
		}

	}

}
