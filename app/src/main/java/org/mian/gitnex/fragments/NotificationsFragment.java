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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.apache.commons.lang3.StringUtils;
import org.gitnex.tea4j.models.NotificationThread;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.IssueDetailActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.NotificationsAdapter;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.databinding.FragmentNotificationsBinding;
import org.mian.gitnex.helpers.AppUtil;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.SimpleCallback;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.helpers.contexts.IssueContext;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Author opyale
 * Modified M M Arif
 */

public class NotificationsFragment extends Fragment implements NotificationsAdapter.OnNotificationClickedListener, NotificationsAdapter.OnMoreClickedListener {

	private FragmentNotificationsBinding viewBinding;
	private final List<NotificationThread> notificationThreads = new ArrayList<>();
	private NotificationsAdapter notificationsAdapter;

	private Activity activity;
	private Context context;
	private Menu menu;

	private int pageCurrentIndex = 1;
	private int pageResultLimit;
	private String currentFilterMode = "unread";
	private String instanceToken;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		viewBinding = FragmentNotificationsBinding.inflate(inflater, container, false);
		setHasOptionsMenu(true);

		activity = requireActivity();
		context = getContext();

		instanceToken = ((BaseActivity) requireActivity()).getAccount().getAuthorization();

		pageResultLimit = Constants.getCurrentResultLimit(context);

		notificationsAdapter = new NotificationsAdapter(context, notificationThreads, this, this);

		LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(viewBinding.notifications.getContext(), DividerItemDecoration.VERTICAL);

		viewBinding.notifications.setHasFixedSize(true);
		viewBinding.notifications.setLayoutManager(linearLayoutManager);
		viewBinding.notifications.setAdapter(notificationsAdapter);
		viewBinding.notifications.addItemDecoration(dividerItemDecoration);

		notificationsAdapter.setLoadMoreListener(() -> {
			pageCurrentIndex++;
			loadNotifications(true);
		});

