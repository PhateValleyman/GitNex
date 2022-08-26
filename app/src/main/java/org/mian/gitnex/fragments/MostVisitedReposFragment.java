package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.MostVisitedReposAdapter;
import org.mian.gitnex.database.api.BaseApi;
import org.mian.gitnex.database.api.RepositoriesApi;
import org.mian.gitnex.database.models.Repository;
import org.mian.gitnex.databinding.FragmentDraftsBinding;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author M M Arif
 */

public class MostVisitedReposFragment extends Fragment {

	private FragmentDraftsBinding fragmentDraftsBinding;
	private Context ctx;
	private MostVisitedReposAdapter adapter;
	private RepositoriesApi repositoriesApi;
	private List<Repository> mostVisitedReposList;
	private int currentActiveAccountId;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentDraftsBinding = FragmentDraftsBinding.inflate(inflater, container, false);

		ctx = getContext();
		setHasOptionsMenu(true);

		((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navMostVisited));

		TinyDB tinyDb = TinyDB.getInstance(ctx);

		mostVisitedReposList = new ArrayList<>();
		repositoriesApi = BaseApi.getInstance(ctx, RepositoriesApi.class);

		fragmentDraftsBinding.recyclerView.setHasFixedSize(true);
		fragmentDraftsBinding.recyclerView.setLayoutManager(new LinearLayoutManager(ctx));

		adapter = new MostVisitedReposAdapter(ctx, mostVisitedReposList);
		currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");
		fragmentDraftsBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			mostVisitedReposList.clear();
			fetchDataAsync(currentActiveAccountId);
		}, 250));

		fetchDataAsync(currentActiveAccountId);

		return fragmentDraftsBinding.getRoot();
	}

	private void fetchDataAsync(int accountId) {

		repositoriesApi.fetchAllMostVisited(accountId).observe(getViewLifecycleOwner(), mostVisitedRepos -> {

			fragmentDraftsBinding.pullToRefresh.setRefreshing(false);
			assert mostVisitedRepos != null;
			if(mostVisitedRepos.size() > 0) {

				mostVisitedReposList.clear();
				fragmentDraftsBinding.noData.setVisibility(View.GONE);
				mostVisitedReposList.addAll(mostVisitedRepos);
				adapter.notifyDataChanged();
				fragmentDraftsBinding.recyclerView.setAdapter(adapter);
			}
			else {

				fragmentDraftsBinding.noData.setVisibility(View.VISIBLE);
			}
		});
	}

	public void resetAllRepositoryCounter(int accountId) {

		if(mostVisitedReposList.size() > 0) {

			Objects.requireNonNull(BaseApi.getInstance(ctx, RepositoriesApi.class)).resetAllRepositoryMostVisited(accountId);
			mostVisitedReposList.clear();
			adapter.notifyDataChanged();
			Toasty.success(ctx, getResources().getString(R.string.resetMostReposCounter));
		}
		else {
			Toasty.warning(ctx, getResources().getString(R.string.noDataFound));
		}
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

		inflater.inflate(R.menu.reset_menu, menu);
		inflater.inflate(R.menu.search_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);

		MenuItem searchItem = menu.findItem(R.id.action_search);
		SearchView searchView = (SearchView) searchItem.getActionView();
		searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {

				return false;
			}

			@Override
			public boolean onQueryTextChange(String newText) {

				filter(newText);
				return false;
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if(item.getItemId() == R.id.reset_menu_item) {

			if(mostVisitedReposList.size() == 0) {
				Toasty.warning(ctx, getResources().getString(R.string.noDataFound));
			}
			else {
				new MaterialAlertDialogBuilder(ctx).setTitle(R.string.reset).setMessage(R.string.resetCounterAllDialogMessage).setPositiveButton(R.string.reset, (dialog, which) -> {

					resetAllRepositoryCounter(currentActiveAccountId);
					dialog.dismiss();
				}).setNeutralButton(R.string.cancelButton, null).show();
			}
		}

		return super.onOptionsItemSelected(item);
	}

	private void filter(String text) {

		List<Repository> arr = new ArrayList<>();

		for(Repository d : mostVisitedReposList) {

			if(d == null || d.getRepositoryOwner() == null || d.getRepositoryName() == null) {
				continue;
			}

			if(d.getRepositoryOwner().toLowerCase().contains(text) || d.getRepositoryName().toLowerCase().contains(text)) {
				arr.add(d);
			}
		}

		adapter.updateList(arr);
	}

}
