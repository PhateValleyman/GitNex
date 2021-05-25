package org.mian.gitnex.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import org.apache.commons.lang3.StringUtils;
import org.gitnex.tea4j.models.NotificationThread;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.adapters.NotificationsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.FragmentNotificationsBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.InfiniteScrollListener;
import org.mian.gitnex.helpers.SimpleCallback;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author opyale
 */

public class NotificationsFragment extends Fragment implements NotificationsAdapter.OnNotificationClickedListener, NotificationsAdapter.OnMoreClickedListener, BottomSheetNotificationsFragment.OnOptionSelectedListener {

	private List<NotificationThread> notificationThreads;
	private NotificationsAdapter notificationsAdapter;

	private ExtendedFloatingActionButton markAllAsRead;
	private ProgressBar progressBar;
	private ProgressBar loadingMoreView;
	private TextView noDataNotifications;
	private SwipeRefreshLayout pullToRefresh;

	private Activity activity;
	private Context context;
	private TinyDB tinyDB;
	private Menu menu;

	private int pageCurrentIndex = 1;
	private int pageResultLimit;
	private String currentFilterMode = "unread";

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		FragmentNotificationsBinding fragmentNotificationsBinding = FragmentNotificationsBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);

		activity = requireActivity();
		context = getContext();
		tinyDB = TinyDB.getInstance(context);

		pageResultLimit = Constants.getCurrentResultLimit(context);
		tinyDB.putString("notificationsFilterState", currentFilterMode);

		markAllAsRead = fragmentNotificationsBinding.markAllAsRead;
		noDataNotifications = fragmentNotificationsBinding.noDataNotifications;
		loadingMoreView = fragmentNotificationsBinding.loadingMoreView;
		progressBar = fragmentNotificationsBinding.progressBar;

		notificationThreads = new ArrayList<>();
		notificationsAdapter = new NotificationsAdapter(context, notificationThreads, this, this);

		RecyclerView recyclerView = fragmentNotificationsBinding.notifications;
		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);

		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(linearLayoutManager);
		recyclerView.setAdapter(notificationsAdapter);
		recyclerView.addItemDecoration(dividerItemDecoration);
		recyclerView.addOnScrollListener(new InfiniteScrollListener(pageResultLimit, linearLayoutManager) {

			@Override
			public void onScrolledToEnd(int firstVisibleItemPosition) {
				pageCurrentIndex++;
				loadNotifications(true);
			}
		});

		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {

				if(currentFilterMode.equalsIgnoreCase("unread")) {

					if(dy > 0 && markAllAsRead.isShown()) {
						markAllAsRead.setVisibility(View.GONE);
					} else if(dy < 0) {
						markAllAsRead.setVisibility(View.VISIBLE);
					}
				}
			}

			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
			}

		});

		markAllAsRead.setOnClickListener(v1 ->
			RetrofitClient.getApiInterface(context)
				.markNotificationThreadsAsRead(Authorization.get(context), AppUtil.getTimestampFromDate(context, new Date()), true, new String[]{"unread", "pinned"}, "read")
				.enqueue((SimpleCallback<Void>) (call, voidResponse) -> {

					if(voidResponse.isPresent() && voidResponse.get().isSuccessful()) {
						Toasty.success(context, getString(R.string.markedNotificationsAsRead));
						loadNotifications(false);
					} else {
						activity.runOnUiThread(() -> Toasty.error(context, getString(R.string.genericError)));
					}
				}));

		pullToRefresh = fragmentNotificationsBinding.pullToRefresh;
		pullToRefresh.setOnRefreshListener(() -> {

			pageCurrentIndex = 1;
			loadNotifications(false);

		});

		loadNotifications(false);
		return fragmentNotificationsBinding.getRoot();

	}

	private void loadNotifications(boolean append) {

		noDataNotifications.setVisibility(View.GONE);

		if(pageCurrentIndex == 1 || !append) {

			notificationThreads.clear();
			notificationsAdapter.notifyDataSetChanged();
			pullToRefresh.setRefreshing(false);
			progressBar.setVisibility(View.VISIBLE);

		} else {

			loadingMoreView.setVisibility(View.VISIBLE);
		}

		String loginUid = tinyDB.getString("loginUid");
		String instanceToken = "token " + tinyDB.getString(loginUid + "-token");

		String[] filter = tinyDB.getString("notificationsFilterState").equals("read") ?
			new String[]{"pinned", "read"} :
			new String[]{"pinned", "unread"};

		RetrofitClient
			.getApiInterface(context)
			.getNotificationThreads(instanceToken, false, filter, Constants.defaultOldestTimestamp, "", pageCurrentIndex, pageResultLimit)
			.enqueue((SimpleCallback<List<NotificationThread>>) (call1, listResponse) -> {

				if(listResponse.isPresent() && listResponse.get().isSuccessful() && listResponse.get().body() != null) {
					if(!append) {
						notificationThreads.clear();
					}

					notificationThreads.addAll(listResponse.get().body());
					notificationsAdapter.notifyDataSetChanged();
				}

				AppUtil.setMultiVisibility(View.GONE, loadingMoreView, progressBar);
				pullToRefresh.setRefreshing(false);

				if(notificationThreads.isEmpty()) {
					noDataNotifications.setVisibility(View.VISIBLE);
				}
		});
	}

	private void changeFilterMode() {

		int filterIcon = currentFilterMode.equalsIgnoreCase("read") ?
			R.drawable.ic_filter_closed :
			R.drawable.ic_filter;

		menu.getItem(0).setIcon(filterIcon);

		if(currentFilterMode.equalsIgnoreCase("read")) {
			markAllAsRead.setVisibility(View.GONE);
		} else {
			markAllAsRead.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		this.menu = menu;

		inflater.inflate(R.menu.filter_menu_notifications, menu);

		currentFilterMode = tinyDB.getString("notificationsFilterState");
		changeFilterMode();

		super.onCreateOptionsMenu(menu, inflater);

	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {

		if(item.getItemId() == R.id.filterNotifications) {

			BottomSheetNotificationsFilterFragment bottomSheetNotificationsFilterFragment = new BottomSheetNotificationsFilterFragment();
			bottomSheetNotificationsFilterFragment.show(getChildFragmentManager(), "notificationsFilterBottomSheet");
			bottomSheetNotificationsFilterFragment.setOnDismissedListener(() -> {

				pageCurrentIndex = 1;
				currentFilterMode = tinyDB.getString("notificationsFilterState");

				changeFilterMode();
				loadNotifications(false);

			});

			return true;

		}

		return super.onOptionsItemSelected(item);

	}

	@Override
	public void onNotificationClicked(NotificationThread notificationThread) {

		RetrofitClient.getApiInterface(context)
			.markNotificationThreadAsRead(Authorization.get(context), notificationThread.getId(), "read")
			.enqueue((SimpleCallback<Void>) (call, voidResponse) -> {

				if(voidResponse.isPresent() && voidResponse.get().isSuccessful()) {
					loadNotifications(false);
				}
			});

		if(StringUtils.containsAny(notificationThread.getSubject().getType().toLowerCase(), "pull", "issue")) {

			Intent intent = new Intent(context, IssueDetailActivity.class);
			String issueUrl = notificationThread.getSubject().getUrl();

			tinyDB.putString("issueNumber", issueUrl.substring(issueUrl.lastIndexOf("/") + 1));
			tinyDB.putString("issueType", notificationThread.getSubject().getType());
			tinyDB.putString("repoFullName", notificationThread.getRepository().getFullName());

			startActivity(intent);

		}
	}

	@Override
	public void onMoreClicked(NotificationThread notificationThread) {

		BottomSheetNotificationsFragment bottomSheetNotificationsFragment = new BottomSheetNotificationsFragment();
		bottomSheetNotificationsFragment.onAttach(context, notificationThread, this);
		bottomSheetNotificationsFragment.show(getChildFragmentManager(), "notificationsBottomSheet");

	}

	@Override
	public void onSelected() {

		pageCurrentIndex = 1;
		loadNotifications(false);

	}

}
