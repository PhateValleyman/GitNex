package org.mian.gitnex.activities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import org.mian.gitnex.R;
import org.mian.gitnex.clients.RetrofitClient;
import org.mian.gitnex.databinding.ActivityRepositorySettingsBinding;
import org.mian.gitnex.databinding.CustomRepositoryEditPropertiesDialogBinding;
import org.mian.gitnex.helpers.TinyDB;
import org.mian.gitnex.helpers.Toasty;
import org.mian.gitnex.models.UserRepositories;
import retrofit2.Call;
import retrofit2.Callback;

/**
 * Author M M Arif
 */

public class RepositorySettingsActivity extends BaseActivity {

	private ActivityRepositorySettingsBinding viewBinding;
	private View.OnClickListener onClickListener;
	private Context ctx = this;
	private Context appCtx;
	private TinyDB tinyDb;

	private String instanceUrl;
	private String loginUid;
	private String instanceToken;

	private String repositoryOwner;
	private String repositoryName;

	@Override
	protected int getLayoutResourceId(){
		return R.layout.activity_repository_settings;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		appCtx = getApplicationContext();
		tinyDb = new TinyDB(appCtx);

		viewBinding = ActivityRepositorySettingsBinding.inflate(getLayoutInflater());
		View view = viewBinding.getRoot();
		setContentView(view);

		instanceUrl = tinyDb.getString("instanceUrl");
		loginUid = tinyDb.getString("loginUid");
		String repoFullName = tinyDb.getString("repoFullName");
		String[] parts = repoFullName.split("/");
		repositoryOwner = parts[0];
		repositoryName = parts[1];
		instanceToken = "token " + tinyDb.getString(loginUid + "-token");

		ImageView closeActivity = findViewById(R.id.close);

		initCloseListener();
		closeActivity.setOnClickListener(onClickListener);

		viewBinding.editProperties.setOnClickListener(editProperties -> {
			showRepositoryProperties();
		});

	}

	private void showRepositoryProperties() {

		Dialog dialog = new Dialog(ctx, R.style.ThemeOverlay_MaterialComponents_Dialog_Alert);

		if (dialog.getWindow() != null) {
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		}

		CustomRepositoryEditPropertiesDialogBinding propBinding = CustomRepositoryEditPropertiesDialogBinding
			.inflate(LayoutInflater.from(ctx));

		View view = propBinding.getRoot();
		dialog.setContentView(view);

		propBinding.cancel.setOnClickListener(editProperties -> {
			dialog.dismiss();
		});

		Call<UserRepositories> call = RetrofitClient
			.getInstance(instanceUrl, ctx)
			.getApiInterface()
			.getUserRepository(instanceToken, repositoryOwner, repositoryName);

		call.enqueue(new Callback<UserRepositories>() {

			@Override
			public void onResponse(@NonNull Call<UserRepositories> call, @NonNull retrofit2.Response<UserRepositories> response) {

				UserRepositories repoInfo = response.body();

				if (response.code() == 200) {

					assert repoInfo != null;
					propBinding.repoName.setText(repoInfo.getName());
					propBinding.repoWebsite.setText(repoInfo.getWebsite());
					propBinding.repoDescription.setText(repoInfo.getDescription());
					propBinding.repoPrivate.setChecked(repoInfo.getPrivateFlag());
					propBinding.repoAsTemplate.setChecked(repoInfo.isTemplate());

					propBinding.repoEnableIssues.setChecked(repoInfo.getHas_issues());
					if(repoInfo.getHas_issues()) {
						propBinding.repoExternalIssueTrackerLayout.setVisibility(View.GONE);
					}

					propBinding.repoEnableWiki.setChecked(repoInfo.isHas_wiki());
					if(repoInfo.isHas_wiki()) {
						propBinding.repoExternalWikiLayout.setVisibility(View.GONE);
					}

					propBinding.repoEnablePr.setChecked(repoInfo.isHas_pull_requests());
					propBinding.repoEnableTimer.setChecked(repoInfo.getInternal_tracker().isEnable_time_tracker());
					propBinding.repoEnableMerge.setChecked(repoInfo.isAllow_merge_commits());
					propBinding.repoEnableRebase.setChecked(repoInfo.isAllow_rebase());
					propBinding.repoEnableSquash.setChecked(repoInfo.isAllow_squash_merge());
					propBinding.repoEnableForceMerge.setChecked(repoInfo.isAllow_rebase_explicit());

				}
				else {

					Toasty.error(ctx, getString(R.string.genericError));
				}

			}

			@Override
			public void onFailure(@NonNull Call<UserRepositories> call, @NonNull Throwable t) {

				Toasty.error(ctx, getString(R.string.genericServerResponseError));
			}
		});

		dialog.show();

	}

	private void initCloseListener() {
		onClickListener = view -> finish();
	}

}