		viewBinding.notifications.addOnScrollListener(new RecyclerView.OnScrollListener() {

			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
				if(currentFilterMode.equalsIgnoreCase("unread")) {
					if(dy > 0 && viewBinding.markAllAsRead.isShown()) {
						viewBinding.markAllAsRead.setVisibility(View.GONE);
					}
					else if(dy < 0) {
						viewBinding.markAllAsRead.setVisibility(View.VISIBLE);
					}
				}
			}

			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
				super.onScrollStateChanged(recyclerView, newState);
			}
		});

		viewBinding.markAllAsRead.setOnClickListener(v1 ->
			RetrofitClient.getApiInterface(context)
				.markNotificationThreadsAsRead(instanceToken, AppUtil.getTimestampFromDate(context, new Date()), true, new String[]{"unread", "pinned"}, "read")
				.enqueue((SimpleCallback<Void>) (call, voidResponse) -> {

					if(voidResponse.isPresent() && voidResponse.get().isSuccessful()) {
						Toasty.success(context, getString(R.string.markedNotificationsAsRead));
						pageCurrentIndex = 1;
						loadNotifications(false);
					} else {
						activity.runOnUiThread(() -> Toasty.error(context, getString(R.string.genericError)));
					}
		}));

		viewBinding.pullToRefresh.setOnRefreshListener(() -> {
			viewBinding.pullToRefresh.setRefreshing(false);
			pageCurrentIndex = 1;
			loadNotifications(false);
		});

		loadNotifications(true);
		return viewBinding.getRoot();
	}

	private void loadNotifications(boolean append) {

		viewBinding.noDataNotifications.setVisibility(View.GONE);
		viewBinding.progressBar.setVisibility(View.VISIBLE);
		String[] filter = currentFilterMode.equals("read") ?
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

					if(listResponse.get().body().size() > 0) {
						notificationThreads.addAll(listResponse.get().body());
						notificationsAdapter.notifyDataSetChanged();
					}
					else {
						notificationsAdapter.setMoreDataAvailable(false);
					}

				}

				AppUtil.setMultiVisibility(View.GONE, viewBinding.progressBar);

				if(notificationThreads.isEmpty()) {
					viewBinding.noDataNotifications.setVisibility(View.VISIBLE);
				}
				else {
					viewBinding.noDataNotifications.setVisibility(View.GONE);
				}

				if(currentFilterMode.equalsIgnoreCase("unread")) {
					if(notificationThreads.isEmpty()) {
						viewBinding.markAllAsRead.setVisibility(View.GONE);
					}
					else {
						viewBinding.markAllAsRead.setVisibility(View.VISIBLE);
					}
				}
			});
	}

	private void changeFilterMode() {

		int filterIcon = currentFilterMode.equalsIgnoreCase("read") ?
			R.drawable.ic_filter_closed :
			R.drawable.ic_filter;

		menu.getItem(0).setIcon(filterIcon);

		if(currentFilterMode.equalsIgnoreCase("read")) {
			viewBinding.markAllAsRead.setVisibility(View.GONE);
		} else {
			viewBinding.markAllAsRead.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		this.menu = menu;
		inflater.inflate(R.menu.filter_menu_notifications, menu);
		changeFilterMode();

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {

		if(item.getItemId() == R.id.filterNotifications) {

			BottomSheetNotificationsFilterFragment bottomSheetNotificationsFilterFragment = new BottomSheetNotificationsFilterFragment();
			bottomSheetNotificationsFilterFragment.show(getChildFragmentManager(), "notificationsFilterBottomSheet");
			bottomSheetNotificationsFilterFragment.setOnClickListener((text) -> {
				currentFilterMode = text;
				changeFilterMode();
				pageCurrentIndex = 1;
				loadNotifications(false);

			});
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onNotificationClicked(NotificationThread notificationThread) {

		if(notificationThread.isUnread() && !notificationThread.isPinned()) {
			RetrofitClient.getApiInterface(context).markNotificationThreadAsRead(instanceToken, notificationThread.getId(), "read").enqueue((SimpleCallback<Void>) (call, voidResponse) -> {
				if(voidResponse.isPresent() && voidResponse.get().isSuccessful()) {
					pageCurrentIndex = 1;
					loadNotifications(false);
				}
			});
		}

		if(StringUtils.containsAny(notificationThread.getSubject().getType().toLowerCase(), "pull", "issue")) {

			RepositoryContext repo = new RepositoryContext(notificationThread.getRepository(), context);
			String issueUrl = notificationThread.getSubject().getUrl();

			int currentActiveAccountId = TinyDB.getInstance(requireContext()).getInt("currentActiveAccountId");
			RepositoriesApi repositoryData = BaseApi.getInstance(context, RepositoriesApi.class);

			Integer count = repositoryData.checkRepository(currentActiveAccountId, repo.getOwner(), repo.getName());

			if(count == 0) {
				long id = repositoryData.insertRepository(currentActiveAccountId, repo.getOwner(), repo.getName());
				repo.setRepositoryId((int) id);
			}
			else {
				Repository data = repositoryData.getRepository(currentActiveAccountId, repo.getOwner(), repo.getName());
				repo.setRepositoryId(data.getRepositoryId());
			}

			Intent intent = new IssueContext(
				new RepositoryContext(notificationThread.getRepository(), context),
				Integer.parseInt(issueUrl.substring(issueUrl.lastIndexOf("/") + 1)),
				notificationThread.getSubject().getType()
			).getIntent(context, IssueDetailActivity.class);
			intent.putExtra("openedFromLink", "true");
			startActivity(intent);
		} else if(notificationThread.getSubject().getType().equalsIgnoreCase("repository")) {
			startActivity(new RepositoryContext(notificationThread.getRepository(), context).getIntent(context, RepoDetailActivity.class));
		}
	}

	@Override
	public void onMoreClicked(NotificationThread notificationThread) {
		BottomSheetNotificationsFragment bottomSheetNotificationsFragment = new BottomSheetNotificationsFragment();
		bottomSheetNotificationsFragment.onAttach(context, notificationThread, () -> {
			pageCurrentIndex = 1;
			loadNotifications(false);
		});
		bottomSheetNotificationsFragment.show(getChildFragmentManager(), "notificationsBottomSheet");
	}
}
