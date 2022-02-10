package org.mian.gitnex.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.mian.gitnex.adapters.AdminCronTasksAdapter;
import org.mian.gitnex.databinding.ActivityAdminCronTasksBinding;
import org.mian.gitnex.viewmodels.AdminCronTasksViewModel;

/**
 * Author M M Arif
 */

public class AdminCronTasksActivity extends BaseActivity {

	private View.OnClickListener onClickListener;
	private AdminCronTasksAdapter adapter;

	private ActivityAdminCronTasksBinding activityAdminCronTasksBinding;

	public static final int PAGE = 1;
	public static final int LIMIT = 50;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		activityAdminCronTasksBinding = ActivityAdminCronTasksBinding.inflate(getLayoutInflater());
		setContentView(activityAdminCronTasksBinding.getRoot());

		initCloseListener();
		activityAdminCronTasksBinding.close.setOnClickListener(onClickListener);

		Toolbar toolbar = activityAdminCronTasksBinding.toolbar;
		setSupportActionBar(toolbar);

		activityAdminCronTasksBinding.recyclerView.setHasFixedSize(true);
		activityAdminCronTasksBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(activityAdminCronTasksBinding.recyclerView.getContext(),
			DividerItemDecoration.VERTICAL);
		activityAdminCronTasksBinding.recyclerView.addItemDecoration(dividerItemDecoration);

		activityAdminCronTasksBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			activityAdminCronTasksBinding.pullToRefresh.setRefreshing(false);
			AdminCronTasksViewModel.loadCronTasksList(ctx, getAccount().getAuthorization(), PAGE, LIMIT);

		}, 500));

		fetchDataAsync(ctx, getAccount().getAuthorization());
	}

	private void fetchDataAsync(Context ctx, String instanceToken) {

		AdminCronTasksViewModel cronTasksViewModel = new ViewModelProvider(this).get(AdminCronTasksViewModel.class);

		cronTasksViewModel.getCronTasksList(ctx, instanceToken, PAGE, LIMIT).observe(this, cronTasksListMain -> {

			adapter = new AdminCronTasksAdapter(ctx, cronTasksListMain);

			if(adapter.getItemCount() > 0) {

				activityAdminCronTasksBinding.recyclerView.setVisibility(View.VISIBLE);
				activityAdminCronTasksBinding.recyclerView.setAdapter(adapter);
				activityAdminCronTasksBinding.noData.setVisibility(View.GONE);
			}
			else {

				activityAdminCronTasksBinding.recyclerView.setVisibility(View.GONE);
				activityAdminCronTasksBinding.noData.setVisibility(View.VISIBLE);
			}

		});

	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}
}
