package org.mian.gitnex.fragments;

import android.content.Intent;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.CreateRepoActivity;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.ReposListAdapter;
import org.mian.gitnex.databinding.FragmentRepositoriesBinding;
import org.mian.gitnex.helpers.Constants;
import org.mian.gitnex.helpers.DividerItemDecorator;
import org.mian.gitnex.viewmodels.RepositoriesViewModel;

/**
 * @author M M Arif
 */

public class RepositoriesFragment extends Fragment {

	private FragmentRepositoriesBinding fragmentRepositoriesBinding;
	private ReposListAdapter adapter;
	private int page = 1;
	private final int resultLimit = Constants.resultLimitNewGiteaInstances;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		fragmentRepositoriesBinding = FragmentRepositoriesBinding.inflate(inflater, container, false);

		setHasOptionsMenu(true);
		((MainActivity) requireActivity()).setActionBarTitle(getResources().getString(R.string.navRepos));

		fragmentRepositoriesBinding.addNewRepo.setOnClickListener(view -> {
			Intent intent = new Intent(view.getContext(), CreateRepoActivity.class);
			startActivity(intent);
		});

		fragmentRepositoriesBinding.recyclerView.setHasFixedSize(true);
		fragmentRepositoriesBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

		RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecorator(ContextCompat.getDrawable(requireContext(), R.drawable.shape_list_divider));
		fragmentRepositoriesBinding.recyclerView.addItemDecoration(dividerItemDecoration);

		fragmentRepositoriesBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

			page = 1;
			fragmentRepositoriesBinding.pullToRefresh.setRefreshing(false);
			fetchDataAsync();
			fragmentRepositoriesBinding.progressBar.setVisibility(View.VISIBLE);
		}, 50));

		fetchDataAsync();

		return fragmentRepositoriesBinding.getRoot();
	};

	private void fetchDataAsync() {

		RepositoriesViewModel reposModel = new ViewModelProvider(this).get(RepositoriesViewModel.class);

		reposModel.getRepositories(page, resultLimit, null, "repos", null, getContext()).observe(getViewLifecycleOwner(), reposListMain -> {

			adapter = new ReposListAdapter(reposListMain, getContext());
			adapter.setLoadMoreListener(new ReposListAdapter.OnLoadMoreListener() {

				@Override
				public void onLoadMore() {

					page += 1;
					RepositoriesViewModel.loadMoreRepos(page, resultLimit, null, "repos", null, getContext(), adapter);
					fragmentRepositoriesBinding.progressBar.setVisibility(View.VISIBLE);
				}

				@Override
				public void onLoadFinished() {

					fragmentRepositoriesBinding.progressBar.setVisibility(View.GONE);
				}
			});

			if(adapter.getItemCount() > 0) {
				fragmentRepositoriesBinding.recyclerView.setAdapter(adapter);
				fragmentRepositoriesBinding.noData.setVisibility(View.GONE);
			}
			else {
				adapter.notifyDataChanged();
				fragmentRepositoriesBinding.recyclerView.setAdapter(adapter);
				fragmentRepositoriesBinding.noData.setVisibility(View.VISIBLE);
			}

			fragmentRepositoriesBinding.progressBar.setVisibility(View.GONE);
		});
	}

    @Override
    public void onResume() {
        super.onResume();

        if(MainActivity.repoCreated) {
			page = 1;
            fetchDataAsync();
	        MainActivity.repoCreated = false;
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(fragmentRepositoriesBinding.recyclerView.getAdapter() != null) {
                    adapter.getFilter().filter(newText);
                }
                return false;
            }
        });
    }
}
