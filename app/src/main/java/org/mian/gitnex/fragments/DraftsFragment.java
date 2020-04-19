package org.mian.gitnex.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.DraftsAdapter;
import org.mian.gitnex.database.repository.DraftsRepository;

/**
 * Author M M Arif
 */

public class DraftsFragment extends Fragment {

    private DraftsAdapter adapter;
    private RecyclerView mRecyclerView;
    private DraftsRepository draftsRepository;
    private TextView noData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_drafts, container, false);

        draftsRepository = new DraftsRepository(getContext());

        noData = v.findViewById(R.id.noData);
        mRecyclerView = v.findViewById(R.id.recyclerView);
        final SwipeRefreshLayout swipeRefresh = v.findViewById(R.id.pullToRefresh);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        swipeRefresh.setOnRefreshListener(() -> new Handler().postDelayed(() -> {

            swipeRefresh.setRefreshing(false);
            fetchDataAsync(1);

        }, 250));

        fetchDataAsync(1);

        return v;

    }

    private void fetchDataAsync(int accountId) {

        draftsRepository.getDrafts(accountId).observe(getViewLifecycleOwner(), drafts -> {

            assert drafts != null;
            if(drafts.size() > 0) {

                noData.setVisibility(View.GONE);
                adapter = new DraftsAdapter(getContext(), drafts);
                mRecyclerView.setAdapter(adapter);

            }
            else {

                noData.setVisibility(View.VISIBLE);

            }

        });

    }

}
