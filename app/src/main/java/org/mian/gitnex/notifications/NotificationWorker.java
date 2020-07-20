package org.mian.gitnex.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.gson.JsonElement;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.TinyDB;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author opyale
 */

public class NotificationWorker extends Worker {

	private static final int NOTIFICATION_ID = 71951418;
	private static final long[] VIBRATION_PATTERN = new long[]{1000, 1000};

	private Context context;
	private TinyDB tinyDB;

	public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {

		super(context, workerParams);

		this.context = context;
		this.tinyDB = new TinyDB(context);

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

				if(response.code() == 200) {

					assert response.body() != null;

					int previousUnreadNotifications = tinyDB.getInt("previousUnreadNotifications");
					int unreadNotifications = response.body().getAsJsonObject().get("new").getAsInt();

					Log.i("ReceivedNotifications", String.valueOf(unreadNotifications));

					if(previousUnreadNotifications != unreadNotifications) {

						if(unreadNotifications > previousUnreadNotifications) {
							sendNotification(unreadNotifications);
						}

						tinyDB.putInt("previousUnreadNotifications", unreadNotifications);
					}

				}
				else {

					Log.e("onError", String.valueOf(response.code()));
				}
			}

			@Override
			public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {

				Log.e("onError", t.toString());
			}

		});

		return Result.success();

	}

	private void sendNotification(int notificationsCount) {

		Intent intent = new Intent(context, MainActivity.class);
		intent.putExtra("launchFragment", "notifications");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		if(notificationManager != null) {

			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

				NotificationChannel notificationChannel = new NotificationChannel(context.getPackageName(), context.getString(R.string.app_name),
					NotificationManager.IMPORTANCE_HIGH);

				notificationChannel.enableLights(true);
				notificationChannel.setLightColor(Color.GREEN);
				notificationChannel.enableVibration(true);
				notificationChannel.setVibrationPattern(VIBRATION_PATTERN);

				notificationManager.createNotificationChannel(notificationChannel);
			}

			String notificationsTitle = context.getString(R.string.notificationsTitle);
			String notificationsText = context.getString(R.string.notificationsText);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(context, context.getPackageName())
				.setSmallIcon(R.drawable.gitnex_transparent).setContentTitle(notificationsTitle)
				.setContentText(String.format(notificationsText, notificationsCount))
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).setPriority(NotificationCompat.PRIORITY_HIGH)
				.setContentIntent(pendingIntent).setVibrate(VIBRATION_PATTERN).setAutoCancel(true);

			notificationManager.notify(NOTIFICATION_ID, builder.build());

		}
	}

}
