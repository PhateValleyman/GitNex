package org.mian.gitnex.fragments;

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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.MilestonesAdapter;
import org.mian.gitnex.databinding.FragmentMilestonesBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.models.Milestones;
import org.mian.gitnex.util.TinyDB;
import org.mian.gitnex.viewmodels.MilestonesViewModel;
import java.util.List;

/**
 * Author M M Arif
 */

public class MilestonesFragment extends Fragment {

    private FragmentMilestonesBinding viewBinding;

    private MilestonesAdapter adapter;
    private static String repoNameF = "param2";
    private static String repoOwnerF = "param1";

    private String repoName;
    private String repoOwner;
    private String msState = "open";

    private OnFragmentInteractionListener mListener;

    public MilestonesFragment() {
    }

    public static MilestonesFragment newInstance(String param1, String param2) {
        MilestonesFragment fragment = new MilestonesFragment();
        Bundle args = new Bundle();
        args.putString(repoOwnerF, param1);
        args.putString(repoNameF, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            repoName = getArguments().getString(repoNameF);
            repoOwner = getArguments().getString(repoOwnerF);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewBinding = FragmentMilestonesBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);

        TinyDB tinyDb = new TinyDB(getContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");
        final String locale = tinyDb.getString("locale");
        final String timeFormat = tinyDb.getString("dateFormat");

        viewBinding.recyclerView.setHasFixedSize(true);
        viewBinding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(viewBinding.recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        viewBinding.recyclerView.addItemDecoration(dividerItemDecoration);

        viewBinding.pullToRefresh.setOnRefreshListener(() -> new Handler().postDelayed(() -> {

            viewBinding.pullToRefresh.setRefreshing(false);
            MilestonesViewModel.loadMilestonesList(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, msState, getContext());

        }, 50));

        fetchDataAsync(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName);

        return viewBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        final TinyDB tinyDb = new TinyDB(getContext());
        final String instanceUrl = tinyDb.getString("instanceUrl");
        final String loginUid = tinyDb.getString("loginUid");
        String repoFullName = tinyDb.getString("repoFullName");
        String[] parts = repoFullName.split("/");
        final String repoOwner = parts[0];
        final String repoName = parts[1];
        final String instanceToken = "token " + tinyDb.getString(loginUid + "-token");

        if(tinyDb.getBoolean("milestoneCreated")) {
            MilestonesViewModel.loadMilestonesList(instanceUrl, Authorization.returnAuthentication(getContext(), loginUid, instanceToken), repoOwner, repoName, msState, getContext());
            tinyDb.putBoolean("milestoneCreated", false);
        }
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

    private void fetchDataAsync(String instanceUrl, String instanceToken, String owner, String repo) {

        MilestonesViewModel msModel = new ViewModelProvider(this).get(MilestonesViewModel.class);

        msModel.getMilestonesList(instanceUrl, instanceToken, owner, repo, msState, getContext()).observe(getViewLifecycleOwner(), new Observer<List<Milestones>>() {
            @Override
            public void onChanged(@Nullable List<Milestones> msListMain) {

                adapter = new MilestonesAdapter(getContext(), msListMain);
                if(adapter.getItemCount() > 0) {
                    viewBinding.recyclerView.setAdapter(adapter);
                    viewBinding.noDataMilestone.setVisibility(View.GONE);
                }
                else {
                    adapter.notifyDataSetChanged();
                    viewBinding.recyclerView.setAdapter(adapter);
                    viewBinding.noDataMilestone.setVisibility(View.VISIBLE);
                }
                viewBinding.progressBar.setVisibility(View.GONE);
            }

        });

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

                if(viewBinding.recyclerView.getAdapter() != null) {
                    adapter.getFilter().filter(newText);
                }
                return false;

            }

        });

    }

}
