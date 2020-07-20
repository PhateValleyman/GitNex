package org.mian.gitnex.notifications;

import android.content.Context;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.models.NotificationThread;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Author opyale
 */

public class NotificationsApi {

	public enum NotificationStatus {READ, UNREAD, PINNED}

	private TinyDB tinyDB;
	private Context context;
	private String instanceUrl;
	private String instanceToken;

	public NotificationsApi(Context context) {

		this.context = context;
		this.tinyDB = new TinyDB(context);

		String loginUid = tinyDB.getString("loginUid");

		instanceUrl = tinyDB.getString("instanceUrl");
		instanceToken = "token " + tinyDB.getString(loginUid + "-token");

	}

	public void setNotificationStatus(NotificationThread notificationThread, NotificationStatus notificationStatus) throws IOException {

		Call<ResponseBody> call = RetrofitClient.getInstance(instanceUrl, context).getApiInterface()
			.markNotificationThreadAsRead(instanceToken, notificationThread.getId(), notificationStatus.name());

		if(!call.execute().isSuccessful()) {

			throw new IllegalStateException();
		}
	}

	public boolean setAllNotificationsRead(Date date) throws IOException {

		String locale = tinyDB.getString("locale");
		String currentTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", new Locale(locale)).format(date);

		Call<ResponseBody> call = RetrofitClient.getInstance(instanceUrl, context).getApiInterface()
			.markNotificationThreadsAsRead(instanceToken, currentTime, true, new String[]{"unread", "pinned"}, "read");

		return call.execute().isSuccessful();

	}

}
