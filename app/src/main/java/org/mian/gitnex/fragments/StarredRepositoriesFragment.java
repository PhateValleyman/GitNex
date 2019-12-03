package org.mian.gitnex.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.mian.gitnex.R;
import org.mian.gitnex.activities.MainActivity;
import org.mian.gitnex.activities.NewRepoActivity;
import org.mian.gitnex.adapters.StarredReposListAdapter;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.models.UserRepositories;
import org.mian.gitnex.util.AppUtil;
import org.mian.gitnex.util.TinyDB;
import org.mian.gitnex.viewmodels.StarredRepositoriesViewModel;

import java.util.List;
import java.util.Objects;

/**
 * Author M M Arif
 */

public class StarredRepositoriesFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private ProgressBar mProgressBar;
    private RecyclerView mRecyclerView;
    private StarredReposListAdapter adapter;
    private ImageView createNewRepo;
    private TextView noData;

    private OnFragmentInteractionListener mListener;

    public StarredRepositoriesFragment() {
    }

    public static StarredRepositoriesFragment newInstance(String param1, String param2) {
        StarredRepositoriesFragment fragment = new StarredRepositoriesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_starred_repositories, container, false);
        boolean connToInternet = AppUtil.haveNetworkConnection(Objects.requireNonNull(getContext()));
        setHasOptionsMenu(true);
        ((MainActivity) Objects.requireNonNull(getActivity())).setActionBarTitle(getResources().getString(R.string.pageTitleStarredRepos));

        TinyDB tinyDb = new TinyDB(getContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        final SwipeRefreshLayout swipeRefresh = v.findViewById(R.id.pullToRefresh);

        noData = v.findViewById(R.id.noData);
        mProgressBar = v.findViewById(R.id.progress_bar);
        mRecyclerView = v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        createNewRepo = v.findViewById(R.id.addNewRepo);

        createNewRepo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), NewRepoActivity.class);
                startActivity(intent);
            }

        });

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && createNewRepo.isShown()) {
                    createNewRepo.setVisibility(View.GONE);
                } else if (dy < 0) {
                    createNewRepo.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefresh.setRefreshing(false);
                        StarredRepositoriesViewModel.loadStarredReposList(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), getContext());
                    }
                }, 50);
            }
        });

        fetchDataAsync(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken));

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        TinyDB tinyDb = new TinyDB(getContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        StarredRepositoriesViewModel.loadStarredReposList(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), getContext());

    }

    private void fetchDataAsync(String instanceUrl, String instanceToken) {

        StarredRepositoriesViewModel starredRepoModel = new ViewModelProvider(this).get(StarredRepositoriesViewModel.class);

        starredRepoModel.getUserStarredRepositories(instanceUrl, instanceToken, getContext()).observe(getViewLifecycleOwner(), new Observer<List<UserRepositories>>() {
            @Override
            public void onChanged(@Nullable List<UserRepositories> starredReposListMain) {
                adapter = new StarredReposListAdapter(getContext(), starredReposListMain);
                if (adapter.getItemCount() > 0) {
                    mRecyclerView.setAdapter(adapter);
                    noData.setVisibility(View.GONE);
                } else {
                    adapter.notifyDataSetChanged();
                    mRecyclerView.setAdapter(adapter);
                    noData.setVisibility(View.VISIBLE);
                }
                mProgressBar.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        boolean connToInternet = AppUtil.haveNetworkConnection(Objects.requireNonNull(getContext()));

        inflater.inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        //searchView.setQueryHint(getContext().getString(R.string.strFilter));

        /*if(!connToInternet) {
            return;
        }*/

        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (mRecyclerView.getAdapter() != null) {
                    adapter.getFilter().filter(newText);
                }
                return false;
            }
        });

    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
