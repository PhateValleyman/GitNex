package org.mian.gitnex.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import org.mian.gitnex.R;
import org.mian.gitnex.adapters.UserGridAdapter;
import org.mian.gitnex.databinding.ActivityRepoWatchersBinding;
import org.mian.gitnex.helpers.contexts.RepositoryContext;
import org.mian.gitnex.viewmodels.RepoWatchersViewModel;

/**
 * Author M M Arif
 */

public class RepoWatchersActivity extends BaseActivity {

    private TextView noDataWatchers;
    private View.OnClickListener onClickListener;
    private UserGridAdapter adapter;
    private GridView mGridView;
    private ProgressBar mProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

	    ActivityRepoWatchersBinding activityRepoWatchersBinding = ActivityRepoWatchersBinding.inflate(getLayoutInflater());
	    setContentView(activityRepoWatchersBinding.getRoot());

        ImageView closeActivity = activityRepoWatchersBinding.close;
        TextView toolbarTitle = activityRepoWatchersBinding.toolbarTitle;
        noDataWatchers = activityRepoWatchersBinding.noDataWatchers;
        mGridView = activityRepoWatchersBinding.gridView;
        mProgressBar = activityRepoWatchersBinding.progressBar;

        RepositoryContext repository = RepositoryContext.fromIntent(getIntent());
        final String repoOwner = repository.getOwner();
        final String repoName = repository.getName();

        initCloseListener();
        closeActivity.setOnClickListener(onClickListener);

        toolbarTitle.setText(R.string.repoWatchersInMenu);

        fetchDataAsync(getAccount().getAuthorization(), repoOwner, repoName);
    }

    private void fetchDataAsync(String instanceToken, String repoOwner, String repoName) {

        RepoWatchersViewModel repoWatchersModel = new ViewModelProvider(this).get(RepoWatchersViewModel.class);

        repoWatchersModel.getRepoWatchers(instanceToken, repoOwner, repoName, ctx).observe(this, watchersListMain -> {

            adapter = new UserGridAdapter(ctx, watchersListMain);

            if(adapter.getCount() > 0) {

                mGridView.setAdapter(adapter);
                noDataWatchers.setVisibility(View.GONE);
            }
            else {

                adapter.notifyDataSetChanged();
                mGridView.setAdapter(adapter);
                noDataWatchers.setVisibility(View.VISIBLE);
            }

            mProgressBar.setVisibility(View.GONE);
        });

    }

    private void initCloseListener() {

        onClickListener = view -> finish();
    }

}
