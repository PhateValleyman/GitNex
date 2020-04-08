package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.NotificationsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.helpers.Toasty;
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

public class NotificationsFragment extends Fragment {
	private ListView listView;
	private List<NotificationThread> notificationThreads;
	private NotificationsAdapter notificationsAdapter;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.fragment_notifications, container, false);

		notificationThreads = new ArrayList<>();
		notificationsAdapter = new NotificationsAdapter(getContext(), notificationThreads);

		listView = v.findViewById(R.id.notifications);
		listView.setAdapter(notificationsAdapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				Toasty.info(getContext(), ((TextView) view.findViewById(R.id.repository)).getText().toString());
			}
		});

		loadNotifications();

		SwipeRefreshLayout pullToRefresh = v.findViewById(R.id.pullToRefresh);
		pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

			@Override
			public void onRefresh() {

				loadNotifications();
				pullToRefresh.setRefreshing(false);

			}

		});

		return v;
	}

	private void loadNotifications() {

		TinyDB tinyDb = new TinyDB(getContext());

		final String instanceUrl = tinyDb.getString("instanceUrl");
		final String loginUid = tinyDb.getString("loginUid");
		final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
		final String locale = tinyDb.getString("locale");

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, -10);

		Call<List<NotificationThread>> call = RetrofitClient.getInstance(instanceUrl, getContext())
				.getApiInterface()
				.getNotificationThreads(instanceToken, "false", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", new Locale(locale)).format(calendar.getTime()), "", "1", "50");

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

			}

			@Override
			public void onFailure(@NonNull Call<List<NotificationThread>> call, @NonNull Throwable t) {
				Log.e("onError", t.toString());
			}

		});

	}



}
