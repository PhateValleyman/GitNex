package org.mian.gitnex.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import org.mian.gitnex.adapters.ProfileFollowingAdapter;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.models.UserInfo;
import org.mian.gitnex.viewmodels.ProfileFollowingViewModel;
import java.util.List;

/**
 * Author M M Arif
 */

public class ProfileFollowingFragment extends Fragment {

    private ProgressBar mProgressBar;
    private ProfileFollowingAdapter adapter;
    private RecyclerView mRecyclerView;
    private TextView noDataFollowing;
    private static String repoNameF = "param2";
    private static String repoOwnerF = "param1";

    private String repoName;
    private String repoOwner;

    private OnFragmentInteractionListener mListener;

    public ProfileFollowingFragment() {
    }

    public static ProfileFollowingFragment newInstance(String param1, String param2) {
        ProfileFollowingFragment fragment = new ProfileFollowingFragment();
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_profile_following, container, false);

        TinyDB tinyDb = TinyDB.getInstance(getContext());

        final SwipeRefreshLayout swipeRefresh = v.findViewById(R.id.pullToRefresh);

        noDataFollowing = v.findViewById(R.id.noDataFollowing);
        mRecyclerView = v.findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mProgressBar = v.findViewById(R.id.progress_bar);

        swipeRefresh.setOnRefreshListener(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {

            swipeRefresh.setRefreshing(false);
            ProfileFollowingViewModel.loadFollowingList(Authorization.get(getContext()), getContext());

        }, 200));

        fetchDataAsync(Authorization.get(getContext()));

        return v;
    }

    private void fetchDataAsync(String instanceToken) {

        ProfileFollowingViewModel pfModel = new ViewModelProvider(this).get(ProfileFollowingViewModel.class);

        pfModel.getFollowingList(instanceToken, getContext()).observe(getViewLifecycleOwner(), new Observer<List<UserInfo>>() {
            @Override
            public void onChanged(@Nullable List<UserInfo> pfListMain) {
                adapter = new ProfileFollowingAdapter(getContext(), pfListMain);
                if(adapter.getItemCount() > 0) {
                    mRecyclerView.setAdapter(adapter);
                    noDataFollowing.setVisibility(View.GONE);
                }
                else {
                    adapter.notifyDataSetChanged();
                    mRecyclerView.setAdapter(adapter);
                    noDataFollowing.setVisibility(View.VISIBLE);
                }
                mProgressBar.setVisibility(View.GONE);
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
