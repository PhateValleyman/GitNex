package org.mian.gitnex.actions;

import android.content.Context;
import org.gitnex.tea4j.models.NotificationThread;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.AppUtil;
import java.io.IOException;
import java.util.Date;
import retrofit2.Call;

/**
 * Author opyale
 */

public class NotificationsActions {

	public enum NotificationStatus {READ, UNREAD, PINNED}

	private final Context context;
	private final String instanceToken;

	public NotificationsActions(Context context) {
		this.context = context;
		instanceToken = ((BaseActivity) context).getAccount().getAuthorization();
	}

	public void setNotificationStatus(NotificationThread notificationThread, NotificationStatus notificationStatus) throws IOException {

		Call<Void> call = RetrofitClient.getApiInterface(context)
			.markNotificationThreadAsRead(instanceToken, notificationThread.getId(), notificationStatus.name());

		if(!call.execute().isSuccessful()) {

			throw new IllegalStateException();
		}
	}

	public boolean setAllNotificationsRead(Date date) throws IOException {

		Call<Void> call = RetrofitClient.getApiInterface(context)
			.markNotificationThreadsAsRead(instanceToken, AppUtil.getTimestampFromDate(context, date), true,
				new String[]{"unread", "pinned"}, "read");

		return call.execute().isSuccessful();

	}

}
