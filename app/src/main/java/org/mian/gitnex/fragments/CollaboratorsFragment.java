package org.mian.gitnex.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import org.gitnex.tea4j.models.Collaborators;
import org.mian.gitnex.adapters.CollaboratorsAdapter;
import org.mian.gitnex.databinding.FragmentCollaboratorsBinding;
import org.mian.gitnex.helpers.Authorization;
import org.mian.gitnex.viewmodels.CollaboratorsViewModel;
import java.util.List;

/**
 * Author M M Arif
 */

public class CollaboratorsFragment extends Fragment {

    private ProgressBar mProgressBar;
    private CollaboratorsAdapter adapter;
    private GridView mGridView;
    private TextView noDataCollaborators;
    private static String repoNameF = "param2";
    private static String repoOwnerF = "param1";

    private String repoName;
    private String repoOwner;

    public CollaboratorsFragment() {
    }

    public static CollaboratorsFragment newInstance(String param1, String param2) {
        CollaboratorsFragment fragment = new CollaboratorsFragment();
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

	    FragmentCollaboratorsBinding fragmentCollaboratorsBinding = FragmentCollaboratorsBinding.inflate(inflater, container, false);

        noDataCollaborators = fragmentCollaboratorsBinding.noDataCollaborators;
        mProgressBar = fragmentCollaboratorsBinding.progressBar;
        mGridView = fragmentCollaboratorsBinding.gridView;

        fetchDataAsync(Authorization.get(getContext()), repoOwner, repoName);
        return fragmentCollaboratorsBinding.getRoot();

    }

    private void fetchDataAsync(String instanceToken, String owner, String repo) {

        CollaboratorsViewModel collaboratorsModel = new ViewModelProvider(this).get(CollaboratorsViewModel.class);

        collaboratorsModel.getCollaboratorsList(instanceToken, owner, repo, getContext()).observe(getViewLifecycleOwner(), new Observer<List<Collaborators>>() {
            @Override
            public void onChanged(@Nullable List<Collaborators> collaboratorsListMain) {
                adapter = new CollaboratorsAdapter(getContext(), collaboratorsListMain);
                if(adapter.getCount() > 0) {
                    mGridView.setAdapter(adapter);
                    noDataCollaborators.setVisibility(View.GONE);
                }
                else {
                    adapter.notifyDataSetChanged();
                    mGridView.setAdapter(adapter);
                    noDataCollaborators.setVisibility(View.VISIBLE);
                }
                mProgressBar.setVisibility(View.GONE);
            }
        });

    }

}
