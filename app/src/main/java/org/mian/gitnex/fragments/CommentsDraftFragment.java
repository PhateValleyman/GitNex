package org.mian.gitnex.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
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
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.adapters.CommentsDraftAdapter;
import org.mian.gitnex.database.models.CommentsDraft;
import org.mian.gitnex.database.repository.CommentsDraftRepository;
import java.util.List;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class CommentsDraftFragment extends Fragment {

    private CommentsDraftAdapter adapter;
    private RecyclerView mRecyclerView;
    private CommentsDraftRepository draftsRepository;
    private TextView noData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_comments_draft, container, false);
        ((MainActivity) Objects.requireNonNull(getActivity())).setActionBarTitle(getResources().getString(R.string.pageTitleCommentsDraft));

        draftsRepository = new CommentsDraftRepository(getContext());

        noData = v.findViewById(R.id.noData);
        mRecyclerView = v.findViewById(R.id.recyclerView);
        final SwipeRefreshLayout swipeRefresh = v.findViewById(R.id.pullToRefresh);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefresh.setRefreshing(false);
                        fetchDataAsync(1);
                    }
                }, 250);
            }
        });

        fetchDataAsync(1);

        return v;

    }

    private void fetchDataAsync(int accountId) {

        draftsRepository.getComments(accountId).observe(this, new Observer<List<CommentsDraft>>() {
            @Override
            public void onChanged(@Nullable List<CommentsDraft> comments) {

                assert comments != null;
                if(comments.size() > 0) {

                    noData.setVisibility(View.GONE);
                    adapter = new CommentsDraftAdapter(getContext(), comments);
                    mRecyclerView.setAdapter(adapter);

                }
                else {

                    noData.setVisibility(View.VISIBLE);

                }

            }
        });

    }

}
