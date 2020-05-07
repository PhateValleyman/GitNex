package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.NotificationsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.VersionCheck;
import org.mian.gitnex.models.NotificationThread;
import org.mian.gitnex.util.TinyDB;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Author opyale
 */

public class NotificationsFragment extends Fragment {

	private static int notificationsSupported = -1;

	private List<NotificationThread> notificationThreads;
	private NotificationsAdapter notificationsAdapter;

	private ProgressBar progressBar;
	private TextView noDataNotifications;
	private SwipeRefreshLayout pullToRefresh;

	private Context context;
	private TinyDB tinyDB;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.fragment_notifications, container, false);

		context = getContext();
		tinyDB = new TinyDB(context);

		progressBar = v.findViewById(R.id.progress_bar);
		noDataNotifications = v.findViewById(R.id.noDataNotifications);

		if(notificationsSupported == -1) {

			String currentVersion = tinyDB.getString("giteaVersion");

			if(tinyDB.getBoolean("loggedInMode") && !currentVersion.isEmpty()) {
				notificationsSupported = VersionCheck.compareVersion("1.12.0", currentVersion) >= 1 ? 1 : 0;
			}

		}

		notificationThreads = new ArrayList<>();
		notificationsAdapter = new NotificationsAdapter(context, notificationThreads);

		ListView listView = v.findViewById(R.id.notifications);
		listView.setAdapter(notificationsAdapter);
		listView.setOnItemClickListener((parent, view, position, id) -> {

			Toasty.info(context, ((TextView) view.findViewById(R.id.repository)).getText().toString());
		});

		pullToRefresh = v.findViewById(R.id.pullToRefresh);
		pullToRefresh.setOnRefreshListener(() -> {

			loadNotifications();
			pullToRefresh.setRefreshing(false);

		});

		loadNotifications();

		return v;
	}

	private void loadNotifications() {

		if(notificationsSupported != -1) {

			final String instanceUrl = tinyDB.getString("instanceUrl");
			final String loginUid = tinyDB.getString("loginUid");
			final String instanceToken = "token " + tinyDB.getString(loginUid + "-token");
			final String locale = tinyDB.getString("locale");

			Calendar calendar = Calendar.getInstance();

			String before = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", new Locale(locale))
					.format(calendar.getTime());

			Call<List<NotificationThread>> call = RetrofitClient.getInstance(instanceUrl, getContext())
					.getApiInterface()
					.getNotificationThreads(instanceToken, "true", "", before, "1", "50");

			call.enqueue(new Callback<List<NotificationThread>>() {

				@Override
				public void onResponse(@NonNull Call<List<NotificationThread>> call, @NonNull Response<List<NotificationThread>> response) {

					if(call.isExecuted() && response.code() == 200) {

						if(response.body() != null) {

							notificationThreads.clear();
							notificationThreads.addAll(response.body());
							notificationsAdapter.notifyDataSetChanged();

						}

					} else {

						Log.e("onError", String.valueOf(response.code()));

					}

					progressBar.setVisibility(View.GONE);

					if(notificationThreads.size() > 0) {

						noDataNotifications.setVisibility(View.GONE);
					} else {

						noDataNotifications.setVisibility(View.VISIBLE);
					}

				}

				@Override
				public void onFailure(@NonNull Call<List<NotificationThread>> call, @NonNull Throwable t) {
					Log.e("onError", t.toString());
				}

			});

		} else {

			pullToRefresh.setEnabled(false);
			progressBar.setVisibility(View.GONE);

			noDataNotifications.setText("Notifications are not supported.");
			noDataNotifications.setVisibility(View.VISIBLE);

		}

	}

}
