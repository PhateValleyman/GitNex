package org.mian.gitnex.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.gitnex.tea4j.v2.models.Release;
import org.mian.gitnex.R;
import org.mian.gitnex.activities.BaseActivity;
import org.mian.gitnex.activities.RepoDetailActivity;
import org.mian.gitnex.adapters.ReleasesAdapter;
import org.mian.gitnex.adapters.TagsAdapter;
import org.mian.gitnex.databinding.FragmentReleasesBinding;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.ReleasesViewModel;
import java.util.List;

/**
 * @author M M Arif
 */

public class ReleasesFragment extends Fragment {

	private ReleasesViewModel releasesViewModel;
    private ReleasesAdapter adapter;
    private TagsAdapter tagsAdapter;
    private RepositoryContext repository;
    private FragmentReleasesBinding fragmentReleasesBinding;
    private String releaseTag;
    private int page = 1;
    private int pageReleases = 1;

	public ReleasesFragment() {
    }

    public static ReleasesFragment newInstance(RepositoryContext repository) {
        ReleasesFragment fragment = new ReleasesFragment();
        fragment.setArguments(repository.getBundle());
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = RepositoryContext.fromBundle(requireArguments());
        releaseTag = requireActivity().getIntent().getStringExtra("releaseTagName");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentReleasesBinding = FragmentReleasesBinding.inflate(inflater, container, false);
	    releasesViewModel = new ViewModelProvider(this).get(ReleasesViewModel.class);

	    fragmentReleasesBinding.recyclerView.setHasFixedSize(true);
	    fragmentReleasesBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(fragmentReleasesBinding.recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
	    fragmentReleasesBinding.recyclerView.addItemDecoration(dividerItemDecoration);

	    fragmentReleasesBinding.pullToRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

		    fragmentReleasesBinding.pullToRefresh.setRefreshing(false);
	        if(repository.isReleasesViewTypeIsTag()) {
		        releasesViewModel.loadTagsList(repository.getOwner(), repository.getName(), getContext());
	        } else {
		        releasesViewModel.loadReleasesList(repository.getOwner(), repository.getName(), getContext());
	        }
	        fragmentReleasesBinding.progressBar.setVisibility(View.VISIBLE);

        }, 50));

        fetchDataAsync(repository.getOwner(), repository.getName());

        setHasOptionsMenu(true);
	    ((RepoDetailActivity) requireActivity()).setFragmentRefreshListenerReleases(type -> {
			if(type != null) repository.setReleasesViewTypeIsTag(type.equals("tags"));
			page = 1;
			pageReleases = 1;
		    if(repository.isReleasesViewTypeIsTag()) {
			    releasesViewModel.loadTagsList(repository.getOwner(), repository.getName(), getContext());
		    } else {
			    releasesViewModel.loadReleasesList(repository.getOwner(), repository.getName(), getContext());
		    }
		    fragmentReleasesBinding.progressBar.setVisibility(View.VISIBLE);
	    });

        return fragmentReleasesBinding.getRoot();
    }

    private void fetchDataAsync(String owner, String repo) {

        ReleasesViewModel releasesModel = new ViewModelProvider(this).get(ReleasesViewModel.class);

        releasesModel.getReleasesList(owner, repo, getContext()).observe(getViewLifecycleOwner(), releasesListMain -> {
	        if(!repository.isReleasesViewTypeIsTag()) {
		        adapter = new ReleasesAdapter(getContext(), releasesListMain);
		        adapter.setLoadMoreListener(new ReleasesAdapter.OnLoadMoreListener() {

			        @Override
			        public void onLoadMore() {
				        pageReleases += 1;
				        releasesViewModel.loadMoreReleases(owner, repo, pageReleases, getContext(), adapter);
				        fragmentReleasesBinding.progressBar.setVisibility(View.VISIBLE);
			        }

			        @Override
			        public void onLoadFinished() {
				        fragmentReleasesBinding.progressBar.setVisibility(View.GONE);
			        }
		        });
		        if(adapter.getItemCount() > 0) {
			        fragmentReleasesBinding.recyclerView.setAdapter(adapter);
			        if(releasesListMain != null && releaseTag != null) {
				        int index = getReleaseIndex(releaseTag, releasesListMain);
				        releaseTag = null;
				        if(index != -1) {
					        fragmentReleasesBinding.recyclerView.scrollToPosition(index);
				        }
			        }
			        fragmentReleasesBinding.noDataReleases.setVisibility(View.GONE);
		        }
		        else {
			        adapter.notifyDataChanged();
			        fragmentReleasesBinding.recyclerView.setAdapter(adapter);
			        fragmentReleasesBinding.noDataReleases.setVisibility(View.VISIBLE);
		        }
		        fragmentReleasesBinding.progressBar.setVisibility(View.GONE);
	        }
        });

	    releasesModel.getTagsList(owner, repo, getContext()).observe(getViewLifecycleOwner(), tagList -> {
		    if(repository.isReleasesViewTypeIsTag()) {
			    tagsAdapter = new TagsAdapter(getContext(), tagList, owner, repo);
			    tagsAdapter.setLoadMoreListener(new TagsAdapter.OnLoadMoreListener() {

				    @Override
				    public void onLoadMore() {
					    page += 1;
					    releasesViewModel.loadMoreTags(owner, repo , page, getContext(), tagsAdapter);
					    fragmentReleasesBinding.progressBar.setVisibility(View.VISIBLE);
				    }

				    @Override
				    public void onLoadFinished() {
					    fragmentReleasesBinding.progressBar.setVisibility(View.GONE);
				    }
			    });
			    if(tagsAdapter.getItemCount() > 0) {
				    fragmentReleasesBinding.recyclerView.setAdapter(tagsAdapter);
				    fragmentReleasesBinding.noDataReleases.setVisibility(View.GONE);
			    }
			    else {
				    tagsAdapter.notifyDataChanged();
				    fragmentReleasesBinding.recyclerView.setAdapter(tagsAdapter);
				    fragmentReleasesBinding.noDataReleases.setVisibility(View.VISIBLE);
			    }
			    fragmentReleasesBinding.progressBar.setVisibility(View.GONE);
		    }
	    });

    }

	private static int getReleaseIndex(String tag, List<Release> releases) {
		for (Release release : releases) {
			if(release.getTagName().equals(tag)) {
				return releases.indexOf(release);
			}
		}
		return -1;
	}

	@Override
	public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    	if(!((BaseActivity) requireActivity()).getAccount().requiresVersion("1.15.0"))
    		return;
		inflater.inflate(R.menu.filter_menu_releases, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}
}
