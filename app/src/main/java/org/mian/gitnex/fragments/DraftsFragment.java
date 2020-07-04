package org.mian.gitnex.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.DraftsAdapter;
import org.mian.gitnex.database.models.DraftsWithRepositories;
import org.mian.gitnex.database.repository.DraftsRepository;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.util.TinyDB;
import java.util.ArrayList;
import java.util.List;

/**
 * Author M M Arif
 */

public class DraftsFragment extends Fragment {

	private Context ctx;
    private DraftsAdapter adapter;
    private RecyclerView mRecyclerView;
    private DraftsRepository draftsRepository;
    private TextView noData;
	private List<DraftsWithRepositories> draftsList_;
	private int currentActiveAccountId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_drafts, container, false);
	    ctx = getContext();
        setHasOptionsMenu(true);

        TinyDB tinyDb = new TinyDB(ctx);

	    draftsList_ = new ArrayList<>();
        draftsRepository = new DraftsRepository(ctx);

        noData = v.findViewById(R.id.noData);
        mRecyclerView = v.findViewById(R.id.recyclerView);
        final SwipeRefreshLayout swipeRefresh = v.findViewById(R.id.pullToRefresh);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ctx));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

	    adapter = new DraftsAdapter(getContext(), draftsList_);

        swipeRefresh.setOnRefreshListener(() -> new Handler().postDelayed(() -> {

	        draftsList_.clear();
            swipeRefresh.setRefreshing(false);
            fetchDataAsync(1);

        }, 250));

        currentActiveAccountId = tinyDb.getInt("currentActiveAccountId");

        fetchDataAsync(currentActiveAccountId);

        return v;

    }

    private void fetchDataAsync(int accountId) {

        draftsRepository.getDrafts(accountId).observe(getViewLifecycleOwner(), drafts -> {

            assert drafts != null;
            if(drafts.size() > 0) {

	            draftsList_.clear();
                noData.setVisibility(View.GONE);
	            draftsList_.addAll(drafts);
	            adapter.notifyDataSetChanged();
                mRecyclerView.setAdapter(adapter);

            }
            else {

                noData.setVisibility(View.VISIBLE);

            }

        });

    }

	@Override
	public void onResume() {
		super.onResume();
		draftsList_.clear();
		fetchDataAsync(currentActiveAccountId);
	}

	public void deleteAllDrafts(int accountId) {

    	if(draftsList_.size() > 0) {

		    DraftsRepository.deleteAllDrafts(accountId);
		    draftsList_.clear();
		    adapter.notifyDataSetChanged();
		    Toasty.info(ctx, getResources().getString(R.string.draftsDeleteSuccess));

	    }
    	else {
		    Toasty.error(ctx, getResources().getString(R.string.draftsListEmptyError));
	    }

	}

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        inflater.inflate(R.menu.generic_nav_dotted_menu, menu);
	    inflater.inflate(R.menu.search_menu, menu);
	    super.onCreateOptionsMenu(menu, inflater);

	    MenuItem searchItem = menu.findItem(R.id.action_search);
	    SearchView searchView = (SearchView) searchItem.getActionView();
	    searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

	    ((SearchView) searchView).setOnQueryTextListener(new SearchView.OnQueryTextListener() {

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

	private void filter(String text) {

		List<DraftsWithRepositories> arr = new ArrayList<>();

		for(DraftsWithRepositories d : draftsList_) {

			if(d == null || d.getRepositoryOwner() == null || d.getRepositoryName() == null || d.getDraftText() == null) {
				continue;
			}

			if(d.getRepositoryOwner().toLowerCase().contains(text) || d.getRepositoryName().toLowerCase().contains(text)
				|| d.getDraftText().toLowerCase().contains(text) || String.valueOf(d.getIssueId()).contains(text)) {
				arr.add(d);
			}
		}

		adapter.updateList(arr);
	}

}
