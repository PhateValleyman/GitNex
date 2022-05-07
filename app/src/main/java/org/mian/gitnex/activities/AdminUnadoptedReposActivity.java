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
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.AdminUnadoptedReposAdapter;
import org.mian.gitnex.databinding.ActivityAdminCronTasksBinding;
import org.mian.gitnex.viewmodels.AdminUnadoptedReposViewModel;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author M M Arif
 * @author qwerty287
 */

public class AdminUnadoptedReposActivity extends BaseActivity {

	private AdminUnadoptedReposViewModel viewModel;
	private View.OnClickListener onClickListener;
	private AdminUnadoptedReposAdapter adapter;

	private ActivityAdminCronTasksBinding binding;

	public int PAGE = 1;
	public final int LIMIT = 50;
	private boolean reload = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		binding = ActivityAdminCronTasksBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		viewModel = new ViewModelProvider(this).get(AdminUnadoptedReposViewModel.class);

		initCloseListener();
		binding.close.setOnClickListener(onClickListener);

		Toolbar toolbar = binding.toolbar;
		setSupportActionBar(toolbar);

		binding.toolbarTitle.setText(R.string.unadoptedRepos);

		binding.recyclerView.setHasFixedSize(true);
		binding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.recyclerView.getContext(),
			DividerItemDecoration.VERTICAL);
		binding.recyclerView.addItemDecoration(dividerItemDecoration);

		binding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			binding.pullToRefresh.setRefreshing(false);
			PAGE = 1;
			binding.progressBar.setVisibility(View.VISIBLE);
			reload = true;
			viewModel.loadRepos(ctx, PAGE, LIMIT, null);

		}, 500));

		adapter = new AdminUnadoptedReposAdapter(new ArrayList<>(), () -> {
			PAGE = 1;
			binding.progressBar.setVisibility(View.VISIBLE);
			reload = true;
			viewModel.loadRepos(ctx, PAGE, LIMIT, null);
		}, () -> {
			PAGE += 1;
			binding.progressBar.setVisibility(View.VISIBLE);
			viewModel.loadRepos(ctx, PAGE, LIMIT, null);
		});

		binding.recyclerView.setAdapter(adapter);

		fetchDataAsync(ctx);
	}

	private void fetchDataAsync(Context ctx) {

		binding.progressBar.setVisibility(View.VISIBLE);
		AtomicInteger prevSize = new AtomicInteger();

		viewModel.getUnadoptedRepos(ctx, PAGE, LIMIT, null).observe(this, list -> {

			binding.progressBar.setVisibility(View.GONE);

			boolean hasMore = reload || list.size() > prevSize.get();
			reload = false;

			prevSize.set(list.size());

			if(list.size() > 0) {

				adapter.updateList(list);
				adapter.setHasMore(hasMore);
				binding.recyclerView.setVisibility(View.VISIBLE);
				binding.noData.setVisibility(View.GONE);
			}
			else {

				binding.recyclerView.setVisibility(View.GONE);
				binding.noData.setVisibility(View.VISIBLE);
			}

		});

	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}
}
